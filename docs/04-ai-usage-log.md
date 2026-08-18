# AI-Assisted Engineering Log

This log records meaningful AI-assisted engineering interactions.
Routine autocomplete suggestions are intentionally excluded.

All AI outputs were reviewed by the engineer before acceptance.

---

## AI-001 — Requirement Analysis

### Tool
GitHub Copilot Chat

### Intent
Challenge the initial interpretation of the assignment before coding.

### Context
The system is a Java/Spring Boot URL shortener with PostgreSQL persistence
and a React frontend to be implemented after the backend is validated.

### AI contribution
Copilot proposed functional requirements, non-functional requirements,
ambiguities, risks, assumptions, and acceptance criteria.

### Engineer decisions

Accepted:
- URL creation
- redirect
- link metadata
- aggregate analytics
- expiration
- input validation
- explicit error handling

Modified:
- Analytics is intentionally limited to aggregate click count and
  last-access timestamp for this prototype.

Rejected:
- Microservices
- Kafka/event streaming
- Authentication
- Kubernetes
- Advanced analytics

### Rationale
These capabilities were not necessary to demonstrate the requested engineering
outcomes and would increase implementation and validation risk.

### Validation
Requirements were reviewed and normalized into explicit acceptance criteria
before implementation.

### Engineer sign-off
Approved.

---

## AI-002 — Planning Review

### Tool
GitHub Copilot Chat

### Intent
Review the normalized requirements, architecture, and execution order before implementation begins.

### Context
Greenfield URL-shortener baseline with expiration intentionally deferred to a later brownfield enhancement.

### AI contribution
Copilot reviewed the requirements, architecture, and execution-plan documents and called out missing testability, baseline expiration leakage, and soft-delete ambiguity.

### Engineer decisions

Accepted:
- Hard deletion as the baseline delete strategy
- Aggregate analytics limited to click count and last-accessed timestamp
- Modular monolith with PostgreSQL
- Brownfield expiration as a later phase

Modified:
- Moved expiration entirely out of the greenfield baseline
- Added a separate brownfield expiration section in the architecture review
- Tightened the execution order so expiration follows a working greenfield baseline

Rejected:
- Soft deletion
- Baseline expiration fields and behavior
- Microservices
- Kafka/event streaming
- Redis
- Multiple databases

### Rationale
The assignment is specifically evaluating requirement normalization, greenfield sequencing, and brownfield discipline. Keeping expiration out of the initial baseline makes the prototype easier to validate and clearly demonstrates controlled change against an existing codebase.

### Validation
Requirements, architecture, and execution-plan documents were manually cross-checked for consistency.

### Engineer sign-off
Approved.

---

## AI-003 — Persistence / Domain Baseline

### Tool
GitHub Copilot Chat

### Intent
Design and implement the initial persistence/domain layer for the V1 schema.

### Context
Greenfield baseline only: JPA `Link` entity, `LinkRepository`, and `ShortCodeGenerator` against the existing Flyway V1 table.

### AI contribution
Copilot proposed the minimal file set, JPA mappings, `Instant` usage for TIMESTAMPTZ fields, a concrete `ShortCodeGenerator` using `SecureRandom`, and a focused generator test.

### Engineer decisions

Accepted:
- `Link` mapped directly to the existing `links` table
- `GenerationType.IDENTITY` for the V1 `BIGSERIAL` primary key
- `Instant` for timestamp columns
- Minimal `LinkRepository` with short-code lookup
- Concrete `ShortCodeGenerator` with a fixed Base62 alphabet
- Focused generator test for length and character set

Modified:
- Removed silent trimming from the entity; URL normalization will happen explicitly at the application boundary
- Deferred a repository integration test until Testcontainers is introduced so tests are not coupled to a locally running Compose database

Rejected:
- No collision database lookup inside `ShortCodeGenerator`
- No service/controller/DTO implementation yet
- No expiration or soft deletion
- Collision retry logic
- Additional repository methods not needed for current lookup requirements
- Interface/implementation pairs

