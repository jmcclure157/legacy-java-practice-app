# legacy-java-practice-app

An **intentionally legacy** Java 8 / Spring Boot 2.7 REST API, built as the clean "before" state for a
modernization practice exercise. Nothing here should be fixed in place — the point is that a later
migration to Java 21 / Spring Boot 3.5 has real work to do.

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

## Deliberately outdated bits

- `javax.persistence.*` and `javax.validation.*` imports (pre-`jakarta` rename)
- `@MockBean` in tests (deprecated in favor of `@MockitoBean`)
- Explicit `spring.jpa.database-platform=org.hibernate.dialect.H2Dialect`, which Hibernate 6 infers
- Java 8 idioms: `Collections.<Todo>emptyList()`, anonymous `Comparator`/`Executable` classes,
  index-based `for` loops, manual boxing (`Long.valueOf`, `Integer.valueOf`), no `var`
- No test covering `GET /api/todos/` (trailing slash), so the Spring MVC 6 trailing-slash change is
  free to surface during migration

## Build and test

Requires a JDK 21 toolchain:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn test
mvn spring-boot:run
```
