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