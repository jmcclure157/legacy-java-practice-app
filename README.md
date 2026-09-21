# legacy-java-practice-app

A small todo REST API that started life as an **intentionally legacy** Java 8 / Spring Boot 2.7 app
and is being modernized to Java 21 / Spring Boot 3.5 one reviewable step at a time. Each PR isolates
a single failure mode of the migration and explains it; `git log` is the lesson plan.

## Stack

- Java 21 (`maven.compiler.release=21`) — step 1 of the modernization
- Spring Boot 3.5.5 (`spring-boot-starter-parent`)
- Spring Web, Spring Data JPA, Bean Validation (`jakarta.validation`)
- Hibernate 6 with the dialect auto-detected from JDBC metadata
- H2 in-memory database, `ddl-auto=update`
- JUnit 5 + Mockito via `spring-boot-starter-test`, `@WebMvcTest` + `@MockitoBean`

## API

Base path `/api/todos`:

| Method | Path                      | Description                                      |
|--------|---------------------------|--------------------------------------------------|
| GET    | `/api/todos`              | List all todos; `?completed=true` or `?q=text`   |
| GET    | `/api/todos/{id}`         | Fetch one todo (404 when missing)                |
| GET    | `/api/todos/remaining`    | Count of incomplete todos                        |
| POST   | `/api/todos`              | Create a todo (validated title)                  |
| PUT    | `/api/todos/{id}`         | Replace title/notes/completed                    |
| DELETE | `/api/todos/{id}`         | Delete a todo (404 when missing)                 |

## Migration steps

1. **JDK 8 → 21** — toolchain only, `maven.compiler.release` replaces `source`/`target` (PR #1)
2. **Spring Boot 2.7 → 3.5** — breaks the build with `package javax.persistence does not exist` (PR #2)
3. **javax → jakarta** — plus removal of the explicit `H2Dialect`, which Hibernate 6 infers (PR #2)
4. **`@MockBean` → `@MockitoBean`** — the annotation moved from Boot into Spring Framework (PR #3)
5. **Trailing-slash regression** — `GET /api/todos/` 404s under Spring MVC 6; restored with a
   `UrlHandlerFilter` in `WebConfig` and pinned by `TrailingSlashTest` (PR #4)
6. **Java 21 idiom cleanup** — still pending: `Collections.<Todo>emptyList()`, anonymous
   `Comparator` classes, index-based `for` loops, manual boxing, no `var`

## Build and test

Requires a JDK 21 toolchain:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn test
mvn spring-boot:run
```
