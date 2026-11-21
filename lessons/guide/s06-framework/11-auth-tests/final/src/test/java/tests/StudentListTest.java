// The first test of the framework, now written with the per-thread client: no address and no key in the test.
// Run:  mvn test -Denv=docker   (or local, campus)
package tests;

import framework.clients.ApiClient;
import framework.config.TestConfig;
import framework.http.SpecFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

// --- keep
// The class-level @Test puts every test of this class in the smoke group: quick checks that the target is up.
@Test(groups = "smoke")
// --- keep
// A TestNG test class is a plain class; every method marked @Test is one test.
public class StudentListTest {

    // --- keep
    // Before the first test, print the target so every log says where the run went.
    private final TestConfig config = TestConfig.get();

    @BeforeClass
    public void showTarget() {
        System.out.println("Target: " + config.describe());
    }

    // --- keep
    // The request() helper is gone: ApiClient.given() already carries the base URI, JSON and the key.
    // spec(SpecFactory.okJson()) checks status 200 and a JSON content type in one call.
    @Test
    public void listReturnsStudents() {
        ApiClient.given()
                .when().get("/student/list")
                .then().spec(SpecFactory.okJson()).body("size()", greaterThan(0));
    }

    // --- keep
    // Same idea for one student by id.
    @Test
    public void firstStudentHasAName() {
        ApiClient.given().pathParam("id", 1)
                .when().get("/student/{id}")
                .then().spec(SpecFactory.okJson()).body("firstName", notNullValue()).body("email", notNullValue());
    }
    // --- keep
    // end of the class
}
