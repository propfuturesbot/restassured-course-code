// Fast checks for every pull request (group "smoke"): Campus Lab is up and our key reads students.
// Run:  mvn test -Dgroups=smoke
package campus;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class StudentsSmokeTest {

    @Test(groups = "smoke")
    public void campusLabIsUp() {
        given().baseUri(System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000"))
                .when().get("/health")
                .then().statusCode(200).body("status", equalTo("ok"));
    }

    @Test(groups = "smoke")
    public void ourKeyReadsStudents() {
        given().spec(Campus.spec()).queryParam("limit", 5)
                .when().get("/api/v1/students")
                .then().statusCode(200)
                .body("data.size()", equalTo(5))
                .body("meta.total", greaterThanOrEqualTo(100));
    }
}
