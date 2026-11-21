// The consumer: a small client a "student portal" uses to talk to the Students app.
// Its base URL is a parameter, so a test can point it at Pact's mock server instead of the real app.
package portal;

import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class StudentClient {

    private final String baseUrl;

    public StudentClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Response getStudent(int id) {
        return given().baseUri(baseUrl).accept("application/json").when().get("/student/" + id);
    }

    public Response addStudent(Map<String, Object> student) {
        return given().baseUri(baseUrl).contentType("application/json").body(student).when().post("/student");
    }
}
