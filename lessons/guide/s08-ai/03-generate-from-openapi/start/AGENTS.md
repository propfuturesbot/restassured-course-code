# Campus Lab API tests: conventions for coding agents

Java 21, Maven, REST Assured 6.3.0, TestNG 7.12, Hamcrest 3, Jackson, Allure 2.35 (allure-testng, allure-rest-assured).
The system under test is Campus Lab, a university API (students, courses, enrollments, ...).
The contract is `openapi.json` of Campus Lab (or the part of it in `ai/inputs/`). Use only routes, fields and
status codes that are in it, or that a real run showed. Never guess an endpoint.

## Commands

- Run everything: `mvn -B test`
- One class: `mvn -B test -Dtest=StudentsRegressionTest`
- One group: `mvn -B test -Dgroups=smoke` (pull requests) or `-Dgroups=regression` (nightly)
- Report: `mvn -B allure:report` (opens from `target/site/allure-maven-plugin/index.html`)
- After you change tests, RUN them and show the real result line (`Tests run: N, Failures: N, ...`).
  Do not say a test passes unless you ran it.

## Environment (never hard-code these)

- `CAMPUS_BASE_URL` (default `http://localhost:8000`) and `CAMPUS_API_KEY`.
- Read them only through `Campus.spec()`. Never put a key, password or token in code, a comment, a test name,
  a log line, a commit message or a prompt. If the key is missing, the tests must fail, not skip.

## How a test looks

- Package `campus`, one class per resource and level: `CoursesSmokeTest`, `CoursesRegressionTest`. Names end in `Test`.
- Every request starts with `given().spec(Campus.spec())`. The spec adds the key, the Allure filter and the
  log blacklist that hides `X-API-Key` in reports. Do not add the header yourself.
- Every `@Test` has `groups = "smoke"` or `groups = "regression"`. A test without a group never runs in CI.
- One behaviour per test, named after it: `aDuplicateEmailIsAConflict`, not `test1` or `testPost`.
- Assert what matters, not only the status: the status code, the fields you sent coming back, and for an error
  the problem type (`body("type", endsWith("/problems/validation-error"))`) and the field in `errors[0].field`.
- Hamcrest matchers in `then()`. No `assertTrue(response.statusCode() < 500)`, no try/catch around assertions.
- Test data is unique per run (`System.nanoTime()` in emails and codes) and every record a test creates is
  deleted again in the same test. No fixed emails: the second run would get 409.
- No `Thread.sleep`. No static mutable state shared between tests. Tests must pass in any order.
- Comments in plain English, two lines at most, saying why.

## Facts about the API (checked with real requests)

- Every API call needs `X-API-Key` (or Basic / Bearer). Without it: 401, `application/problem+json`,
  type `.../problems/key-required`. A wrong key: 401 `.../problems/unauthorized`.
- Errors are problem+json: `type`, `title`, `status`, `detail`, `instance`, often `errors[]` with
  `field`, `pointer`, `message`, `code`.
- POST answers 201 with the created object and a `Location` header. DELETE answers 204. A missing id: 404
  `.../problems/not-found`. Invalid body: 422 `.../problems/validation-error`. Duplicate: 409.
- Lists are `{"data": [...], "meta": {"total", "limit", "offset", "count", "nextCursor", "prevCursor"}}`.
  `limit` is 1 to 100; `limit=0` or `limit=101` answers 400.
- Other people may use the same sandbox key: assert `meta.total >= 100`, never `== 100`.
