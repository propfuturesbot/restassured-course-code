// API tests against the Students app that the workflow starts in a Docker container inside the job.
// Run:  mvn test -Dtest=StudentsInCiTest   (the app at STUDENTS_BASE_URI, default http://localhost:8080)
package students;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class StudentsInCiTest {

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    @Test
    public void theListHasTheSeededStudents() {
        given().queryParam("limit", 5)
                .when().get("/list")
                .then().statusCode(200).body("$", hasSize(5));
    }

    @Test
    public void createAStudent() {
        Map<String, Object> student = Map.of(
                "firstName", "Grace", "lastName", "Hopper",
                "email", "grace." + System.currentTimeMillis() + "@example.com",
                "programme", "Computer Science", "courses", List.of("COBOL"));
        given().contentType(ContentType.JSON).body(student)
                .when().post()
                .then().statusCode(201).body("msg", equalTo("Student added"));
    }

    @Test
    public void aMissingStudentIs404() {
        given().when().get("/9999")
                .then().statusCode(404).body("error", equalTo("Not Found!"));
    }
}
