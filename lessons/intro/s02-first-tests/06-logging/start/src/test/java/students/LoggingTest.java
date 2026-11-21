// Keep the console quiet when tests pass and show the full request and response when one fails.
// Run:  mvn test -Dtest=LoggingTest   (the app at STUDENTS_BASE_URI, default http://localhost:8080)
package students;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

// --- keep
// The base URI comes from an environment variable, as in every lesson.
class LoggingTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // ---
    // log().ifValidationFails() on both sides: nothing is printed while the checks pass.
    // This test passes, so the console shows no request and no response.


    // ---
    // Our own filter on one request: it prints a single timing line, pass or fail.

    // --- keep
    // end of the class
}
