// Tests for PATCH /api/v1/students/{id}.
package campus;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertTrue;

public class StudentsPatchTest {

    static int studentId;

    @BeforeClass
    public void createStudent() {
        studentId = given()
                .baseUri(System.getenv("CAMPUS_BASE_URL"))
                .header("X-API-Key", System.getenv("CAMPUS_API_KEY"))
                .contentType(ContentType.JSON)
                .body(Map.of("firstName", "Test", "lastName", "User", "email", "test.user@example.edu",
                        "programme", "Computer Science"))
                .when().post("/api/v1/students")
                .then().extract().path("id");
    }

    @Test(groups = "regression")
    public void patchProgramme() {
        given().spec(Campus.spec()).contentType(ContentType.JSON).body(Map.of("programme", "Mathematics"))
                .when().patch("/api/v1/students/" + studentId)
                .then().statusCode(200);
    }

    @Test
    public void patchYear() throws InterruptedException {
        // give the API time to save the previous change
        Thread.sleep(2000);
        Response response = given().spec(Campus.spec()).contentType(ContentType.JSON).body(Map.of("year", 3))
                .when().patch("/api/v1/students/" + studentId);
        assertTrue(response.statusCode() < 500);
    }

    @Test(groups = "regression")
    public void patchReadOnlyRoleIsRejected() {
        given().spec(Campus.spec()).contentType(ContentType.JSON).body(Map.of("role", "admin"))
                .when().patch("/api/v1/students/" + studentId)
                .then().statusCode(422);
    }
}
