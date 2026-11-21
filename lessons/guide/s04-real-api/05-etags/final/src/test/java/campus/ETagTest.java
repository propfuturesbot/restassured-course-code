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
    @Test
    public void notModifiedGives304() {
        String etag = given().spec(CampusLab.spec()).when().get("/api/v1/students/{id}", id)
                .then().statusCode(200).extract().header("ETag");
        System.out.println("ETag " + etag);
        given().spec(CampusLab.spec()).header("If-None-Match", etag)
                .when().get("/api/v1/students/{id}", id)
                .then().statusCode(304).body(emptyString()).header("ETag", equalTo(etag));
    }

    // ---
    // Optimistic locking. Alice and Bob both read the student and hold the same ETag.
    // Alice patches with If-Match: it matches, the change is saved, and the student gets a NEW ETag.
    // Bob patches with the old ETag: 412 Precondition Failed, and his change is NOT applied.
    @Test
    public void staleIfMatchGives412() {
        String etag = given().spec(CampusLab.spec()).when().get("/api/v1/students/{id}", id).header("ETag");
        String newEtag = given().spec(CampusLab.spec()).contentType("application/merge-patch+json").header("If-Match", etag)
                .body(Map.of("year", 2))
                .when().patch("/api/v1/students/{id}", id)
                .then().statusCode(200).body("year", equalTo(2)).extract().header("ETag");
        assertNotEquals(newEtag, etag);

        given().spec(CampusLab.spec()).contentType("application/merge-patch+json").header("If-Match", etag)
                .body(Map.of("year", 5))
                .when().patch("/api/v1/students/{id}", id)
                .then().statusCode(412).contentType("application/problem+json")
                .body("type", endsWith("/problems/precondition-failed"))
                .body("currentEtag", equalTo(newEtag));
    }

    // ---
    // What Bob does next: GET again (the new version, with the new ETag), and retry with it.
    // An old If-None-Match no longer matches either: the GET is a full 200, not a 304.
    @Test(dependsOnMethods = "staleIfMatchGives412")
    public void readAgainAndRetry() {
        Response fresh = given().spec(CampusLab.spec()).header("If-None-Match", "\"an-old-etag\"")
                .when().get("/api/v1/students/{id}", id);
        fresh.then().statusCode(200).body("year", equalTo(2)).body(not(emptyString()));
        given().spec(CampusLab.spec()).contentType("application/merge-patch+json").header("If-Match", fresh.header("ETag"))
                .body(Map.of("year", 5))
                .when().patch("/api/v1/students/{id}", id)
                .then().statusCode(200).body("year", equalTo(5));
    }

    // --- keep
    // Clean up. DELETE honours If-Match too; without the header it just deletes.
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (id > 0) {
            given().spec(CampusLab.spec()).when().delete("/api/v1/students/{id}", id).then().statusCode(204);
        }
    }
}
