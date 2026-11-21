// One Postman request, rewritten as REST Assured: the same URL, params, headers and checks, line for line.
// Run:  mvn test -Dtest=PostmanToCodeTest   (needs CAMPUS_BASE_URL and CAMPUS_API_KEY)
package campus;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;

// --- keep
// The Postman request: GET {{baseUrl}}/api/v1/students?programme=Computer Science&limit=5, header X-API-Key: {{apiKey}}.
public class PostmanToCodeTest {

    static final String BASE_URL = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");
    static final String API_KEY = System.getenv("CAMPUS_API_KEY");

    // ---
    // given() is everything Postman shows ABOVE the Send button:
    // the base URL ({{baseUrl}}), the Params tab (queryParam) and the Headers tab (header).
    // when() is the Send button: the method and the path.
    // then() is Postman's Tests tab: status code, header and body checks.
    @Test
    public void listComputerScienceStudents() {
        given()
                .baseUri(BASE_URL)
                .queryParam("programme", "Computer Science")
                .queryParam("limit", 5)
                .header("X-API-Key", API_KEY)
                .header("Accept", "application/json")
        .when()
                .get("/api/v1/students")
        .then()
                .statusCode(200)
                .header("Content-Type", equalTo("application/json"))
                .time(lessThan(2000L))
                .body("data", hasSize(5))
                .body("data.programme", everyItem(equalTo("Computer Science")))
                .body("meta.limit", equalTo(5));
    }

    // ---
    // Postman's console shows what was really sent. log().all() is the same thing in code:
    // the full URL with the encoded space (Computer%20Science), every header, then the response.
    @Test
    public void sameRequestWithTheConsoleView() {
        given().baseUri(BASE_URL)
                .queryParam("programme", "Computer Science").queryParam("limit", 2)
                .header("X-API-Key", API_KEY)
                .log().uri().log().params()
        .when()
                .get("/api/v1/students")
        .then()
                .log().body()
                .statusCode(200);
    }
    // --- keep
    // end of the class
}
