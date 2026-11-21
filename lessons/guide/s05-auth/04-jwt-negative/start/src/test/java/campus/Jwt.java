// Build JSON Web Tokens by hand (header.payload.signature), so tests can make broken ones on purpose.
// Used by JwtNegativeTest.
package campus;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

// --- keep
// A JWT is three base64url parts joined by dots. Jackson turns the header and claims maps into JSON.
public final class Jwt {

    static final ObjectMapper JSON = new ObjectMapper();
    static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private Jwt() {}

    // ---
    // Sign: base64url(header) + "." + base64url(claims), then HMAC-SHA256 over that text with the secret.
    // The signature is the third part. Change one byte of the first two parts and it no longer matches.


    // ---
    // The "alg none" attack: a header that says "no signature", the claims, and an empty third part.
    // A server that trusts the header's alg would accept anything; a safe one only allows the algorithms it uses.


    // --- keep
    // Read the claims of a real token (the middle part), and the two encoders.
    @SuppressWarnings("unchecked")
    public static Map<String, Object> claimsOf(String token) {
        try {
            return JSON.readValue(Base64.getUrlDecoder().decode(token.split("\\.")[1]), Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    static String part(Map<String, Object> json) {
        try {
            return B64.encodeToString(JSON.writeValueAsBytes(json));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    static byte[] hmac(String text, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(text.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
