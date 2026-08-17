# URL Shortener Engineering Instructions

This repository implements a production-oriented URL shortener as part of
a Senior Full Stack Developer interview assignment.

The engineer owns all architectural decisions, correctness, security,
maintainability, testing, and final approval. AI assists within scoped tasks.

## Backend

- Java 17
- Spring Boot 4.0.x
- Maven
- PostgreSQL
- Spring Data JPA / Hibernate
- Flyway
- REST
- Bean Validation

## Frontend

- React
- TypeScript
- Vite

## Architecture

Use a modular monolith.

Maintain clear separation between:
- REST/API layer
- application/service layer
- domain concepts
- persistence
- infrastructure/configuration

Do not introduce microservices, Kafka, Kubernetes, Redis, authentication,
or other infrastructure without explicit engineer approval.

## Coding standards

- Use constructor injection.
- Keep controllers thin.
- Do not expose JPA entities through REST APIs.
- Use API request/response DTOs.
- Validate all external input.
- Keep business logic outside controllers.
- Prefer simple solutions over speculative abstractions.
- Do not add unnecessary dependencies.
- Handle errors explicitly.
- Do not leak implementation details in API errors.

## AI-assisted engineering protocol

For non-trivial tasks:

Before implementation:
1. Restate the requirement.
2. Identify assumptions.
3. Identify affected files/components.
4. Identify edge cases.
5. Identify risks.
6. Propose an implementation plan.

Do not modify unrelated code.

For architectural, persistence, API-contract, security, concurrency,
or reliability changes, present the design before implementation.

After implementation:
1. Compile.
2. Run relevant tests.
3. Summarize changed files.
4. Identify unresolved risks.
5. Do not commit changes.

All Git commits require engineer review and approval.