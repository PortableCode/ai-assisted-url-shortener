# Architecture Review — URL Shortener Prototype

## 1. Suitability

* The proposed architecture is suitable for this prototype and aligns with the stated constraints.
* A Spring Boot modular monolith with PostgreSQL is the right default for a URL shortener:

    * One transactional domain
    * A small number of workflows
    * Predictable persistence rules
    * No strong need for cross-service coordination
* A separate React frontend layer can consume the REST API cleanly later without changing the backend design.
* This is a good **“single service, single database”** foundation, provided the modules are logical packages inside one application rather than independent deployable services.

## 2. Module Boundaries

### Good Boundaries

* **REST API layer:** request validation, HTTP contracts, DTOs
* **Link Management module:** create, inspect, delete, and status rules
* **Redirect module:** code resolution and HTTP redirect behavior
* **Analytics module:** click recording and aggregate summaries
* **Persistence layer:** JPA repositories and schema
* **Shared domain/configuration:** validation rules, constants, status enums, and error handling

### Important Rule

* Do not split these into separate services in a prototype.
* They should be separate modules/packages, not microservices.
* Boundaries should prevent:

    * The redirect path from embedding business logic unrelated to redirect behavior
    * Analytics from becoming a hidden side effect that negatively impacts latency

## 3. Create-Link Flow

### Suggested Flow

1. Client POSTs destination URL.
2. API layer validates the request.
3. Link Management service validates URL format.
4. Service generates a unique short code.
5. Persist in PostgreSQL in one transaction.
6. Return short URL metadata.

### Correctness Expectations

* Code uniqueness is guaranteed by a database constraint.
* Invalid URLs are rejected early.
* Deleted state is handled by physical row removal.
* Creation does not depend on any external system.

This flow is appropriate and simple.

The weak point is code generation if it is not constrained: it must be collision-safe under concurrency.

## 4. Redirect Flow

### Suggested Flow

1. Client requests `/{shortCode}`.
2. Redirect module resolves the code.
3. Check link status:

    * Exists
    * Not deleted
4. If valid, return an HTTP redirect to the original URL.
5. Record the analytics hit.

### Correctness

* Status checks must occur before redirect.
* Redirect status should be explicit and consistent, such as `302` or `307`.
* Deleted or unknown links must not redirect.

### Risk

If the analytics write is synchronous and database-bound, redirect latency can suffer under load.

The flow should be as lightweight as possible. If analytics is critical, prefer a low-overhead write or a queued write only if justified later.

## 5. Analytics Flow

A minimal prototype should track:

* Total click count
* Last access time

### Avoid Premature Complexity

* No geo/device/referrer processing by default
* No per-request event store initially

### Recommended Shape

* A link record with aggregate click count and last-accessed timestamp
* Increment the counter on redirect

Analytic writes should not be allowed to block redirect responses in a way that harms user experience.

The prototype should explicitly define **“aggregate analytics”** as the baseline rather than a full event stream.

## 6. Data Consistency Concerns

### Core Consistency Concerns

* Short code uniqueness
* Deletion semantics
* Atomic create/update operations
* Analytics increments not losing counts

### Key Design Choices

* Use database uniqueness constraints for code generation.
* Use transactions for create and delete transitions.
* Use hard deletion only.
* Do not retain deleted link rows.

### Deletion Strategy

The prototype uses hard deletion.

Deleting a short link physically removes the database row. Subsequent metadata,
analytics, and redirect requests for that short code return 404 Not Found.

Soft deletion and audit retention are intentionally out of scope for the prototype.

### Planned Brownfield Enhancement — Expiration

Expiration is intentionally excluded from the greenfield baseline.

When added later, it should require:

* A Flyway migration
* Domain/entity changes
* Request/response DTO changes
* Validation updates
* Redirect behavior changes
* New `410 Gone` handling
* Additional tests

That keeps the baseline clean while still documenting the expected brownfield path.

### Main Concern

Redirect and analytics writes are not a single ACID unit if designed naively.

That is acceptable if the system accepts **“best effort” analytics**, but the contract must be explicit.

There is no need for multiple databases at this scale.

## 7. Concurrency Concerns

### Most Important Concurrency Issues

* Two create requests trying to generate the same short code
* Race between delete and redirect
* Analytics counter increment under high traffic

### Mitigations

* Unique database index on short code
* Transactional create operation
* Status check and redirect using current database state at read time
* Avoid optimistic assumptions in code generation

The architecture is fine for prototype-level concurrency.

It does not justify Kafka or separate worker services unless extreme throughput or decoupling is explicitly required.

## 8. Failure Scenarios

### Database Unavailable

* Create and redirect fail gracefully.
* No invalid data should be published.

### Invalid URL

* Reject before persistence.

### Unknown Code

* Return `404 Not Found`.

### Deleted Code

* Return `404 Not Found`.

### Analytics Failure

* Redirect should still succeed if the analytics write fails, unless strict consistency is required.

### Partial Writes

* Avoid leaving codes in inconsistent states by using appropriate transactional boundaries.

### Operational Requirements

* Logs and health endpoints are necessary.
* Complex distributed tracing or an event bus is not needed at this stage.

## 9. Security Concerns

The biggest security risk is **open redirect behavior**:

* The service is effectively redirecting users to arbitrary external URLs.
* Unvalidated destinations can lead to malicious redirects.

### Must Do

* Strict URL validation
* Allow only `http`/`https`
* Reject malformed or dangerous URLs
* Avoid leaking internal errors
* Sanitize logging

### Additional Concerns

* No authentication on the prototype unless explicitly required.
* Avoid exposing database/server internals in API errors.
* Do not log raw redirect targets if they could contain sensitive query strings.

This architecture is secure enough for a prototype if validation is explicit and narrow.

## 10. Scalability Limitations

The architecture will scale to a moderate load, but it has real limits:

* Single PostgreSQL writer
* All reads/writes share one database
* Analytics increments can become a hot spot
* Redirect traffic and analytics write volume may bottleneck the application

These limitations are not a problem for a prototype, but they should be acknowledged.

A monolith can still scale horizontally by running multiple application instances behind a load balancer, but the database remains the shared chokepoint.

This does not justify microservices or a multi-database architecture for the prototype.

## 11. Alternatives That Should Be Rejected as Overengineering

### Microservices

**Rejected because:**

* The service has a single transactional domain.
* There is no real separation of independently deployable concerns.
* The boundaries here are conceptual modules, not autonomous services.

### Kafka / Event Streaming

**Rejected because:**

* A basic URL shortener with aggregate analytics does not require it.
* It adds operational complexity without a clear benefit.
* Analytics can be handled in-process or through a lightweight asynchronous pattern later if demand increases.

### Redis

**Not necessary for a prototype because:**

* There is no proven cache-hit problem.
* The application is not sufficiently read-heavy to justify a second datastore initially.
* Using Redis for code generation or redirect caching is premature.

### Multiple Databases

**Not justified because:**

* This is a single-application, single-domain system.
* Multiple databases introduce data consistency and operational complexity without a corresponding benefit.

### Additional Rejected Complexity

* Separate authentication service
* Event-driven architecture
* Per-click detailed analytics streams
* Multi-region deployment
* Queue-based background processing for basic link creation and redirects

## Conclusion

The proposed architecture is solid for the prototype.

The main issue is not the architecture itself; it is avoiding the temptation to add distributed systems and specialized infrastructure before there is a measured need.

For this assignment, a **modular monolith with PostgreSQL** is the correct and deliberately minimal design.
