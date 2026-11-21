# REST Assured courses: updated code (2026)

The code of the two REST Assured courses on Udemy, updated for Java 21 and REST Assured 6.3.0:

- **REST Assured API Automation, Allure Reports - An Introduction** (lectures `I...`, folder `lessons/intro`)
- **REST API Automation With REST Assured - A Complete Guide** (lectures `G...`, folder `lessons/guide`)

Start with the course's "Update notes and FAQ" article: it explains what changed and answers the most common questions.

## Download

- The whole repository: **Code > Download ZIP** (or `git clone https://github.com/propfuturesbot/restassured-course-code.git`).
- **The fixed Students app**: `apps/students-app/studentApp-java21.jar`, also attached to the release
  [students-app-java21](https://github.com/propfuturesbot/restassured-course-code/releases/tag/students-app-java21)
  together with the two Spring Security apps (`studentSpringSecurity.war`, `studentSpringSecurity_CSRF.war`) used by the
  form-login and CSRF lectures.

## What you need

| Tool | Version used in the course | Notes |
|---|---|---|
| JDK | 21 (Eclipse Temurin 21) | REST Assured 6 needs Java 17+; the projects are set to Java 21 |
| Maven | 3.9 | 3.6.3 or newer; older Maven stops with "requires Maven version 3.6.3" |
| IDE | VS Code (Extension Pack for Java), IntelliJ IDEA or Eclipse | every project runs with `mvn test` from a terminal |
| Docker | optional | for the Students app image and the BestBuy playground |
| Node.js | optional | only for the Allure 3 report (`npx allure@3.22.1 ...`) |

Libraries: REST Assured 6.3.0, JUnit Jupiter 6.1.3 (Introduction), TestNG 7.12.0 (Complete Guide framework),
Jackson 2.23, Hamcrest 3.0, Datafaker, Allure. Each folder's `pom.xml` lists exactly what it uses.

## The practice apps

**Students app** (port 8080) - `apps/students-app`

```
cd apps/students-app
java -jar studentApp-java21.jar                  # Java 17 or newer; ./run.sh 8085 for another port
curl http://localhost:8080/student/list
```

or with Docker (the Dockerfile builds natively on Intel/AMD and Apple Silicon):

```
docker build -t students-app apps/students-app
docker run -d -p 8080:8080 students-app
```

`studentApp-java21.jar` is the course's `studentApp-java17.jar` with one fix: new students no longer clash with the
100 seeded ones, so POST answers 201 instead of 500 "Email address already exists". Details: `apps/students-app/FIX.md`.
The app keeps its data in memory: a restart brings back the 100 seeded students.

**BestBuy playground** (port 3030), for the JsonPath and Hamcrest lectures:

```
docker run -d -p 3030:3030 tejasn1/best-buy-api-playground
```

(The image is amd64 only; on Apple Silicon Docker runs it under emulation.) `apps/docker-compose.yml` starts both apps:
`docker compose -f apps/docker-compose.yml up -d --build`.

**Campus Lab** - the practice API used by most new lectures: <https://campus-lab-production.up.railway.app>.
Get a free key (no sign-up) at <https://campus-lab-production.up.railway.app/keys>; the page also shows your Basic auth
user and an OAuth client id and secret. Keys expire after 30 days. The code reads them from environment variables:

```
export CAMPUS_BASE_URL=https://campus-lab-production.up.railway.app
export CAMPUS_API_KEY=<your key>
export CAMPUS_CLIENT_ID=<client id>          # OAuth lectures
export CAMPUS_CLIENT_SECRET=<client secret>
export CAMPUS_BASIC_USER=<basic user>        # Basic auth lectures
export CAMPUS_BASIC_PASSWORD=<basic password>
```

(Windows PowerShell: `$env:CAMPUS_API_KEY="<your key>"`.) Never commit a key: the code only reads the variables.

Notes for the public Campus Lab:

- Lessons default to `http://localhost:8000` when `CAMPUS_BASE_URL` is not set (the address of a local Campus Lab, as
  recorded); setting the variable above points them at the public lab.
- The TestNG framework (`lessons/guide/s06-framework`) reads the address from its env file; for `-Denv=campus` also set
  `BASE_URI=https://campus-lab-production.up.railway.app` (or pass `-Dbase.uri=...`).
- A few lessons need things only a local Campus Lab has, and skip or fail on the public one: the JWT bad-claim tests
  (`JwtNegativeTest`, they need the lab's signing key; three of its six tests are skipped without it), gRPC
  (`localhost:50051`), and `docker-compose.yml` / `run-local.sh` of the framework lessons 17-18, which build Campus Lab
  from source.
- The ETag lesson's check that the 304 answer repeats the exact same tag can fail on the public lab: its hosting proxy
  may send the tag in weak form (`W/"..."`). The If-None-Match / If-Match behaviour itself is the same.

## Folders

```
apps/        the practice apps (Students app jar + Dockerfile + run.sh, docker-compose.yml)
lessons/
  intro/     the Introduction's new lectures:   sNN-section/LL-lesson/{start,final}
  guide/     the Complete Guide's new lectures: sNN-section/LL-lesson/{start,final}
legacy/      the projects from the older lectures' downloads, upgraded (see below)
ai/prompts/  prompts used in the AI lectures
```

Every lesson has `start/` (type along with the video) and `final/` (the finished code). Both are complete Maven
projects. Some lessons fail ON PURPOSE in one test to show what a failure looks like in the report (the video says so).

### Introduction (lessons/intro)

| Lecture | Title | Folder | Run (inside `final/` or `start/`) | Needs |
|---|---|---|---|---|
| I1.2 | VS Code for Java: extensions, run and debug tests | [`lessons/intro/s01-setup/02-vscode`](lessons/intro/s01-setup/02-vscode) | `mvn -B test -Dtest=StudentsAppTest` | students |
| I2.1 | Upgrade the course project live: from REST Assured 4 to 6 | [`lessons/intro/s02-first-tests/01-upgrade`](lessons/intro/s02-first-tests/01-upgrade) | `mvn -B test` | students |
| I2.2 | Same CRUD tests on REST Assured 6 + JUnit 6 | [`lessons/intro/s02-first-tests/02-crud`](lessons/intro/s02-first-tests/02-crud) | `mvn -B test -Dtest=StudentCrudTest` | students |
| I2.3 | POJOs with Jackson and Java records; extract().as() | [`lessons/intro/s02-first-tests/03-pojo`](lessons/intro/s02-first-tests/03-pojo) | `mvn -B test -Dtest=StudentObjectsTest` | students |
| I2.4 | Test data with Datafaker (replaces Java Faker) | [`lessons/intro/s02-first-tests/04-datafaker`](lessons/intro/s02-first-tests/04-datafaker) | `mvn -B test -Dtest=FakeStudentTest` | students |
| I2.5 | JSON Schema validation (most-requested topic) | [`lessons/intro/s02-first-tests/05-schema`](lessons/intro/s02-first-tests/05-schema) | `mvn -B test -Dtest=StudentSchemaTest` | students |
| I2.6 | Logging only on failure, with filters | [`lessons/intro/s02-first-tests/06-logging`](lessons/intro/s02-first-tests/06-logging) | `mvn -B test -Dtest=LoggingTest` | students |
| I3.2 | The same auth in REST Assured on Campus Lab | [`lessons/intro/s03-auth/02-campus-auth`](lessons/intro/s03-auth/02-campus-auth) | `mvn -B test -Dtest=CampusAuthTest` | campus |
| I3.3 | OAuth2 client credentials in code | [`lessons/intro/s03-auth/03-client-credentials`](lessons/intro/s03-auth/03-client-credentials) | `mvn -B test -Dtest=ClientCredentialsTest` | campus |
| I4.1 | allure-junit5 and the REST Assured filter: every call in the report | [`lessons/intro/s04-allure/01-allure-filter`](lessons/intro/s04-allure/01-allure-filter) | `mvn -B test -Dtest=StudentReportTest` | students |
| I4.2 | Allure 3 report and a link you can share | [`lessons/intro/s04-allure/02-allure3`](lessons/intro/s04-allure/02-allure3) | `mvn -B test` | students |
| I5.1 | Let Claude write tests from Campus Lab's OpenAPI, then review them | [`lessons/intro/s05-ai/01-claude-openapi`](lessons/intro/s05-ai/01-claude-openapi) | `mvn -B test -Dtest=StudentsApiTest` | campus |

### Complete Guide (lessons/guide)

| Lecture | Title | Folder | Run (inside `final/` or `start/`) | Needs |
|---|---|---|---|---|
| G1.4 | Upgrade a 2017 project to REST Assured 6, error by error | [`lessons/guide/s01-env/04-upgrade-2017`](lessons/guide/s01-env/04-upgrade-2017) | `mvn -B test -Dtest='*'` | - |
| G2.3 | From Postman to given/when/then | [`lessons/guide/s02-fundamentals/03-postman-to-code`](lessons/guide/s02-fundamentals/03-postman-to-code) | `mvn -B test -Dtest=PostmanToCodeTest` | campus |
| G3.1 | Assertions with Hamcrest on BestBuy data (single + multiple) | [`lessons/guide/s03-fixed/01-hamcrest-bestbuy`](lessons/guide/s03-fixed/01-hamcrest-bestbuy) | `mvn -B test -Dtest=HamcrestAssertionsTest` | bestbuy |
| G3.2 | Root path and soft assertions on BestBuy | [`lessons/guide/s03-fixed/02-rootpath`](lessons/guide/s03-fixed/02-rootpath) | `mvn -B test -Dtest=RootPathTest` | bestbuy |
| G3.3 | XmlPath on Campus Lab XML responses | [`lessons/guide/s03-fixed/03-xmlpath`](lessons/guide/s03-fixed/03-xmlpath) | `mvn -B test -Dtest=XmlPathTest` | campus |
| G3.4 | SOAP: Campus Lab StudentService (WSDL, envelope, faults) | [`lessons/guide/s03-fixed/04-soap`](lessons/guide/s03-fixed/04-soap) | `mvn -B test -Dtest=StudentServiceSoapTest` | campus |
| G3.5 | File upload and download on Campus Lab (replaces Zamzar) | [`lessons/guide/s03-fixed/05-upload`](lessons/guide/s03-fixed/05-upload) | `mvn -B test -Dtest=DocumentUploadTest` | campus |
| G3.6 | Form login and CSRF with REST Assured 6 (csrf() API) | [`lessons/guide/s03-fixed/06-form-csrf`](lessons/guide/s03-fixed/06-form-csrf) | `mvn -B test` | csrf-war, campus |
| G4.2 | Deserialize instead of digging: POJOs, records, TypeRef, generics | [`lessons/guide/s04-real-api/02-deserialize`](lessons/guide/s04-real-api/02-deserialize) | `mvn -B test -Dtest=DeserializeTest` | campus |
| G4.3 | JSON Schema validation and problem+json errors | [`lessons/guide/s04-real-api/03-schema`](lessons/guide/s04-real-api/03-schema) | `mvn -B test -Dtest=SchemaTest` | campus |
| G4.4 | Pagination with Link headers and cursors | [`lessons/guide/s04-real-api/04-pagination`](lessons/guide/s04-real-api/04-pagination) | `mvn -B test -Dtest=PaginationTest` | campus |
| G4.5 | ETags: If-None-Match 304 and If-Match 412 | [`lessons/guide/s04-real-api/05-etags`](lessons/guide/s04-real-api/05-etags) | `mvn -B test -Dtest=ETagTest` | campus |
| G4.6 | Idempotency keys and retry on 429 with Retry-After | [`lessons/guide/s04-real-api/06-idempotency-429`](lessons/guide/s04-real-api/06-idempotency-429) | `mvn -B test -Dtest=IdempotencyAndRateLimitTest` | campus |
| G4.7 | Async jobs: 202, poll with Awaitility, download the result | [`lessons/guide/s04-real-api/07-async`](lessons/guide/s04-real-api/07-async) | `mvn -B test -Dtest=AsyncJobTest` | campus |
| G4.8 | Comparing whole JSON while ignoring fields (JSONAssert customisation) | [`lessons/guide/s04-real-api/08-jsonassert`](lessons/guide/s04-real-api/08-jsonassert) | `mvn -B test -Dtest=JsonAssertTest` | campus |
| G5.2 | Bearer tokens with a caching auth filter | [`lessons/guide/s05-auth/02-auth-filter`](lessons/guide/s05-auth/02-auth-filter) | `mvn -B test -Dtest=BearerTokenFilterTest` | campus |
| G5.3 | OAuth2 authorization code + PKCE in Java | [`lessons/guide/s05-auth/03-pkce`](lessons/guide/s05-auth/03-pkce) | `mvn -B test -Dtest=PkceFlowTest` | campus |
| G5.4 | JWT negative tests: expired, wrong audience, tampered, alg none | [`lessons/guide/s05-auth/04-jwt-negative`](lessons/guide/s05-auth/04-jwt-negative) | `mvn -B test -Dtest=JwtNegativeTest` | campus |
| G5.5 | OWASP API Top 10 in tests: BOLA, mass assignment, rate limits | [`lessons/guide/s05-auth/05-owasp`](lessons/guide/s05-auth/05-owasp) | `mvn -B test -Dtest=ApiSecurityTest` | campus |
| G5.6 | TLS settings: trust stores and relaxed HTTPS for test servers | [`lessons/guide/s05-auth/06-tls`](lessons/guide/s05-auth/06-tls) | `mvn -B test -Dtest=TlsSettingsTest` | students |
| G6.2 | Project skeleton: Maven, TestNG suites, Java 21 | [`lessons/guide/s06-framework/02-skeleton`](lessons/guide/s06-framework/02-skeleton) | `mvn -B test` | students |
| G6.3 | Config per environment: local jar, Docker, Campus Lab (-Denv, env vars, secrets) | [`lessons/guide/s06-framework/03-config-per-env`](lessons/guide/s06-framework/03-config-per-env) | `mvn -B test -Denv=local` | students-jar, students, campus |
| G6.4 | Thread-safe specs and clients (no static RestAssured state) | [`lessons/guide/s06-framework/04-thread-safe-specs`](lessons/guide/s06-framework/04-thread-safe-specs) | `mvn -B test -Denv=docker` | students, campus |
| G6.5 | API client layer: StudentsApi, EnrollmentsApi | [`lessons/guide/s06-framework/05-api-clients`](lessons/guide/s06-framework/05-api-clients) | `mvn -B test -Dtest='StudentStepsTest,EnrollmentStepsTest' -Denv=docker` | students, students-jar, campus |
| G6.6 | Models with records + builders; Datafaker test data; cleanup and sandbox reset | [`lessons/guide/s06-framework/06-models-data-cleanup`](lessons/guide/s06-framework/06-models-data-cleanup) | `mvn -B test -Dtest=StudentDataTest -Denv=docker` | students, students-jar, campus |
| G6.7 | Assertions: AssertJ, soft assertions, custom matchers | [`lessons/guide/s06-framework/07-assertions`](lessons/guide/s06-framework/07-assertions) | `mvn -B test -Dtest=AssertionsTest -Denv=docker` | students, campus |
| G6.8 | Smoke and functional suites with groups | [`lessons/guide/s06-framework/08-suites-groups`](lessons/guide/s06-framework/08-suites-groups) | `mvn -B test -Denv=docker` | students, campus |
| G6.9 | Negative and boundary tests (400, 404, 409, 422) | [`lessons/guide/s06-framework/09-negative-boundary`](lessons/guide/s06-framework/09-negative-boundary) | `mvn -B test -Dtest='NegativeTest,EnrollmentRulesTest,BoundaryTest' -Denv=campus` | campus, students |
| G6.10 | Contract tests: JSON Schema on every response | [`lessons/guide/s06-framework/10-schema-contract`](lessons/guide/s06-framework/10-schema-contract) | `mvn -B test -Denv=docker` | students, campus |
| G6.11 | Auth tests in the framework | [`lessons/guide/s06-framework/11-auth-tests`](lessons/guide/s06-framework/11-auth-tests) | `mvn -B test -Dtest=AuthTest -Denv=campus` | campus |
| G6.12 | Data-driven tests: DataProvider from CSV, JSON and Excel (Apache POI) | [`lessons/guide/s06-framework/12-data-driven`](lessons/guide/s06-framework/12-data-driven) | `mvn -B test -Dtest=DataDrivenTest -Denv=campus` | campus, students |
| G6.13 | Parallel runs: methods vs classes, thread count, avoiding shared data | [`lessons/guide/s06-framework/13-parallel`](lessons/guide/s06-framework/13-parallel) | `mvn -B test -Dtest='SharedStateDemoTest,ParallelSafeTest' -Ddemo=true` | students, campus |
| G6.14 | Listeners and retry analyzer (and why retries are not fixes) | [`lessons/guide/s06-framework/14-listeners-retry`](lessons/guide/s06-framework/14-listeners-retry) | `mvn -B test -Dtest=RetryDemoTest -Ddemo=true` | students |
| G6.15 | Logging to file with SLF4J/Logback and request/response capture | [`lessons/guide/s06-framework/15-logging`](lessons/guide/s06-framework/15-logging) | `mvn -B test -Dtest='StudentStepsTest,AuthTest' -Denv=campus` | campus, students |
| G6.16 | Allure TestNG: steps, attachments, environment, history, trends | [`lessons/guide/s06-framework/16-allure-testng`](lessons/guide/s06-framework/16-allure-testng) | `./allure-report.sh -Denv=campus   (run it twice: the second report has a 2-run trend)` | campus, students |
| G6.17 | Run it all locally: docker compose with the Students app and Campus Lab | [`lessons/guide/s06-framework/17-compose-local`](lessons/guide/s06-framework/17-compose-local) | `./run-local.sh` | students (own container), campus (own container) |
| G6.18 | Code walkthrough and how to extend it (README tour) | [`lessons/guide/s06-framework/18-walkthrough`](lessons/guide/s06-framework/18-walkthrough) | `mvn -B test -Denv=campus` | campus, students-jar, students |
| G6.19 | Optional: Cucumber BDD layer on the same clients | [`lessons/guide/s06-framework/19-cucumber`](lessons/guide/s06-framework/19-cucumber) | `mvn -B test -Dtest=CucumberRunnerTest -Denv=docker` | students, campus |
| G7.2 | First workflow: checkout, setup-java, mvn test | [`lessons/guide/s07-github-actions/02-first-workflow`](lessons/guide/s07-github-actions/02-first-workflow) | `mvn -B test` | campus |
| G7.3 | Service containers: start the Students app inside the job | [`lessons/guide/s07-github-actions/03-service-containers`](lessons/guide/s07-github-actions/03-service-containers) | `mvn -B test` | students |
| G7.4 | Secrets, matrix (JDK 17/21), smoke on PR, full regression nightly | [`lessons/guide/s07-github-actions/04-secrets-matrix`](lessons/guide/s07-github-actions/04-secrets-matrix) | `mvn -B test -Dgroups=smoke` | campus |
| G7.5 | Publish the Allure report to GitHub Pages; fail the PR on red | [`lessons/guide/s07-github-actions/05-allure-pages`](lessons/guide/s07-github-actions/05-allure-pages) | `mvn -B test` | campus |
| G8.2 | A conventions file so Claude writes tests in your framework's style | [`lessons/guide/s08-ai/02-conventions-file`](lessons/guide/s08-ai/02-conventions-file) | `mvn -B test` | campus |
| G8.3 | Generate tests from Campus Lab's OpenAPI spec, run them, review them | [`lessons/guide/s08-ai/03-generate-from-openapi`](lessons/guide/s08-ai/03-generate-from-openapi) | `mvn -B test -Dtest='Courses*'` | campus |
| G8.4 | Test-case design with AI: boundaries, negatives, auth matrix from a JSON schema | [`lessons/guide/s08-ai/04-ai-test-design`](lessons/guide/s08-ai/04-ai-test-design) | `mvn -B test -Dtest='StudentCreateValidationTest,StudentAuthMatrixTest'` | campus |
| G8.5 | AI review of a pull request of tests: what it catches, what it misses | [`lessons/guide/s08-ai/05-ai-pr-review`](lessons/guide/s08-ai/05-ai-pr-review) | `mvn -B test` | campus |
| G8.6 | Triage a red CI run: logs and Allure to a root cause | [`lessons/guide/s08-ai/06-triage-red-ci`](lessons/guide/s08-ai/06-triage-red-ci) | `mvn -B test -Dgroups=smoke` | campus |
| G9.2 | GraphQL with REST Assured: queries, variables, errors with HTTP 200, depth limits | [`lessons/guide/s09-beyond-rest/02-graphql`](lessons/guide/s09-beyond-rest/02-graphql) | `mvn -B test -Dtest=GraphQlTest` | campus |
| G9.4 | WebSocket tests with OkHttp + Awaitility | [`lessons/guide/s09-beyond-rest/04-websocket`](lessons/guide/s09-beyond-rest/04-websocket) | `mvn -B test -Dtest=WebSocketTest` | campus |
| G9.5 | SSE event stream tests | [`lessons/guide/s09-beyond-rest/05-sse`](lessons/guide/s09-beyond-rest/05-sse) | `mvn -B test -Dtest=SseTest` | campus |
| G9.7 | gRPC tests with grpc-java: unary, streaming, status codes, deadlines | [`lessons/guide/s09-beyond-rest/07-grpc`](lessons/guide/s09-beyond-rest/07-grpc) | `mvn -B test -Dtest=GrpcStudentServiceTest` | campus-grpc |
| G9.8 | Webhooks: receive, verify the HMAC signature, test retries | [`lessons/guide/s09-beyond-rest/08-webhooks`](lessons/guide/s09-beyond-rest/08-webhooks) | `mvn -B test -Dtest=WebhookTest` | campus |
| G10.2 | WireMock: stubs, faults, delays, verify | [`lessons/guide/s10-mocks-contracts/02-wiremock`](lessons/guide/s10-mocks-contracts/02-wiremock) | `mvn -B test -Dtest=GatewayMockTest` | campus |
| G10.3 | Consumer-driven contracts with Pact (consumer side) | [`lessons/guide/s10-mocks-contracts/03-pact-consumer`](lessons/guide/s10-mocks-contracts/03-pact-consumer) | `mvn -B test -Dtest=StudentsAppPactTest` | - |
| G10.4 | Pact provider verification against the Students app | [`lessons/guide/s10-mocks-contracts/04-pact-provider`](lessons/guide/s10-mocks-contracts/04-pact-provider) | `mvn -B test -Dtest=StudentsAppProviderTest` | students |
| G10.5 | Validate requests and responses against OpenAPI in a filter | [`lessons/guide/s10-mocks-contracts/05-openapi-filter`](lessons/guide/s10-mocks-contracts/05-openapi-filter) | `mvn -B test -Dtest=OpenApiFilterTest` | campus |

"Needs": `students` = Students app on :8080, `bestbuy` = BestBuy playground on :3030, `campus` = Campus Lab (see above).

## legacy/: the older lectures' projects

The projects you downloaded from the older lectures, with upgraded poms (REST Assured 6.3.0, Java 21, current
surefire/compiler plugins) and only the code changes REST Assured 6 forces (`parameters()` -> `params()`,
`NodeChildrenImpl` -> `NodeChildren`, `withCsrfFieldName` -> `csrf()`, `com.jayway.restassured` -> `io.restassured`).
Dead third-party services (Walmart, Yahoo, Zamzar, PayPal without your own sandbox account, Twitter) were left as they
were: those tests fail because the service is gone, not because of the code. `legacy/README.md` lists every project,
what `mvn test` prints today and every change.

Classes named `*Example` / `*Examples` are not run by `mvn test` (Maven's surefire runs `*Test` classes only): run them
from the IDE or with `mvn test -Dtest='*'`.

### Java 8 or 11? Stay on REST Assured 5.5.7

REST Assured 6 needs Java 17. On Java 8 or 11 use REST Assured **5.5.7** (the last 5.x release) for the legacy projects:
in the project's `pom.xml` change the `rest-assured` version to `5.5.7`, delete
`<maven.compiler.release>21</maven.compiler.release>`, and set the compiler plugin's `<source>` and `<target>` to `8`
(or `11`). Tested with `legacy/StudentAppBasics` on JDK 8 and Maven 3.9.9: it compiles and runs its tests. The new
lessons (records, REST Assured 6) need Java 21, and the fixed Students app needs Java 17, so on an older Java run the
app with Docker.

## Questions

Ask in the course's Q&A on Udemy, with the lecture name, the error and your `java -version`.
