// GraphQL with REST Assured on Campus Lab: queries, variables, errors that arrive with HTTP 200, depth limits.
// Run:  mvn test -Dtest=GraphQlTest   (CAMPUS_BASE_URL, CAMPUS_API_KEY)
package campus;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

// --- keep
// GraphQL is one URL, always POST, with a JSON body {"query": ..., "variables": {...}}.
public class GraphQlTest {

    RequestSpecification graphql;

    @BeforeClass
    public void setUp() {
        graphql = new RequestSpecBuilder()
                .setBaseUri(System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000"))
                .setBasePath("/graphql")
                .addHeader("X-API-Key", System.getenv("CAMPUS_API_KEY"))
                .setContentType(ContentType.JSON)
                .build();
    }

    // ---
    // One helper sends any query. REST Assured turns the Map into the JSON body GraphQL expects.


    // ---
    // A query asks for exactly the fields we want, nested: a student, their enrollments, each course's code.
    // The answer always sits under "data", in the same shape as the query.


    // ---
    // Variables keep the query fixed and pass values separately: $programme and $limit are declared
    // with their types, and the values travel in "variables". Never paste values into the query string.


    // ---
    // The trap: a GraphQL error still answers HTTP 200. statusCode(200) alone proves nothing.
    // Check "errors": the message and the machine-readable code in extensions.


    // ---
    // A mutation with invalid input: still 200, the error says 422 inside extensions,
    // and lists the field that failed.


    // ---
    // Depth limit: courses have prerequisite courses, which have prerequisites, and so on.
    // A query nested deeper than 6 levels could cost the server a lot, so it is refused before it runs.


    // --- keep
    // Not every failure is a GraphQL error: without a key the HTTP layer says no first, with a real 401.
    @Test
    public void withoutAKeyItIsAPlainHttp401() {
        given().contentType(ContentType.JSON).body(Map.of("query", "{ departments { code } }"))
                .when().post(System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000") + "/graphql")
                .then().statusCode(401);
    }
}
