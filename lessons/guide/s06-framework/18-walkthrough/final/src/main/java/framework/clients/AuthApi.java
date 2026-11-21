// Campus Lab's ways to get a token: OAuth 2.0 client credentials (with scopes) and a student-scoped JWT.
package framework.clients;

import framework.config.TestConfig;
import io.restassured.http.ContentType;

// --- keep
// Credentials come from the environment (CAMPUS_CLIENT_ID / CAMPUS_CLIENT_SECRET), never from a file.
public class AuthApi {

    // --- keep
    // Client credentials: a form POST to /oauth/token. The scope we ask for is the scope the token gets.
    // The token endpoint needs no API key: the client id and secret ARE the credentials.
    public String clientCredentialsToken(String scope) {
        TestConfig config = TestConfig.get();
        return ApiClient.anonymous().contentType(ContentType.URLENC)
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", config.secret("CAMPUS_CLIENT_ID").orElseThrow())
                .formParam("client_secret", config.secret("CAMPUS_CLIENT_SECRET").orElseThrow())
                .formParam("scope", scope)
                .when().post("/oauth/token")
                .then().statusCode(200).extract().path("access_token");
    }

    // --- keep
    // A student token: a JWT that may only read ONE student's records (the "student" role).
    // Minted with our API key, so it uses the normal client.
    public String studentToken(int studentId) {
        return ApiClient.given().body(java.util.Map.of("studentId", studentId))
                .when().post("/api/v1/auth/student-token")
                .then().statusCode(200).extract().path("accessToken");
    }
    // --- keep
    // end of the class
}
