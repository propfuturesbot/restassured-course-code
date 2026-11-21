// OWASP API Top 10 as tests on Campus Lab: broken object level authorization, mass assignment, rate limits.
// Run:  mvn test -Dtest=ApiSecurityTest   (two tests FAIL on purpose: they catch the flaws of training mode)
package campus;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// --- keep
// A brand-new key for this class: its own copy of the data and its own rate limit, so we can break things
// without touching anyone else. The class deletes the sandbox at the end.
public class ApiSecurityTest {

    String apiKey;
    String aliceToken, bobToken;

    // ---
    // Two users. Campus Lab mints a student token: a JWT that may only touch ONE student's record.
    // Alice is student 1, Bob is student 2. Each gets a token, using our key (the "admin").
    @BeforeClass
    public void twoUsers() {
        RestAssured.baseURI = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
        apiKey = given().contentType("application/json").body("{}")
                .when().post("/api/keys").then().statusCode(201).extract().path("apiKey");
        aliceToken = studentToken(1);
        bobToken = studentToken(2);
    }

    String studentToken(int studentId) {
        return given().header("X-API-Key", apiKey).contentType("application/json").body(Map.of("studentId", studentId))
                .when().post("/api/v1/auth/student-token")
                .then().statusCode(200).extract().path("accessToken");
    }

    // ---
    // Every security test runs twice: in the normal mode and in Campus Lab's deliberately vulnerable
    // training mode (header X-Training-Mode: vulnerable). The assertions are the same in both:
    // they describe what a SAFE API does. In the vulnerable mode they should fail - that is the test working.
    @DataProvider
    public Object[][] modes() {
        return new Object[][]{{"normal"}, {"vulnerable"}};
    }

    RequestSpecification as(String token, String mode) {
        RequestSpecification spec = given().auth().oauth2(token);
        return mode.equals("vulnerable") ? spec.header("X-Training-Mode", "vulnerable") : spec;
    }

    // ---
    // API1 Broken Object Level Authorization (BOLA, also called IDOR).
    // Alice can read her own transcript. Changing the id in the URL to Bob's must be refused with 403.
    @Test(dataProvider = "modes", priority = 1)
    public void aliceCannotReadBobsTranscript(String mode) {
        as(aliceToken, mode).when().get("/api/v1/students/1/transcript").then().statusCode(200);
        as(aliceToken, mode).when().get("/api/v1/students/2/transcript")
                .then().log().ifValidationFails().statusCode(403);
    }

    // ---
    // API3 Broken Object Property Level Authorization (mass assignment).
    // Bob may edit his own record, but not the fields only the school sets: role and gpa.
    // A safe API answers 422 and names the fields; it must never store role = admin.
    @Test(dataProvider = "modes", priority = 2)
    public void bobCannotMakeHimselfAdmin(String mode) {
        Response r = as(bobToken, mode).contentType("application/merge-patch+json")
                .body(Map.of("year", 3, "role", "admin", "gpa", 4.0))
                .when().patch("/api/v1/students/2");
        System.out.println(mode + ": " + r.statusCode() + " role=" + r.path("role") + " gpa=" + r.path("gpa"));
        r.then().statusCode(422).body("errors.field", hasItems("role", "gpa"));
    }

    // ---
    // API4 Unrestricted Resource Consumption: a rate limit must exist and say so.
    // Send requests until the server answers 429, at most the advertised limit plus a few.
    @Test(priority = 3)
    public void theRateLimitStopsAFlood() {
        Response first = given().header("X-API-Key", apiKey).get("/api/v1/students/1");
        int limit = Integer.parseInt(first.header("RateLimit-Limit"));
        int sent = 1;
        Response r = first;
        while (r.statusCode() != 429 && sent <= limit + 5) {
            r = given().header("X-API-Key", apiKey).get("/api/v1/students/1");
            sent++;
        }
        System.out.println("limit " + limit + "/min, got 429 after " + sent + " requests, Retry-After " + r.header("Retry-After") + " s");
        assertEquals(r.statusCode(), 429);
        assertTrue(sent <= limit + 1, "more requests got through than the limit says");
        r.then().header("Retry-After", notNullValue()).body("type", endsWith("rate-limited"))
                .header("RateLimit-Remaining", equalTo("0"));
    }

    // --- keep
    // Clean up: delete the sandbox this class created (with its key and its data).
    @AfterClass(alwaysRun = true)
    public void deleteSandbox() {
        if (apiKey != null) {
            given().header("X-API-Key", apiKey).when().delete("/api/v1/sandbox");
        }
    }
}
