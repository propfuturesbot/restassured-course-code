// A student's whole life written as steps on the client layer. Runs on all three targets.
// Run:  mvn test -Dtest=StudentStepsTest -Denv=docker   (or local, campus)
package tests;

import framework.clients.StudentsApi;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

// --- keep
// The test talks to StudentsApi, never to URLs: if a path changes, only the client changes.
public class StudentStepsTest {

    private final StudentsApi students = new StudentsApi();

    // ---
    // Create a student and get its id, whatever the app behind the URL.


        // ---
        // Read it back, change the email, then delete it: each line is one step of the story.


        // ---
        // Delete answers 204 on both apps; afterwards the student is gone (404).

    // --- keep
    // end of the class
}
