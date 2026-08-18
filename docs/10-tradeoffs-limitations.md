# Trade-offs and Limitations

## Modular Monolith

One Spring Boot deployment and one PostgreSQL database suit this small,
transactional domain. Package-level module boundaries keep API, application,
persistence, and redirect concerns separate without the operational cost of
distributed services.

## PostgreSQL Correctness and Analytics

PostgreSQL is the source of truth for short-code uniqueness. Creation retries a
unique-constraint collision up to five times. Redirect analytics use an atomic
database update for `click_count` and `last_accessed_at`, avoiding lost
increments but retaining only aggregates—not individual click events.

## Rate Limiting

The fixed-window limiter is in memory: it resets on restart, is process-local,
is not shared by replicas, and its client map can grow. A gateway or
distributed store would be a production evolution only when deployment and
traffic justify it.

## Lifecycle and Code Policy

Deletion is a hard delete, so there is no audit trail or recovery path.
Expiration is stored as nullable `expires_at`; expired rows remain stored and
return `410`, with no cleanup scheduler. Fixed seven-character Base62 codes are
simple and unpredictable for this scope, but code length and capacity policy
would need reassessment at much larger scale.

## Frontend and Local Delivery

The React UI is a small SPA with no router, state library, or component
framework because its create, lookup, analytics, and delete flows do not need
them. Docker Compose embeds local PostgreSQL credentials for evaluator-friendly
startup; they are not production secrets.

## Intentionally Omitted

The solution does not implement authentication or user accounts, click-event
history, destination reputation scanning, distributed caching/rate limiting,
cloud deployment, Kubernetes, Kafka, or separate services. These are not
required for the documented scenarios and would add unsupported operational
complexity.
