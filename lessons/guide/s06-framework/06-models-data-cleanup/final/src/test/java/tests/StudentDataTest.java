// Records, a builder and Datafaker: create random students, read them back as records, compare whole objects.
// Run:  mvn test -Dtest=StudentDataTest -Denv=docker   (Campus Lab: add -Dsandbox.reset=true to start clean)
package tests;

import framework.data.StudentData;
import framework.models.Student;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

// --- keep
// Extends BaseTest: the clients, the cleanup after each test and the suite banner come from there.
public class StudentDataTest extends BaseTest {

    // ---
    // Three random students. Each is created, read back as a record, and compared as ONE object:
    // records compare by value, so the student we sent plus its new id must equal what the server stores.
    // No delete here: createStudent registered the cleanup.
    @Test
    public void randomStudentsRoundTrip() {
        for (int i = 0; i < 3; i++) {
            Student sent = StudentData.randomStudent();
            int id = createStudent(sent);
            Student stored = students.fetch(id);
            System.out.println("stored " + stored);
            assertEquals(stored, sent.withId(id));
        }
    }

    // ---
    // The builder: only what this test cares about is set, everything else keeps its default.
    @Test
    public void builderSetsOnlyWhatMatters() {
        Student physicist = Student.builder()
                .firstName("Marie").lastName("Curie").programme("Physics")
                .email("marie." + System.nanoTime() + "@example.com")
                .build();
        int id = createStudent(physicist);
        assertEquals(students.fetch(id).programme(), "Physics");
    }
    // --- keep
    // end of the class
}
