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


    // ---
    // DESERIALIZE: extract().as(Student.class) turns the JSON answer into a Student.
    // From here on it is plain Java: getters and normal assertions.


    // ---
    // The same answer as a record. Records print themselves, so we can see every field at once.


    // ---
    // A whole list: Java erases generic types, so List<StudentRecord>.class does not exist.
    // TypeRef keeps the full type, and every element becomes a record.

    // --- keep
    // end of the class
}
