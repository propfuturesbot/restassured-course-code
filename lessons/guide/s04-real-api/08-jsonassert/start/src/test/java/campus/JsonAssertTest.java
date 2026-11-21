// Compare a WHOLE response with an expected JSON file, while ignoring the fields that change on every run.
// Run:  mvn test -Dtest=JsonAssertTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.http.ContentType;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertTrue;

// --- keep
// Field-by-field checks miss what you did not think of. A whole-JSON comparison catches every field,
// but ids, timestamps and today's date differ on every run. JSONAssert lets us say how to compare those.
public class JsonAssertTest {

    static int id;
    static String actual;

    // --- keep
    // A student of our own, read back as the raw JSON string the server sent.
    @BeforeClass
    public void createAndRead() {
        id = given().spec(CampusLab.spec()).contentType(ContentType.JSON)
                .body(Map.of("firstName", "Ada", "lastName", "Lovelace", "email", "ada." + System.currentTimeMillis() + "@example.edu",
                        "programme", "Mathematics", "year", 2, "dateOfBirth", "2004-12-10"))
                .when().post("/api/v1/students").then().statusCode(201).extract().path("id");
        actual = given().spec(CampusLab.spec()).when().get("/api/v1/students/{id}", id).then().statusCode(200).extract().asString();
    }

    // --- keep
    // The expected files live in src/test/resources/expected.
    static String expected(String name) throws Exception {
        return Files.readString(Path.of("src/test/resources/expected", name));
    }

    // ---
    // A plain STRICT compare fails, and JSONCompare tells us exactly why without throwing:
    // id, email, enrolledOn, createdAt and updatedAt are different every run. Those are the fields to customise.


    // ---
    // A CustomComparator: STRICT for everything, except the customised paths.
    // id may be anything; email and the timestamps must match a pattern; enrolledOn must be today (UTC).
    // A matcher gets (actual, expected), in that order. Any OTHER difference still fails the test.


    // ---
    // LENIENT allows extra fields in the response and any order in arrays: here the expected file lists
    // the courses in the other order. "**" in a path matches at any depth, so one customisation ignores
    // every course's term.


    // --- keep
    // Clean up the student.
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (id > 0) {
            given().spec(CampusLab.spec()).when().delete("/api/v1/students/{id}", id).then().statusCode(204);
        }
    }
}
