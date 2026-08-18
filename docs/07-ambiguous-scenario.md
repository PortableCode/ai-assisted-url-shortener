# Ambiguous Requirement Scenario — Abuse Protection

## Original Requirement

"Protect the service from abuse."

## Ambiguities Identified

- Which endpoint is protected
- What counts as abuse
- Identity model: IP, user, API key, or global
- Threshold and time window
- Anonymous vs authenticated access
- Single instance vs distributed deployment
- Proxy/load balancer behavior
- Whether redirects are rate limited
- Whether malicious destination URLs are in scope

## Normalized Prototype Requirement

- `POST /api/v1/links` only
- anonymous clients
- 20 requests per 60 seconds per client
- client identified by remote address
- 429 above the limit
- in-memory fixed-window implementation

## Engineering Decision

An in-memory limiter is appropriate for the prototype because the requirement is narrowly scoped to a single endpoint and the current backend is a modular monolith without Redis, authentication, or distributed infrastructure. It keeps the implementation small, testable, and isolated from persistence concerns while still demonstrating basic abuse control.

## Implementation

The create request is intercepted before controller execution. The interceptor reads `HttpServletRequest.getRemoteAddr()` and delegates to a thread-safe in-memory limiter. The limiter uses `ConcurrentHashMap.compute(...)` with immutable per-client window state to count requests within a 60-second fixed window. The 21st request in the same window throws a dedicated `RateLimitExceededException`, which is mapped centrally to RFC 9457 `ProblemDetail` with HTTP 429.

## Validation

Validation includes deterministic automated tests for:

- requests below the threshold
- the 20th request being allowed
- the 21st request being rejected
- per-client isolation
- window reset after 60 seconds
- standardized 429 error handling

A manual 21-request check was also performed against the live application path.

## Security Considerations

`X-Forwarded-For` is not blindly trusted because the prototype does not assume a trusted proxy chain. The client identity is intentionally derived from the remote address only. Traffic-rate abuse is handled here; malicious or phishing destination detection is a separate production concern and is not implemented.

## Production Evolution

Future production options could include:

- trusted reverse-proxy configuration
- API gateway enforcement
- Redis or another distributed counter store
- authenticated quotas

## Limitations

- process-local only
- resets on restart
- not horizontally distributed
- in-memory client-state growth

## Engineer Approval

The ambiguity was resolved before implementation, and the product policy was explicitly normalized by engineering review. AI followed the resolved requirement; it did not independently choose the rate-limiting policy.