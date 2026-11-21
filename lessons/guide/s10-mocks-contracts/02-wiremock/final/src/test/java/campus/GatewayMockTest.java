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
    @BeforeClass
    public void startWireMock() {
        wm = new WireMockServer(options().port(8122));
        wm.start();
        gateway = given().baseUri("http://localhost:8122").contentType("application/json");
    }

    @BeforeMethod
    public void reset() {
        wm.resetAll();
    }

    @AfterClass(alwaysRun = true)
    public void stop() {
        wm.stop();
        RestAssured.reset();
    }

    // ---
    // A stub: when a POST to /fake-gateway/charges carries this JSON, answer 201 with a charge.
    // The answer has the same fields as the real gateway, so code under test cannot tell the difference.
    @Test
    public void aStubAnswersLikeTheGateway() {
        wm.stubFor(post(urlEqualTo("/fake-gateway/charges"))
                .withRequestBody(equalToJson("{\"amount\": 2500, \"currency\": \"USD\", \"source\": \"tok_visa\"}"))
                .willReturn(aResponse().withStatus(201).withHeader("Content-Type", "application/json")
                        .withBody(SUCCEEDED)));
        given().spec(gateway).body(CHARGE).when().post("/fake-gateway/charges")
                .then().statusCode(201).body("status", is("succeeded")).body("id", is("ch_wiremock01"));
    }

    // ---
    // Faults: things a real server does on a bad day. A connection reset makes the client throw,
    // before there is any status code to check. This is what error handling must survive.
    @Test
    public void aConnectionResetIsAnException() {
        wm.stubFor(post(urlEqualTo("/fake-gateway/charges")).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        SocketException e = expectThrows(SocketException.class, () -> given().spec(gateway).body(CHARGE).post("/fake-gateway/charges"));
        System.out.println("fault: " + e);
    }

    // ---
    // Delays: the stub waits 2 s before answering. With a 1 s socket timeout the client gives up.
    // Without a timeout, a slow dependency would hang the test (or the app) for as long as it likes.
    @Test
    public void aSlowGatewayHitsTheTimeout() {
        wm.stubFor(post(urlEqualTo("/fake-gateway/charges")).willReturn(aResponse().withStatus(201).withFixedDelay(2000)));
        RestAssuredConfig oneSecond = RestAssured.config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.socket.timeout", 1000));
        SocketTimeoutException e = expectThrows(SocketTimeoutException.class,
                () -> given().spec(gateway).config(oneSecond).body(CHARGE).post("/fake-gateway/charges"));
        System.out.println("delay: " + e);
    }

    // ---
    // A scenario is a stub with state: the first call fails with 503, the second succeeds.
    // That is how we test retry logic. Then verify(): the gateway received exactly two calls,
    // both with the same Idempotency-Key, so a retry cannot charge the card twice.
    @Test
    public void aRetryAfter503IsVerified() {
        wm.stubFor(post(urlEqualTo("/fake-gateway/charges")).inScenario("flaky").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(503)).willSetStateTo("recovered"));
        wm.stubFor(post(urlEqualTo("/fake-gateway/charges")).inScenario("flaky").whenScenarioStateIs("recovered")
                .willReturn(aResponse().withStatus(201)));
        int first = given().spec(gateway).header("Idempotency-Key", "pay-42").body(CHARGE).post("/fake-gateway/charges").statusCode();
        int second = given().spec(gateway).header("Idempotency-Key", "pay-42").body(CHARGE).post("/fake-gateway/charges").statusCode();
        assertEquals(first, 503);
        assertEquals(second, 201);
        wm.verify(2, postRequestedFor(urlEqualTo("/fake-gateway/charges"))
                .withHeader("Idempotency-Key", equalTo("pay-42"))
                .withRequestBody(matchingJsonPath("$.amount", equalTo("2500"))));
    }

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
