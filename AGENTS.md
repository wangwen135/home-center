# Repository Guidelines

## Project Structure & Module Organization

This is a Java 8 Spring Boot 2.4 application built with Maven. Main application code lives under `src/main/java/com/wwh/home/center`, with controllers in `controller`, services in `service` and `service/impl`, persistence interfaces in `dao/mapper`, configuration in `config`, and shared utilities or models in `common` and `model`.

Runtime resources are in `src/main/resources`: MyBatis XML mappings in `mapper`, Thymeleaf templates in `templates`, environment configuration in `application*.yml`, and browser assets in `static` (`js`, `css`, `device`, `weather`, `bootstrap`, `lib`). Tests and test-only helpers are under `src/test/java`; temporary test resources are under `src/test/resources`.

## Build, Test, and Development Commands

- `mvn clean package`: compile, run tests, and build the Spring Boot jar.
- `mvn test`: run the test suite only.
- `mvn spring-boot:run`: start the app locally using Maven.
- `mvn spring-boot:run -Dspring-boot.run.profiles=dev`: run with the `dev` profile when local configuration is available.

There is no Maven wrapper in this repository, so use an installed Maven version compatible with Java 8.

## Coding Style & Naming Conventions

Use standard Java formatting with 4-space indentation. Keep package names lowercase and aligned with `com.wwh.home.center`. Follow existing suffix patterns: `*Controller`, `*Service`, `*ServiceImpl`, `*Mapper`, `*Config`, `*Vo`, `*Qo`, and entity classes in `model/entity`. Prefer Lombok where the project already uses it, and keep comments concise. Static frontend code is plain HTML, CSS, and JavaScript; keep file-local style consistent with the surrounding page.

## Testing Guidelines

Tests use Spring Boot Test and JUnit. Place tests in the matching package under `src/test/java`, and name test classes with a `Test` suffix, for example `UserServiceTest`. For service or database behavior, prefer focused tests around the changed method and profile/config dependencies. Run `mvn test` before submitting changes; use `mvn -Dtest=UserServiceTest test` for a single class.

## Commit & Pull Request Guidelines

Recent history uses concise messages such as `feat: add pc-agent API` and short Chinese summaries. Keep commits brief and action-oriented; use a prefix like `feat:`, `fix:`, or `refactor:` when it clarifies the change.

Pull requests should describe the behavior changed, list verification commands, and mention any configuration, database, Redis, MQTT, or static asset impact. Include screenshots for visible UI changes in `static` or `templates`, and link related issues when available.

## Security & Configuration Tips

Do not commit secrets, tokens, private hostnames, or production credentials. Keep environment-specific values in the appropriate `application-*.yml` file and document any new required property.
