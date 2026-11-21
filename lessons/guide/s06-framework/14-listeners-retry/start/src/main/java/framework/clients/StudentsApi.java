// The /student endpoints as Java methods. Both targets serve them: the Students app and Campus Lab's legacy API.
package framework.clients;

import framework.config.App;
import framework.config.TestConfig;
import framework.models.Student;
import io.restassured.response.Response;

import java.util.Map;
import java.util.Optional;

// --- keep
// A client knows URLs and HTTP methods; it does NOT assert. Each method returns the Response,
// and the test decides what it expects. That keeps one client usable by happy and negative tests alike.
public class StudentsApi {

    private static final String STUDENT = "/student";

    // --- keep
    // Reads. The list filters only by programme and limit (that is all the Students app supports).
    public Response list() {
        return ApiClient.given().when().get(STUDENT + "/list");
    }

    public Response listByProgramme(String programme) {
        return ApiClient.given().queryParam("programme", programme).when().get(STUDENT + "/list");
    }

    public Response get(int id) {
        return ApiClient.given().pathParam("id", id).when().get(STUDENT + "/{id}");
    }

    // --- keep
    // Writes. The body is any object Jackson can turn into JSON: a Map today, a record soon.
    public Response create(Object student) {
        return ApiClient.given().body(student).when().post(STUDENT);
    }

    public Response replace(int id, Object student) {
        return ApiClient.given().pathParam("id", id).body(student).when().put(STUDENT + "/{id}");
    }

    public Response patchEmail(int id, String email) {
        return ApiClient.given().pathParam("id", id).body(Map.of("email", email)).when().patch(STUDENT + "/{id}");
    }

    public Response delete(int id) {
        return ApiClient.given().pathParam("id", id).when().delete(STUDENT + "/{id}");
    }

    // --- keep
    // The one place that knows the two contracts differ: Campus Lab answers a POST with the new student
    // (id included), the Students app only with {"msg":"Student added"}, so there we look the id up by email.
    // Tests just call createAndGetId and never care which app is behind the URL.
    public int createAndGetId(Object student, String email) {
        Response created = create(student);
        created.then().statusCode(201);
        if (TestConfig.get().app() == App.CAMPUS) {
            return created.path("id");
        }
        return findIdByEmail(email).orElseThrow(() -> new AssertionError("created but not listed: " + email));
    }

    // --- keep
    // Typed helpers on top of the raw calls: create from a record, read back INTO a record.
    // as(Student.class) is Jackson turning the JSON body into the record.
    public int createAndGetId(Student student) {
        return createAndGetId(student, student.email());
    }

    public Student fetch(int id) {
        return get(id).then().statusCode(200).extract().as(Student.class);
    }

    // --- keep
    // Look a student up in the list by email (the Students app's POST gives no id).
    public Optional<Integer> findIdByEmail(String email) {
        Integer id = list().then().statusCode(200).extract().path("find { it.email == '" + email + "' }.id");
        return Optional.ofNullable(id);
    }
    // --- keep
    // end of the class
}