### Rationale
The baseline needed only enough structure to match the V1 schema cleanly and support the next application-layer step later, without introducing premature abstractions.

### Validation
`./mvnw clean test` → BUILD SUCCESS

### Engineer sign-off
Approved.

---

## AI-004 — Repository Test Isolation

### Tool
GitHub Copilot Chat

### Intent
Isolate persistence tests using real PostgreSQL.

### Context
Repository integration testing for `LinkRepository` against the existing V1 schema.

### AI contribution
Copilot proposed a real PostgreSQL Testcontainers setup with Flyway-driven schema creation and a repository save/find round trip.

### Engineer decisions

Accepted:
- Testcontainers PostgreSQL
- Flyway runs during the integration test
- Repository save/find validation

Rejected:
- H2
- Local Compose dependency
- Schema duplication

### Rationale
The repository test needed to exercise the real mapping and schema without depending on a developer’s local Compose database or an alternate in-memory dialect.

### Validation
- `./mvnw -Dtest=LinkRepositoryTest test` → BUILD SUCCESS
- `./mvnw clean test` → BUILD SUCCESS

### Engineer sign-off
Approved.

---

## AI-005 — Testcontainers Version Management

### Tool
GitHub Copilot Chat

### Intent
Validate the Testcontainers dependency setup for the repository integration test.

### Context
Spring Boot 4.0.7 repository integration testing with a real PostgreSQL Testcontainer.

### AI contribution
AI suggested relying on Spring Boot dependency management for the Testcontainers modules.

### Engineer decisions

Accepted:
- Real PostgreSQL Testcontainers setup
- Flyway-driven schema creation during the integration test
- Repository save/find validation

Modified:
- Restored explicit `1.21.4` versions for the Testcontainers modules after Maven validation required them in this project

Rejected:
- H2
- Local Compose dependency
- Schema duplication

### Rationale
The project’s current Maven setup required explicit Testcontainers versions, so relying on dependency management alone was not sufficient. Restoring the explicit versions kept the build deterministic and aligned with the existing project configuration.

### Validation
- `./mvnw -Dtest=LinkRepositoryTest test` → BUILD SUCCESS
- `./mvnw clean test` → BUILD SUCCESS

### Engineer sign-off
Approved.

---

## AI-006 — Create-Link API

### Tool
GitHub Copilot Chat

### Intent
Implement the POST `/api/v1/links` create-link feature.

### Context
Greenfield create-link API on top of the existing Link entity, repository, and short-code generator.

### AI contribution
Copilot proposed the controller/service split, URI-based validation, bounded collision handling, and request-derived short URL assembly.

### Engineer decisions

Accepted:
- Thin controller + DTO boundary
- URI-based HTTP/HTTPS validation
- `saveAndFlush()` so uniqueness failures occur inside retry handling
- Five bounded attempts
- Database uniqueness remains authoritative
- Request-derived short URL rather than hardcoded localhost

Accepted with limitation:
- SQLState `23505` retry is sufficient while `short_code` is the only unique application constraint; make it constraint-specific if more unique constraints are added
- `ServletUriComponentsBuilder` is correct for the prototype/direct local deployment; trusted proxy/public-base-URL handling will be documented for production

Deferred:
- Centralized/global error handling
- Proxy forwarding configuration
- Broader URL policy/malware filtering

### Rationale
The feature stays intentionally small while still enforcing valid HTTP(S) URLs, preserving the database as the source of truth for uniqueness, and avoiding premature infrastructure or policy work.

### Validation
- `./mvnw -Dtest=LinkServiceTest,LinkControllerTest test` → BUILD SUCCESS
- `./mvnw clean test` → BUILD SUCCESS

### Engineer sign-off
Approved.

---

## AI-007 — Redirect + Aggregate Analytics

