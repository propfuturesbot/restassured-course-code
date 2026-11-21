// Campus Lab with a bearer token: the filter fetches it once, reuses it, and refreshes it when it expires.
// Run:  mvn test -Dtest=BearerTokenFilterTest   (CAMPUS_BASE_URL, CAMPUS_CLIENT_ID, CAMPUS_CLIENT_SECRET)
package campus;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

// --- keep
// The tests share one filter, so they run in order (priority) and see each other's tokens.
public class BearerTokenFilterTest {

    static final String BASE = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    final AtomicReference<Instant> now = new AtomicReference<>(Instant.now());
    BearerTokenFilter auth;
    RequestSpecification campus;

    // ---
    // One filter for the whole class, built from the client id and secret that came with our key.
    // The spec carries the filter, so every request that uses the spec is authenticated, with no header in the test.


    // ---
    // Two calls, one token. /api/v1/meta tells us how the server saw us: authMethod is "oauth".


    // ---
    // Move the clock 61 minutes on: the cached token has expired, so the next call fetches a new one.


    // ---
    // The server refuses a token we still think is good (here: a token it never issued).
    // The filter sees the 401, fetches a fresh token and retries, so the test still gets 200.


    // --- keep
    // A token for students:read only: writing is refused with 403 and an insufficient-scope problem.
    @Test(priority = 4)
    public void theTokenCarriesOnlyItsScope() {
        given().spec(campus).contentType("application/merge-patch+json").body("{\"year\": 2}")
                .when().patch("/api/v1/students/3")
                .then().statusCode(403).body("type", org.hamcrest.Matchers.endsWith("insufficient-scope"));
    }
}
