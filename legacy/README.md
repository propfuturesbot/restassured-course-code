# legacy/ — the old course downloads on REST Assured 6.3.0 + JDK 21

Checked for real on 21 Nov 2026 on the build box (JDK 21.0.13, Maven 3.8.7), against the box targets
(Students app :8080, BestBuy :3030). Command: `mvn -B test` in each folder (also `-Dtest='*'` where no class is
named `*Test`, because surefire then runs nothing).

The poms were upgraded first; five projects then did not compile on REST Assured 6. The minimal code changes in
the table "Code changes REST Assured 6 forces" are now APPLIED (21 Nov 2026) and all 11 projects compile. Nothing
else in the code was changed: dead APIs, hard-coded ids and on-purpose failures are left as the course had them.

## Status per project

| Project | mvn -B test today | Notes |
|---|---|---|
| BestBuy-jsonpath-examples | BUILD SUCCESS, 0 tests run | No class is named `*Test`. `mvn -B test -Dtest='*'`: Tests run: 16, Failures: 0 (JsonPathJaywayExamples 8, JsonPathJsonSlurperExamples 8) against BestBuy :3030 |
| JsonPath-Examples-intro | BUILD SUCCESS, 0 tests run | `-Dtest='*'`: Tests run: 8, Failures: 0 (JsonPathJaywayExamples) |
| RestAssuredExamples | BUILD SUCCESS, 0 tests run (no `*Test` class) | Was: COMPILATION ERROR (9 x `parameters(...)`, 2 x `NodeChildrenImpl is not public`), fixed. `-Dtest='*'`: Tests run: 34, Errors: 32, all `java.net.UnknownHostException: query.yahooapis.com` (Yahoo YQL is dead); FileDownLoadExample passes, FileUploadLoadExample passes without an assertion (Zamzar 401). Upgraded live in lessons/guide/s01-env/04-upgrade-2017 |
| StudentApp-framework | Tests run: 23, Failures: 2 | Both in CrudTest: `Expected header "Content-Type" was not "application/json;charset=UTF-8", was "application/json"` (Spring Boot 3 jar). Fix: `containsString("application/json")` |
| StudentAppBasics | Tests run: 8, Failures: 2 | Data, not code: on a fresh Students app all 8 pass (see lessons/intro/s02-first-tests/01-upgrade). On the shared app: CreateStudentStringPayloadTest `Expected status code <201> but was <500>` (its fixed email test5@gmail.com already exists) and UpdateStudentPojoPayloadTest `<200> but was <500>` (PUT /student/101, a hard-coded id) |
| allure-examples | Tests run: 8, Failures: 1 | `MyCalculatorTest.tc4: Expected: <6.0> but: was <5.0>`: 20/4 checked against 6, a failure the original shows on purpose in the report |
| csrf-form-auth-ra6 | Tests run: 1, Failures: 1 on the box's :8080 | The box's :8080 is the plain Students app. Against studentSpringSecurity_CSRF.war (apps/, Java 17) on :8080: Tests run: 1, Failures: 0, BUILD SUCCESS |
| paypalexamples | BUILD SUCCESS, 0 tests run (no `*Test` class) | Was: COMPILATION ERROR `BaseClass.java:[30,33] ... method parameters(java.lang.String,java.lang.String)`, fixed. `-Dtest='*'`: Tests run: 3, Errors: 3 `IllegalArgumentException: accessToken cannot be null` (no PayPal sandbox credentials, so no token) |
| rest-assured-examples-p1 | BUILD SUCCESS, 0 tests run (no `*Test` class) | Was: COMPILATION ERROR `SearchXMLPathExamples.java:[7,40]` / `[88,9] NodeChildrenImpl is not public`, fixed. `-Dtest='*'`: Tests run: 31, Failures: 10, Errors: 11. FilterExamples and RequestSpecificationExample pass (Students app); the rest call api.walmartlabs.com, which no longer serves that API (`ClassCastException: ... NodeChildrenImpl cannot be cast to class java.util.List`, `Expected: <10>`, `a collection containing "Refurbished Apple iPod nano 16GB, Blue"`): a dead target, not code |
| restassured-examples | BUILD SUCCESS, 0 tests run (no `*Test` class) | Was: COMPILATION ERROR `FromAuthWithCsrf.java:[22,33] ... withCsrfFieldName(java.lang.String)`, fixed. `-Dtest='*'`: Tests run: 13, Failures: 3, Errors: 4. HolidayWsExample (SOAP) passes. FormAuthExample / FromAuthWithCsrf / ProxyExample: `Connection refused` (they need the form-login wars on :8085 and a proxy, not running on the box); JsonAssertExample `Expected 100 values but got 108` (shared app has more students), SoftAssertionsExample `Expected: Vernonw` (a typo the original fails on purpose); JsoupExamples 1 error |
| students-application-WithLogging | Tests run: 6, Failures: 1 (shared app) | Was: COMPILATION ERROR `package com.jayway.restassured does not exist` (8 files), fixed. On the shared Students app StudentPostTest `Expected status code <201> but was <500>` (its fixed email exists after an earlier run) and with `-Dtest='*'` StudentsPut `<200> but was <500>` (hard-coded id); the logging examples (9 tests) pass. In a scratch copy on a cleaner app: Tests run: 6, Failures: 0 |

