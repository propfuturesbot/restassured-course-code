// ETags: ask "has it changed?" with If-None-Match (304), and refuse to overwrite a newer version with If-Match (412).
// Run:  mvn test -Dtest=ETagTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertNotEquals;

// --- keep
// GET /api/v1/students/{id} sends an ETag header: a fingerprint of this version of the student.
// If-None-Match: <etag> on a GET answers 304 when nothing changed; If-Match: <etag> on PATCH / PUT / DELETE
// answers 412 when the student changed since we read it. The tests use a student of their own.
public class ETagTest {

    static int id;

    // --- keep
    // A fresh student: nobody else changes it while the tests run.
    @BeforeClass
    public void createStudent() {
        id = given().spec(CampusLab.spec()).contentType(ContentType.JSON)
                .body(Map.of("firstName", "Edsger", "lastName", "Dijkstra",
                        "email", "edsger." + System.currentTimeMillis() + "@example.edu", "programme", "Computer Science"))
                .when().post("/api/v1/students")
                .then().statusCode(201).extract().path("id");
    }

    // ---
    // Read once and keep the ETag. Ask again with If-None-Match: the server answers 304 Not Modified,
    // with no body at all, so a client with a cache saves the whole download.


    // ---
    // Optimistic locking. Alice and Bob both read the student and hold the same ETag.
    // Alice patches with If-Match: it matches, the change is saved, and the student gets a NEW ETag.
    // Bob patches with the old ETag: 412 Precondition Failed, and his change is NOT applied.


    // ---
    // What Bob does next: GET again (the new version, with the new ETag), and retry with it.
    // An old If-None-Match no longer matches either: the GET is a full 200, not a 304.


    // --- keep
    // Clean up. DELETE honours If-Match too; without the header it just deletes.
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (id > 0) {
            given().spec(CampusLab.spec()).when().delete("/api/v1/students/{id}", id).then().statusCode(204);
        }
    }
}
