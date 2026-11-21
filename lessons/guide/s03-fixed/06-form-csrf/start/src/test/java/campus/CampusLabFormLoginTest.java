// The same form login with CSRF on Campus Lab: a session cookie instead of an API key, and CSRF on every write.
// Run:  mvn test -Dtest=CampusLabFormLoginTest   (Campus Lab at CAMPUS_BASE_URL; the key's Basic user and password
//       in CAMPUS_BASIC_USER / CAMPUS_BASIC_PASSWORD, from POST /api/keys)
package campus;

import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

// --- keep
// Campus Lab's GET /login sets a JSESSIONID cookie and renders a form with a hidden _csrf field.
// POST /login takes username, password and _csrf (form-encoded). CSRF is always on.
// No X-API-Key here: the session cookie is what signs us in.
public class CampusLabFormLoginTest {

    static final String USER = System.getenv().getOrDefault("CAMPUS_BASIC_USER", "");
    static final String PASSWORD = System.getenv().getOrDefault("CAMPUS_BASIC_PASSWORD", "");
    static SessionFilter session = new SessionFilter();
    static String csrfToken;

    // --- keep
    // The Basic credentials of the key come from environment variables, never from the code.
    @BeforeClass
    public void checkCredentials() {
        if (USER.isBlank() || PASSWORD.isBlank()) {
            throw new IllegalStateException("Set CAMPUS_BASIC_USER and CAMPUS_BASIC_PASSWORD (from POST /api/keys)");
        }
    }

    // ---
    // A login POST without the token: Campus Lab answers 403 with a problem+json whose type ends in /csrf.


    // ---
    // csrf(<full URL of /login>) gets the form, its token and its cookie before our POST. The SessionFilter keeps the
    // NEW JSESSIONID that a successful login sets. The portal's JSON list then answers with the session
    // alone, and hands us the session's CSRF token in the X-CSRF-TOKEN header.


    // ---
    // Reads work with the cookie alone, but a write signed in by cookie needs the token too.
    // Without the X-CSRF-TOKEN header the POST is refused with 403; with it the student is created.

    // --- keep
    // end of the class
}
