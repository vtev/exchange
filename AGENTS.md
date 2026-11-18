# AGENTS.md

Guidance for AI coding agents (and human contributors) working on this repository.

## What this service is

`exchangeservice` exposes one REST endpoint that converts a monetary amount between two
currencies, using a fixed snapshot of EUR-based exchange rates seeded into PostgreSQL by
Flyway. Built for the Awin "Billing & Payments" take-home assignment.

## Stack

- Java 25, Spring Boot 4.1.2, Gradle 9.9.1 (use `./gradlew`)
- PostgreSQL, Spring Data JPA, Flyway, springdoc-openapi (Swagger UI at `/swagger-ui.html`)
- Testcontainers for integration testing (`@ServiceConnection`, no manual wiring)

## Package layout (domain driven, do not introduce layer packages)

```
com.demo.exchangeservice
├── common/GlobalExceptionHandler    # @RestControllerAdvice -> ProblemDetail for ALL errors
├── config/OpenApiConfig             # OpenAPI metadata only
└── exchange/
    ├── controller/                  # ConversionController (thin), ConversionApi (OpenAPI
    │                                #   annotations live on the interface), DTO records
    ├── service/                     # ExchangeConverter (only business component), Currency enum
    └── repository/                  # ExchangeRate entity, ExchangeRateRepository
```

Wiring is strictly controller → service → repository. Controllers hold no business logic;
business logic holds no Spring web types.

## Commands

```bash
./gradlew test                 # all tests (unit + controller + integration w/ Docker)
./gradlew test --tests "com.demo.exchangeservice.exchange.service.ExchangeConverterTest"
./gradlew bootTestRun          # dev run: Testcontainers Postgres started alongside the app
./gradlew bootJar              # build the fat jar (build/libs/)
docker build -t exchangeservice . && docker run -p 8080:8080 -e DB_URL=... exchangeservice
```

Integration tests require a running Docker daemon (Testcontainers).

## Hard rules

- **Schema is owned by Flyway only.** Never add/alter tables or columns outside
  `src/main/resources/db/migration`. Never run new migrations in code. Hibernate runs with
  `ddl-auto: validate` — a mapping that doesn't match the schema fails at startup, which is
  the point.
- **Migration naming:** `V<n>__<description>.sql`. Version numbers are mandatory (Flyway
  order/history). New schema or data changes go in a *new* migration file; never edit one
  that may have already been applied.
- **Money is `BigDecimal`.** No `double`/`float` for amounts or rates. Rates are stored at
  full precision (`NUMERIC(20,10)`); only the final converted result is rounded, once, to
  2 decimal places with `RoundingMode.HALF_UP` (see `ExchangeConverter`).
- **Currencies are the `Currency` enum** (`exchange.service.Currency`), mapped with
  `@Enumerated(EnumType.STRING)` into the `VARCHAR(3)` `currency_code` column.
  Adding a currency = add enum constant + seed row in a new migration, in the same change.
- **Validation lives in the web layer only:** the controller applies Bean Validation via
  `@Validated` on the request parameter (annotations on `ConversionRequest`: amount > 0,
  currencies present). `GlobalExceptionHandler.handleValidation` returns every invalid
  field with its message in a `fields` map. `ExchangeConverter` assumes valid input and
  only reports missing rates (via `ExchangeException`).
- **All error responses are RFC 9457 ProblemDetail** produced by
  `common.GlobalExceptionHandler`. Don't build error bodies in controllers. The
  advice logs every handled exception: `warn` with the reason for 4xx responses,
  `error` with the exception (full stack trace) for the 500 fallback. Tracing is
  prod-only (`management.tracing.enabled: true` in prod, disabled in dev/test);
  when enabled, Boot's Micrometer tracing puts `traceId`/`spanId` in the MDC and the
  structured JSON logs include them automatically — never log correlation IDs manually.
- **OpenAPI annotations go on the `ConversionApi` interface**, not the controller.
- DTOs are records; use constructor injection everywhere; no field injection.
- JPA entities use a `private` no-arg constructor (Hibernate instantiates them
  reflectively; do not use `protected` — it invites subclassing just to reach the
  constructor), a public constructor for legitimate creation, and getters only —
  no setters.
- **No explanatory comments or Javadoc.** The code (names, structure) speaks for itself;
  JPA/Bean Validation/OpenAPI annotations carry their own semantics. The only permitted
  comments are the `// given`, `// when`, `// then` markers in tests — exactly these
  three words, nothing after them.

## Configuration

Three yaml files: `application.yaml` (common, `dev` is the default profile),
`application-dev.yaml` (human-readable logs; the dev datasource comes from the
Testcontainers `@ServiceConnection`, not from yaml; OTLP export disabled via
`management.defaults.metrics.export.enabled=false` and `management.tracing.enabled=false`
— this also covers tests since they inherit the dev default profile), and
`application-prod.yaml` (`DB_URL`/`DB_USER`/`DB_PASSWORD` env vars, JSON structured logs
via `logging.structured.format.console: logstash`).

## Tests

- Unit: `ExchangeConverterTest` — plain JUnit + Mockito, no Spring context.
- Controller: `ConversionControllerTest` — `@WebMvcTest` + `@MockitoBean`; note Spring
  Boot 4 test modules: `WebMvcTest` is in `org.springframework.boot.webmvc.test.autoconfigure`
  and `@AutoConfigureTestRestTemplate` in `org.springframework.boot.resttestclient.autoconfigure`.
- Integration: `ExchangeFlowIntegrationTest` — `@SpringBootTest(RANDOM_PORT)` +
  `TestcontainersConfiguration` (PostgreSQL via `@ServiceConnection`). Full flow: Flyway
  migrate → seed → HTTP convert → assert exact converted amount.
- Dev run uses the same mechanism: `TestExchangeserviceApplication` (test classpath)
  + `./gradlew bootTestRun` starts `TestcontainersConfiguration`'s PostgreSQL container
  next to the app — no compose file, no manually configured dev datasource.
- All tests follow the Given / When / Then structure: a `given` section (arrange state,
  stub mocks — extracted into `given*` helper methods where reused), a single `when`
  section (the call under test / HTTP request), and a `then` section (assertions),
  each marked with a bare `// given` / `// when` / `// then` comment (only these three
  words, no extra prose). Test names describe the behavior: `returnsConvertedAmount`,
  `rejectsNonPositiveAmount`, ... Test methods come first, private helpers at the
  bottom of the class.
