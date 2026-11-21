// Campus Lab's ways to get a token: OAuth 2.0 client credentials (with scopes) and a student-scoped JWT.
package framework.clients;

import framework.config.TestConfig;
import io.restassured.http.ContentType;

// --- keep
// Credentials come from the environment (CAMPUS_CLIENT_ID / CAMPUS_CLIENT_SECRET), never from a file.
public class AuthApi {

    // ---
    // Client credentials: a form POST to /oauth/token. The scope we ask for is the scope the token gets.
    // The token endpoint needs no API key: the client id and secret ARE the credentials.


    // ---
    // A student token: a JWT that may only read ONE student's records (the "student" role).
    // Minted with our API key, so it uses the normal client.

    // --- keep
    // end of the class
}
