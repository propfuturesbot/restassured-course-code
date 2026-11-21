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
    @BeforeClass
    public void loadContract() {
        openApi = given().baseUri(CampusLab.BASE_URL).when().get("/openapi.json")
                .then().statusCode(200).extract().asString();
    }

    // ---
    // One student must match the StudentOut component; a whole page must match StudentList
    // (which pulls in StudentOut for every item and ListMeta for the paging data).
    @Test
    public void studentAndListMatchTheContract() throws Exception {
        given().spec(CampusLab.spec())
                .when().get("/api/v1/students/{id}", 7)
                .then().statusCode(200).body(matchesJsonSchema(OpenApiSchema.of(openApi, "StudentOut")));
        given().spec(CampusLab.spec()).queryParam("limit", 50)
                .when().get("/api/v1/students")
                .then().statusCode(200).body(matchesJsonSchema(OpenApiSchema.of(openApi, "StudentList")));
    }

    // ---
    // Errors have a contract too. A 404 is application/problem+json (RFC 9457) and matches the Problem component.
    // Its type is a URL that really resolves: GET it and the server explains the problem.
    @Test
    public void notFoundIsAProblem() throws Exception {
        String type = given().spec(CampusLab.spec())
                .when().get("/api/v1/students/{id}", 999999)
                .then().statusCode(404).contentType("application/problem+json")
                .body(matchesJsonSchema(OpenApiSchema.of(openApi, "Problem")))
                .body("type", endsWith("/problems/not-found"))
                .extract().path("type");
        given().when().get(type).then().statusCode(200);
    }

    // ---
    // The OpenAPI Problem is loose (errors is optional). For a validation error we want more, so this schema
    // is our own file in src/test/resources/schemas: status 422, and every error points at a field.
    @Test
    public void validationErrorMatchesOurStricterSchema() {
        given().spec(CampusLab.spec()).contentType(ContentType.JSON)
                .body(Map.of("firstName", "", "email", "not-an-email"))
                .when().post("/api/v1/students")
                .then().statusCode(422).contentType("application/problem+json")
                .body(matchesJsonSchemaInClasspath("schemas/validation-problem.json"))
                .body("errors.find { it.field == 'email' }.message", equalTo("must be a well-formed email address"));
    }

    // ---
    // A schema check is only useful if it can fail. The v2 student (Accept: application/vnd.campuslab.v2+json)
    // changed its shape, so it must NOT match the v1 StudentOut schema. We catch the failure and print it.
    @Test
    public void theV2ShapeBreaksTheV1Contract() throws Exception {
        String v1 = OpenApiSchema.of(openApi, "StudentOut");
        AssertionError error = expectThrows(AssertionError.class, () ->
                given().spec(CampusLab.spec()).accept("application/vnd.campuslab.v2+json")
                        .when().get("/api/v1/students/{id}", 7)
                        .then().statusCode(200).body(matchesJsonSchema(v1)));
        System.out.println(error.getMessage().lines().limit(8).reduce("", (a, b) -> a + b + "\n"));
    }
    // --- keep
    // end of the class
}
