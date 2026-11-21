// A student profile: created once, then read back and checked.
// Run:  mvn test -Dtest=StudentProfileTest
package campus;

import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class StudentProfileTest {

    private static int profileId;

    @Test(groups = "regression")
    public void createProfile() {
        Map<String, Object> grace = Map.of("firstName", "Grace", "lastName", "Hopper",
                "email", "grace.profile." + System.nanoTime() + "@example.edu", "programme", "Physics", "year", 3);
        profileId = given().spec(Campus.spec()).contentType(ContentType.JSON).body(grace)
                .when().post("/api/v1/students")
                .then().statusCode(201).extract().path("id");
    }

    @Test(groups = "smoke")
    public void readProfile() {
        given().spec(Campus.spec())
                .when().get("/api/v1/students/" + profileId)
                .then().statusCode(200)
                .body("programme", equalTo("Physics"))
                .body("programmeCode", equalTo("PHYS"));
    }
}
