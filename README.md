# AI-Assisted URL Shortener

A production-minded, full-stack URL shortener. It demonstrates a greenfield
baseline, a backward-compatible brownfield enhancement, and normalization of
an ambiguous abuse-protection requirement.

## Tech Stack

- Java 17, Spring Boot 4, Spring Data JPA, Flyway
- PostgreSQL
- React, TypeScript, Vite, Nginx
- Docker Compose, Testcontainers, GitHub Actions

## Features

- HTTP/HTTPS link creation with optional future expiration
- Seven-character Base62 short codes and PostgreSQL uniqueness enforcement
- `302` redirects with aggregate click count and last-accessed timestamp
- Metadata and analytics lookup; hard deletion
- `410` for expired links and `404` for unknown or deleted links
- Scoped create-link rate limiting and RFC 9457 Problem Details
- OpenAPI/Swagger, Actuator health, and a responsive React UI

## Run Locally

Prerequisites: Git and Docker.

```bash
git clone https://github.com/PortableCode/ai-assisted-url-shortener
cd ai-assisted-url-shortener
docker compose up --build
```

- Frontend: <http://localhost>
- Swagger: <http://localhost:8080/swagger-ui/index.html>
- Health: <http://localhost:8080/actuator/health>

## API Summary

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/links` | Create a short link |
| `GET` | `/api/v1/links/{shortCode}` | Get metadata |
| `GET` | `/api/v1/links/{shortCode}/analytics` | Get aggregate analytics |
| `DELETE` | `/api/v1/links/{shortCode}` | Hard-delete a link |
| `GET` | `/{shortCode}` | Redirect to the destination |

The create endpoint accepts `originalUrl` and optional `expiresAt`. It is
limited to 20 requests per remote address per 60-second fixed window.

## Architecture

The backend is a modular monolith: REST controllers and DTOs delegate to
application services, which use JPA repositories and Flyway-managed PostgreSQL.
The redirect path atomically updates aggregate analytics in PostgreSQL. The
React SPA is served by Nginx, which proxies API and public short-code requests
to the backend.

## Engineering Scenarios

- [Greenfield baseline](docs/05-greenfield-scenario.md)
- [Brownfield expiration enhancement](docs/06-brownfield-scenario.md)
- [Ambiguous abuse-protection requirement](docs/07-ambiguous-scenario.md)

## AI-Assisted Engineering

AI was used for scoped analysis, implementation suggestions, testing support,
and skeptical review. The engineer normalized requirements, reviewed proposed
changes and diffs, ran validation, and approved decisions. See the
[AI-assisted engineering log](docs/04-ai-usage-log.md).

## Testing

```bash
cd backend && ./mvnw clean test
cd frontend && npm ci && npm run lint && npm run build
```

See [testing and validation](docs/08-testing-validation.md) for coverage,
manual checks, Docker verification, and CI gates.

## Documentation

1. [Requirements analysis](docs/01-requirements-analysis.md)
2. [Architecture](docs/02-architecture.md)
3. [Execution plan](docs/03-execution-plan.md)
4. [AI-assisted engineering log](docs/04-ai-usage-log.md)
5. [Greenfield scenario](docs/05-greenfield-scenario.md)
6. [Brownfield scenario](docs/06-brownfield-scenario.md)
7. [Ambiguous requirement scenario](docs/07-ambiguous-scenario.md)
8. [Testing and validation](docs/08-testing-validation.md)
9. [Security and risk analysis](docs/09-security-risk-analysis.md)
10. [Trade-offs and limitations](docs/10-tradeoffs-limitations.md)
11. [Final engineering summary](docs/11-final-engineering-summary.md)

## Limitations

This is a single-instance prototype: the in-memory rate limiter is not shared
across replicas, analytics are aggregate-only, and expired records are retained.
It has no authentication or malicious-destination reputation checks. See
[trade-offs and limitations](docs/10-tradeoffs-limitations.md).