### Tool
GitHub Copilot Chat

### Intent
Implement the greenfield redirect flow for `GET /{shortCode}`.

### Context
Redirect and aggregate analytics on top of the existing Link entity and repository.

### AI contribution
Copilot proposed the redirect controller/service split, atomic database-side click updates, and a Testcontainers concurrency test.

### Engineer decisions

Accepted:
- Separate redirect controller/service
- `302 Found`
- Database-side atomic click increment
- `last_accessed_at` update
- Service-level transaction
- Affected-row check for lookup/delete race
- Testcontainers concurrency verification

Accepted trade-off:
- Short-code format enforced both by route regex and service validation
- Concurrency test validates final aggregate count rather than transaction ordering internals

Rejected:
- Java-side read → increment → save
- Redis/Kafka/event analytics
- Expiration in the greenfield redirect flow

### Rationale
The redirect path needs to preserve click counts under concurrency without introducing a read-modify-write race in Java, while still keeping the implementation small and consistent with the current greenfield scope.

### Validation
- Focused tests → BUILD SUCCESS
- `./mvnw clean test` → BUILD SUCCESS

### Engineer sign-off
Approved.

---

## AI-008 — Metadata, Analytics, and Hard Delete

### Tool
GitHub Copilot Chat

### Intent
Implement metadata lookup, analytics lookup, and hard delete APIs.

### Context
Greenfield read/delete APIs built on the existing Link entity, controller, and repository.

### AI contribution
Copilot proposed reusing the current service/controller, response DTO boundaries, and hard-delete-by-short-code behavior.

### Engineer decisions

Accepted:
- Reuse existing `LinkService` and `LinkController`
- Dedicated metadata/analytics response DTOs
- Database ID not exposed
- Physical delete by short code
- Affected-row count determines not-found
- Nullable `lastAccessedAt`

Added after review:
- Explicit integration coverage proving deleted short code returns 404 through the redirect endpoint

Rejected:
- Soft delete
- Audit table
- New schema migration
- Expiration
- Analytics event table
- Unnecessary services

### Rationale
The read/delete APIs stay aligned with the existing greenfield model: simple DTO boundaries, hard deletion, and no extra persistence structures or lifecycle flags.

### Validation
`./mvnw clean test` → BUILD SUCCESS

### Engineer sign-off
Approved.

---

## AI-009 — Greenfield Backend Quality Baseline

### Tool
GitHub Copilot Chat

### Intent
Finish the greenfield backend quality baseline.

### Context
Existing greenfield backend with create, redirect, metadata, analytics,
and hard-delete APIs already implemented, requiring centralized API error
handling, OpenAPI exposure, focused test cleanup, and a final skeptical review.

### AI contribution
Copilot proposed the centralized error-handling plan, OpenAPI configuration,
test-gap review, and a skeptical greenfield readiness review after
implementation.

### Engineer decisions

Accepted:
- RFC 9457 `ProblemDetail`
- Centralized `@RestControllerAdvice`
- Single not-found exception shared by link and redirect flows
- `springdoc` 3.x for Spring Boot 4
- Selective high-value tests rather than 100% coverage

Modified:
- Replaced initial framework-specific OpenAPI smoke-test ideas with a simpler
  real HTTP smoke test against `/swagger-ui/index.html` and `/v3/api-docs`
  after Spring Boot 4 test-support compatibility issues appeared in this project
- Kept OpenAPI annotations concise and endpoint-focused instead of adding
  verbose field-level documentation

Rejected:
- Low-value tests
- A new custom error abstraction beyond Spring's built-in `ProblemDetail`
  facilities
- Unrelated refactors

### Rationale
This checkpoint needed consistent API behavior, evaluator-friendly API
documentation, and targeted validation without expanding scope or adding
infrastructure beyond what the greenfield backend already required.

