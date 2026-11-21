// Send a student as a Java object and read students back as objects, with a POJO and with a record.
// Run:  mvn test -Dtest=StudentObjectsTest   (the app at STUDENTS_BASE_URI, default http://localhost:8080)
package students;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// --- keep
// The base URI comes from an environment variable, as in every lesson.
class StudentObjectsTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // ---
    // SERIALIZE: body() takes the POJO and Jackson writes the JSON for us.
    // No string building, no Map: the compiler checks every field name.
    @Test
    void sendAStudentObject() {
        Student student = new Student("Grace", "Hopper", "grace." + System.currentTimeMillis() + "@example.com",
                "Computer Science", List.of("COBOL", "Compilers"));
        given().contentType(ContentType.JSON).body(student)
                .when().post()
                .then().statusCode(201).body("msg", equalTo("Student added"));
    }

    // ---
    // DESERIALIZE: extract().as(Student.class) turns the JSON answer into a Student.
    // From here on it is plain Java: getters and normal assertions.
    @Test
    void readAStudentAsPojo() {
        Student student = given().pathParam("id", 1)
                .when().get("/{id}")
                .then().statusCode(200)
                .extract().as(Student.class);
        System.out.println(student.getFirstName() + " " + student.getLastName() + " studies " + student.getProgramme());
        assertEquals(1, student.getId());
        assertTrue(student.getEmail().contains("@"));
    }

    // ---
    // The same answer as a record. Records print themselves, so we can see every field at once.
    @Test
    void readAStudentAsRecord() {
        StudentRecord student = given().pathParam("id", 1)
                .when().get("/{id}")
                .then().statusCode(200)
                .extract().as(StudentRecord.class);
        System.out.println(student);
        assertEquals(1, student.id());
    }

    // ---
    // A whole list: Java erases generic types, so List<StudentRecord>.class does not exist.
    // TypeRef keeps the full type, and every element becomes a record.
    @Test
    void readTheListAsRecords() {
        List<StudentRecord> students = given().queryParam("limit", 5)
                .when().get("/list")
                .then().statusCode(200)
                .extract().as(new TypeRef<List<StudentRecord>>() {});
        students.forEach(s -> System.out.println(s.id() + ": " + s.firstName() + " " + s.lastName()));
        assertEquals(5, students.size());
    }
    // --- keep
    // end of the class
}
