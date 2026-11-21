// Auth in the framework: no key, a wrong key, Basic, scopes (read cannot write), roles (a student sees only itself),
// a tampered token and an expired one. Campus Lab only: the Students app has no authentication at all.
// Run:  mvn test -Dtest=AuthTest -Denv=campus
package tests;

import framework.auth.Jwt;
import framework.clients.ApiClient;
import framework.clients.AuthApi;
import framework.config.App;
import framework.data.StudentData;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;

// --- keep
// Every request here starts from ApiClient.anonymous(): no key, so each test decides the credentials itself.
@Test(groups = "functional")
public class AuthTest extends BaseTest {

    private static final String STUDENTS = "/api/v1/students";
    private final AuthApi auth = new AuthApi();

    @BeforeClass(alwaysRun = true)
    public void campusOnly() {
        if (config.app() != App.CAMPUS) throw new SkipException("The Students app has no authentication");
    }

    // ---
    // 401 means "who are you?": no credentials, or credentials that are wrong.
    // Campus Lab says how to authenticate in the WWW-Authenticate header.
    public void noKeyIs401() {
        ApiClient.anonymous().when().get(STUDENTS)
                .then().statusCode(401).header("WWW-Authenticate", containsString("ApiKey"))
                .body("type", endsWith("/key-required"));
    }

    public void wrongKeyIs401() {
        ApiClient.anonymous().header("X-API-Key", "clk_not-a-real-key").when().get(STUDENTS).then().statusCode(401);
    }

    // ---
    // HTTP Basic with the key's own username and password (from the environment) is accepted too.
    public void basicAuthWorks() {
        String user = config.secret("CAMPUS_BASIC_USER").orElseThrow(() -> new SkipException("no CAMPUS_BASIC_USER"));
        String password = config.secret("CAMPUS_BASIC_PASSWORD").orElseThrow();
        ApiClient.anonymous().auth().preemptive().basic(user, password).when().get(STUDENTS + "/1")
                .then().statusCode(200);
    }

    // ---
    // 403 means "I know who you are, and you may not": a token with only students:read can read but not write.
    public void readScopeCannotWrite() {
        String token = auth.clientCredentialsToken("students:read");
        ApiClient.anonymous().auth().oauth2(token).when().get(STUDENTS + "/1").then().statusCode(200);
        ApiClient.anonymous().auth().oauth2(token).body(StudentData.randomStudent()).when().post(STUDENTS)
                .then().statusCode(403).body("type", endsWith("/insufficient-scope"))
                .header("WWW-Authenticate", containsString("insufficient_scope"));
    }

    // ---
    // Roles: a student token may read its own record and nobody else's.
    public void studentSeesOnlyItself() {
        String token = auth.studentToken(4);
        ApiClient.anonymous().auth().oauth2(token).when().get(STUDENTS + "/4").then().statusCode(200);
        ApiClient.anonymous().auth().oauth2(token).when().get(STUDENTS + "/5").then().statusCode(403);
    }

    // ---
    // Tampering: give the read-only token a write scope in its payload. The signature no longer matches -> 401.
    public void tamperedTokenIs401() {
        String token = auth.clientCredentialsToken("students:read");
        String forged = Jwt.withPayload(token, Jwt.payload(token).replace("students:read", "students:read students:write"));
        ApiClient.anonymous().auth().oauth2(forged).when().get(STUDENTS + "/1").then().statusCode(401);
    }

    // ---
    // Expired: a token whose exp is in the past. Signing one needs the server's secret, which only a LOCAL
    // Campus Lab has (CAMPUS_JWT_SECRET, the value in its docker-compose.yml). Elsewhere the test skips.
    // Every other claim is right (issuer, audience, this key's sandbox), so expiry is the only reason to refuse.
    public void expiredTokenIs401() {
        String secret = config.secret("CAMPUS_JWT_SECRET")
                .orElseThrow(() -> new SkipException("needs CAMPUS_JWT_SECRET (a local Campus Lab)"));
        String sandbox = ApiClient.given().when().get("/api/v1/meta").then().statusCode(200).extract().path("sandbox.id");
        long now = System.currentTimeMillis() / 1000;
        String claims = "{\"iss\":\"campus-lab\",\"aud\":\"campus-lab-api\",\"sub\":\"client\",\"sid\":\"" + sandbox
                + "\",\"scope\":\"students:read\",\"typ\":\"access\",\"iat\":" + (now - 7200) + ",\"exp\":" + (now - 3600) + "}";
        ApiClient.anonymous().auth().oauth2(Jwt.signHs256(claims, secret)).when().get(STUDENTS + "/1")
                .then().statusCode(401).header("WWW-Authenticate", containsString("expired"));
    }
    // --- keep
    // end of the class
}
