// Check whole responses against JSON Schemas: the schemas come from Campus Lab's own openapi.json,
// and the errors (problem+json) are checked too, not only the happy path.
// Run:  mvn test -Dtest=SchemaTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.expectThrows;

// --- keep
// One schema check replaces dozens of field-by-field assertions: every field, its type, what is required.
public class SchemaTest {

    static String openApi;

    // ---
    // The contract is public: GET /openapi.json needs no key. We read it once for all the tests.
    // OpenApiSchema.of(...) cuts out one component (with everything it refers to) as a JSON Schema.


    // ---
    // One student must match the StudentOut component; a whole page must match StudentList
    // (which pulls in StudentOut for every item and ListMeta for the paging data).


    // ---
    // Errors have a contract too. A 404 is application/problem+json (RFC 9457) and matches the Problem component.
    // Its type is a URL that really resolves: GET it and the server explains the problem.


    // ---
    // The OpenAPI Problem is loose (errors is optional). For a validation error we want more, so this schema
    // is our own file in src/test/resources/schemas: status 422, and every error points at a field.


    // ---
    // A schema check is only useful if it can fail. The v2 student (Accept: application/vnd.campuslab.v2+json)
    // changed its shape, so it must NOT match the v1 StudentOut schema. We catch the failure and print it.

    // --- keep
    // end of the class
}
