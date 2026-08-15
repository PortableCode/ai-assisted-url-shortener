# Requirements Analysis

## 1. Assignment Objective

## 2. Functional Requirements

### 2.1 Greenfield Baseline

create short URL
validate only HTTP/HTTPS destinations
7-character Base62 short code
SecureRandom
database UNIQUE constraint
bounded collision retries
redirect
aggregate click count
last-accessed timestamp
metadata lookup
hard delete
consistent API errors
PostgreSQL persistence
tests
Swagger/OpenAPI
Actuator health

Expiration is intentionally excluded from the greenfield baseline so it can be introduced later as a brownfield enhancement against an existing working codebase.


### 2.2 Brownfield Enhancement

Expiration is intentionally excluded from the greenfield baseline so it can be introduced later as a brownfield enhancement against an existing working codebase.

Expected behavior:

Active        → 302
Expired       → 410
Unknown       → 404
Deleted       → 404
No expiration → continues working normally

### 2.3 Ambiguous Requirement Scenario

Protect the service from abuse.
It is intentionally unresolved at baseline and will later be normalized into a scoped prototype requirement.

### 2.4 Frontend Requirements

## 3. Non-Functional Requirements

### 3.1 Reliability
### 3.2 Security
### 3.3 Maintainability
### 3.4 Testability
### 3.5 Reviewer Usability

git clone https://github.com/PortableCode/ai-assisted-url-shortener
cd ai-assisted-url-shortener
docker compose up --build

no local Java, Maven, Node, npm, PostgreSQL, or IDE required

## 4. API Requirements

## 5. Data Requirements

## 6. Assumptions

## 7. Explicit Non-Goals

microservices
Kafka
Redis initially
Kubernetes
authentication
Spring Security
RAG
runtime LLM integration
MCP runtime
multi-agent architecture
cloud deployment
geo/device/referrer analytics
soft deletion
expiration in the baseline

## 8. Identified Ambiguities

## 9. Acceptance Criteria