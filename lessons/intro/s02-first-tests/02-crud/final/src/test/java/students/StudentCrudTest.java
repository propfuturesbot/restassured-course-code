// The same CRUD tests as before, the 2026 way: REST Assured 6 and JUnit 6, against the Students app.
// Run:  mvn test -Dtest=StudentCrudTest   (the app at STUDENTS_BASE_URI, default http://localhost:8080)
package students;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

// --- keep
// One student goes through its whole life: created, read, replaced, patched, deleted.
// The tests share that student, so JUnit must run them in the order we number them.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentCrudTest {

    static String email = "ada." + System.currentTimeMillis() + "@example.com";
    static int id;

    // ---
    // Before any test runs, tell REST Assured where the app lives.
    // baseURI comes from an environment variable, so the same tests run against the jar, Docker or a server.
    // basePath is the part every URL shares, so each test only writes what is different.
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // ---
    // CREATE. given() builds the request, when() sends it, then() checks the answer.
    // A Map becomes the JSON body. This app answers 201 with a message, not with the new student.
    @Test
    @Order(1)
    void createStudent() {
        Map<String, Object> student = Map.of(
                "firstName", "Ada", "lastName", "Lovelace", "email", email,
                "programme", "Computer Science", "courses", List.of("Java", "Testing"));
        given().contentType(ContentType.JSON).body(student)
                .when().post()
                .then().statusCode(201).body("msg", equalTo("Student added"));
    }

    // ---
    // READ. The POST did not give us the id, so we look the student up in the list by email.
    // extract().path() runs a GPath expression on the response: find the student whose email is ours.
    @Test
    @Order(2)
    void findTheNewStudent() {
        id = given().when().get("/list")
                .then().statusCode(200)
                .extract().path("find { it.email == '" + email + "' }.id");
        given().pathParam("id", id)
                .when().get("/{id}")
                .then().statusCode(200).body("firstName", equalTo("Ada")).body("courses", hasItems("Java", "Testing"));
    }

    // ---
    // UPDATE with PUT: send the WHOLE student. Every field is replaced.
    @Test
    @Order(3)
    void replaceStudent() {
        Map<String, Object> student = Map.of(
                "firstName", "Ada", "lastName", "King", "email", email,
                "programme", "Mathematics", "courses", List.of("Java", "Algebra"));
        given().contentType(ContentType.JSON).body(student).pathParam("id", id)
                .when().put("/{id}")
                .then().statusCode(200).body("msg", equalTo("Student Updated"));
    }

    // ---
    // UPDATE with PATCH: send only what changes. In this app PATCH changes the email only.
    // Then we read the student again to prove both changes really happened.
    @Test
    @Order(4)
    void patchEmail() {
        email = "ada.king." + System.currentTimeMillis() + "@example.com";
        given().contentType(ContentType.JSON).body(Map.of("email", email)).pathParam("id", id)
                .when().patch("/{id}")
                .then().statusCode(200);
        given().pathParam("id", id)
                .when().get("/{id}")
                .then().body("lastName", equalTo("King")).body("programme", equalTo("Mathematics"))
                .body("email", equalTo(email));
    }

    // ---
    // DELETE answers 204, no content. Reading the student afterwards must give 404.
    @Test
    @Order(5)
    void deleteStudent() {
        given().pathParam("id", id).when().delete("/{id}").then().statusCode(204);
        given().pathParam("id", id).when().get("/{id}").then().statusCode(404).body("error", equalTo("Not Found!"));
    }
    // --- keep
    // end of the class
}
