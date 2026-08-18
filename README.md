# AI-Assisted URL Shortener

A production-minded full-stack URL shortener built as a software-engineering interview assignment. The repository demonstrates:

- greenfield implementation from scratch,
- a backward-compatible brownfield enhancement,
- normalization of an ambiguous requirement, and
- disciplined AI-assisted engineering with explicit human review and validation.

## Tech Stack

- **Backend:** Java 17, Spring Boot 4.0.7, Spring Data JPA / Hibernate, Flyway, PostgreSQL 17, Maven Wrapper
- **Frontend:** React, TypeScript, Vite, Axios, Nginx
- **Quality / Delivery:** Testcontainers, Swagger / OpenAPI, Actuator, Docker Compose, GitHub Actions

## Features

- Create short links for valid HTTP/HTTPS URLs
- Optional future expiration timestamp
- Seven-character Base62 short codes generated with `SecureRandom`
- PostgreSQL uniqueness enforcement with bounded collision retry
- `302 Found` redirects for active links
- `410 Gone` for expired links
- `404 Not Found` for unknown or deleted links
- Aggregate analytics: `clickCount` and `lastAccessedAt`
- Metadata and analytics lookup APIs
- Hard deletion
- In-memory prototype rate limiting for link creation
- RFC 9457-style `ProblemDetail` API errors
- Swagger / OpenAPI documentation and Actuator health endpoint
- Responsive React UI for create, lookup, analytics, and delete flows

## Quick Start

### Prerequisites

- Git
- Docker with Docker Compose v2

No local Java, Maven, Node.js, npm, or PostgreSQL installation is required for the recommended evaluator workflow.

### Run the Full Stack

```bash
git clone https://github.com/PortableCode/ai-assisted-url-shortener
cd ai-assisted-url-shortener
docker compose up --build
```

Once startup completes:

- **Frontend UI:** <http://localhost>
- **Swagger UI:** <http://localhost:8080/swagger-ui/index.html>
- **OpenAPI JSON:** <http://localhost:8080/v3/api-docs>
- **Health:** <http://localhost:8080/actuator/health>

To stop the stack:

```bash
docker compose down
```

To stop the stack and remove the local PostgreSQL volume:

```bash
docker compose down -v
```

## Evaluator Smoke Test

1. Open <http://localhost>
2. Create a link for `https://example.com`
3. Open the generated short URL and confirm it redirects
4. Look up the short code to view metadata and analytics
5. Delete the loaded link and confirm later access returns `404`
6. Optionally create a link with a future expiration and verify expiry behavior through the API

## API Summary

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/links` | Create a short link |
| `GET` | `/api/v1/links/{shortCode}` | Retrieve metadata |
| `GET` | `/api/v1/links/{shortCode}/analytics` | Retrieve aggregate analytics |
| `DELETE` | `/api/v1/links/{shortCode}` | Hard-delete a link |
| `GET` | `/{shortCode}` | Redirect to the original destination |

`POST /api/v1/links` accepts:

```json
{
  "originalUrl": "https://example.com",
  "expiresAt": "2026-08-19T12:00:00Z"
}
```

- `originalUrl` is required and must be a valid absolute HTTP/HTTPS URL
- `expiresAt` is optional and must be strictly in the future when provided
- Link creation is limited to **20 requests per remote address per 60-second fixed window**

Short URLs are host-aware:

- requests through the frontend/Nginx proxy return `http://localhost/{shortCode}`
- direct requests to the backend on port `8080` return `http://localhost:8080/{shortCode}`

## Architecture Summary

The backend is a **modular monolith**. Controllers and DTOs delegate to application services, which use JPA repositories backed by Flyway-managed PostgreSQL.

Key implementation choices:

- PostgreSQL is the source of truth for short-code uniqueness
- redirect analytics use a database-side atomic update to avoid lost increments
- deletion is a physical hard delete
- expiration is an additive brownfield change stored as nullable `expires_at`
- the React UI is served by Nginx, which proxies both `/api/*` and public short-code routes to the backend

## Engineering Scenarios

- [Greenfield scenario](docs/05-greenfield-scenario.md)
- [Brownfield scenario](docs/06-brownfield-scenario.md)
- [Ambiguous requirement scenario](docs/07-ambiguous-scenario.md)

## AI-Assisted Engineering Workflow

AI was used for scoped analysis, implementation support, testing support, and skeptical review. Requirement normalization, design approval, diff review, validation, and final acceptance remained engineer-owned.

See the [AI-assisted engineering log](docs/04-ai-usage-log.md).

## Testing

Backend:

```bash
cd backend
./mvnw clean test
```

Frontend:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

See [Testing and Validation](docs/08-testing-validation.md) for automated coverage, Testcontainers usage, concurrency validation, manual checks, Docker verification, and CI gates.

## Documentation

1. [Requirements analysis](docs/01-requirements-analysis.md)
2. [Architecture](docs/02-architecture.md)
3. [Execution plan](docs/03-execution-plan.md)
4. [AI-assisted engineering log](docs/04-ai-usage-log.md)
5. [Greenfield scenario](docs/05-greenfield-scenario.md)
6. [Brownfield scenario](docs/06-brownfield-scenario.md)
7. [Ambiguous requirement scenario](docs/07-ambiguous-scenario.md)
8. [Testing and Validation](docs/08-testing-validation.md)
9. [Security and Risk Analysis](docs/09-security-risk-analysis.md)
10. [Trade-offs and Limitations](docs/10-tradeoffs-limitations.md)
11. [Final Engineering Summary](docs/11-final-engineering-summary.md)

## Concise Limitations

- The rate limiter is process-local and resets on restart
- Analytics are aggregate-only, not per-click event history
- Expired rows remain stored; no cleanup scheduler is implemented
- There is no authentication or private-link access control
- Malicious destination reputation checks are not implemented

See [Trade-offs and Limitations](docs/10-tradeoffs-limitations.md) and [Security and Risk Analysis](docs/09-security-risk-analysis.md).
