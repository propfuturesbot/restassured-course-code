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
    @Test
    void noKeyIsRejected() {
        given().baseUri(BASE)
                .when().get("/api/v1/students")
                .then().statusCode(401)
                .contentType("application/problem+json")
                .body("type", endsWith("/problems/key-required"))
                .body("title", equalTo("Unauthorized"));
    }

    // ---
    // The API key travels in the X-API-Key header. We read it from the environment, so the code holds no secret.
    @Test
    void apiKeyFromTheEnvironment() {
        String key = System.getenv("CAMPUS_API_KEY");
        given().baseUri(BASE).header("X-API-Key", key)
                .queryParam("limit", 2)
                .when().get("/api/v1/students")
                .then().statusCode(200)
                .body("data", hasSize(2))
                .body("meta.limit", equalTo(2));
    }

    // ---
    // A request spec holds what every call repeats: base URI, the key, JSON.
    // Build it once; each test then starts from spec(withKey) and writes only what is different.
    @BeforeAll
    static void buildSpec() {
        withKey = new RequestSpecBuilder()
                .setBaseUri(BASE)
                .addHeader("X-API-Key", System.getenv("CAMPUS_API_KEY"))
                .setAccept(ContentType.JSON)
                .build();
    }

    @Test
    void requestSpecCarriesTheKey() {
        given().spec(withKey)
                .when().get("/api/v1/meta")
                .then().statusCode(200)
                .body("sandbox.authMethod", equalTo("api_key"));
    }

    // ---
    // Basic auth: the same sandbox, a user name and a password instead of the key.
    // preemptive() sends the credentials on the first request instead of waiting for a 401 challenge.
    @Test
    void basicAuth() {
        given().baseUri(BASE)
                .auth().preemptive().basic(System.getenv("CAMPUS_BASIC_USER"), System.getenv("CAMPUS_BASIC_PASSWORD"))
                .when().get("/api/v1/meta")
                .then().statusCode(200)
                .body("sandbox.authMethod", equalTo("basic"));
    }

    // ---
    // A bearer token: ask Campus Lab for a token that belongs to student 1 (the key pays for it),
    // then send it as "Authorization: Bearer ...". It opens student 1 and nobody else.
    @Test
    void bearerTokenForOneStudent() {
        String token = given().spec(withKey).contentType(ContentType.JSON).body(Map.of("studentId", 1))
                .when().post("/api/v1/auth/student-token")
                .then().statusCode(200).body("tokenType", equalTo("Bearer"))
                .extract().path("accessToken");

        given().baseUri(BASE).auth().oauth2(token)
                .when().get("/api/v1/students/1")
                .then().statusCode(200).body("id", equalTo(1));

        given().baseUri(BASE).auth().oauth2(token)
                .when().get("/api/v1/students/2")
                .then().statusCode(403)
                .body("detail", equalTo("A student token can only access its own student record."));
    }
    // --- keep
    // end of the class
}
