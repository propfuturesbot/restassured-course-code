// Small JWT helpers for negative tests: read the payload, tamper with it, or sign a token yourself (HS256).
package framework.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// --- keep
// A JWT is header.payload.signature, each part base64url. Only the server's secret can make a valid signature.
public final class Jwt {

    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();

    private Jwt() {
    }

    public static String payload(String token) {
        return new String(DEC.decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
    }

    // Same header and signature, a different payload: the signature no longer matches.
    public static String withPayload(String token, String newPayloadJson) {
        String[] parts = token.split("\\.");
        return parts[0] + "." + ENC.encodeToString(newPayloadJson.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];
    }

    // A correctly signed HS256 token with any claims. Only possible when you know the server's secret
    // (a local test server); never with a real one.
    public static String signHs256(String payloadJson, String secret) {
        try {
            String header = ENC.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            String body = ENC.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal((header + "." + body).getBytes(StandardCharsets.UTF_8));
            return header + "." + body + "." + ENC.encodeToString(signature);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
