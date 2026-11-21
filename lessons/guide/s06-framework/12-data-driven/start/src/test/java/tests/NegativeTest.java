// Negative tests on both apps: missing ids, invalid bodies, duplicate emails. The Students app's 500s are found defects.
// Run:  mvn test -Dtest=NegativeTest -Denv=campus   |  -Denv=docker (defects skip)  |  -Denv=docker -Ddefects=fail
package tests;

import framework.config.App;
import framework.data.StudentData;
import framework.models.Student;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static framework.defects.KnownDefects.known;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

// --- keep
// Every test asserts what a correct API answers. Campus Lab does; the Students app has two known defects.
@Test(groups = "functional")
public class NegativeTest extends BaseTest {

    // --- keep
    // 404 for an id that does not exist. Both apps agree on the status; the bodies differ:
    // the Students app says {"error":"Not Found!"}, Campus Lab sends problem+json with a type URI.
    public void unknownStudentIs404() {
        Response response = students.get(999_999);
        response.then().statusCode(404);
        if (config.app() == App.CAMPUS) {
            response.then().contentType("application/problem+json").body("type", endsWith("/not-found"));
        } else {
            response.then().body("error", equalTo("Not Found!"));
        }
    }

    // --- keep
    // 400 for an invalid email. The Students app answers 500 (known defect STUDENTS-1): a client error
    // reported as a server error. Campus Lab answers 400 and names the field.
    public void invalidEmailIs400() {
        Response response = students.create(StudentData.randomStudent().withEmail("not-an-email"));
        known(App.STUDENTS, "STUDENTS-1", "validation errors answer 500 instead of 400", response, 400);
        response.then().statusCode(400).body("errors.field", hasItem("email"));
    }

    // --- keep
    // An empty body: every required field is missing. Same defect on the Students app.
    public void emptyBodyIs400() {
        Response response = students.create(Map.of());
        known(App.STUDENTS, "STUDENTS-1", "validation errors answer 500 instead of 400", response, 400);
        response.then().statusCode(400).body("errors.field", hasItem("firstName"));
    }

    // --- keep
    // 409 for a second student with the same email. The first is created (and cleaned up) normally;
    // the second must be a conflict. The Students app answers 500 (known defect STUDENTS-2).
    public void duplicateEmailIs409() {
        Student first = StudentData.randomStudent();
        createStudent(first);
        Response response = students.create(first);
        known(App.STUDENTS, "STUDENTS-2", "a duplicate email answers 500 instead of 409", response, 409);
        response.then().statusCode(409).body("type", endsWith("/duplicate-email"));
    }
    // --- keep
    // end of the class
}
