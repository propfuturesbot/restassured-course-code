// Boundary values for one rule: Campus Lab accepts first names of 2 to 60 characters. Test both sides of both edges.
// Run:  mvn test -Dtest=BoundaryTest -Denv=campus
package tests;

import framework.config.App;
import framework.data.StudentData;
import framework.models.Student;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;

// --- keep
// The Students app has no length limit (1 and 61 characters were both accepted when we tried), so this is Campus only.
@Test(groups = "functional")
public class BoundaryTest extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void campusOnly() {
        if (config.app() != App.CAMPUS) throw new SkipException("The Students app has no length rules to test");
    }

    // ---
    // Boundary value analysis: the last invalid and first valid value at each edge, nothing in between.
    // A @DataProvider returns rows; TestNG runs the test once per row.
    @DataProvider
    public Object[][] firstNameLengths() {
        return new Object[][]{{1, 400}, {2, 201}, {60, 201}, {61, 400}};
    }

    // ---
    // One test, four runs. A valid name is created (and cleaned up); an invalid one must name the field.
    @Test(dataProvider = "firstNameLengths")
    public void firstNameLength(int length, int expectedStatus) {
        Student student = StudentData.randomStudent();
        Student withName = new Student(null, "a".repeat(length), student.lastName(), student.email(),
                student.programme(), student.courses());
        Response response = students.create(withName);
        if (response.statusCode() == 201) {
            int id = response.path("id");
            framework.data.Cleanup.register(() -> students.delete(id));
        }
        assertEquals(response.statusCode(), expectedStatus, "firstName of " + length + " characters");
        if (expectedStatus == 400) response.then().body("errors.field", hasItem("firstName"));
    }
    // --- keep
    // end of the class
}
