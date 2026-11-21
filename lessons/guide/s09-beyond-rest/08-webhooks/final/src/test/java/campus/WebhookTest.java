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
    Setup inboxAndWebhook() {
        JsonPath inbox = given().spec(campus).post("/inbox").then().statusCode(201).extract().jsonPath();
        JsonPath hook = given().spec(campus)
                .body(Map.of("url", inbox.getString("url"), "events", List.of("student.created")))
                .when().post("/api/v1/webhooks")
                .then().statusCode(201).body("deliversTo", equalTo("inbox")).extract().jsonPath();
        cleanup.add("/api/v1/webhooks/" + hook.getInt("id"));
        cleanup.add("/inbox/" + inbox.getString("id"));
        return new Setup(inbox.getString("id"), hook.getInt("id"), hook.getString("secret"));
    }

    // ---
    // Receive: create a student, wait until the delivery lands in the inbox, then verify it like a receiver would.
    // The genuine body passes; the same body with one character changed fails.
    @Test
    public void aDeliveryArrivesSignedAndVerifies() {
        Setup setup = inboxAndWebhook();
        String email = "hook." + System.currentTimeMillis() + "@example.edu";
        int student = given().spec(campus)
                .body(Map.of("firstName", "Hugo", "lastName", "Hook", "email", email, "programme", "History"))
                .when().post("/api/v1/students").then().statusCode(201).extract().path("id");
        cleanup.add("/api/v1/students/" + student);

        String requests = "/inbox/" + setup.inbox() + "/requests";
        await().atMost(Duration.ofSeconds(10)).until(() -> given().get(requests).jsonPath().getList("data").size() == 1);
        JsonPath delivery = given().get(requests).jsonPath().setRootPath("data[0]");
        String body = delivery.getString("body");
        String signature = delivery.getString("headers.x-campuslab-signature");
        System.out.println("event " + delivery.getString("headers.x-campuslab-event") + ", signature " + signature);

        assertEquals(JsonPath.from(body).getString("data.email"), email);
        assertTrue(WebhookSignature.verify(setup.secret(), body, signature, 300));
        assertFalse(WebhookSignature.verify(setup.secret(), body.replace("Hugo", "Hacker"), signature, 300));
        assertFalse(WebhookSignature.verify("whsec_not-the-secret", body, signature, 300));
    }

    // ---
    // Retries: delete the inbox, so the next delivery fails (Campus Lab records 410 for a gone inbox).
    // Send a test ping and read the delivery log: attempt 1 fails and schedules a retry 5 s later.
    @Test
    public void aFailedDeliveryIsRetriedWithBackoff() {
        Setup setup = inboxAndWebhook();
        given().delete("/inbox/" + setup.inbox()).then().statusCode(204);
        String log = "/api/v1/webhooks/" + setup.hook() + "/deliveries";
        given().spec(campus).post("/api/v1/webhooks/" + setup.hook() + "/test").then().statusCode(202);

        // ---
        // Wait for the second attempt (the log is newest first). Check what the receiver would see:
        // both failed with 410, attempt 2 came about 5 s after attempt 1, and attempt 3 is planned 25 s later.
        await().atMost(Duration.ofSeconds(15)).pollInterval(Duration.ofSeconds(1))
                .until(() -> given().spec(campus).get(log).jsonPath().getList("data").size() >= 2);
        JsonPath deliveries = given().spec(campus).get(log).then().body("data", hasSize(2))
                .body("data.statusCode", equalTo(List.of(410, 410)))
                .body("data.attempt", equalTo(List.of(2, 1))).extract().jsonPath();
        Instant first = Instant.parse(deliveries.getString("data[1].createdAt"));
        Instant second = Instant.parse(deliveries.getString("data[0].createdAt"));
        Instant third = Instant.parse(deliveries.getString("data[0].nextRetryAt"));
        System.out.println("attempt 1 -> 2: " + Duration.between(first, second).toMillis() + " ms, next retry in "
                + Math.round(Duration.between(second, third).toMillis() / 1000.0) + " s, error: " + deliveries.getString("data[0].error"));
        assertTrue(Duration.between(first, second).toSeconds() >= 4);
    }

    // --- keep
    // Remove what each test created (webhook, inbox, student); a missing inbox is fine.
    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        cleanup.forEach(path -> given().spec(campus).delete(path));
        cleanup.clear();
    }
}
