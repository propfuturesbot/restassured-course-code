// Campus Lab's enrollments as Java methods: enrol a student in a course, read, drop, delete.
package framework.clients;

import io.restassured.response.Response;

import java.util.Map;

// --- keep
// Only Campus Lab has enrollments; the Students app has no such endpoint.
public class EnrollmentsApi {

    private static final String ENROLLMENTS = "/api/v1/enrollments";

    // ---
    // Enrolling is a POST of two ids. Campus Lab answers 201 with the enrollment (status "enrolled"),
    // 409 for a full course or a duplicate, 422 for missing prerequisites or an inactive student.


    // ---
    // A student's enrollments live under the student: GET /api/v1/students/{id}/enrollments -> {"data": [...]}.


    // ---
    // Dropping keeps the record with status "dropped" (history). DELETE follows the same rules and answers 204:
    // after the course's drop deadline, or for an enrollment already dropped, both answer 409.

    // --- keep
    // end of the class
}
