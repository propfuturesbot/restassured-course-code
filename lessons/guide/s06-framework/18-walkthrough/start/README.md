# REST Assured + TestNG framework for the Students app and Campus Lab

Java 21, REST Assured 6.3.0, TestNG 7.12.0, Maven. One suite, three targets:

| `-Denv=` | Target | Needs |
|---|---|---|
| `local`  | the Students app jar on this machine, port 8131 | `java -jar studentApp-java21.jar --server.port=8131` |
| `docker` | the Students app in Docker, port 8080 (default) | `docker compose up -d students` |
| `campus` | Campus Lab, port 8000 | `export CAMPUS_API_KEY=...` (get one at `/keys`) |

```
mvn test -Denv=docker                          # the whole suite (testng.xml)
mvn test -Denv=campus -Dsuite=suites/smoke.xml # one suite file
mvn test -Denv=campus -Dgroups=smoke           # one group
mvn test -Dtest=NegativeTest -Denv=docker      # one class
./run-local.sh                                 # start both apps in Docker, run everything on both, stop
./allure-report.sh -Denv=campus                # run + Allure report with history (trends)
```

## The layers (top to bottom)

```
tests/                      what we check: one class per area, groups smoke / functional / demo
  BaseTest                  clients, banner, cleanup after every test, optional sandbox reset
framework/steps/            @Step methods: the vocabulary of the Allure report
framework/clients/          one class per API: StudentsApi, EnrollmentsApi, CoursesApi, AuthApi, SandboxApi
  ApiClient                 the per-thread request specification every client starts from
framework/http/             SpecFactory (no static RestAssured state) + filters on every request:
                            HttpLogFilter -> AllureRestAssured -> RateLimitFilter -> ContractFilter
framework/models/           records: Student (+ builder), Enrollment, Course
framework/data/             StudentData (Datafaker), DataFiles (CSV / JSON / Excel), Cleanup (per thread)
framework/assertions/       StudentAssert (AssertJ), ApiMatchers (Hamcrest)
framework/defects/          KnownDefects: the Students app's 500s, reported instead of hidden
framework/listeners/        TestListener (log lines, MDC), RetryAnalyzer + RetryTransformer (transient only)
framework/report/           AllureEnvironment
framework/config/           TestConfig (-Denv, -D / env var / file, secrets from the environment), App
src/main/resources/env/     local / docker / campus .properties      schemas/  JSON Schemas
src/test/resources/         data/ (csv, json, xlsx), logback-test.xml, allure.properties
```

Rules the code follows:

- **No static state.** Specs come from `SpecFactory`, one per thread (`ApiClient`); `Cleanup` and the log
  MDC are per thread too. That is why `suites/parallel-methods.xml` works.
- **Clients never assert.** They return the `Response`; the test decides. The same client serves happy
  and negative tests.
- **Every response is a contract test.** `ContractFilter` validates each body against its schema.
- **Tests state the correct behaviour.** Known defects of the Students app (validation and duplicate email
  answer 500) skip with their id; `-Ddefects=fail` shows them failing.
- **Secrets only from the environment.** Files name the variable (`api.key.env=CAMPUS_API_KEY`); logs mask
  keys, tokens and passwords.

## The two contracts

Both apps serve `/student` (list, get, create, replace, patch, delete). The differences live in one place
each: `StudentsApi.createAndGetId` (Campus Lab answers a POST with the student, the Students app with
`{"msg"}`), `ContractFilter.schemaFor` (problem+json vs `{"error"}`), `KnownDefects`. Enrollments, courses,
auth and the sandbox exist only on Campus Lab; those classes skip on the Students app.

## Extending it: a new endpoint in four steps

The course catalog (`/api/v1/courses`) was added this way:

1. **Model** - `framework/models/Course.java`, a record with `@JsonIgnoreProperties(ignoreUnknown = true)`.
2. **Client** - `framework/clients/CoursesApi.java`, methods that build on `ApiClient.given()` and return `Response`.
3. **Contract** - `schemas/course.json` + `course-page.json`, and two lines in `ContractFilter.schemaFor`.
4. **Tests** - `tests/CoursesTest.java`, extends `BaseTest`, in a group, skipped where the endpoint does not exist.

A new target is one more file in `src/main/resources/env/`. A new data file goes in `src/test/resources/data`
and is read with `DataFiles`.

## Running everything locally

`run-local.sh` uses `docker-compose.yml`, which builds the two apps from folders next to this project
(override with `STUDENTS_APP_DIR` and `CAMPUS_LAB_DIR`):

```
parent/
  students-app/     studentApp-java21.jar + Dockerfile (from the course's apps/ folder)
  campus-lab/       a clone of the Campus Lab repo
  this-project/
```
