// A student's whole life written as steps on the client layer. Runs on all three targets.
// Run:  mvn test -Dtest=StudentStepsTest -Denv=docker   (or local, campus)
package tests;

import framework.clients.StudentsApi;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

// --- keep
// A test can be in several groups: the lifecycle is a smoke check AND part of the functional suite.
@Test(groups = {"smoke", "functional"})
// --- keep
// The test talks to StudentsApi, never to URLs: if a path changes, only the client changes.
public class StudentStepsTest {

    private final StudentsApi students = new StudentsApi();

    // --- keep
    // Create a student and get its id, whatever the app behind the URL.
    @Test
    public void studentLifecycle() {
        String email = "ada." + System.nanoTime() + "@example.com";
        Map<String, Object> ada = Map.of("firstName", "Ada", "lastName", "Lovelace", "email", email,
                "programme", "Computer Science", "courses", List.of("Java", "Testing"));
        int id = students.createAndGetId(ada, email);

        // --- keep
        // Read it back, change the email, then delete it: each line is one step of the story.
        students.get(id).then().statusCode(200).body("email", equalTo(email));
        String newEmail = "ada.king." + System.nanoTime() + "@example.com";
        students.patchEmail(id, newEmail).then().statusCode(200);
        students.get(id).then().statusCode(200).body("email", equalTo(newEmail));

        // --- keep
        // Delete answers 204 on both apps; afterwards the student is gone (404).
        students.delete(id).then().statusCode(204);
        students.get(id).then().statusCode(404);
    }
    // --- keep
    // end of the class
}
