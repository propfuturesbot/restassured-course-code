// Negative tests for Campus Lab's bearer tokens: tampered, alg none, wrong key, expired, wrong audience and issuer.
// Run:  mvn test -Dtest=JwtNegativeTest
//       (CAMPUS_JWT_SECRET: the LOCAL lab's signing key; the claim tests are skipped without it)
package campus;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

// --- keep
// realToken: a genuine token from the server. sandbox: the id the claims must carry (the "sid" claim).
public class JwtNegativeTest {

    static final String SECRET = System.getenv("CAMPUS_JWT_SECRET");
    String realToken;
    String sandbox;

    // ---
    // Get one real token with the client credentials grant, and our sandbox id from /api/v1/meta.
    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
        realToken = given().auth().preemptive().basic(System.getenv("CAMPUS_CLIENT_ID"), System.getenv("CAMPUS_CLIENT_SECRET"))
                .formParam("grant_type", "client_credentials").formParam("scope", "students:read")
                .when().post("/oauth/token").then().statusCode(200).extract().path("access_token");
        sandbox = given().header("X-API-Key", System.getenv("CAMPUS_API_KEY"))
                .when().get("/api/v1/meta").then().extract().path("sandbox.id");
        System.out.println("real token claims: " + Jwt.claimsOf(realToken));
    }

    // ---
    // Tampered: take the real token's claims, give ourselves the write scope, and keep the old signature.
    // The signature covers the claims, so the server refuses it.
    @Test
    public void aTamperedTokenIsRefused() {
        Map<String, Object> claims = new HashMap<>(Jwt.claimsOf(realToken));
        claims.put("scope", "students:read students:write");
        String[] parts = realToken.split("\\.");
        String tampered = parts[0] + "." + Jwt.part(claims) + "." + parts[2];
        call(tampered).statusCode(401).body("detail", containsString("Signature verification failed"));
    }

    // ---
    // alg none: the real claims, no signature at all. Campus Lab only allows HS256, so it refuses the header.
    @Test
    public void anUnsignedAlgNoneTokenIsRefused() {
        call(Jwt.unsigned(Jwt.claimsOf(realToken)))
                .statusCode(401).body("detail", containsString("alg value is not allowed"));
    }

    // ---
    // The control: signed with the lab's key and correct claims, our own token WORKS.
    // Only with this green can we trust that each red test below fails for the one claim we broke.
    @Test
    public void aCorrectSelfMadeTokenIsAccepted() {
        call(sign(validClaims())).statusCode(200).body("sandbox.id", equalTo(sandbox));
    }

    // ---
    // Expired: exp one minute in the past. Campus Lab says so in the detail and in WWW-Authenticate.
    @Test
    public void anExpiredTokenIsRefused() {
        Map<String, Object> claims = validClaims();
        claims.put("exp", Instant.now().minusSeconds(60).getEpochSecond());
        call(sign(claims)).statusCode(401).body("detail", equalTo("The access token has expired."))
                .header("WWW-Authenticate", containsString("error_description=\"expired\""));
    }

    // ---
    // Wrong audience and wrong issuer: a token made for another API, or by another server, is refused.
    @Test
    public void aTokenForAnotherAudienceOrIssuerIsRefused() {
        Map<String, Object> claims = validClaims();
        claims.put("aud", "some-other-api");
        call(sign(claims)).statusCode(401).body("detail", containsString("Audience doesn't match"));
        claims = validClaims();
        claims.put("iss", "evil-issuer");
        call(sign(claims)).statusCode(401).body("detail", containsString("Invalid issuer"));
    }

    // ---
    // Right claims, wrong key: someone who does not know the secret cannot make a token.
    @Test
    public void aTokenSignedWithAnotherKeyIsRefused() {
        call(Jwt.hs256(validClaims(), "not-the-server-secret-0123456789abcdef"))
                .statusCode(401).body("detail", containsString("Signature verification failed"));
    }

    // --- keep
    // Helpers: the claims Campus Lab requires, signing with the lab key (skip without it), one authenticated call.
    Map<String, Object> validClaims() {
        long now = Instant.now().getEpochSecond();
        return new HashMap<>(Map.of("iss", "campus-lab", "aud", "campus-lab-api", "sub", "client:test", "sid", sandbox,
                "scope", "students:read", "typ", "access", "iat", now, "exp", now + 300));
    }

    String sign(Map<String, Object> claims) {
        if (SECRET == null || SECRET.isBlank()) {
            throw new SkipException("CAMPUS_JWT_SECRET not set: only the tests that need no key run");
        }
        return Jwt.hs256(claims, SECRET);
    }

    ValidatableResponse call(String token) {
        return given().auth().oauth2(token).when().get("/api/v1/meta").then().log().ifValidationFails();
    }
}
