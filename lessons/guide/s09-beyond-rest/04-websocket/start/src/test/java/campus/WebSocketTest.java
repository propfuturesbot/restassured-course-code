// WebSocket tests on Campus Lab with OkHttp + Awaitility: auth on connect, subscribe, assert pushed events.
// Run:  mvn test -Dtest=WebSocketTest   (CAMPUS_BASE_URL, CAMPUS_API_KEY)
package campus;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertEquals;

// --- keep
// ws:// is the same host and port as the REST API. One OkHttp client for the class.
public class WebSocketTest {

    static final String BASE = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    static final String WS = BASE.replaceFirst("^http", "ws") + "/ws/notifications";
    static final String KEY = System.getenv("CAMPUS_API_KEY");
    final OkHttpClient client = new OkHttpClient();

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = BASE;
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        client.dispatcher().executorService().shutdown();
    }

    // ---
    // A wrong key: Campus Lab accepts the connection, then closes it with code 1008 and a reason.
    // Awaitility polls until the close arrives (or fails the test after 5 seconds) - no Thread.sleep.


    // ---
    // Authenticate with the first message instead of the URL (keys in URLs end up in logs).
    // The server answers "welcome". Then a ping gets a pong with the same ts back.


    // ---
    // Subscribe to one topic, then cause the event through the REST API: create a student.
    // The event is pushed to us. Other testers may share the key, so we wait for OUR student's email.
