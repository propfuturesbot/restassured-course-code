// A deliberate failure, to SEE what soft assertions report. Skipped unless you ask for it with -Ddemo=true.
// Run:  mvn test -Dtest=SoftAssertionsDemoTest -Ddemo=true   (meant to fail)
package tests;

import framework.models.Student;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.Test;

// --- keep
// Three wrong expectations about student 1. A hard assertion would stop at the first one.
@Test(groups = "demo")
public class SoftAssertionsDemoTest extends BaseTest {

    @Test
    public void threeWrongExpectations() {
        if (!Boolean.getBoolean("demo")) throw new SkipException("demo only: run with -Ddemo=true");
        Student first = students.fetch(1);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(first.firstName()).as("firstName").isEqualTo("Nobody");
        softly.assertThat(first.email()).as("email").endsWith("@nowhere.test");
        softly.assertThat(first.courses()).as("courses").hasSize(99);
        softly.assertAll();
    }
}
