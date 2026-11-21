// REFERENCE, written and reviewed by hand (not by Claude): the strong version of the tests the prompt asks for.
// Use it to judge Claude's StudentsApiTest. Run:  mvn test -Dtest=StudentsApiReferenceTest   (needs CAMPUS_API_KEY)
package campus;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

class StudentsApiReferenceTest {

    static RequestSpecification api;

    @BeforeAll
    static void spec() {
        api = new RequestSpecBuilder()
                .setBaseUri(System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000"))
                .addHeader("X-API-Key", System.getenv("CAMPUS_API_KEY"))
                .setAccept(ContentType.JSON)
                .build();
    }

    static Map<String, Object> newStudent() {
        return Map.of("firstName", "Ada", "lastName", "Lovelace",
                "email", "ada." + System.nanoTime() + "@example.edu", "programme", "Computer Science", "year", 2);
    }

    // Weak version: statusCode(200) only. Strong version: every item has the programme, and limit is honoured.
    @Test
    void listFiltersByProgrammeAndLimit() {
        given().spec(api).queryParam("programme", "Computer Science").queryParam("limit", 3)
                .when().get("/api/v1/students")
                .then().statusCode(200)
                .body("data", hasSize(lessThanOrEqualTo(3)))
                .body("data.programme", everyItem(equalTo("Computer Science")))
                .body("meta.limit", equalTo(3));
    }

    // Weak version: statusCode(201) only. Strong version: the body echoes what we sent and Location points to it,
    // then a GET on that Location returns the same student.
    @Test
    void createThenReadBack() {
        Map<String, Object> student = newStudent();
        Response created = given().spec(api).contentType(ContentType.JSON).body(student)
                .when().post("/api/v1/students")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("email", equalTo(student.get("email")))
                .body("fullName", equalTo("Ada Lovelace"))
                .extract().response();
        int id = created.path("id");
        String location = created.header("Location");
        assertThat(location, equalTo("/api/v1/students/" + id));

        given().spec(api)
                .when().get(location)
                .then().statusCode(200)
                .body("id", equalTo(id))
                .body("email", equalTo(student.get("email")))
                .body("programme", equalTo("Computer Science"));
    }

    // Weak version: statusCode(409) only. Strong version: problem+json, the duplicate-email type, the field named.
    @Test
    void duplicateEmailIsAConflict() {
        Map<String, Object> student = newStudent();
        given().spec(api).contentType(ContentType.JSON).body(student).when().post("/api/v1/students")
                .then().statusCode(201);
        given().spec(api).contentType(ContentType.JSON).body(student)
                .when().post("/api/v1/students")
                .then().statusCode(409)
                .contentType("application/problem+json")
                .body("type", endsWith("/problems/duplicate-email"))
                .body("errors.field", containsInAnyOrder("email"));
    }

    // Weak version: statusCode(4xx). Strong version: 422, one error per bad field, by name.
    @Test
    void invalidFieldsAreListed() {
        given().spec(api).contentType(ContentType.JSON)
                .body(Map.of("firstName", "", "lastName", "L", "email", "not-an-email", "programme", "C"))
                .when().post("/api/v1/students")
                .then().statusCode(422)
                .contentType("application/problem+json")
                .body("type", endsWith("/problems/validation-error"))
                .body("errors.field", containsInAnyOrder("firstName", "email", "programme"));
    }

    // Weak version: statusCode(404) only. Strong version: problem+json with the not-found type and the id in detail.
    @Test
    void unknownIdIsNotFound() {
        given().spec(api)
                .when().get("/api/v1/students/{id}", 99999)
                .then().statusCode(404)
                .contentType("application/problem+json")
                .body("type", endsWith("/problems/not-found"))
                .body("detail", equalTo("Student 99999 does not exist in this sandbox."));
    }
}
