// The glue between the Gherkin sentences and the framework: each method is one step, and it only calls existing clients.
package bdd;

import framework.clients.StudentsApi;
import framework.config.App;
import framework.data.Cleanup;
import framework.data.StudentData;
import framework.models.Student;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static framework.defects.KnownDefects.known;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;

// --- keep
// Cucumber creates a NEW instance of this class for every scenario, so these fields are per scenario:
// they carry the student from the Given to the Then, and never leak into the next scenario.
public class StudentStepDefs {

    private final StudentsApi students = new StudentsApi();
    private Student student;
    private int id;
    private String newEmail;
    private Response lastResponse;

    // ---
    // {string} captures the quoted text of the sentence: "Physics" becomes the programme parameter.
    @Given("a new student in {string}")
    public void aNewStudentIn(String programme) {
        Student random = StudentData.randomStudent();
        student = new Student(null, random.firstName(), random.lastName(), random.email(), programme, random.courses());
    }

    // ---
    // The same client call and the same cleanup registration as in the TestNG tests.
    @When("the student is created")
    public void theStudentIsCreated() {
        id = students.createAndGetId(student);
        Cleanup.register(() -> students.delete(id));
    }

    @Then("reading the student returns the same data")
    public void readingReturnsTheSameData() {
        assertThat(students.fetch(id)).isEqualTo(student.withId(id));
    }

    @Then("the student is listed in {string}")
    public void theStudentIsListedIn(String programme) {
        students.listByProgramme(programme).then().statusCode(200).body("email", hasItem(student.email()));
    }

    // ---
    // The email steps: PATCH, then read back.
    @When("the student's email is changed")
    public void theEmailIsChanged() {
        newEmail = StudentData.uniqueEmail(student.firstName(), student.lastName());
        students.patchEmail(id, newEmail).then().statusCode(200);
    }

    @Then("reading the student shows the new email")
    public void readingShowsTheNewEmail() {
        assertThat(students.fetch(id).email()).isEqualTo(newEmail);
    }

    // ---
    // The conflict steps. On the Students app the known defect (500 instead of 409) skips the scenario.
    @When("another student with the same email is created")
    public void anotherWithTheSameEmail() {
        lastResponse = students.create(student);
    }

    @Then("the answer is 409 Conflict")
    public void theAnswerIs409() {
        known(App.STUDENTS, "STUDENTS-2", "a duplicate email answers 500 instead of 409", lastResponse, 409);
        lastResponse.then().statusCode(409);
    }

    // ---
    // A Cucumber hook: after every scenario, pass or fail, delete what it created.
    @After
    public void cleanUp() {
        Cleanup.runAll();
    }
    // --- keep
    // end of the class
}
