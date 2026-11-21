// Enrol a new student in a course, see it on the student, drop it. Campus Lab only.
// Run:  mvn test -Dtest=EnrollmentStepsTest -Denv=campus
package tests;

import framework.clients.EnrollmentsApi;
import framework.clients.StudentsApi;
import framework.config.App;
import framework.config.TestConfig;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

// ---
// Functional, and Campus Lab only (the skip below still decides that).

// --- keep
// Two clients working together: StudentsApi makes the student, EnrollmentsApi enrols it.
public class EnrollmentStepsTest {

    private static final int CS101 = 1;   // Introduction to Programming: no prerequisites, seats free

    private final StudentsApi students = new StudentsApi();
    private final EnrollmentsApi enrollments = new EnrollmentsApi();

    // ---
    // alwaysRun = true: configuration methods are not in any group, so without it a -Dgroups run
    // would never call this check, and the test would run (and fail) on the Students app.


    // --- keep
    // A fresh student enrols in CS101: 201 and status "enrolled", and the student's list shows the course.
    @Test
    public void enrolAndDrop() {
        String email = "grace." + System.nanoTime() + "@example.com";
        int studentId = students.createAndGetId(Map.of("firstName", "Grace", "lastName", "Hopper",
                "email", email, "programme", "Computer Science", "courses", List.of()), email);

        int enrollmentId = enrollments.enroll(studentId, CS101)
                .then().statusCode(201).body("status", equalTo("enrolled"))
                .extract().path("id");
        enrollments.forStudent(studentId).then().statusCode(200).body("data.courseId", hasItem(CS101));

        // --- keep
        // Dropping keeps the record and marks it "dropped"; dropping it again is a conflict (409).
        // Then we clean up the student we made.
        enrollments.drop(enrollmentId).then().statusCode(200).body("status", equalTo("dropped"));
        enrollments.drop(enrollmentId).then().statusCode(409);
        students.delete(studentId).then().statusCode(204);
    }
    // --- keep
    // end of the class
}
