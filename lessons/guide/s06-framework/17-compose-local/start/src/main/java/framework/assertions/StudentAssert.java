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

    // --- keep
    // The entry point, used like AssertJ's own: StudentAssert.assertThat(student).hasEmail(...)
    public static StudentAssert assertThat(Student actual) {
        return new StudentAssert(actual);
    }

    // --- keep
    // Each check: isNotNull() first, then compare, and fail with a message written for humans.
    public StudentAssert hasEmail(String email) {
        isNotNull();
        if (!email.equals(actual.email())) {
            failWithMessage("Expected student %s to have email <%s> but was <%s>", actual.id(), email, actual.email());
        }
        return this;
    }

    public StudentAssert isInProgramme(String programme) {
        isNotNull();
        if (!programme.equals(actual.programme())) {
            failWithMessage("Expected student %s in programme <%s> but was in <%s>", actual.id(), programme,
                    actual.programme());
        }
        return this;
    }

    public StudentAssert takesCourse(String course) {
        isNotNull();
        if (actual.courses() == null || !actual.courses().contains(course)) {
            failWithMessage("Expected student %s to take <%s> but the courses were %s", actual.id(), course,
                    actual.courses());
        }
        return this;
    }
    // --- keep
    // end of the class
}
