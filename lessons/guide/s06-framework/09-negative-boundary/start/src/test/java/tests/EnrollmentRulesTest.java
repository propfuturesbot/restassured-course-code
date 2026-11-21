// The business rules of enrolling, as negative tests: 422 for prerequisites and inactive students, 409 for duplicates and full courses.
// Run:  mvn test -Dtest=EnrollmentRulesTest -Denv=campus
package tests;

import framework.config.App;
import framework.data.StudentData;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

// --- keep
// Seed data facts (identical in every Campus Lab sandbox): CS101 id 1 has no prerequisites, CS102 id 2 needs CS101,
// CS201 id 3 needs CS102, CS499 id 9 needs CS201 and is full (3 of 3 seats), student 25 is suspended.
@Test(groups = "functional")
public class EnrollmentRulesTest extends BaseTest {

    private static final int CS101 = 1, CS102 = 2, CS201 = 3, CS499 = 9, SUSPENDED_STUDENT = 25;

    @BeforeClass(alwaysRun = true)
    public void campusOnly() {
        if (config.app() != App.CAMPUS) throw new SkipException("Enrollments exist only on Campus Lab");
    }

    // ---
    // 422 Unprocessable: the request is well formed, the rule says no. The body lists what is missing.


    // ---
    // 422 for a suspended student: only active students can enrol.


    // ---
    // 409 Conflict: the same student in the same course twice. The body points at the existing enrollment.


    // ---
    // 409 for a full course, the capacity boundary: CS499 has 3 seats and 3 students.
    // To reach that rule the student must first PASS the chain CS101 -> CS102 -> CS201 (enrol + grade each),
    // otherwise the prerequisite rule (422) answers first.

    // --- keep
    // end of the class
}
