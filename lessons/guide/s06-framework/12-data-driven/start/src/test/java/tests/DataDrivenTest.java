// Data-driven tests: the same test runs once per row of a CSV file, a JSON file and an Excel sheet.
// Run:  mvn test -Dtest=DataDrivenTest -Denv=docker   (or campus)
package tests;

import framework.config.App;
import framework.data.DataFiles;
import framework.data.StudentData;
import framework.models.Student;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static framework.defects.KnownDefects.known;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;

// --- keep
// A @DataProvider returns Object[][]: one inner array per run, its items become the test's parameters.
@Test(groups = "functional")
public class DataDrivenTest extends BaseTest {

    // ---
    // CSV -> rows. Each row becomes ONE parameter (the row map), so adding a column never changes the signature.


    // ---
    // One run per CSV row: create the student, then the programme filter must list it.


    // ---
    // JSON -> negative cases: each item is a whole invalid body plus the field that is wrong.
    // Campus Lab answers 400 and names the field; the Students app hits its known 500 defect.


    // ---
    // Excel -> students, with a comma-separated courses column. Read back as a record and compared whole.

    // --- keep
    // end of the class
}
