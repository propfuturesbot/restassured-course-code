// OAuth2 authorization code + PKCE against Campus Lab, with no browser: authorize, read the code, exchange it.
// Run:  mvn test -Dtest=PkceFlowTest   (CAMPUS_BASE_URL, CAMPUS_CLIENT_ID, CAMPUS_BASIC_USER, CAMPUS_BASIC_PASSWORD)
package campus;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

// --- keep
// The redirect URI only has to be allowed (Campus Lab allows http://localhost:*). Nothing listens there:
// we never follow the redirect, we read the code out of its Location header.
public class PkceFlowTest {

    static final String CLIENT_ID = System.getenv("CAMPUS_CLIENT_ID");
    static final String REDIRECT = "http://localhost:8123/callback";

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    }

    // ---
    // In a browser, GET /oauth/authorize shows a consent page. We check it is there and asks for S256.
    // A request without PKCE is refused: Campus Lab sends the browser back with error=invalid_request.
    @Test
    public void theAuthorizeStepRequiresPkce() {
        given().queryParams(authorizeParams(Pkce.challengeFor(Pkce.newVerifier()), "s1"))
                .when().get("/oauth/authorize")
                .then().statusCode(200).body(containsString("Authorize access"));
        given().redirects().follow(false)
                .queryParam("response_type", "code").queryParam("client_id", CLIENT_ID).queryParam("redirect_uri", REDIRECT)
                .when().get("/oauth/authorize")
                .then().statusCode(302).header("Location", containsString("error=invalid_request"));
    }

    // ---
    // A test has no browser, so it does what the consent page's form does: POST the same parameters
    // with the sandbox user's name and password and decision=allow. The answer is a 302 to our redirect URI
    // carrying ?code=...&state=... . The state must come back unchanged.
    String authorize(String challenge) {
        String state = UUID.randomUUID().toString();
        Map<String, String> form = new HashMap<>(authorizeParams(challenge, state));
        form.put("username", System.getenv("CAMPUS_BASIC_USER"));
        form.put("password", System.getenv("CAMPUS_BASIC_PASSWORD"));
        form.put("decision", "allow");
        String location = given().redirects().follow(false).formParams(form)
                .when().post("/oauth/authorize")
                .then().statusCode(302).header("Location", startsWith(REDIRECT)).extract().header("Location");
        Map<String, String> query = queryOf(location);
        assertEquals(query.get("state"), state);
        return query.get("code");
    }

    // ---
    // The whole flow: new verifier, its challenge to authorize, then the code plus the VERIFIER to /oauth/token.
    // The server hashes the verifier and compares it with the challenge it kept. We get an access token
    // and a refresh token, and the access token works on the API.
    @Test
    public void codeAndVerifierGiveAnAccessToken() {
        String verifier = Pkce.newVerifier();
        String challenge = Pkce.challengeFor(verifier);
        System.out.println("code_verifier  " + verifier + " (" + verifier.length() + " chars)");
        System.out.println("code_challenge " + challenge);
        String code = authorize(challenge);
        Response tokens = exchange(code, verifier)
                .then().statusCode(200)
                .body("token_type", equalTo("Bearer")).body("expires_in", equalTo(3600))
                .body("refresh_token", startsWith("rt_"))
                .extract().response();
        given().auth().oauth2(tokens.path("access_token"))
                .when().get("/api/v1/meta")
                .then().statusCode(200).body("sandbox.authMethod", equalTo("oauth"));
    }

    // ---
    // The point of PKCE: a stolen code is useless without the verifier. A different verifier is refused,
    // and the code is then spent: Campus Lab codes are single use.
    @Test
    public void aCodeWithTheWrongVerifierIsRefused() {
        String code = authorize(Pkce.challengeFor(Pkce.newVerifier()));
        exchange(code, Pkce.newVerifier())
                .then().statusCode(400).body("error", equalTo("invalid_grant"))
                .body("error_description", containsString("PKCE verification failed"));
        exchange(code, Pkce.newVerifier())
                .then().statusCode(400).body("error_description", containsString("already used"));
    }

    // ---
    // The refresh token gives a new access token AND a new refresh token (rotation).
    // The old refresh token stops working.
    @Test
    public void refreshTokensRotate() {
        String verifier = Pkce.newVerifier();
        String oldRefresh = exchange(authorize(Pkce.challengeFor(verifier)), verifier)
                .then().statusCode(200).extract().path("refresh_token");
        String newRefresh = refresh(oldRefresh).then().statusCode(200)
                .body("access_token", notNullValue()).extract().path("refresh_token");
        assertNotEquals(newRefresh, oldRefresh);
        refresh(oldRefresh).then().statusCode(400).body("error", equalTo("invalid_grant"));
    }

    // --- keep
    // Helpers: the authorize parameters, the two token calls, and reading a query string.
    Map<String, String> authorizeParams(String challenge, String state) {
        return Map.of("response_type", "code", "client_id", CLIENT_ID, "redirect_uri", REDIRECT,
                "code_challenge", challenge, "code_challenge_method", "S256", "state", state, "scope", "students:read");
    }

    Response exchange(String code, String verifier) {
        return given().formParam("grant_type", "authorization_code").formParam("code", code)
                .formParam("redirect_uri", REDIRECT).formParam("client_id", CLIENT_ID).formParam("code_verifier", verifier)
                .when().post("/oauth/token");
    }

    Response refresh(String refreshToken) {
        return given().formParam("grant_type", "refresh_token").formParam("refresh_token", refreshToken)
                .formParam("client_id", CLIENT_ID).when().post("/oauth/token");
    }

    static Map<String, String> queryOf(String url) {
        Map<String, String> out = new HashMap<>();
        for (String pair : URI.create(url).getRawQuery().split("&")) {
            String[] kv = pair.split("=", 2);
            out.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return out;
    }
}
