# The framework we build in this section (roadmap)

One Maven project, built lesson by lesson; each lesson's `final/` is the previous one plus one change.
The finished project (lesson 18, plus the optional Cucumber layer in 19) looks like this.

## Layers: who talks to whom

```
        tests (TestNG)                  bdd (Cucumber, optional)
  smoke | functional | demo groups      .feature files + step definitions
                 \                          /
                  framework/steps   (@Step vocabulary for the Allure report)
                            |
                  framework/clients (StudentsApi, EnrollmentsApi, CoursesApi, AuthApi, SandboxApi)
                            |          return Responses, never assert
                  framework/clients/ApiClient   one request specification per thread
                            |
                  framework/http/SpecFactory    base URI, JSON, key; filters on EVERY call:
                     HttpLogFilter -> AllureRestAssured -> RateLimitFilter -> ContractFilter
                            |
                  framework/config/TestConfig   -Denv=local|docker|campus, -D > env var > file,
                                                secrets from the environment only
                            |
           the Students app (jar :8131 / Docker :8080)      Campus Lab (:8000, X-API-Key)

  beside the chain:  models (records + builder) | data (Datafaker, CSV/JSON/Excel, Cleanup)
                     assertions (AssertJ StudentAssert, Hamcrest ApiMatchers) | defects (KnownDefects)
                     listeners (TestListener, RetryAnalyzer) | report (AllureEnvironment)
```

## What each test type proves

| Type | Classes | Proves |
|---|---|---|
| Smoke | StudentListTest, StudentStepsTest, ContractTest, ReportedStudentTest | the target is up and the main path works (seconds) |
| Functional | StudentDataTest, AssertionsTest, EnrollmentStepsTest, CoursesTest, ThreadSafetyTest, ParallelSafeTest | the API does what it should with real data |
| Negative + boundary | NegativeTest, EnrollmentRulesTest, BoundaryTest | 400 / 404 / 409 / 422, the edges of every rule, found defects |
| Contract | ContractFilter (every response) + ContractTest | each body matches its JSON Schema |
| Auth | AuthTest | 401 vs 403, Basic, scopes, roles, tampered and expired tokens |
| Data-driven | DataDrivenTest, BoundaryTest | the same check over CSV, JSON and Excel rows |
| Demo (excluded) | SoftAssertionsDemoTest, ContractDemoTest, SharedStateDemoTest, RetryDemoTest | deliberate failures to see on camera (-Ddemo=true) |

Not every test fits every target: enrollments, courses, auth and boundaries exist only on Campus Lab
(skipped on the Students app); the Students app's two known defects (validation and duplicate email
answer 500) are reported by KnownDefects.

## Folder tree of the final project

```
pom.xml                          Java 21, REST Assured 6.3.0, TestNG 7.12.0, surefire 3.5.6 (reads testng.xml)
testng.xml                       the whole suite (demo group excluded)
suites/smoke.xml  functional.xml  parallel-methods.xml  parallel-classes.xml
docker-compose.yml  run-local.sh  allure-report.sh  README.md
src/main/java/framework/
  config/        App, TestConfig
  http/          SpecFactory, HttpLogFilter, RateLimitFilter, ContractFilter
  clients/       ApiClient, StudentsApi, EnrollmentsApi, CoursesApi, AuthApi, SandboxApi
  models/        Student, Enrollment, Course
  data/          StudentData, DataFiles, Cleanup
  assertions/    StudentAssert, ApiMatchers
  defects/       KnownDefects
  auth/          Jwt
  listeners/     TestListener, RetryAnalyzer, RetryTransformer
  report/        AllureEnvironment
  steps/         StudentSteps
src/main/resources/
  env/           local.properties  docker.properties  campus.properties
  schemas/       student, student-list, message, students-error, problem, enrollment, enrollment-page, course, course-page
src/test/java/
  tests/         BaseTest + every *Test class above, CucumberRunnerTest (lesson 19)
  bdd/           StudentStepDefs (lesson 19)
src/test/resources/
  data/          new-students.csv  invalid-students.json  students.xlsx
  features/      students.feature (lesson 19)
  logback-test.xml  allure.properties
```

## Lesson by lesson

| Lesson | Adds |
|---|---|
| 02 skeleton | pom, testng.xml, the first test |
| 03 config | TestConfig + env files: the same tests on the jar, Docker and Campus Lab |
| 04 specs | SpecFactory, ApiClient (ThreadLocal), RateLimitFilter |
| 05 clients | StudentsApi, EnrollmentsApi |
| 06 models | Student record + builder, StudentData, Cleanup, BaseTest, sandbox reset |
| 07 assertions | AssertJ, soft assertions, StudentAssert, ApiMatchers |
| 08 groups | smoke / functional / demo, suite files |
| 09 negative | KnownDefects, NegativeTest, EnrollmentRulesTest, BoundaryTest |
| 10 contract | schemas + ContractFilter on every response |
| 11 auth | anonymous spec, AuthApi, Jwt, AuthTest |
| 12 data-driven | DataFiles (CSV, JSON, Excel), DataDrivenTest |
| 13 parallel | parallel suites, real timing, the shared-state race, a rate-limit filter fix |
| 14 listeners | TestListener, RetryAnalyzer (transient only) |
| 15 logging | Logback, HttpLogFilter (masked), MDC |
| 16 Allure | Allure TestNG, steps, attachments, environment, history and trends |
| 17 compose | docker-compose.yml + run-local.sh: one command to a green suite |
| 18 walkthrough | README tour + a new endpoint in four steps (CoursesApi) |
| 19 Cucumber | optional BDD layer on the same clients |
