// SOAP with REST Assured: read the WSDL, POST envelopes to Campus Lab's StudentService, and check a fault.
// Run:  mvn test -Dtest=StudentServiceSoapTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.http.ContentType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;

// --- keep
// SOAP is XML over HTTP POST: one URL, the operation is the first element inside soap:Body.
// The service lives at /soap/StudentService and takes the same API key as the REST API.
public class StudentServiceSoapTest {

    static final String SERVICE = "/soap/StudentService";
    static final String NS = "urn:campuslab:students:v1";
    static String email = "alan." + System.currentTimeMillis() + "@example.edu";
    static int id;

    // --- keep
    // Every request is the same envelope; only the operation inside the Body changes.
    static String envelope(String operation) {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tns=\"" + NS + "\">"
                + "<soap:Body>" + operation + "</soap:Body></soap:Envelope>";
    }

    // ---
    // The WSDL is the contract: which operations exist, what they take, and where to send them.
    // It is XML, so XmlPath reads it. @name reads an attribute; namespace prefixes are left out of the path.
    @Test
    public void readTheWsdl() {
        given().spec(CampusLab.spec()).queryParam("wsdl", "")
                .when().get(SERVICE)
                .then().statusCode(200).contentType("text/xml")
                .body("definitions.@name", equalTo("StudentService"))
                .body("definitions.portType.operation.@name", hasItems("GetStudent", "ListStudents", "CreateStudent"))
                .body("definitions.service.port.address.@location", endsWith(SERVICE));
    }

    // ---
    // CreateStudent: SOAP 1.1 wants Content-Type text/xml and a SOAPAction header (the WSDL gives its value).
    // The answer is an envelope too; the new student is inside CreateStudentResponse.
    @Test
    public void createStudent() {
        String body = envelope("<tns:CreateStudent><tns:student><tns:firstName>Alan</tns:firstName>"
                + "<tns:lastName>Turing</tns:lastName><tns:email>" + email + "</tns:email>"
                + "<tns:programme>Mathematics</tns:programme><tns:year>2</tns:year></tns:student></tns:CreateStudent>");
        id = given().spec(CampusLab.spec()).contentType("text/xml; charset=utf-8")
                .header("SOAPAction", NS + "/CreateStudent").body(body)
                .when().post(SERVICE)
                .then().statusCode(200)
                .body("Envelope.Body.CreateStudentResponse.student.lastName", equalTo("Turing"))
                .extract().xmlPath().getInt("Envelope.Body.CreateStudentResponse.student.id");
    }

    // ---
    // GetStudent by id, then ListStudents with a filter. A root path keeps the long envelope paths short.
    @Test(dependsOnMethods = "createStudent")
    public void getAndListStudents() {
        given().spec(CampusLab.spec()).contentType("text/xml; charset=utf-8").header("SOAPAction", NS + "/GetStudent")
                .body(envelope("<tns:GetStudent><tns:id>" + id + "</tns:id></tns:GetStudent>"))
                .when().post(SERVICE)
                .then().statusCode(200)
                .rootPath("Envelope.Body.GetStudentResponse.student")
                .body("email", equalTo(email)).body("year", equalTo("2")).body("status", equalTo("active"));

        given().spec(CampusLab.spec()).contentType("text/xml; charset=utf-8").header("SOAPAction", NS + "/ListStudents")
                .body(envelope("<tns:ListStudents><tns:programme>Mathematics</tns:programme><tns:limit>5</tns:limit></tns:ListStudents>"))
                .when().post(SERVICE)
                .then().statusCode(200)
                .rootPath("Envelope.Body.ListStudentsResponse")
                .body("total", equalTo("5"))
                .body("student.programme", everyItem(equalTo("Mathematics")));
    }

    // ---
    // A SOAP error is not a 404: it is HTTP 500 with a soap:Fault in the Body.
    // faultcode says whose mistake it is (soap:Client = ours), detail carries the service's own error code.
    @Test
    public void unknownStudentIsAFault() {
        given().spec(CampusLab.spec()).contentType("text/xml; charset=utf-8").header("SOAPAction", NS + "/GetStudent")
                .body(envelope("<tns:GetStudent><tns:id>99999</tns:id></tns:GetStudent>"))
                .when().post(SERVICE)
                .then().statusCode(500).contentType(ContentType.XML)
                .rootPath("Envelope.Body.Fault")
                .body("faultcode", equalTo("soap:Client"))
                .body("faultstring", equalTo("Student 99999 does not exist in this sandbox."))
                .body("detail.errorCode", equalTo("NOT_FOUND"));
    }

    // --- keep
    // Clean up through the REST API: the same student, the same sandbox.
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (id > 0) {
            given().spec(CampusLab.spec()).when().delete("/api/v1/students/" + id).then().statusCode(204);
        }
    }
}
