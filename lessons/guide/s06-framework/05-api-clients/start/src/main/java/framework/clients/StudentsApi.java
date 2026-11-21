// The /student endpoints as Java methods. Both targets serve them: the Students app and Campus Lab's legacy API.
package framework.clients;

import framework.config.App;
import framework.config.TestConfig;
import io.restassured.response.Response;

import java.util.Map;
import java.util.Optional;

// --- keep
// A client knows URLs and HTTP methods; it does NOT assert. Each method returns the Response,
// and the test decides what it expects. That keeps one client usable by happy and negative tests alike.
public class StudentsApi {

    private static final String STUDENT = "/student";

    // ---
    // Reads. The list filters only by programme and limit (that is all the Students app supports).


    // ---
    // Writes. The body is any object Jackson can turn into JSON: a Map today, a record soon.


    // ---
    // The one place that knows the two contracts differ: Campus Lab answers a POST with the new student
    // (id included), the Students app only with {"msg":"Student added"}, so there we look the id up by email.
    // Tests just call createAndGetId and never care which app is behind the URL.

    // --- keep
    // end of the class
}
