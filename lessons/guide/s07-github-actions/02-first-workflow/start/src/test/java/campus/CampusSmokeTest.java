// Two checks that need no API key: Campus Lab is up, and it refuses a request that has no key.
// Run:  mvn test -Dtest=CampusSmokeTest   (Campus Lab at CAMPUS_BASE_URL, default http://localhost:8000)
package campus;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

public class CampusSmokeTest {

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    }

    @Test
    public void healthIsOk() {
        given().when().get("/health")
                .then().statusCode(200).body("status", equalTo("ok"));
    }

    @Test
    public void studentsNeedAnApiKey() {
        given().when().get("/api/v1/students")
                .then().statusCode(401)
                .contentType("application/problem+json")
                .body("type", endsWith("/problems/key-required"));
    }
}
