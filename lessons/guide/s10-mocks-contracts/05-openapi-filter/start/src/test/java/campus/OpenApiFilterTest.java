// Every request and response checked against Campus Lab's own OpenAPI document, by one REST Assured filter.
// Run:  mvn test -Dtest=OpenApiFilterTest   (ONE test fails on purpose: it is a real spec drift)
package campus;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.expectThrows;

// --- keep
// The spec is the one the server publishes at /openapi.json (OpenAPI 3.1, generated from the code).
public class OpenApiFilterTest {

    static final String BASE = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    RequestSpecification campus;

    // ---
    // OpenApiValidationFilter reads the spec once. Added to the request spec, it checks every request we send
    // and every response we get: path, method, parameters, headers, content types and body schemas.
    // A mismatch throws OpenApiValidationException, so the test fails even if its own assertions pass.


    // ---
    // Ordinary calls: nothing to assert about the schema, the filter does it on every call.


    // ---
    // A broken REQUEST is caught too: this body misses three required fields.
    // The filter's report says which ones, at location REQUEST - our test is wrong, not the server.


    // ---
    // Spec drift: Campus Lab's README says v1 also serves the v2 shape when you ask for
    // Accept: application/vnd.campuslab.v2+json. The server does it - but the spec never says so.
    // A client generated from the spec would not know this response exists. This test FAILS: that is the drift.


    // ---
    // Not every red is the server's fault. Campus Lab's Problem schema says additionalProperties: true,
    // yet the validator rejects the extra existingEnrollmentId field of a 409 (a gap in its OpenAPI 3.1 support).
    // Check the spec first; then lower just that one rule to a warning, for this spec, and keep everything else.
