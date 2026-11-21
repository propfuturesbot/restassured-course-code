// Pay an invoice safely twice with an Idempotency-Key, and survive a 429 by honouring Retry-After.
// Run:  mvn test -Dtest=IdempotencyAndRateLimitTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// --- keep
// POST /api/v1/invoices/{id}/payments charges a (fake) card, so sending it twice must not charge twice.
// The client sends an Idempotency-Key: the same key with the same body replays the first answer.
// Campus Lab also allows 120 requests per minute per key, then answers 429 with Retry-After.
public class IdempotencyAndRateLimitTest {

    static int studentId;
    static int invoiceId;
    static final Map<String, Object> PAYMENT = Map.of("amount", "40.00", "currency", "USD", "paymentToken", "tok_visa");

    // --- keep
    // A student and an invoice of our own (deleting the student at the end deletes both).
    @BeforeClass
    public void createInvoice() {
        studentId = given().spec(CampusLab.spec()).contentType(ContentType.JSON)
                .body(Map.of("firstName", "Leslie", "lastName", "Lamport",
                        "email", "leslie." + System.currentTimeMillis() + "@example.edu", "programme", "Mathematics"))
                .when().post("/api/v1/students").then().statusCode(201).extract().path("id");
        invoiceId = given().spec(CampusLab.spec()).contentType(ContentType.JSON)
                .body(Map.of("studentId", studentId, "description", "Lab fee", "amount", "200.00", "currency", "USD"))
                .when().post("/api/v1/invoices").then().statusCode(201).extract().path("id");
    }

    // ---
    // The same payment sent twice with the same key (a client retrying after a lost answer).
    // The second answer is the stored first one: same payment id, header Idempotent-Replayed: true,
    // and the invoice has only ONE payment.
    @Test
    public void sameKeyIsChargedOnce() {
        String key = UUID.randomUUID().toString();
        int first = given().spec(CampusLab.spec()).header("Idempotency-Key", key).contentType(ContentType.JSON).body(PAYMENT)
                .when().post("/api/v1/invoices/{id}/payments", invoiceId)
                .then().statusCode(201).extract().path("id");
        given().spec(CampusLab.spec()).header("Idempotency-Key", key).contentType(ContentType.JSON).body(PAYMENT)
                .when().post("/api/v1/invoices/{id}/payments", invoiceId)
                .then().statusCode(201).header("Idempotent-Replayed", "true").body("id", equalTo(first));
        given().spec(CampusLab.spec()).when().get("/api/v1/invoices/{id}/payments", invoiceId)
                .then().statusCode(200).body("data.size()", equalTo(1));
    }

    // ---
    // The rules around the key: no key at all is a 400, and reusing a key for a DIFFERENT body is a 422,
    // because the server cannot know which of the two you meant.
    @Test(dependsOnMethods = "sameKeyIsChargedOnce")
    public void keyRules() {
        given().spec(CampusLab.spec()).contentType(ContentType.JSON).body(PAYMENT)
                .when().post("/api/v1/invoices/{id}/payments", invoiceId)
                .then().statusCode(400).body("type", endsWith("/problems/idempotency-key-required"));
        String key = UUID.randomUUID().toString();
        given().spec(CampusLab.spec()).header("Idempotency-Key", key).contentType(ContentType.JSON)
                .body(Map.of("amount", "10.00", "paymentToken", "tok_visa"))
                .when().post("/api/v1/invoices/{id}/payments", invoiceId).then().statusCode(201);
        given().spec(CampusLab.spec()).header("Idempotency-Key", key).contentType(ContentType.JSON)
                .body(Map.of("amount", "99.00", "paymentToken", "tok_visa"))
                .when().post("/api/v1/invoices/{id}/payments", invoiceId)
                .then().statusCode(422).body("type", endsWith("/problems/idempotency-key-reused"));
    }

    // ---
    // A failure is not stored. X-Chaos: error500 makes the gateway fail (502). Retrying with the SAME key
    // is then a real new attempt, not a replay: this is why retrying with one key is always safe.
    @Test(dependsOnMethods = "keyRules")
    public void retryAfterAServerErrorIsSafe() {
        String key = UUID.randomUUID().toString();
        Map<String, Object> payment = Map.of("amount", "25.00", "paymentToken", "tok_visa");
        given().spec(CampusLab.spec()).header("Idempotency-Key", key).header("X-Chaos", "error500")
                .contentType(ContentType.JSON).body(payment)
                .when().post("/api/v1/invoices/{id}/payments", invoiceId)
                .then().statusCode(502);
        Response retry = given().spec(CampusLab.spec()).header("Idempotency-Key", key).contentType(ContentType.JSON).body(payment)
                .when().post("/api/v1/invoices/{id}/payments", invoiceId);
        retry.then().statusCode(201).body("status", equalTo("succeeded"));
        assertEquals(retry.header("Idempotent-Replayed"), null, "a real charge, not a replay");
    }

    // ---
    // 429. To leave the shared key alone, this test makes a SECOND key (POST /api/keys needs no key;
    // it is never printed). It spends that key's 120 requests a minute, then asks once more through
    // RetryOn429Filter: the filter waits Retry-After seconds and the request gets through.
    @Test
    public void retryOn429HonoursRetryAfter() {
        String secondKey = given().baseUri(CampusLab.BASE_URL).contentType(ContentType.JSON)
                .body(Map.of("label", "rate limit test"))
                .when().post("/api/keys").then().statusCode(201).extract().path("apiKey");
        RequestSpecification second = new RequestSpecBuilder().setBaseUri(CampusLab.BASE_URL)
                .addHeader("X-API-Key", secondKey).build();
        Response answer;
        int sent = 0;
        do {
            answer = given().spec(second).when().get("/api/v1/meta");
            sent++;
        } while (answer.statusCode() != 429 && sent < 200);
        System.out.println("429 after " + sent + " requests, RateLimit-Remaining " + answer.header("RateLimit-Remaining")
                + ", Retry-After " + answer.header("Retry-After"));
        answer.then().statusCode(429).body("type", endsWith("/problems/rate-limited"));

        RetryOn429Filter retry = new RetryOn429Filter(2);
        given().spec(second).filter(retry).when().get("/api/v1/meta").then().statusCode(200);
        System.out.println("got through after " + retry.retries + " retry, waited " + retry.waitedMillis / 1000 + " s");
        assertTrue(retry.retries >= 1);
        given().spec(second).when().delete("/api/v1/sandbox").then().statusCode(204);
    }

    // --- keep
    // Clean up the student (its invoice and payments go with it).
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (studentId > 0) {
            given().spec(CampusLab.spec()).when().delete("/api/v1/students/{id}", studentId).then().statusCode(204);
        }
    }
}
