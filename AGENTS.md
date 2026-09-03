# AGENTS.md

## Project Overview

Java 25, Spring Boot 4.1.1, Maven wrapper. Modulith architecture with DDD modules: `user`, `category`, `transaction`,
`budget`, `shared`.

## Key Commands

| Task            | Command                                                 |
|-----------------|---------------------------------------------------------|
| Build           | `./mvnw compile`                                        |
| Run (dev)       | `./mvnw spring-boot:run -Dspring.profiles.active=dev`   |
| Test            | `./mvnw test` (uses Testcontainers - requires Docker)   |
| Verify (CI)     | `./mvnw verify` (runs spotless, checkstyle, PMD, tests) |
| Format check    | `./mvnw spotless:check`                                 |
| Format apply    | `./mvnw spotless:apply`                                 |
| Checkstyle only | `./mvnw checkstyle:check`                               |
| PMD only        | `./mvnw pmd:check`                                      |

## Git Hooks (auto-installed via Maven initialize phase)

- **pre-commit**: `./mvnw spotless:check`
- **pre-push**: `./mvnw verify`

## Architecture Notes

- **Spring Modulith** enforces module boundaries - verify with `./mvnw spring-modulith:verify`
- **Modular monolith** architecture with Spring Boot modules: user (auth), category, transaction, budget, shared
  (security, exceptions, config)
- **Hexagonal/DDD** architecture: each module has `domain` (model, ports), `application` (services), `infrastructure`
  (rest, persistence)
- MapStruct for mapping, Lombok for boilerplate, JJWT for tokens
- Code in English, user-facing messages in Spanish

## Configuration

| Profile | File                                  | Key Settings                                                       |
|---------|---------------------------------------|--------------------------------------------------------------------|
| default | `application.yaml`                    | Port 8081, DB via env vars, JWT secret via env, cookie-secure=true |
| dev     | `application-dev.yaml`                | ddl-auto=none, show-sql=true, cookie-secure=false                  |
| test    | `src/test/resources/application.yaml` | Fixed test JWT secret                                              |

Required env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `SERVER_PORT` (optional)

## Code Quality Rules

- **Spotless**: Eclipse formatter, 120 char line width, 2-space indent, removes unused imports, sorts imports (java,
  javax, org, com, dev)
- **Checkstyle**: Max method length 60, max params 11, max cyclomatic complexity 10, max returns 5 (3 for void), no star
  imports, FinalClass, AbbreviationAsWordInName (max 2 chars), package-private fields disallowed
- **PMD**: Rulesets: bestpractices (excl. ImplicitFunctionalInterface), errorprone, performance

## Testing

- Testcontainers with PostgreSQL 18-alpine (auto-started)
- Test config in `src/test/resources/application.yaml`
- Run single test: `./mvnw test -Dtest=ClassName#methodName`

## Common Pitfalls

1. Docker must be running for tests (Testcontainers)
2. `./mvnw verify` must pass before push (enforced by pre-push hook)
3. Spotless formatting is strict - run `./mvnw spotless:apply` before committing
4. Checkstyle fails on method length > 60 lines