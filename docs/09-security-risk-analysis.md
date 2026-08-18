# Security and Risk Analysis

## URL Validation

Creation accepts only syntactically valid absolute `http` or `https` URLs with
a host. Blank, malformed, and unsupported-scheme values receive `400` Problem
Details. This validates redirect targets; it does not establish that a target
is safe or trustworthy.

## Abuse and Rate Limiting

Only `POST /api/v1/links` is limited: 20 requests per remote address in a
60-second fixed window. The thread-safe limiter is intentionally in-memory and
is documented in the [ambiguous requirement scenario](07-ambiguous-scenario.md).
It does not protect redirects or provide distributed enforcement.

## Proxy and IP Trust

The limiter uses `HttpServletRequest.getRemoteAddr()` and does not trust
client-supplied `X-Forwarded-For`. The application enables framework forwarded
headers for generated public URLs, but a production deployment must define and
enforce its trusted proxy boundary before treating forwarded addresses as client
identity.

## Destination Risk

The product is an open redirector for stored HTTP(S) destinations. Phishing,
malware, reputation checks, deny lists, and destination allow lists are not
implemented. Production policy may require scanning, reporting/takedown
processes, and abuse monitoring.

## Error and Data Exposure

Centralized Problem Details return stable status, title, detail, and request
instance without stack traces, SQL errors, or database internals. Destination
URLs can contain sensitive query parameters or tokens; production logging
should avoid recording complete URLs and access logs should be reviewed with
that risk in mind.

Seven-character Base62 codes offer a large namespace but are not authorization
controls. They can be discovered or shared, so private-link access control is
outside the current design.

## Secrets and Production Hardening

Compose database credentials are local-development values only. Production
needs externally managed secrets, TLS termination, least-privilege database
credentials, explicit trusted-proxy configuration, dependency/vulnerability
scanning, monitoring and alerting, and—if scale requires it—gateway or
distributed rate limiting. Authentication should be added only if a product
requirement calls for it.
