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


    // ---
    // CREATE. given() builds the request, when() sends it, then() checks the answer.
    // A Map becomes the JSON body. This app answers 201 with a message, not with the new student.


    // ---
    // READ. The POST did not give us the id, so we look the student up in the list by email.
    // extract().path() runs a GPath expression on the response: find the student whose email is ours.


    // ---
    // UPDATE with PUT: send the WHOLE student. Every field is replaced.


    // ---
    // UPDATE with PATCH: send only what changes. In this app PATCH changes the email only.
    // Then we read the student again to prove both changes really happened.


    // ---
    // DELETE answers 204, no content. Reading the student afterwards must give 404.

    // --- keep
    // end of the class
}
