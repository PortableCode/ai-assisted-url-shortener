# Execution Plan — URL Shortener Prototype

## Phase 1 — Requirements, Architecture, AI Workflow

- Review and normalize the assignment requirements.
- Confirm the modular-monolith architecture and prototype scope.
- Record AI-assisted review decisions in the engineering log.

## Phase 2 — Spring Boot Bootstrap

- Create the backend application skeleton.
- Establish package structure, base configuration, and dependency wiring.

## Phase 3 — PostgreSQL + Flyway

- Add PostgreSQL persistence.
- Introduce Flyway migrations and baseline schema.

## Phase 4 — Greenfield Link Creation

- Implement short-link creation.
- Validate HTTP/HTTPS destinations.
- Generate 7-character Base62 codes with collision protection.

## Phase 5 — Redirect + Analytics

- Implement redirect resolution.
- Track aggregate click count and last-accessed timestamp.

## Phase 6 — Metadata + Deletion + Standardized Errors

- Implement link metadata lookup.
- Implement hard deletion.
- Standardize API error responses.

## Phase 7 — Backend Testing and Quality Gates

- Add unit and integration tests for core flows.
- Add validation and regression coverage for error scenarios.

## Phase 8 — OpenAPI / Health / Operational Baseline

- Publish OpenAPI documentation.
- Add health and readiness endpoints.
- Establish the operational baseline.

## Phase 9 — Greenfield Scenario Documentation

- Document the completed greenfield baseline.
- Capture the implemented behaviors and known boundaries.

## Phase 10 — Brownfield Expiration Analysis and Implementation

- Analyze expiration against the existing working codebase.
- Add expiration through explicit brownfield changes.
- Verify the baseline remains intact before and after the change.

## Phase 11 — Brownfield Documentation

- Document the expiration enhancement separately from the greenfield baseline.

## Phase 12 — Ambiguous Abuse-Prevention Analysis and Implementation

- Analyze abuse-prevention ambiguity against the existing codebase.
- Implement only the scoped solution that is justified by the requirement.

## Phase 13 — Ambiguous-Scenario Documentation

- Document the abuse-prevention decision and its rationale.

## Phase 14 — React/TypeScript Frontend

- Build the frontend against the stabilized REST API.

## Phase 15 — Full-Stack Containerization

- Package the backend, frontend, and supporting services with Docker Compose.

## Phase 16 — GitHub Actions CI

- Add CI checks for build, test, and validation workflows.

## Phase 17 — Security / Performance / Failure-Mode Review

- Review security, performance, and resilience concerns.
- Address only issues that are relevant to the prototype scope.

## Phase 18 — Final Documentation and AI Traceability

- Consolidate architecture, execution, and AI usage documentation.

## Phase 19 — Fresh-Clone End-to-End Validation

- Validate the system from a clean clone through the full setup path.

## Phase 20 — Public GitHub Submission Review

- Perform a final review of the repository state before submission.
- Confirm the implementation and documentation are consistent.

## Dependency Ordering

- Baseline working before documentation.
- Greenfield documented before expiration analysis.
- Expiration analyzed against existing code before expiration implementation.
- Expiration implemented before related brownfield documentation.
- Documentation and traceability before final submission review.