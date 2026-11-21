// Campus Lab's course catalog as a client. Extending the framework, step 2 of 4: the client.
package framework.clients;

import framework.models.Course;
import io.restassured.response.Response;

import java.util.List;

// --- keep
// Same rules as every client: build on ApiClient.given(), return Responses, never assert.
public class CoursesApi {

    private static final String COURSES = "/api/v1/courses";

    // ---
    // One page: limit + offset. The total count is in the X-Total-Count header, the next page in Link.


    // ---
    // A typed helper: one page as records (the list sits under "data").

    // --- keep
    // end of the class
}
