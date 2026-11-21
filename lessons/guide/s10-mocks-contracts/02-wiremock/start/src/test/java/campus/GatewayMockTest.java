// WireMock in a test: stub a payment gateway, break it with faults and delays, and verify what was sent.
// Run:  mvn test -Dtest=GatewayMockTest   (WireMock on :8122; one test also calls the real Campus Lab gateway)
package campus;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

// --- keep
// The gateway we pretend to be: Campus Lab's /fake-gateway/charges (POST {amount, currency, source} -> 201).
public class GatewayMockTest {

    WireMockServer wm;
    RequestSpecification gateway;
    static final Map<String, Object> CHARGE = Map.of("amount", 2500, "currency", "USD", "source", "tok_visa");
    static final String SUCCEEDED = "{\"id\":\"ch_wiremock01\",\"amount\":2500,\"currency\":\"USD\",\"source\":\"tok_visa\","
            + "\"createdAt\":\"2026-11-21T05:41:03Z\",\"status\":\"succeeded\",\"failureCode\":null,\"failureMessage\":null}";

    // ---
    // Start WireMock inside the test JVM on a fixed port, and point REST Assured at it.
    // Before every test reset it, so stubs and the request journal of one test never leak into the next.


    // ---
    // A stub: when a POST to /fake-gateway/charges carries this JSON, answer 201 with a charge.
    // The answer has the same fields as the real gateway, so code under test cannot tell the difference.


    // ---
    // Faults: things a real server does on a bad day. A connection reset makes the client throw,
    // before there is any status code to check. This is what error handling must survive.


    // ---
    // Delays: the stub waits 2 s before answering. With a 1 s socket timeout the client gives up.
    // Without a timeout, a slow dependency would hang the test (or the app) for as long as it likes.


    // ---
    // A scenario is a stub with state: the first call fails with 503, the second succeeds.
    // That is how we test retry logic. Then verify(): the gateway received exactly two calls,
    // both with the same Idempotency-Key, so a retry cannot charge the card twice.


    // --- keep
    // Keep the stub honest: the real Campus Lab gateway answers with the same field names as our stub.
    // (Its docs say "no auth" and show 5 fields; the real endpoint needs the key and answers 8 fields.)
    @Test
    public void theStubHasTheRealGatewaysFields() {
        Map<String, Object> real = given().baseUri(System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000"))
                .header("X-API-Key", System.getenv("CAMPUS_API_KEY"))
                .contentType("application/json").body(CHARGE).when().post("/fake-gateway/charges")
                .then().statusCode(201).extract().jsonPath().getMap("");
        assertEquals(real.keySet(), io.restassured.path.json.JsonPath.from(SUCCEEDED).getMap("").keySet());
    }
}
