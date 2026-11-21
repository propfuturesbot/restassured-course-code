// Server-sent events on Campus Lab: the ready event, topic filters, and events caused through the REST API.
// Run:  mvn test -Dtest=SseTest   (CAMPUS_BASE_URL, CAMPUS_API_KEY)
package campus;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.endsWith;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// --- keep
// The stream lives at /api/v1/events/stream; topics=... filters it.
public class SseTest {

    static final String BASE = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    static final String KEY = System.getenv("CAMPUS_API_KEY");

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = BASE;
    }

    // ---
    // Connect: the response is text/event-stream, and the first event is "ready", with a retry hint
    // of 3000 ms (how long a browser's EventSource waits before reconnecting) and the topics we asked for.


    // ---
    // Cause two events with REST calls: create a student, then delete it.
    // The stream must deliver student.created and then student.deleted, each with an evt_ id.


    // ---
    // The filter works: subscribed to payment.* only, a student.created event must NOT arrive.
    // To prove a negative we wait a fixed 2 seconds, then check nothing of ours came.


    // --- keep
    // A plain response, so plain REST Assured: no key, 401 with a problem+json body.
    @Test
    public void withoutAKeyThe401IsPlainJson() {
        given().when().get("/api/v1/events/stream")
                .then().statusCode(401).contentType("application/problem+json").body("type", endsWith("key-required"));
    }

    int createStudent() {
        return given().header("X-API-Key", KEY).contentType("application/json")
                .body(Map.of("firstName", "Sam", "lastName", "Stream", "email", "sse." + System.nanoTime() + "@example.edu",
                        "programme", "Physics"))
                .when().post("/api/v1/students").then().statusCode(201).extract().path("id");
    }

    // The events about one student (the key is shared, other testers' events may be in the stream too).
    static List<SseStream.Event> ours(SseStream sse, int id) {
        return sse.events.stream().filter(e -> e.name() != null && e.name().startsWith("student."))
                .filter(e -> JsonPath.from(e.data()).getInt("data.id") == id).toList();
    }
}
