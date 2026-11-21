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

    // --- keep
    // CSV -> rows. Each row becomes ONE parameter (the row map), so adding a column never changes the signature.
    @DataProvider
    public Object[][] csvStudents() {
        return DataFiles.csv("data/new-students.csv").stream().map(row -> new Object[]{row}).toArray(Object[][]::new);
    }

    // --- keep
    // One run per CSV row: create the student, then the programme filter must list it.
    @Test(dataProvider = "csvStudents")
    public void createFromCsv(Map<String, String> row) {
        Student student = new Student(null, row.get("firstName"), row.get("lastName"),
                StudentData.uniqueEmail(row.get("firstName"), row.get("lastName")), row.get("programme"),
                List.of(row.get("course")));
        createStudent(student);
        students.listByProgramme(row.get("programme")).then().statusCode(200).body("email", hasItem(student.email()));
    }

    // --- keep
    // JSON -> negative cases: each item is a whole invalid body plus the field that is wrong.
    // Campus Lab answers 400 and names the field; the Students app hits its known 500 defect.
    @DataProvider
    public Object[][] invalidStudents() {
        return DataFiles.json("data/invalid-students.json").stream().map(c -> new Object[]{c.get("name"), c})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "invalidStudents")
    public void invalidBodyIsRejected(String name, Map<String, Object> testCase) {
        Response response = students.create(testCase.get("student"));
        known(App.STUDENTS, "STUDENTS-1", "validation errors answer 500 instead of 400", response, 400);
        response.then().statusCode(400).body("errors.field", hasItem(testCase.get("field")));
    }

    // --- keep
    // Excel -> students, with a comma-separated courses column. Read back as a record and compared whole.
    @DataProvider
    public Object[][] excelStudents() {
        return DataFiles.excel("data/students.xlsx", "students").stream().map(row -> new Object[]{row})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "excelStudents")
    public void createFromExcel(Map<String, String> row) {
        Student sent = new Student(null, row.get("firstName"), row.get("lastName"),
                StudentData.uniqueEmail(row.get("firstName"), row.get("lastName")), row.get("programme"),
                Arrays.stream(row.get("courses").split(",")).map(String::trim).toList());
        int id = createStudent(sent);
        assertEquals(students.fetch(id), sent.withId(id));
    }
    // --- keep
    // end of the class
}
