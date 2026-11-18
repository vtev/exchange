# Exchange Service

A small REST service that converts monetary amounts between currencies. Built with Java 25
and Spring Boot 4.1.2.

The full API is documented interactively via OpenAPI — Swagger UI at
<http://localhost:8080/swagger-ui.html>.

## How to run

Prerequisite: JDK 25 (Gradle toolchain) and a running Docker daemon.

### Dev

```bash
./gradlew bootTestRun --console=plain          # http://localhost:8080
```

### Production

```bash
./gradlew bootJar
DB_URL=jdbc:postgresql://prod-host:5432/exchange \
DB_USER=exchange \
DB_PASSWORD=secret \
java -jar build/libs/exchangeservice-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Try it

```bash
curl -s -X POST localhost:8080/api/v1/convert \
  -H 'Content-Type: application/json' \
  -d '{"sourceCurrency": "EUR", "targetCurrency": "USD", "amount": 100}'
# {"convertedAmount":116.06}
```

## Tests

```bash
./gradlew test
```

## Code style

Formatting is enforced by Spotless with google-java-format (Google style):

```bash
./gradlew spotlessApply
```

Architecture, conventions and design decisions are documented in `AGENTS.md`.
AI usage notes are in `AI-USAGE-NOTES.md`.
