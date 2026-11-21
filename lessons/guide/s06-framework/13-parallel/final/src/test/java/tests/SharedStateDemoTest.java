// Why parallel tests must not share data: six runs on three threads share ONE field, and read each other's student.
// Run:  mvn test -Dtest=SharedStateDemoTest -Ddemo=true   (meant to fail)
package tests;

import framework.data.StudentData;
import framework.models.Student;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

// --- keep
// The classic mistake, the way most sequential test classes are written: state kept in a field.
@Test(groups = "demo")
public class SharedStateDemoTest extends BaseTest {

    // ---
    // One field for all runs. Each run creates a student, stores its id and email here, waits a moment
    // (any network call does), then reads "its" student back. Another thread may have replaced the field.
    private int id;
    private String email;

    @Test(invocationCount = 6, threadPoolSize = 3)
    public void sharedFieldsRace() throws InterruptedException {
        if (!Boolean.getBoolean("demo")) throw new SkipException("demo only: run with -Ddemo=true");
        Student mine = StudentData.randomStudent();
        id = createStudent(mine);
        email = mine.email();
        Thread.sleep(300);
        assertEquals(students.fetch(id).email(), mine.email(), "read another thread's student");
    }
    // --- keep
    // end of the class
}
