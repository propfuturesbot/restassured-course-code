// The full checks (group "regression"), run every night: create, conflict, validation, delete.
// Every student a test creates is deleted again, so nightly runs do not fill the sandbox quota.
// Run:  mvn test -Dgroups=regression
package campus;

import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

public class StudentsRegressionTest {

    private static Map<String, Object> newStudent(String email) {
        return Map.of("firstName", "Ada", "lastName", "Lovelace", "email", email,
                "programme", "Computer Science", "year", 2);
    }

    private static String uniqueEmail() {
        return "ada." + System.nanoTime() + "@example.edu";
    }

    @Test(groups = "regression")
    public void createReadDelete() {
        int id = given().spec(Campus.spec()).contentType(ContentType.JSON).body(newStudent(uniqueEmail()))
                .when().post("/api/v1/students")
                .then().statusCode(201).body("programmeCode", equalTo("CS"))
                .extract().path("id");
        given().spec(Campus.spec()).when().delete("/api/v1/students/" + id).then().statusCode(204);
        given().spec(Campus.spec()).when().get("/api/v1/students/" + id)
                .then().statusCode(404).body("type", endsWith("/problems/not-found"));
    }

    @Test(groups = "regression")
    public void aDuplicateEmailIsAConflict() {
        String email = uniqueEmail();
        int id = given().spec(Campus.spec()).contentType(ContentType.JSON).body(newStudent(email))
                .when().post("/api/v1/students").then().statusCode(201).extract().path("id");
        given().spec(Campus.spec()).contentType(ContentType.JSON).body(newStudent(email))
                .when().post("/api/v1/students")
                .then().statusCode(409).body("type", endsWith("/problems/duplicate-email"));
        given().spec(Campus.spec()).when().delete("/api/v1/students/" + id).then().statusCode(204);
    }

    @Test(groups = "regression")
    public void yearSevenIsRejected() {
        Map<String, Object> body = new HashMap<>(newStudent(uniqueEmail()));
        body.put("year", 7);
        given().spec(Campus.spec()).contentType(ContentType.JSON).body(body)
                .when().post("/api/v1/students")
                .then().statusCode(422)
                .body("type", endsWith("/problems/validation-error"))
                .body("errors[0].field", equalTo("year"));
    }
}
