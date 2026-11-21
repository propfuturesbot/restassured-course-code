// Student tests whose every HTTP call lands in the Allure report, with no logging code in the tests.
// Run:  mvn test -Dtest=StudentReportTest   then   npx -y allure@3.22.1 generate target/allure-results -o target/allure-report
package students;

import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

// --- keep
// The base URI comes from an environment variable, as in every lesson.
class StudentReportTest {

    // ---
    // One line makes every request and response part of the report:
    // RestAssured.filters() adds the Allure filter to EVERY call of every test in this run.
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
        RestAssured.filters(new AllureRestAssured());
    }

    // ---
    // A normal test. In the report it shows the GET with its URL, headers and the JSON answer.
    @Test
    @DisplayName("Read one student by id")
    void readOneStudent() {
        given().pathParam("id", 1)
                .when().get("/{id}")
                .then().statusCode(200).body("id", equalTo(1));
    }

    // ---
    // Allure.step() names a part of the test; the calls made inside it are attached to that step.
    @Test
    @DisplayName("Create a student and find it in the list")
    void createAndFind() {
        String email = "linus." + System.currentTimeMillis() + "@example.com";
        Allure.step("Create the student", () ->
                given().contentType(ContentType.JSON)
                        .body(Map.of("firstName", "Linus", "lastName", "Torvalds", "email", email,
                                "programme", "Computer Science", "courses", List.of("C", "Git")))
                        .when().post()
                        .then().statusCode(201));
        Allure.step("Find it in the list", () ->
                given().when().get("/list")
                        .then().body("email", hasItem(email)));
    }

    // ---
    // This one FAILS on purpose: student 1 is not called Nobody. The report shows the failure
    // next to the exact response that caused it, so nobody has to rerun the test to see what came back.
    @Test
    @DisplayName("A failing check, to see it in the report")
    void failingCheck() {
        given().pathParam("id", 1)
                .when().get("/{id}")
                .then().body("firstName", equalTo("Nobody"));
    }
    // --- keep
    // end of the class
}
