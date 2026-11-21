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


    // ---
    // Postman's console shows what was really sent. log().all() is the same thing in code:
    // the full URL with the encoded space (Computer%20Science), every header, then the response.

    // --- keep
    // end of the class
}
