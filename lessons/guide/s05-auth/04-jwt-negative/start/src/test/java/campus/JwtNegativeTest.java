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


    // ---
    // Tampered: take the real token's claims, give ourselves the write scope, and keep the old signature.
    // The signature covers the claims, so the server refuses it.


    // ---
    // alg none: the real claims, no signature at all. Campus Lab only allows HS256, so it refuses the header.


    // ---
    // The control: signed with the lab's key and correct claims, our own token WORKS.
    // Only with this green can we trust that each red test below fails for the one claim we broke.


    // ---
    // Expired: exp one minute in the past. Campus Lab says so in the detail and in WWW-Authenticate.


    // ---
    // Wrong audience and wrong issuer: a token made for another API, or by another server, is refused.


    // ---
    // Right claims, wrong key: someone who does not know the secret cannot make a token.


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
