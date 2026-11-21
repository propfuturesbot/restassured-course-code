// Unique, realistic test students from Datafaker, sent to the Students app.
// Run:  mvn test -Dtest=FakeStudentTest   (the app at STUDENTS_BASE_URI, default http://localhost:8080)
package students;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

// --- keep
// The base URI comes from an environment variable, as in every lesson.
class FakeStudentTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // ---
    // One Faker builds every value. Names and programmes look real, so the data reads well in reports.
    // The email gets a random UUID part: the app rejects a duplicate email, so every run must use new ones.
    // Names like O'Connell carry an apostrophe, so we keep only letters for the email.


    // ---
    // Create three different students and prove each one is really in the app afterwards.


    // ---
    // A seed makes Faker repeatable: the same seed gives the same "random" student every time.
    // Handy when a failing test must be replayed with exactly the same data.

    // --- keep
    // end of the class
}
