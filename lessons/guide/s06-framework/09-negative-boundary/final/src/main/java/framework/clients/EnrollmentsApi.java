// Campus Lab's enrollments as Java methods: enrol a student in a course, read, drop, delete.
package framework.clients;

import io.restassured.response.Response;

import java.util.Map;

// --- keep
// Only Campus Lab has enrollments; the Students app has no such endpoint.
public class EnrollmentsApi {

    private static final String ENROLLMENTS = "/api/v1/enrollments";

    // --- keep
    // Enrolling is a POST of two ids. Campus Lab answers 201 with the enrollment (status "enrolled"),
    // 409 for a full course or a duplicate, 422 for missing prerequisites or an inactive student.
    public Response enroll(int studentId, int courseId) {
        return ApiClient.given().body(Map.of("studentId", studentId, "courseId", courseId))
                .when().post(ENROLLMENTS);
    }

    public Response get(int enrollmentId) {
        return ApiClient.given().pathParam("id", enrollmentId).when().get(ENROLLMENTS + "/{id}");
    }

    // --- keep
    // A student's enrollments live under the student: GET /api/v1/students/{id}/enrollments -> {"data": [...]}.
    public Response forStudent(int studentId) {
        return ApiClient.given().pathParam("id", studentId).when().get("/api/v1/students/{id}/enrollments");
    }

    // --- keep
    // Dropping keeps the record with status "dropped" (history). DELETE follows the same rules and answers 204:
    // after the course's drop deadline, or for an enrollment already dropped, both answer 409.
    public Response drop(int enrollmentId) {
        return ApiClient.given().pathParam("id", enrollmentId).when().post(ENROLLMENTS + "/{id}/drop");
    }

    public Response delete(int enrollmentId) {
        return ApiClient.given().pathParam("id", enrollmentId).when().delete(ENROLLMENTS + "/{id}");
    }
    // ---
    // Grading an enrollment marks it "completed": that is how a student passes a prerequisite.
    public Response grade(int enrollmentId, String grade) {
        return ApiClient.given().body(Map.of("enrollmentId", enrollmentId, "grade", grade))
                .when().post("/api/v1/grades");
    }

    // --- keep
    // end of the class
}
