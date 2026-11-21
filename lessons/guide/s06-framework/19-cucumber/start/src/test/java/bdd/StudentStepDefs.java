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


    // ---
    // The same client call and the same cleanup registration as in the TestNG tests.


    // ---
    // The email steps: PATCH, then read back.


    // ---
    // The conflict steps. On the Students app the known defect (500 instead of 409) skips the scenario.


    // ---
    // A Cucumber hook: after every scenario, pass or fail, delete what it created.

    // --- keep
    // end of the class
}
