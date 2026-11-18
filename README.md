# Exchange Service

A small REST service that converts monetary amounts between currencies. Built with Java 25
and Spring Boot 4.1.2 for the Awin Billing & Payments take-home assignment.

The service uses a fixed snapshot of EUR-based reference exchange rates (the "price of 1 EUR"
in each currency, e.g. `AUD 1.639644`, `BRL 6.03858`), stored in PostgreSQL and seeded by
Flyway on first startup. Any pair of supported currencies can be converted; cross-currency
conversions go through the EUR base at full precision, and only the final result is rounded
to two decimal places (`HALF_UP`).

## API

### `POST /api/v1/convert`

Request:

```json
{ "sourceCurrency": "EUR", "targetCurrency": "USD", "amount": 100 }
```

Response:

```json
{ "convertedAmount": 116.06 }
```

Validation: the amount must be greater than 0 and both currencies must be among the
supported ones. Errors are returned as RFC 9457 Problem Detail objects.

Interactive docs (Swagger UI): <http://localhost:8080/swagger-ui.html>

### Supported currencies

`AUD BRL CAD CHF CNY CZK DKK EUR GBP HKD HUF IDR ILS INR ISK JPY KRW MXN NOK NZD PLN SEK SGD THB TRY USD ZAR`

## Design decisions

- **BigDecimal everywhere** for amounts and rates — no binary floating point in money math.
- **Rates are not rounded** — they keep full source precision (`NUMERIC(20,10)` in the
  database). Only the final converted amount is rounded to 2 decimal places, once, so error
  does not compound.
- **Schema is owned by Flyway only.** Hibernate runs with `ddl-auto: validate`, so the code
  can never modify the schema; every change is a versioned migration under
  `src/main/resources/db/migration`.
- **Package-by-feature layout** (`exchange/{controller,service,repository}` plus
  `common/GlobalExceptionHandler`) instead of technical layer packages.
- **OpenAPI annotations live on the `ConversionApi` interface**, keeping the controller thin.
- See `AGENTS.md` for more detail on conventions.

## How to run

Prerequisite: JDK 25 (Gradle toolchain) and a running Docker daemon.

### Dev (default profile)

Testcontainers starts PostgreSQL for you — no compose file, no manually provisioned
database:

```bash
./gradlew bootTestRun          # http://localhost:8080
```

This launches the application on the test classpath (`TestExchangeserviceApplication`
+ `TestcontainersConfiguration`), where a `@ServiceConnection PostgreSQLContainer`
(`postgres:17-alpine`) is started alongside the app. Spring Boot derives the datasource
connection from the container automatically; Flyway migrates and seeds the rate snapshot
on every start (fresh database each run — the seed data is the dataset).

Try it:

```bash
curl -s -X POST localhost:8080/api/v1/convert \
  -H 'Content-Type: application/json' \
  -d '{"sourceCurrency": "EUR", "targetCurrency": "USD", "amount": 100}'
# {"convertedAmount":116.06}
```

The OTLP metrics exporter is disabled in dev (`management.defaults.metrics.export.enabled`
in `application-dev.yaml`), so no telemetry is exported and no receiver is needed.

Dev logs are human-readable single-line console output.

### Production mode

```bash
DB_URL=jdbc:postgresql://prod-host:5432/exchange \
DB_USER=exchange \
DB_PASSWORD=secret \
java -jar build/libs/exchangeservice-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

In prod mode logs are emitted as JSON objects (`logging.structured.format.console: logstash`),
one JSON document per line, ready for log aggregation.

### Docker

```bash
docker build -t exchangeservice .
docker run --rm -p 8080:8080 \
  -e DB_URL='jdbc:postgresql://host.docker.internal:5432/exchange' \
  -e DB_USER=exchange -e DB_PASSWORD=exchange \
  exchangeservice
```

The image is multi-stage (Gradle build → JRE 25 runtime), runs as a non-root user and uses
Spring Boot's layered jar extraction so dependency layers cache well.

## Tests

```bash
./gradlew test
```

Three levels:

- **Unit** (`ExchangeConverterTest`): conversion math, rounding policy, error cases.
- **Controller** (`ConversionControllerTest`): request validation, ProblemDetail responses.
- **Integration** (`ExchangeFlowIntegrationTest`): one end-to-end flow against a real
  PostgreSQL container (Testcontainers + `@ServiceConnection`): Flyway migrates, rates are
  seeded, `POST /api/v1/convert` goes through all layers, result asserted exactly.

Integration tests and the dev run (`bootTestRun`) require a running Docker daemon;
Testcontainers manages all containers.

## AI usage notes

As permitted by the assignment: this solution was built with the help of an AI coding agent
(Claude/GLM-family LLM in OpenCode).

- **What it was used for:** scaffolding the codebase structure from an agreed-upon plan,
  writing the Flyway migration seed data, implementing entities/repository/service/controller,
  diagnosing Spring Boot 4 test-module changes (moved `@WebMvcTest`,
  `TestRestTemplate`/`spring-boot-restclient` dependency requirements), and writing this README.
- **Where it helped most:** navigating the many breaking changes in Spring Boot 4.x test
  starters (packages and dependencies moved between modules) which would otherwise have cost
  significant trial-and-error time.
- **What was changed/improved by hand:** architectural decisions were reviewed and steered
  iteratively with the reviewer — domain-driven package split instead of layer packages,
  single `V1__init.sql` migration, DB-default `created_at` instead of JPA auditing, no rate
  rounding, currency enum handling, validation placement, and Testcontainers-based dev run
  (`bootTestRun`) instead of a compose file.