### Validation
- `./mvnw clean test` → BUILD SUCCESS
- Swagger UI → loads
- `/v3/api-docs` → 200
- Validation → standardized 400
- Unknown link → standardized 404

### Engineer sign-off
Approved.

---

## AI-010 — Brownfield Expiration Analysis and Review

### Tool
GitHub Copilot Chat

### Intent
Analyze and document the brownfield expiration enhancement against the completed greenfield backend.

### Context
The greenfield backend was already complete and validated; expiration needed to be added as a backward-compatible brownfield change with a new schema migration, 410 handling, and expiration-aware redirect behavior.

### AI contribution
Copilot reviewed the current repository, identified affected modules and schema changes, drafted the behavior matrix, and called out time-boundary and backward-compatibility risks before and after implementation.

### Engineer decisions

Accepted:
- Optional expiration with `expires_at`
- `V2` Flyway migration only
- `302` for active/no-expiration links
- `410 Gone` for expired links
- `404 Not Found` for unknown/deleted links
- Hard delete unchanged

Modified:
- Kept analytics aggregate-only and left its response shape unchanged
- Used the injected `Clock`/`Instant` pattern for deterministic validation and redirect checks

Rejected:
- Background cleanup
- Scheduler-based expiry processing
- Soft delete
- Redis
- Kafka
- Any unrelated refactor

### Validation
- `./mvnw clean test` → BUILD SUCCESS
- manual runtime checks covered redirect, expired-link 410, metadata, and post-expiration compatibility

### Engineer sign-off
Approved.

---

## AI-011 — Ambiguous Abuse-Protection Analysis and Implementation

### Intent
Resolve the ambiguous abuse-protection requirement, implement the agreed prototype limiter, and validate it end to end.

### Context
The requirement was intentionally underspecified. Engineering first normalized the scope to `POST /api/v1/links` only, anonymous clients, fixed-window per remote address, and no redirect protection or distributed infrastructure.

### AI contribution
Copilot identified the key ambiguities, proposed the smallest prototype-safe design, implemented the in-memory fixed-window limiter, added centralized 429 handling, and reviewed the result skeptically against quota semantics, thread safety, and scope boundaries.

### Engineer decisions

Accepted:
- `POST /api/v1/links` only
- anonymous clients identified by `getRemoteAddr()`
- 20 requests per 60-second fixed window
- in-memory, thread-safe implementation
- centralized `ProblemDetail` 429 response

Modified:
- Added a dedicated rate-limit exception and interceptor-based enforcement
- Used a deterministic `Clock` in tests instead of sleeps

Rejected:
- Redis
- Spring Security
- authentication-based quotas
- redirect rate limiting
- trust in `X-Forwarded-For`
- unrelated refactors

### Validation
- focused automated tests covered threshold, reset, per-client isolation, and standardized 429 handling
- `./mvnw clean test` → BUILD SUCCESS
- manual 21-request validation confirmed the 21st create request is rejected

### Engineer sign-off
Approved.

---

## AI-012 — React + TypeScript Frontend Implementation

### Intent
Implement the evaluator-facing React + TypeScript frontend for the completed URL shortener backend.

### Context
The backend APIs were already complete and validated. The frontend needed to stay simple, professional, and dependency-light while supporting create, metadata lookup, analytics, delete, copy-to-clipboard, and Vite-based local development against the backend.

### AI contribution
Copilot reviewed the scaffolded Vite project, proposed the smallest component and API-client structure, implemented the single-page UI, added proxy/base-URL configuration, handled backend ProblemDetail responses, and fixed build/lint issues surfaced during validation.

### Engineer decisions

Accepted:
- small Axios client layer
- Vite proxy for `/api` to `http://localhost:8080`
- one shared short-code input for lookup and analytics
- simple CSS and no router/state library
- copy-to-clipboard and delete confirmation via `window.confirm`

Modified:
- adjusted the state flow so delete acts on the currently loaded link
- kept the component count small and avoided extra abstractions

