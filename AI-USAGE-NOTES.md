# AI Usage Notes

As permitted by the assignment: this solution was built with the help of an AI coding agent
(GLM-family LLM in OpenCode), working interactively with a human reviewer.

## What AI was used for

- Scaffolding the codebase structure from an agreed-upon plan: Flyway migration and seed
  data, JPA entity/repository, `ExchangeConverter`, controller with OpenAPI annotations on
  the interface, `GlobalExceptionHandler` with ProblemDetail, yaml profiles, Dockerfile,
  AGENTS.md/README.
- Diagnosis of Spring Boot 4 breaking changes: test modules moved between artifacts
  (`@WebMvcTest` package change, `TestRestTemplate` requiring
  `spring-boot-resttestclient` + `spring-boot-restclient`, `ValidationAutoConfiguration`
  import in `@WebMvcTest` slices, Jackson 3 exception types), Hibernate schema validation
  quirks, and Flyway checksum behavior.
- Test writing and refactoring: controller/unit/integration tests in Given/When/Then
  form, including reworking the integration test to use `postForEntity` with the response
  record instead of `ParameterizedTypeReference`.
- Adding tooling: Spotless with google-java-format (Google style) wired into `check`.

## What was changed or improved by hand

Architectural decisions were reviewed and steered iteratively with the reviewer:

- Domain-driven package split (`exchange/{controller,service,repository}` + `common`)
  instead of technical layer packages.
- Single `V1__init.sql` migration; DB-default `created_at` instead of JPA auditing;
  no `CHECK` constraint in the schema (validation in the web layer); surrogate identity
  PK with indexed `currency_code` (`VARCHAR(3)`) instead of a currency primary key.
- No rate rounding; single `HALF_UP` rounding of the final amount; `BigDecimal` everywhere.
- `Currency` as enum mapped with `@Enumerated(EnumType.STRING)` (custom converter and
  `@JsonCreator` removed in favor of built-in behavior and clean error mapping).
- `@Validated`-driven bean validation instead of custom checks inside the request record
  or the service; all-invalid-fields ProblemDetail with a `fields` map.
- Testcontainers-based dev run (`bootTestRun`) instead of a compose file; OTLP export
  disabled in dev/tests; observation/tracing prod-only; logging conventions
  (warn for 4xx, error with stack trace for 500).
- Docs split: concise run-oriented README; conventions in AGENTS.md; these AI notes
  in a separate file.
