# Upgrading a 2017 REST Assured project to REST Assured 6 on JDK 21

Every line below was met for real on JDK 21.0.13 + Maven 3.8.7 (20-21 Nov 2026), in this order, on the
2017 `RestAssuredExamples` project (REST Assured 3.0.0, JUnit 4.12, no `<build>` section at all).

| # | What you see | Why | Fix |
|---|---|---|---|
| 1 | `[ERROR] Source option 5 is no longer supported. Use 8 or later.` and `Target option 5 ...` | No compiler settings: Maven 3.8.7 uses maven-compiler-plugin 3.1, which defaults to Java 5. JDK 21 cannot compile for 5. | `<maven.compiler.release>21</maven.compiler.release>` + maven-compiler-plugin 3.16.0 (+ `project.build.sourceEncoding` UTF-8) |
| 2 | `BUILD SUCCESS`, but no test runs at all (no "Tests run" line) | The classes are named `*Example`, `AddingAssertions`, `VerifyingTime`; surefire only picks up `*Test`, `Test*`, `*Tests`, `*TestCase`. Same with surefire 2.12.4 (Maven's default) and 3.6.1. | Add maven-surefire-plugin 3.6.1, and either rename the classes to `*Test` or run them by name: `mvn test -Dtest='*'` |
| 3 | `cannot find symbol  symbol: method parameters(java.util.HashMap<java.lang.String,java.lang.Object>)  location: interface io.restassured.specification.RequestSpecification` (9 places) | `parameters()` was removed in REST Assured 4. | `params(...)` (same meaning: query params on GET, form params on POST), or the explicit `queryParams(...)` / `formParams(...)` |
| 4 | `io.restassured.internal.path.xml.NodeChildrenImpl is not public in io.restassured.internal.path.xml; cannot be accessed from outside package` | The code imported an INTERNAL class; it is package-private in REST Assured 6. | Use the public interface `io.restassured.path.xml.element.NodeChildren` |
| 5 | `java.net.UnknownHostException: query.yahooapis.com` (32 of 34 tests) | Not an upgrade problem: Yahoo's YQL API was shut down in 2019. | Point the tests at a live API (this course: Campus Lab). |
| 6 | `FileUploadLoadExample` passes although Zamzar answers `HTTP/1.1 401 Unauthorized` | The test has no assertion and an empty API key (`String apiKey="";`). | Add `.statusCode(...)` and read the key from an environment variable. |

Also bump the libraries: rest-assured 3.0.0 -> 6.3.0, junit 4.12 -> 4.13.2, jackson-databind 2.8.5 -> 2.23.0.

## Met on the 2017 framework project, not on this one

- **Old REST Assured on a new JDK**: `StudentApp` (REST Assured 3.3.0, Groovy 2.x) on JDK 21 turns every failed check into
  `groovy.lang.GroovyRuntimeException: could not access constructor: private java.lang.AssertionError(java.lang.String) ...
  IllegalAccessException ... (in module java.base) with modifiers "private"` instead of the real assertion message.
  Upgrading to REST Assured 6.3.0 brings back the normal `1 expectation failed.` message.
- **AspectJ**: NOT reproduced. aspectjweaver 1.9.4 as a surefire `-javaagent` still loads on JDK 21 (with REST Assured
  3.3.0 or 6.3.0, Allure 2.8.1 or 2.35.6); the only output is
  `[AppClassLoader@...] warning javax.* types are not being woven because the weaver option '-Xset:weaveJavaxPackages=true' has not been specified`.
  Use the current weaver anyway (1.9.27): 1.9.4 predates Java 21 class files.
- **Header check**: Spring Boot 3 sends `Content-Type: application/json` (no `;charset=UTF-8`): an exact-match
  check of the old header fails (`CrudTest`: `1 expectation failed.`); use `containsString("application/json")`.
