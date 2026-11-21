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


    // ---
    // CreateStudent: SOAP 1.1 wants Content-Type text/xml and a SOAPAction header (the WSDL gives its value).
    // The answer is an envelope too; the new student is inside CreateStudentResponse.


    // ---
    // GetStudent by id, then ListStudents with a filter. A root path keeps the long envelope paths short.


    // ---
    // A SOAP error is not a 404: it is HTTP 500 with a soap:Fault in the Body.
    // faultcode says whose mistake it is (soap:Client = ours), detail carries the service's own error code.


    // --- keep
    // Clean up through the REST API: the same student, the same sandbox.
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (id > 0) {
            given().spec(CampusLab.spec()).when().delete("/api/v1/students/" + id).then().statusCode(204);
        }
    }
}
