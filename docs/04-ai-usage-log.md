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
