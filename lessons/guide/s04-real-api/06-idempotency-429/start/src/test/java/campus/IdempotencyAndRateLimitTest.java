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


    // ---
    // The rules around the key: no key at all is a 400, and reusing a key for a DIFFERENT body is a 422,
    // because the server cannot know which of the two you meant.


    // ---
    // A failure is not stored. X-Chaos: error500 makes the gateway fail (502). Retrying with the SAME key
    // is then a real new attempt, not a replay: this is why retrying with one key is always safe.


    // ---
    // 429. To leave the shared key alone, this test makes a SECOND key (POST /api/keys needs no key;
    // it is never printed). It spends that key's 120 requests a minute, then asks once more through
    // RetryOn429Filter: the filter waits Retry-After seconds and the request gets through.


    // --- keep
    // Clean up the student (its invoice and payments go with it).
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (studentId > 0) {
            given().spec(CampusLab.spec()).when().delete("/api/v1/students/{id}", studentId).then().statusCode(204);
        }
    }
}
