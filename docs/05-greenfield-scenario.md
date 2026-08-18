# Greenfield Scenario

## Requirement

Initial URL-shortener backend from scratch.

## Normalized Requirements

- Create short URL
- Validate HTTP/HTTPS URL
- 7-character Base62 `SecureRandom` short code
- PostgreSQL `UNIQUE` constraint
- Bounded collision retry
- Redirect
- Atomic `click_count` increment
- `last_accessed_at`
- Metadata
- Analytics
- Hard delete
- Consistent API errors
- Swagger/OpenAPI
- Health endpoint
- PostgreSQL persistence
- Tests

Expiration was intentionally excluded from the greenfield baseline so it can be introduced later as a genuine brownfield change against an already working system.

## Assumptions

- Single Spring Boot backend, no authentication
- Aggregate analytics only: click count and last accessed time
- PostgreSQL is the persistence source of truth
- Local/prototype deployment is sufficient for baseline URL generation and operations

## Decomposition

Actual implementation sequence:

1. Planning and architecture review
2. Backend bootstrap
3. PostgreSQL and Flyway baseline schema
4. Entity, repository, and short-code generator
5. Testcontainers-based PostgreSQL integration coverage
6. Create API
7. Redirect and aggregate analytics
8. Metadata and hard delete
9. Standardized errors and OpenAPI

## Architecture Decisions

- **Modular monolith:** kept the backend in one deployable application with separate link-management and redirect concerns.
- **PostgreSQL source of truth:** persistence rules, stored analytics, and deletion behavior are enforced against the database rather than duplicated elsewhere.
- **Database uniqueness over pre-checks:** short-code collisions are resolved by PostgreSQL `UNIQUE` enforcement, avoiding race-prone application-side availability checks.
- **`saveAndFlush()` for collision detection:** uniqueness failures occur inside the bounded retry loop instead of surfacing only at transaction commit.
- **Database-side atomic analytics update:** redirect uses `UPDATE ... SET click_count = click_count + 1, last_accessed_at = ?` to avoid lost updates under concurrency.
- **Hard delete:** deleting a link physically removes the row so later metadata, analytics, and redirect requests naturally return 404.
- **No Kafka, Redis, or microservices:** not justified for the completed prototype baseline.

## AI-Assisted Execution

The workflow remained engineer-led:

1. Copilot analyzed requirements and proposed scoped designs
2. The engineer reviewed and adjusted assumptions and scope
3. Copilot implemented only the approved slice
4. The engineer reviewed diffs and requested focused changes
5. Tests were run after each feature step
6. The implementation was reviewed skeptically before acceptance
7. The engineer approved the completed greenfield baseline

## Validation

- Maven test suite executed successfully
- Testcontainers verified real PostgreSQL persistence and concurrency behavior
- Manual `curl` validation exercised create, metadata, redirect, analytics, delete, and error flows
- Redirect returned `302 Found` with `Location` set to the stored URL
- Redirect updated analytics (`click_count` increment and `last_accessed_at`)
- Hard delete returned `204 No Content` and the deleted short code then returned `404`
- Swagger UI loaded at `/swagger-ui/index.html`
- OpenAPI returned `200` at `/v3/api-docs`
- Actuator health returned `200` at `/actuator/health`

## Risks and Trade-offs

- `springdoc` is pinned explicitly for Spring Boot 4 compatibility in this repository
- Public base URL and trusted proxy handling are still prototype-level
- Collision retry currently keys off PostgreSQL SQLState `23505`, which is sufficient while `short_code` is the only relevant unique application constraint
- Analytics is aggregate-only and synchronous by design to keep the baseline small and testable

## Engineer Approval

The completed greenfield backend baseline was reviewed skeptically, validated, and accepted before any later brownfield modification.