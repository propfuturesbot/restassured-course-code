// Builds request specifications for the target of the run. Every call returns a NEW object: nothing is shared or static.
package framework.http;

import framework.config.TestConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

// --- keep
// A factory, not a static field: RestAssured.baseURI and RestAssured.requestSpecification are globals,
// and two threads that set them at the same time send each other's requests to the wrong place.
public final class SpecFactory {

    private SpecFactory() {
    }

    // --- keep
    // HttpLogFilter goes FIRST in the chain, so it logs the request exactly as sent and the final answer
    // (after any rate-limit wait). The rest of the builder is unchanged.
    // --- keep
    // Everything a request to this target needs, in one place: base URI, JSON both ways, rate limits,
    // the contract check, and the API key when the target has one.
    // Auth tests need the same request WITHOUT the key, so the key is added in a separate step.
    public static RequestSpecification requestSpec() {
        RequestSpecBuilder builder = baseBuilder();
        TestConfig.get().apiKey().ifPresent(key -> builder.addHeader("X-API-Key", key));
        return builder.build();
    }

    public static RequestSpecification anonymousSpec() {
        return baseBuilder().build();
    }

    private static RequestSpecBuilder baseBuilder() {
        return new RequestSpecBuilder()
                .setBaseUri(TestConfig.get().baseUri())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new HttpLogFilter())
                .addFilter(new AllureRestAssured()
                        .setRequestTemplate("http-request-safe.ftl")
                        .setResponseTemplate("http-response-safe.ftl"))
                .addFilter(new RateLimitFilter())
                .addFilter(new ContractFilter());
    }

    // --- keep
    // What every successful JSON answer must look like. Tests add their own checks on top.
    public static ResponseSpecification okJson() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }
    // --- keep
    // end of the class
}
