// Webhooks on Campus Lab: receive a delivery in an inbox, verify its HMAC signature, and watch the retries.
// Run:  mvn test -Dtest=WebhookTest   (CAMPUS_BASE_URL, CAMPUS_API_KEY; about 10 s, the retry waits 5 s)
package campus;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// --- keep
// Webhooks need a public https receiver. A test box has none, so Campus Lab gives us an INBOX:
// a URL on its own server that records every request sent to it, which we can read back.
public class WebhookTest {

    RequestSpecification campus;
    final List<String> cleanup = new ArrayList<>();

    record Setup(String inbox, int hook, String secret) {}

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
        campus = new RequestSpecBuilder().addHeader("X-API-Key", System.getenv("CAMPUS_API_KEY"))
                .setContentType("application/json").build();
    }

    // ---
    // Create an inbox, then register a webhook that sends student.created events to the inbox URL.
    // The answer to the registration is the only time we see the signing secret: keep it.


    // ---
    // Receive: create a student, wait until the delivery lands in the inbox, then verify it like a receiver would.
    // The genuine body passes; the same body with one character changed fails.


    // ---
    // Retries: delete the inbox, so the next delivery fails (Campus Lab records 410 for a gone inbox).
    // Send a test ping and read the delivery log: attempt 1 fails and schedules a retry 5 s later.


        // ---
        // Wait for the second attempt (the log is newest first). Check what the receiver would see:
        // both failed with 410, attempt 2 came about 5 s after attempt 1, and attempt 3 is planned 25 s later.


    // --- keep
    // Remove what each test created (webhook, inbox, student); a missing inbox is fine.
    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        cleanup.forEach(path -> given().spec(campus).delete(path));
        cleanup.clear();
    }
}
