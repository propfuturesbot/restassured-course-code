// OAuth 2.0 client credentials: trade a client id and secret for an access token, then reuse it in a spec.
// Run:  mvn test -Dtest=ClientCredentialsTest   (needs CAMPUS_BASE_URL, CAMPUS_CLIENT_ID, CAMPUS_CLIENT_SECRET)
package campus;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.oauth2;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

// --- keep
// The client id and secret come from environment variables, never from the code.
class ClientCredentialsTest {

    static final String BASE = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    static final String CLIENT_ID = System.getenv("CAMPUS_CLIENT_ID");
    static final String CLIENT_SECRET = System.getenv("CAMPUS_CLIENT_SECRET");
    static RequestSpecification readOnly;

    // ---
    // The token request is a FORM post to /oauth/token. The client proves who it is with Basic auth,
    // grant_type says which OAuth flow this is, scope says what the token may do.
    static String token(String scope) {
        return given().baseUri(BASE)
                .auth().preemptive().basic(CLIENT_ID, CLIENT_SECRET)
                .formParam("grant_type", "client_credentials")
                .formParam("scope", scope)
                .when().post("/oauth/token")
                .then().statusCode(200)
                .body("token_type", equalTo("Bearer"))
                .body("expires_in", greaterThan(0))
                .body("scope", equalTo(scope))
                .extract().path("access_token");
    }

    // ---
    // Get the token ONCE, before every test, and put it in a request spec.
    // setAuth(oauth2(token)) adds "Authorization: Bearer <token>" to every request built from the spec.
    @BeforeAll
    static void getTokenOnce() {
        readOnly = new RequestSpecBuilder()
                .setBaseUri(BASE)
                .setAuth(oauth2(token("students:read")))
                .setAccept(ContentType.JSON)
                .build();
    }

    // ---
    // The spec opens the API: Campus Lab sees an OAuth client with only the read scope.
    @Test
    void tokenReadsStudents() {
        given().spec(readOnly).queryParam("limit", 3)
                .when().get("/api/v1/students")
                .then().statusCode(200).body("data.size()", equalTo(3));
        given().spec(readOnly)
                .when().get("/api/v1/meta")
                .then().body("sandbox.authMethod", equalTo("oauth"))
                .body("sandbox.scopes", equalTo(List.of("students:read")));
    }

    // ---
    // A read-only token cannot write: 403, and the problem says which scope is missing.
    @Test
    void readTokenCannotWrite() {
        given().spec(readOnly).contentType(ContentType.JSON)
                .body(Map.of("firstName", "Ada", "lastName", "Lovelace",
                        "email", "ada." + System.currentTimeMillis() + "@example.edu", "programme", "Computer Science"))
                .when().post("/api/v1/students")
                .then().statusCode(403)
                .body("type", containsString("insufficient-scope"))
                .header("WWW-Authenticate", containsString("insufficient_scope"));
    }

    // ---
    // A wrong secret never gets a token: 401 with the OAuth error code invalid_client.
    @Test
    void wrongSecretIsRejected() {
        given().baseUri(BASE)
                .auth().preemptive().basic(CLIENT_ID, "not-the-secret")
                .formParam("grant_type", "client_credentials")
                .when().post("/oauth/token")
                .then().statusCode(401)
                .body("error", equalTo("invalid_client"));
    }
    // --- keep
    // end of the class
}
