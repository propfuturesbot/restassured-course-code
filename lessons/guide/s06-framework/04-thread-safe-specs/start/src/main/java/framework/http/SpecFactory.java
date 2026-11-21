// Builds request specifications for the target of the run. Every call returns a NEW object: nothing is shared or static.
package framework.http;

import framework.config.TestConfig;
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

    // ---
    // Everything a request to this target needs, in one place: base URI, JSON both ways,
    // the API key when the target has one, and the filter that respects rate limits.
    // RequestSpecBuilder.build() hands out a fresh specification each time it is called.


    // ---
    // What every successful JSON answer must look like. Tests add their own checks on top.

    // --- keep
    // end of the class
}
