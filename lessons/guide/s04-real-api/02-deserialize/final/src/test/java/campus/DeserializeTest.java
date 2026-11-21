// Turn big responses into Java objects instead of digging through them path by path.
// Run:  mvn test -Dtest=DeserializeTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import campus.model.Page;
import campus.model.Student;
import campus.model.Transcript;
import io.restassured.common.mapper.TypeRef;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

// --- keep
// One page of 100 students is about 35 KB of JSON. Paths are fine for one value;
// for questions about the whole response, typed objects are shorter, safer and checked by the compiler.
public class DeserializeTest {

    // ---
    // A list inside the response: jsonPath().getList("data", Student.class) maps every item to a record.
    // From here it is plain Java: the compiler knows gpa is a Double and programme a String.
    @Test
    public void listOfRecords() {
        List<Student> students = given().spec(CampusLab.spec()).queryParam("limit", 100)
                .when().get("/api/v1/students")
                .then().statusCode(200)
                .extract().jsonPath().getList("data", Student.class);
        Student best = students.stream().filter(s -> s.gpa() != null)
                .max(Comparator.comparing(Student::gpa)).orElseThrow();
        System.out.println(students.size() + " students, best GPA: " + best.firstName() + " " + best.lastName() + " " + best.gpa());
        assertEquals(students.size(), 100);
    }

    // ---
    // The whole body as a generic Page<Student>. as(Page.class) would lose the <Student>:
    // Java erases it, and Jackson would fill the list with Maps. TypeRef keeps the full type.
    @Test
    public void genericPageWithTypeRef() {
        Page<Student> page = given().spec(CampusLab.spec()).queryParam("limit", 100)
                .when().get("/api/v1/students")
                .as(new TypeRef<Page<Student>>() {});
        Map<String, Long> perProgramme = page.data().stream()
                .collect(Collectors.groupingBy(Student::programme, Collectors.counting()));
        System.out.println("total " + page.meta().total() + ", per programme " + perProgramme);
        assertEquals(page.meta().count(), page.data().size());
        assertTrue(page.data().get(0) instanceof Student, "items are real Student records");
    }

    // ---
    // TypeRef also works for plain collections: here every student as a Map, the shape before we
    // write a class. Handy to explore; the records above are what the tests should keep.
    @Test
    public void mapsWithTypeRef() {
        List<Map<String, Object>> raw = given().spec(CampusLab.spec()).queryParam("limit", 3)
                .when().get("/api/v1/students")
                .then().extract().jsonPath().getObject("data", new TypeRef<List<Map<String, Object>>>() {});
        System.out.println("fields of one student: " + raw.get(0).keySet());
        assertEquals(raw.size(), 3);
    }

    // ---
    // A nested object into a classic POJO. as(Transcript.class) fills the nested student and course list,
    // and the checks read like the domain: every completed course has a grade, credits add up.
    @Test
    public void nestedPojo() {
        Transcript t = given().spec(CampusLab.spec())
                .when().get("/api/v1/students/{id}/transcript", 4)
                .as(Transcript.class);
        int earned = t.getCourses().stream().filter(c -> "completed".equals(c.status()))
                .mapToInt(Transcript.CourseResult::credits).sum();
        System.out.println(t.getStudent().name() + ": " + t.getCourses().size() + " courses, GPA " + t.getGpa());
        t.getCourses().stream().filter(c -> "completed".equals(c.status())).forEach(c -> assertNotNull(c.grade(), c.code()));
        assertEquals(t.getCreditsEarned(), earned);
    }
    // --- keep
    // end of the class
}
