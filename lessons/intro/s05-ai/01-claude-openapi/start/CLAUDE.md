# Conventions for tests in this project (Claude Code reads this file first)

- Java 21, REST Assured 6.3.0, JUnit 6 (org.junit.jupiter), Hamcrest. No other libraries.
- Tests live in src/test/java/campus and are named *Test.
- The API is Campus Lab. Base URL from the environment variable CAMPUS_BASE_URL (default http://localhost:8000).
  The API key comes from CAMPUS_API_KEY and is sent as the X-API-Key header. Never write a key into a file.
- Build one RequestSpecification in @BeforeAll (base URI, X-API-Key, Accept JSON) and start every call with spec(...).
- The spec is the source of truth: read openapi.json for paths, fields, status codes. Never guess an endpoint.
- Every test checks the status code AND the content that proves the behaviour (fields, values, sizes, headers).
  A test that only checks the status code is not finished.
- Errors are RFC 9457 problem+json: check Content-Type application/problem+json, status, type and the field errors.
- Tests that create data use a unique email (System.currentTimeMillis()) so they can run again and again.
- No Thread.sleep, no test order dependencies, no System.out debugging left in.
- Run with: mvn -B test -Dtest=<ClassName>
