// The four ways in to Campus Lab from REST Assured: no key, an API key, Basic auth and a bearer token.
// Run:  mvn test -Dtest=CampusAuthTest   (needs CAMPUS_BASE_URL, CAMPUS_API_KEY, CAMPUS_BASIC_USER, CAMPUS_BASIC_PASSWORD)
package campus;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

// --- keep
// Secrets come from environment variables only: never typed into the code, never committed.
class CampusAuthTest {

    static final String BASE = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    static RequestSpecification withKey;

    // ---
    // Without any credentials Campus Lab answers 401 with a problem+json body that says what is missing.


    // ---
    // The API key travels in the X-API-Key header. We read it from the environment, so the code holds no secret.


    // ---
    // A request spec holds what every call repeats: base URI, the key, JSON.
    // Build it once; each test then starts from spec(withKey) and writes only what is different.


    // ---
    // Basic auth: the same sandbox, a user name and a password instead of the key.
    // preemptive() sends the credentials on the first request instead of waiting for a 401 challenge.


    // ---
    // A bearer token: ask Campus Lab for a token that belongs to student 1 (the key pays for it),
    // then send it as "Authorization: Bearer ...". It opens student 1 and nobody else.

    // --- keep
    // end of the class
}
