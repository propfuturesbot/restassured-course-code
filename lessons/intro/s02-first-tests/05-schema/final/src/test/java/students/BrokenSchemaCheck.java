// The same check against a schema that is wrong on purpose, to read what a schema failure looks like.
// Not named *Test, so "mvn test" skips it. Run it on purpose:  mvn test -Dtest=BrokenSchemaCheck   (it FAILS)
package students;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

class BrokenSchemaCheck {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // student-schema-broken.json says id is a string and requires a "phone" field the app never sends.
    @Test
    void studentAgainstABrokenSchema() {
        given().pathParam("id", 1)
                .when().get("/{id}")
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/student-schema-broken.json"));
    }
}
