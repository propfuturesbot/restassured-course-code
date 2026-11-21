// A check that fails on purpose, to see what log().ifValidationFails() prints when it does.
// Not named *Test, so "mvn test" skips it. Run it on purpose:  mvn test -Dtest=FailingLogCheck   (it FAILS)
package students;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

// --- keep
// The base URI comes from an environment variable, as in every lesson.
class FailingLogCheck {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // ---
    // Switch it on once for every test instead of on each request.
    // Student 9999 does not exist, so the 200 we expect is a 404: now the request and the response are printed.
    @Test
    void loggedBecauseItFails() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        given().pathParam("id", 9999)
                .when().get("/{id}")
                .then().statusCode(200);
    }
    // --- keep
    // end of the class
}
