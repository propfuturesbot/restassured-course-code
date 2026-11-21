// Three ways to assert: AssertJ on records, soft assertions that report every failure, and custom matchers.
// Run:  mvn test -Dtest=AssertionsTest -Denv=docker
package tests;

import framework.data.StudentData;
import framework.models.Student;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.List;

import static framework.assertions.ApiMatchers.validEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;

// --- keep
// Functional suite.
@Test(groups = "functional")
// --- keep
// Hamcrest stays for body(...) checks inside REST Assured; AssertJ is for Java objects after extract().
public class AssertionsTest extends BaseTest {

    // --- keep
    // Read the whole list INTO records, then assert on Java objects with AssertJ.
    // extracting(Student::id) pulls one field out of every element: ids must be unique.
    @Test
    public void listAsRecords() {
        List<Student> all = List.of(students.list().then().statusCode(200).extract().as(Student[].class));
        assertThat(all).isNotEmpty()
                .allSatisfy(s -> assertThat(s.firstName()).isNotBlank())
                .extracting(Student::id).doesNotHaveDuplicates();
    }

    // --- keep
    // Soft assertions: every check runs, and assertAll() reports ALL failures at once, not just the first.
    // Use them when one response has many fields to verify.
    @Test
    public void softlyCheckEveryField() {
        Student sent = StudentData.randomStudent();
        Student stored = students.fetch(createStudent(sent));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(stored.firstName()).isEqualTo(sent.firstName());
        softly.assertThat(stored.lastName()).isEqualTo(sent.lastName());
        softly.assertThat(stored.email()).isEqualTo(sent.email());
        softly.assertThat(stored.courses()).containsExactlyElementsOf(sent.courses());
        softly.assertAll();
    }

    // --- keep
    // The custom assertion reads like the domain: a student has an email, is in a programme, takes a course.
    @Test
    public void customStudentAssert() {
        Student sent = StudentData.randomStudent();
        Student stored = students.fetch(createStudent(sent));
        framework.assertions.StudentAssert.assertThat(stored)
                .hasEmail(sent.email())
                .isInProgramme(sent.programme())
                .takesCourse("Testing");
    }

    // --- keep
    // The custom Hamcrest matcher inside REST Assured: every email in the list must be well formed.
    @Test
    public void everyEmailIsWellFormed() {
        students.list().then().statusCode(200).body("email", everyItem(validEmail()));
    }
    // --- keep
    // end of the class
}
