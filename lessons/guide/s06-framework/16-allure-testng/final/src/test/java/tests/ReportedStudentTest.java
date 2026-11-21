// A test written for the report: labels (feature, story, severity), a description, steps, and a step made inline.
// Run:  ./allure-report.sh -Denv=campus   then open target/site/allure-maven-plugin/index.html
package tests;

import framework.data.StudentData;
import framework.models.Student;
import framework.steps.StudentSteps;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

// --- keep
// The labels group tests in the report's Behaviors tab: Feature -> Story -> test.
@Feature("Students")
@Test(groups = {"smoke", "functional"})
public class ReportedStudentTest extends BaseTest {

    private final StudentSteps steps = new StudentSteps();

    // ---
    // Read top to bottom, the report shows: Create student ..., Read student ..., Change the email ...,
    // then the inline step "the new email is stored", each with its HTTP attachments.
    @Story("A student changes their email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Create a random student, change the email with PATCH, read it back.")
    public void studentChangesEmail() {
        Student sent = StudentData.randomStudent();
        int id = steps.create(sent);
        assertEquals(steps.read(id), sent.withId(id));
        String newEmail = StudentData.uniqueEmail(sent.firstName(), sent.lastName());
        steps.changeEmail(id, newEmail);
        Allure.step("the new email is stored", () -> assertEquals(steps.read(id).email(), newEmail));
    }
    // --- keep
    // end of the class
}
