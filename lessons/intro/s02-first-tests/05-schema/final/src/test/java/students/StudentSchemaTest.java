// Check a whole response at once against a JSON Schema file, instead of one field at a time.
// Run:  mvn test -Dtest=StudentSchemaTest   (the app at STUDENTS_BASE_URI, default http://localhost:8080)
package students;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

// --- keep
// The base URI comes from an environment variable, as in every lesson.
class StudentSchemaTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // ---
    // The schema lives in src/test/resources/schemas, so it is on the test classpath.
    // One line checks every field: its type, that it is present, and that no unknown field appeared.
    @Test
    void studentMatchesTheSchema() {
        given().pathParam("id", 1)
                .when().get("/{id}")
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/student-schema.json"));
    }

    // ---
    // The same check on a student we did not pick by hand: the last one in a list of ten.
    @Test
    void anotherStudentMatchesTheSchema() {
        int id = given().queryParam("limit", 10).when().get("/list").then().extract().path("id[-1]");
        given().pathParam("id", id)
                .when().get("/{id}")
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/student-schema.json"));
    }
    // --- keep
    // end of the class
}
