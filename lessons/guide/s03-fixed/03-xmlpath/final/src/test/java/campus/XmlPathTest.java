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
    @Test
    public void createStudentInXml() {
        String xml = "<Student><firstName>Grace</firstName><lastName>Hopper</lastName>"
                + "<email>" + email + "</email><programme>Computer Science</programme>"
                + "<courses><courses>Compilers</courses><courses>Calculus</courses></courses></Student>";
        id = given().spec(CampusLab.spec()).contentType(ContentType.XML).accept(ContentType.XML).body(xml)
                .when().post("/student")
                .then().statusCode(201).contentType(ContentType.XML)
                .body("Student.firstName", equalTo("Grace"))
                .extract().xmlPath().getInt("Student.id");
    }

    // ---
    // Reading XML looks like reading JSON: element names joined by dots.
    // Student.courses.courses is every <courses> inside <courses>: hasItems matches their texts,
    // and size() counts the elements (hasSize would see one node whose text is "CompilersCalculus").
    @Test(dependsOnMethods = "createStudentInXml")
    public void readStudentAsXml() {
        given().spec(CampusLab.spec()).accept(ContentType.XML).pathParam("id", id)
                .when().get("/student/{id}")
                .then().statusCode(200)
                .body("Student.lastName", equalTo("Hopper"))
                .body("Student.courses.courses", hasItems("Compilers", "Calculus"))
                .body("Student.courses.courses.size()", equalTo(2));
    }

    // ---
    // The list is <List> with one <item> per student. GPath works on XML too:
    // find { } returns the first match, findAll { } every match, any { } asks if one child matches.
    // XML values are text, so we compare with strings. Only our student takes Compilers: exactly one match.
    @Test(dependsOnMethods = "createStudentInXml")
    public void searchTheListWithGPath() {
        given().spec(CampusLab.spec()).accept(ContentType.XML).queryParam("programme", "Computer Science")
                .when().get("/student/list")
                .then().statusCode(200)
                .body("List.item.programme", everyItem(equalTo("Computer Science")))
                .body("List.item.find { it.email == '" + email + "' }.lastName", equalTo("Hopper"))
                .body("List.item.findAll { it.courses.courses.any { c -> c.text() == 'Compilers' } }.size()", equalTo(1));
    }

    // ---
    // XmlPath also works on its own, on any XML string. setRootPath shortens every path,
    // getList returns typed values, and ** searches the whole tree at any depth.
    @Test(dependsOnMethods = "createStudentInXml")
    public void xmlPathOnItsOwn() {
        String xml = given().spec(CampusLab.spec()).accept(ContentType.XML).queryParam("limit", 10)
                .when().get("/student/list").asString();
        XmlPath xmlPath = new XmlPath(xml).setRootPath("List.item");
        List<Integer> ids = xmlPath.getList("id", Integer.class);
        int calculus = xmlPath.getInt("**.findAll { it.name() == 'courses' && it.text() == 'Calculus' }.size()");
        System.out.println("ids " + ids + ", Calculus taken " + calculus + " times in the first 10");
        assertEquals(ids.size(), 10);
        assertTrue(ids.get(0) < ids.get(9), "the list is sorted by id");
    }

    // --- keep
    // Clean up: delete the student we created so the shared sandbox stays as it was.
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (id > 0) {
            given().spec(CampusLab.spec()).pathParam("id", id).when().delete("/student/{id}").then().statusCode(204);
        }
    }
}
