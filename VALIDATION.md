# Validation record

Verified on 12 November 2026 with Microsoft OpenJDK 21.0.13 and Maven 3.9.9 on Windows.

- `mvn verify`: BUILD SUCCESS; 9 integration tests, zero failures, zero errors, zero skipped.
- Flyway migration and Hibernate schema validation succeeded against H2.
- JavaScript passed Node syntax checking. Browser interactions were not visually automated.
- Packaged Spring Boot JAR started and served the HTML page and health endpoint.
- HTTP workflow smoke check passed against the running JAR: ticket creation, claim, status progression, requester closure and staff report.
- Runtime check required an explicit `jdk.net.unixdomain.tmpdir` on this Windows computer; see README troubleshooting.
- Maven Wrapper launcher: Maven 3.9.9 started successfully in a Windows path containing spaces.
- The source ZIP excludes build files, database files, local credentials and runtime downloads.

Not exercised here: PostgreSQL runtime, Docker build/Compose, GitHub-hosted CI, external deployment, load benchmarks, durable account lifecycle, or penetration testing. H2 tests do not prove PostgreSQL concurrency behavior.
