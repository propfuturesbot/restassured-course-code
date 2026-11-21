// The first test of the framework, now pointed at a target by the config: the jar, Docker or Campus Lab.
// Run:  mvn test -Denv=local   mvn test -Denv=docker   mvn test -Denv=campus
package tests;

import framework.config.TestConfig;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

// --- keep
// A TestNG test class is a plain class; every method marked @Test is one test.
public class StudentListTest {

    // ---
    // The test no longer knows any address: it asks the config, which read -Denv.
    // Before the first test, print the target so every log says where the run went.
    private final TestConfig config = TestConfig.get();

    @BeforeClass
    public void showTarget() {
        System.out.println("Target: " + config.describe());
    }

    // ---
    // Every request starts here: the target's base URI, plus the API key header when the target needs one.
    // The Students app needs no key; Campus Lab needs X-API-Key on every call.
    private RequestSpecification request() {
        RequestSpecification request = given().baseUri(config.baseUri());
        config.apiKey().ifPresent(key -> request.header("X-API-Key", key));
        return request;
    }

    // --- keep
    // The list endpoint answers 200 with a JSON array that is not empty.
    @Test
    public void listReturnsStudents() {
        request()
                .when().get("/student/list")
                .then().statusCode(200).body("size()", greaterThan(0));
    }

    // --- keep
    // One student by id: the first seeded student has an id, a first name and an email.
    @Test
    public void firstStudentHasAName() {
        request().pathParam("id", 1)
                .when().get("/student/{id}")
                .then().statusCode(200).body("firstName", notNullValue()).body("email", notNullValue());
    }
    // --- keep
    // end of the class
}
