# Testing and Validation

## Automated Backend Tests

`cd backend && ./mvnw clean test`

JUnit 5 and Mockito cover URL and expiration validation, Base62 code shape,
collision retry/exhaustion, metadata, analytics, hard deletion, redirect
responses, rate-limit boundaries, and RFC 9457 Problem Details.

Spring Boot integration tests use PostgreSQL 17 Testcontainers with Flyway,
not H2 or a local Compose database. They verify mapped persistence fields, the
nullable `expires_at` migration, OpenAPI/Swagger availability, expiration
behavior, deletion through the redirect path, and database-backed analytics.

## Concurrency Validation

`RedirectIntegrationTest` submits 16 concurrent redirects against a real
PostgreSQL container. It verifies that every redirect succeeds and the final
`click_count` is 16, exercising the database-side atomic increment rather than
a Java read-modify-write sequence.

## Frontend Validation

```bash
cd frontend
npm ci
npm run lint
npm run build
```

The lint and production build commands are the frontend automated quality
checks. Manual UI validation covered creating links with and without expiration,
copying the short URL, metadata and analytics lookup, deletion, backend error
display, and responsive layout.

## Manual API and Docker Validation

Recorded API checks exercised creation, redirect, metadata, analytics, delete,
expired-link `410`, unknown/deleted-link `404`, and the 21st create request
returning `429`.

`docker compose config` and `docker compose up --build` were used to verify the
local stack: PostgreSQL health, backend startup and Flyway migration, Nginx
frontend delivery, API and short-code proxying, Swagger UI, and Actuator health.

## CI Quality Gates

GitHub Actions runs two jobs on pushes and pull requests to `main`:

- backend: Java 17 and `./mvnw clean test`
- frontend: Node 22, `npm ci`, `npm run lint`, and `npm run build`

The suite focuses on behavior and integration risks; it does not claim a
coverage threshold or browser end-to-end automation.
