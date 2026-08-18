# Brownfield Scenario

## Requirement

Add optional expiration to short links without breaking existing behavior.

## Impact Analysis Before Modification

The completed greenfield backend already supported create, redirect,
metadata, analytics, delete, centralized errors, OpenAPI, PostgreSQL, and
Testcontainers coverage. Expiration touches the create API, the `Link`
entity, redirect decisioning, metadata shape, error handling, and schema, so
it had to be treated as a true brownfield change.

## Affected Modules

- `link/api` for request/response DTOs and metadata output
- `link/application` for validation and lifecycle rules
- `link/domain` for the `Link` entity
- `link/persistence` for expiration-aware analytics updates
- `link/redirect/application` for 410 vs 404 redirect behavior
- `common/exception` for standardized ProblemDetail mapping
- Flyway migrations and Testcontainers-backed persistence tests

## V2 Migration

Add `expires_at TIMESTAMPTZ NULL` in a new Flyway migration:
`V2__add_link_expiration.sql`.
Existing rows remain valid because `NULL` means non-expiring.

## Behavior Matrix

| Case | Redirect result |
| --- | --- |
| No expiration | 302 |
| Future expiration | 302 |
| Expired | 410 |
| Unknown/deleted | 404 |

Expired redirects do not increment `click_count` or update
`last_accessed_at`.

## Backward Compatibility

Existing links with `expires_at = NULL` continue to work exactly as before.
Create requests without `expiresAt` still succeed, metadata exposes the field
additively, and delete remains hard delete.

## Tests

- create without expiration still works
- create with future expiration works
- past expiration rejected
- expiration equal to current time rejected
- existing rows without expiration continue working
- active expiring link redirects
- expired link returns 410
- expired link does not increment click_count
- unknown/deleted remains 404
- metadata returns expiresAt
- Flyway/Testcontainers verifies V2 schema

## Risks and Trade-offs

- Redirect logic must distinguish expired rows from deleted rows without
  breaking the existing 404 behavior
- Expiration is enforced at request time only; no background cleanup is needed
- Time-boundary tests depend on the injected `Clock` and exact `Instant`
  comparisons

## Engineer Approval

The brownfield expiration enhancement was reviewed after implementation and
approved as a backward-compatible change to the completed greenfield baseline.