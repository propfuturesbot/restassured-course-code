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


    // ---
    // HTTP Basic with the key's own username and password (from the environment) is accepted too.


    // ---
    // 403 means "I know who you are, and you may not": a token with only students:read can read but not write.


    // ---
    // Roles: a student token may read its own record and nobody else's.


    // ---
    // Tampering: give the read-only token a write scope in its payload. The signature no longer matches -> 401.


    // ---
    // Expired: a token whose exp is in the past. Signing one needs the server's secret, which only a LOCAL
    // Campus Lab has (CAMPUS_JWT_SECRET, the value in its docker-compose.yml). Elsewhere the test skips.
    // Every other claim is right (issuer, audience, this key's sandbox), so expiry is the only reason to refuse.

    // --- keep
    // end of the class
}