## Code changes REST Assured 6 forces (applied in this folder; each project then compiles)

| Old code | Where | New code |
|---|---|---|
| `given().parameters(map)` / `.parameters("grant_type","client_credentials")` | RestAssuredExamples (10 calls: AddingAssertions, RootPathExamples, RootPathExample2), paypalexamples BaseClass | `.params(map)` (same meaning), or explicit `.queryParams(...)` / `.formParam(...)` (paypal: `formParam`, it is a token POST) |
| `import io.restassured.internal.path.xml.NodeChildrenImpl;` | RestAssuredExamples CurrencyExchangeXMLPathExample, rest-assured-examples-p1 SearchXMLPathExamples | `io.restassured.path.xml.element.NodeChildren` (the public interface; `.size()` works the same) |
| `new FormAuthConfig("/login","uname","pwd").withCsrfFieldName("_csrf")` | restassured-examples FromAuthWithCsrf | `given().csrf("/").auth().form("user","user", new FormAuthConfig("/login","uname","pwd"))` |
| `com.jayway.restassured.*` | students-application-WithLogging (8 files) | `io.restassured.*` |
| exact header `application/json;charset=UTF-8` | StudentApp-framework CrudTest | `containsString("application/json")` (not REST Assured: the Spring Boot 3 app sends no charset) |

## Other things met on the way

- JUnit 5 projects: Maven 3.8.7's default surefire 2.12.4 runs NOTHING (every class "Tests run: 0", BUILD SUCCESS):
  surefire 3.x is required (the upgraded poms have 3.6.1).
- Classes named `*Example` / `*Examples` are never run by `mvn test`; use `-Dtest='*'` or rename them.
- No pom: Maven 3.8.7 compiles for Java 5: `Source option 5 is no longer supported. Use 8 or later.`
- Old REST Assured 3.x on JDK 21: a failing check surfaces as `groovy.lang.GroovyRuntimeException: could not access
  constructor: private java.lang.AssertionError(java.lang.String)` instead of the assertion text (seen on the original
  StudentApp, REST Assured 3.3.0). REST Assured 6 shows the normal `1 expectation failed.`
- AspectJ: aspectjweaver 1.9.4 as a `-javaagent` still loads on JDK 21; only the warning
  `javax.* types are not being woven because the weaver option '-Xset:weaveJavaxPackages=true' has not been specified`. No failure reproduced.
- allure-junit5 is now `allure-jupiter` (Maven warns "has been relocated"), and on JUnit 6 it needs the junit-bom
  (`OutputDirectoryCreator not available; probably due to unaligned versions...` otherwise).
