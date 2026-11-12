# Design and interview guide

## Explain the request path
Follow one POST from the JavaScript fetch call through Spring Security, the controller record validation, service transaction, repository and SQL schema. Show where each failure becomes 400, 401, 403, 404 or 409. Request DTOs are separate from persistence entities, so internal fields are not serialized accidentally.

## Core Java topics to demonstrate
Explain records, enum switches, constructor injection, Optional, exceptions, generics, collections, streams, BigDecimal, Instant, Duration and thread coordination in the tests. Explain why objects need a protected no-argument constructor for JPA. Show where database state is protected by a transaction rather than a Java synchronized block.

## Security
Show how BCrypt hashes passwords and how roles control operations. Demonstrate an ownership denial with a second user. Explain why a 404 is used for private records and why HTTP Basic needs HTTPS. These are demo accounts; durable account management is a future extension.

## Testing
Run the suite and explain three assertions. Tests execute with H2, so PostgreSQL locking and SQL compatibility still deserve a real PostgreSQL integration suite. JaCoCo reports coverage; coverage alone does not prove correctness. No throughput, latency improvement or production scale has been measured.


## SupportDesk questions
- How are deadlines calculated? At creation using an enum-to-duration switch, from a UTC Instant. They measure resolution time, not first response, and do not pause outside office hours.
- What does the SLA report mean? It counts currently OPEN/IN_PROGRESS tickets past due. It does not calculate historical breach rates or average response times.
- Why claim tickets? Ownership of work prevents two agents from processing the same ticket. A pessimistic lock serializes claim and state changes.
- Why a state machine? The service permits only the next valid step. A customer cannot resolve work and an agent cannot close on the customer's behalf.
- Are events tamper-proof? Events are append-only through the application API, but a privileged database operator can modify them. They are not a compliance-grade audit log.
- Why does closure require requester identity? It records requester acceptance of resolution. Reopening and reassignment are intentionally future features.


## Before using the resume bullets
1. Run every demo scenario and read the corresponding service methods.
2. Change something substantial and commit your own implementation: add tests before or alongside it.
3. Add screenshots, repository links and a reproducible demo to the README.
4. Keep only skills you can explain. Label this Project Experience and be candid about AI assistance when asked.

## Suggested extensions

- Reopening and reassignment with audited reasons and transition tests.
- A clock injected into SLA computation and reporting for deterministic boundary tests.
- PostgreSQL Testcontainers, durable accounts and searchable ticket text.