Rejected:
- React Router
- Redux
- UI frameworks
- React Query
- hardcoded backend URLs throughout the components

### Validation
- `npm run build` → passed
- `npm run lint` → passed
- manual UI flow covered create, lookup, analytics, copy, and delete interactions

### Engineer sign-off
Approved.

---

## AI-013 — Remove Redundant Context-Load Test

### Intent
Fix the CI blocker that caused `./mvnw clean test` to depend on a local PostgreSQL instance.

### Context
A generated `UrlShortenerApplicationTests` class was present as a plain `@SpringBootTest` without Testcontainers wiring. On a clean machine and in GitHub Actions, that test tried to connect to `localhost:5432` and failed even though other integration tests already proved application startup with PostgreSQL Testcontainers.

### AI contribution
Copilot inspected the existing Testcontainers-backed `@SpringBootTest` coverage, identified the generated `contextLoads()` test as redundant, and removed it instead of adding another database setup path.

### Engineer decisions

Accepted:
- delete the redundant generated context-load test
- rely on existing `@SpringBootTest + PostgreSQL Testcontainers` tests for application startup coverage

Modified:
- removed the localhost-bound test instead of weakening datasource defaults or adding H2

Rejected:
- adding a PostgreSQL service to CI
- introducing H2
- changing production datasource behavior
- adding another special test-only database configuration

### Validation
- `./mvnw clean test` → BUILD SUCCESS after the deletion

### Engineer sign-off
Approved.

---

## AI-014 — Docker/CI Packaging Path

### Intent
Analyze, implement, and validate the Docker/CI packaging path for the full-stack app.

### Context
The repository needed clean-clone evaluator usability: `docker compose up --build`, backend and frontend containers, plus GitHub Actions coverage that did not depend on a local PostgreSQL instance.

### AI contribution
Copilot analyzed the Docker and CI architecture, implemented the backend/frontend Dockerfiles, Nginx proxying, Compose wiring, and CI workflow, then helped diagnose the remaining CI blocker caused by the generated localhost-bound `UrlShortenerApplicationTests`.

### Engineer decisions

Accepted:
- multi-stage backend and frontend images
- Nginx serving the frontend with `/api` and root short-code proxying
- env-based datasource config plus forwarded headers
- GitHub Actions backend/frontend split
- deleting the redundant generated context-load test

Modified:
- kept Compose and CI intentionally simple
- validated the backend suite after stopping the temporary PostgreSQL container

Rejected:
- Kubernetes
- registry publishing
- extra deployment automation
- H2
- adding a PostgreSQL service to CI

### Validation
- `docker compose config` → passed
- `docker compose up --build` → app stack started
- `./mvnw clean test` → BUILD SUCCESS after removing the generated test and stopping the temporary local PostgreSQL container
- Compose endpoints verified through the frontend and backend

### Engineer sign-off
Approved.

---

## AI-015 — Final Documentation

### Intent
Complete the final evaluator-facing documentation set for the finished solution.

### Context
The README and docs/08-11 files were added after the codebase and packaging were complete. The pass had to reflect the implemented greenfield, brownfield, ambiguous abuse-control, Docker/CI, testing, security, tradeoffs, and final-summary decisions without inventing new capabilities.

### AI contribution
Copilot reviewed the existing repository and scenario docs, then helped structure concise evaluator-facing documentation that links the completed scenarios, validation, risks, tradeoffs, and final engineering summary.

### Engineer decisions

Accepted:
- README as evaluator entry point
- concise testing, security, tradeoff, and final-summary docs
- links between README and scenario documents

Modified:
- clarified the implemented `410 Gone` redirect response in the OpenAPI docs after review

Rejected:
- new functionality claims
- overengineered architecture language
- references to Redis, Kafka, authentication, Kubernetes, or cloud deployment

### Validation
- final documentation review completed against the actual repository state

### Engineer sign-off
Approved.

---

