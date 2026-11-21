// Form login with a CSRF token on the Spring Security student app (the Java 17 war), by hand and with csrf().
// Run:  mvn test -Dtest=StudentAppCsrfTest   (the war at CSRF_APP_BASE_URI, default http://localhost:8111)
package campus;

import io.restassured.authentication.FormAuthConfig;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

// --- keep
// The war is the course's Spring Security app with CSRF protection on.
// Start it with:  java -jar studentSpringSecurity_CSRF.war --server.port=8111   (user / user)
// Its login form is on "/", posts to /login with the fields uname and pwd, and carries a hidden _csrf field.
public class StudentAppCsrfTest {

    static final String APP = System.getenv().getOrDefault("CSRF_APP_BASE_URI", "http://localhost:8111");

    // ---
    // Without the token, Spring Security refuses the login: 403, even with the right password.


    // ---
    // By hand: GET the form page, read the hidden _csrf value with htmlPath, and keep the session cookie.
    // Then POST the form with the token and the same cookie. Success is a 302 to the student list.


    // ---
    // The REST Assured way: csrf(page) makes the filter GET that page first, find the hidden _csrf input,
    // and add it to our POST as a form parameter, with the page's cookie. It is skipped for GET and HEAD.
    // Give csrf() the FULL URL: it fetches the page with a fresh given(), which does not see our baseUri
    // (with "/" it asked RestAssured.baseURI, localhost:8080). The SessionFilter keeps the new session id.


    // ---
    // In REST Assured 6 FormAuthConfig has no CSRF options any more (csrf() replaced them).
    // withAdditionalField("_csrf") still works: form auth reads the login form it is sent to,
    // copies the hidden _csrf value into the login POST, and signs in before our GET.

    // --- keep
    // end of the class
}
