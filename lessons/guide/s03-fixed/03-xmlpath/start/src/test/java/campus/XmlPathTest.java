// The same student data as XML: send XML, ask for XML with Accept, and read it with XmlPath and GPath.
// Run:  mvn test -Dtest=XmlPathTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.http.ContentType;
import io.restassured.path.xml.XmlPath;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// --- keep
// Campus Lab's /student routes answer JSON by default and XML when the request says Accept: application/xml.
// The tests create one student in XML, read it back as XML, and search the whole list.
public class XmlPathTest {

    static String email = "grace." + System.currentTimeMillis() + "@example.edu";
    static int id;

    // ---
    // The request body is XML too: a <Student> element, and the list of courses repeats <courses>.
    // accept(ContentType.XML) asks for XML back. The paths start at the root element: Student.id.
    // Every XML value is text: path() would give the String "105", xmlPath().getInt() converts it.


    // ---
    // Reading XML looks like reading JSON: element names joined by dots.
    // Student.courses.courses is every <courses> inside <courses>: hasItems matches their texts,
    // and size() counts the elements (hasSize would see one node whose text is "CompilersCalculus").


    // ---
    // The list is <List> with one <item> per student. GPath works on XML too:
    // find { } returns the first match, findAll { } every match, any { } asks if one child matches.
    // XML values are text, so we compare with strings. Only our student takes Compilers: exactly one match.


    // ---
    // XmlPath also works on its own, on any XML string. setRootPath shortens every path,
    // getList returns typed values, and ** searches the whole tree at any depth.


    // --- keep
    // Clean up: delete the student we created so the shared sandbox stays as it was.
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (id > 0) {
            given().spec(CampusLab.spec()).pathParam("id", id).when().delete("/student/{id}").then().statusCode(204);
        }
    }
}
