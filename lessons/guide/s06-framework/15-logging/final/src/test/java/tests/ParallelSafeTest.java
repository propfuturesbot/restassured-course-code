// The same test written for parallel runs: everything it needs is a local variable or per-thread framework state.
// Run:  mvn test -Dtest=ParallelSafeTest   and the whole suite with -Dsuite=suites/parallel-methods.xml
package tests;

import framework.data.StudentData;
import framework.models.Student;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

// --- keep
// No fields at all: the id and the student live on this thread's stack, so threads cannot see each other's.
@Test(groups = "functional")
public class ParallelSafeTest extends BaseTest {

    // --- keep
    // Locals instead of fields; unique data from StudentData; cleanup is per thread (Cleanup is a ThreadLocal).
    // Six runs on three threads, every one reads back exactly the student it made.
    @Test(invocationCount = 6, threadPoolSize = 3)
    public void localsDoNotRace() throws InterruptedException {
        Student mine = StudentData.randomStudent();
        int id = createStudent(mine);
        Thread.sleep(50);
        assertEquals(students.fetch(id), mine.withId(id));
    }
    // --- keep
    // end of the class
}
