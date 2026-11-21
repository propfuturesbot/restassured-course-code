// A custom AssertJ assertion for Student: checks read like the domain, and failures say what was wrong.
package framework.assertions;

import framework.models.Student;
import org.assertj.core.api.AbstractAssert;

// --- keep
// AbstractAssert<SELF, ACTUAL>: SELF lets every check return this, so checks chain.
public class StudentAssert extends AbstractAssert<StudentAssert, Student> {

    private StudentAssert(Student actual) {
        super(actual, StudentAssert.class);
    }

    // ---
    // The entry point, used like AssertJ's own: StudentAssert.assertThat(student).hasEmail(...)


    // ---
    // Each check: isNotNull() first, then compare, and fail with a message written for humans.

    // --- keep
    // end of the class
}
