// Two small tests to open in VS Code: run them from the Test Explorer, then debug one with a breakpoint.
// Run:  mvn test -Dtest=StudentsAppTest   (the app at STUDENTS_BASE_URI, default http://localhost:8080)
package students;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StudentsAppTest {

    // Where the app lives comes from an environment variable, so these tests run anywhere.
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // The list endpoint answers 200 and the limit parameter really limits the list.
    @Test
    @DisplayName("The student list honours the limit parameter")
    void listHonoursLimit() {
        List<Integer> ids = given().queryParam("limit", 3)
                .when().get("/list")
                .then().statusCode(200)
                .extract().path("id");
        assertEquals(3, ids.size());
    }

    // One student by id. A good line for a breakpoint: the first name is stored in a variable before the check.
    @Test
    @DisplayName("Student 1 can be read by id")
    void readStudentOne() {
        String firstName = given().pathParam("id", 1)
                .when().get("/{id}")
                .then().statusCode(200).body("id", equalTo(1))
                .extract().path("firstName");
        assertFalse(firstName.isBlank(), "first name is empty");
    }
}
