// Verifies Campus Lab's webhook signature: X-CampusLab-Signature: t=<unix>,v1=<hex HMAC-SHA256(secret, "<t>.<body>")>.
// Used by WebhookTest. This is the code a real receiver runs before it trusts a delivery.
package campus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// --- keep
// A small utility class: one public method, verify().
public final class WebhookSignature {

    private WebhookSignature() {}

    // ---
    // Split the header into its parts: t is when the sender signed, v1 is the signature in hex.
    // Then compute the HMAC ourselves over "<t>.<raw body>" with the shared secret.
    // The RAW body: re-serialising the JSON would change spaces or order and break the signature.
    public static boolean verify(String secret, String rawBody, String header, long toleranceSeconds) {
        Map<String, String> parts = Stream.of(header.split(","))
                .map(p -> p.split("=", 2)).collect(Collectors.toMap(p -> p[0], p -> p[1]));
        long t = Long.parseLong(parts.get("t"));
        String expected = HexFormat.of().formatHex(hmac(secret, t + "." + rawBody));

        // ---
        // Two checks. Compare in constant time (MessageDigest.isEqual), so an attacker cannot guess the
        // signature byte by byte from timing. And refuse old timestamps, so a captured delivery cannot be replayed.
        boolean signatureOk = MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                parts.getOrDefault("v1", "").getBytes(StandardCharsets.US_ASCII));
        boolean fresh = Math.abs(Instant.now().getEpochSecond() - t) <= toleranceSeconds;
        return signatureOk && fresh;
    }

    // --- keep
    // HMAC-SHA256 with the JDK's Mac class.
    static byte[] hmac(String secret, String text) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
