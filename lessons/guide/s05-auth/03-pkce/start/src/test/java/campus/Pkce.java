// PKCE in plain Java: a random code_verifier and its S256 code_challenge (RFC 7636).
// Used by PkceFlowTest.
package campus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

// --- keep
// Two static methods, no state: the test keeps the verifier and sends the challenge.
public final class Pkce {

    private static final Base64.Encoder URL = Base64.getUrlEncoder().withoutPadding();

    private Pkce() {}

    // ---
    // The verifier is a secret the client invents for this one login: 32 random bytes,
    // base64url without padding, which makes 43 characters (the spec allows 43 to 128).


    // ---
    // The challenge is the SHA-256 of the verifier, base64url without padding: method "S256".
    // The challenge goes out with the authorize request; the verifier only goes to the token endpoint,
    // so someone who steals the code cannot exchange it.
