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


    // ---
    // The whole body as a generic Page<Student>. as(Page.class) would lose the <Student>:
    // Java erases it, and Jackson would fill the list with Maps. TypeRef keeps the full type.


    // ---
    // TypeRef also works for plain collections: here every student as a Map, the shape before we
    // write a class. Handy to explore; the records above are what the tests should keep.


    // ---
    // A nested object into a classic POJO. as(Transcript.class) fills the nested student and course list,
    // and the checks read like the domain: every completed course has a grade, credits add up.

    // --- keep
    // end of the class
}
