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

// --- keep
// Two clients working together: StudentsApi makes the student, EnrollmentsApi enrols it.
public class EnrollmentStepsTest {

    private static final int CS101 = 1;   // Introduction to Programming: no prerequisites, seats free

    private final StudentsApi students = new StudentsApi();
    private final EnrollmentsApi enrollments = new EnrollmentsApi();

    // --- keep
    // The Students app has no enrollments: on that target the whole class is SKIPPED, not failed.
    @BeforeClass
    public void campusOnly() {
        if (TestConfig.get().app() != App.CAMPUS) {
            throw new SkipException("Enrollments exist only on Campus Lab");
        }
    }

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
