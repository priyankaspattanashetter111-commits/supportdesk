# SupportDesk

A Java internship portfolio application demonstrating backend engineering through a working domain workflow. This is an educational reference project, generated with AI assistance; review, run and extend it before presenting it as your project experience. It is not evidence of employment or production usage.

## What is implemented

- Requesters create tickets and see only their own records; agents can see the queue.
- Agents claim unassigned tickets; another agent cannot steal a claim.
- Enforced workflow: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED. Only the assignee progresses work; only the requester closes it.
- Priority-based resolution deadlines: URGENT 2h, HIGH 8h, MEDIUM 24h, LOW 72h from creation, measured in elapsed UTC time.
- Authorized comments and chronological audit events (API returns newest first).
- Staff-only report of ticket status counts and active overdue tickets.
- Pagination, optional status filtering and database indexes for queue and SLA queries.

## Stack
Java 21, Spring Boot 3.5.16, Spring MVC, Spring Data JPA/Hibernate, Spring Security with BCrypt and HTTP Basic, Jakarta Bean Validation, SQL, Flyway, H2, PostgreSQL configuration, Maven Wrapper, JUnit 5, MockMvc, JaCoCo, HTML/CSS/JavaScript, Docker Compose and GitHub Actions.
PostgreSQL and Docker paths are supplied as optional configurations. See VALIDATION.md for which paths were actually exercised.

## Run in VS Code on Windows
1. Install a **JDK 21**, VS Code and its **Extension Pack for Java**. Ensure `java -version` and `javac -version` both report 21. Set JAVA_HOME to your JDK directory if required.
2. Extract the ZIP. Open the `supportdesk` folder containing `pom.xml` in VS Code. Accept the recommended Java extensions and allow Maven import to finish.
3. In the VS Code PowerShell terminal, run:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

The first run needs internet to download Maven and dependencies. Maven does not need to be preinstalled. On macOS/Linux use `chmod +x mvnw` once, then `./mvnw clean verify` and `./mvnw spring-boot:run`.

4. Open http://localhost:8081. Sign in using `alice`, `bob`, `agent` or `admin`, password `LearnJava!21`.
5. Stop with Ctrl+C. Default H2 records persist in `data/`; tests use a separate in-memory database. StockFlow seeds two products only on first database initialization. SupportDesk starts with no tickets.

Optional local password override, before starting:
```powershell
$env:DEMO_PASSWORD = 'your-local-demo-password'
.\mvnw.cmd spring-boot:run
```
Tests expect the default demo password. Clear a custom override with `Remove-Item Env:DEMO_PASSWORD` before testing.

You can run both projects together: StockFlow uses 8080 and SupportDesk uses 8081. To change the port, run `.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8090'`.

## Try the workflow

1. Sign in as `alice` and create a HIGH priority ticket. Record its ID and 8-hour deadline.
2. Sign in as `bob`: Alice's ticket is not in his list; its detail URL returns 404.
3. Sign in as `agent`, claim that ticket and move it to IN_PROGRESS.
4. Add a comment, inspect history, then mark it RESOLVED.
5. Sign in as `alice` and close it. Further comments return 409.
6. Sign in as `agent` and load the SLA report; inspect status counts.
7. Create tickets at other priorities and compare deadlines and filters.

## API requests
Open `requests.http` using VS Code's REST Client extension. Requests use demo credentials; update its variables if you change the password. For POST requests send `Content-Type: application/json`; claim/cancel endpoints require an empty JSON object. Business errors use RFC ProblemDetail JSON. Authentication/authorization filter errors can have an empty body.
Lists return `content`, `page`, `size` and `totalElements`; page starts at 0 and size must be 1..100. The browser demo shows at most the latest 100 records. Use the API to paginate older records.

## Tests and coverage
```powershell
.\mvnw.cmd clean verify
```
Integration tests exercise real controllers, authentication, services, JPA, Flyway and H2. Read `target/surefire-reports` for results and open `target/site/jacoco/index.html` for coverage. No coverage percentage or benchmark is claimed. CI repeats verification on each push/PR. There is no PostgreSQL container integration test in this version.

## Optional PostgreSQL and Docker
Install Docker Desktop first. From this project folder:
```powershell
Copy-Item .env.example .env
# Edit BOTH passwords in .env before starting.
docker compose up --build
```
Docker starts PostgreSQL and the Java app. Stop any local copy first to free port 8081. Database data persists in a named volume. To stop: `docker compose down` (keeps data). Never commit `.env`. A direct Java/PostgreSQL setup can use the `postgres` Spring profile and DB_URL, DB_USER, DB_PASSWORD environment variables.

## Architecture and files
```text
Browser or REST client
  -> Spring Security authentication
  -> REST controller and validated request records
  -> transactional domain service with authorization checks
  -> Spring Data repositories
  -> H2 or PostgreSQL
```
`src/main/java/dev/portfolio` holds application code. `db/migration` contains the schema and indexes. `static` contains the demo UI. `src/test` contains integration tests. No Lombok or external account is required.

## Push to your GitHub repository
Create a new **empty** repository in your GitHub account, then run these commands in this project folder. Replace the example remote with your real URL; authentication is handled by Git/GitHub.
```powershell
git init
git add .
git commit -m "Add SupportDesk portfolio application"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/supportdesk.git
git push -u origin main
```
This package has no nested `.git` folder. Build outputs, local databases and secrets are ignored. Add your own screenshots, customization notes and a short demo video before applying.

## Scope and tradeoffs
- Demo users are in memory and share a configurable password. There is no signup, password reset, JWT/OAuth or external identity provider.
- Basic credentials are encoded, not encrypted in transit; HTTPS is required for any remote deployment. The demo is intended for localhost.
- CSRF is disabled for stateless JSON API calls. Mutations require JSON payloads; CORS is not enabled. Review browser authentication and CSRF design before broader deployment.
- No payment processing, email, distributed messaging, Redis, cloud deployment, business calendar or production operations is claimed.
- Database operations are transactional, but external side effects and distributed transactions are out of scope.
- PostgreSQL configuration, container files and CI workflow are supplied; they require verification in your own environment before you claim deployment experience.

## Interview preparation
Read `INTERVIEW_GUIDE.md` and `VALIDATION.md`. Explain the code in your own words and make at least one meaningful extension. These projects support a Project Experience section; they do not justify invented work history.

Framework reference: https://docs.spring.io/spring-boot/3.5/system-requirements.html

 ## Windows troubleshooting
 The Maven wrapper has a small quoting correction for Windows user folders that contain spaces.
 If Java reports `Unable to establish loopback connection` or `Invalid argument: connect`, create a short local temporary folder and point Java at it. From this project folder:
 ```powershell
 New-Item -ItemType Directory -Force .java-tmp | Out-Null
 $javaTmp = (Resolve-Path .java-tmp).Path
 $env:JAVA_TOOL_OPTIONS = '-Djdk.net.unixdomain.tmpdir="' + $javaTmp + '"' 
 .\mvnw.cmd spring-boot:run
 ```
 The temporary-folder setting was needed by the Windows verification environment; it is not a database or application error.
 