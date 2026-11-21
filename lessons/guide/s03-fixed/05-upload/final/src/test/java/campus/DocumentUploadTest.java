// Upload files with multiPart(), meet the 413 and 415 answers, and download a file back to compare its checksum.
// Run:  mvn test -Dtest=DocumentUploadTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;

// --- keep
// Documents belong to a student: POST /api/v1/students/{id}/documents takes multipart/form-data
// with a "file" part (pdf, png, jpg or txt, at most 5 MB) and an optional "description" part.
public class DocumentUploadTest {

    static int studentId;
    static int pngId;
    static String pngSha256;

    // --- keep
    // A student of our own to attach the files to. Deleting it at the end deletes its documents too.
    @BeforeClass
    public void createStudent() {
        studentId = given().spec(CampusLab.spec()).contentType(ContentType.JSON)
                .body(Map.of("firstName", "Rosalind", "lastName", "Franklin",
                        "email", "rosalind." + System.currentTimeMillis() + "@example.edu", "programme", "Chemistry"))
                .when().post("/api/v1/students")
                .then().statusCode(201).extract().path("id");
    }

    // --- keep
    // SHA-256 of some bytes as hex: the server stores the same checksum for every upload.
    static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    // ---
    // multiPart(name, file name, bytes, mime type) builds one part of the form; REST Assured
    // sets Content-Type: multipart/form-data with the boundary for us. A second part carries the description.
    @Test
    public void uploadTextFromBytes() throws Exception {
        byte[] notes = "Lab notes: DNA is a double helix.\n".getBytes(StandardCharsets.UTF_8);
        given().spec(CampusLab.spec())
                .multiPart("file", "lab-notes.txt", notes, "text/plain")
                .multiPart("description", "Lab notes from week 3")
                .when().post("/api/v1/students/{id}/documents", studentId)
                .then().statusCode(201)
                .body("filename", equalTo("lab-notes.txt"))
                .body("contentType", equalTo("text/plain"))
                .body("sizeBytes", equalTo(notes.length))
                .body("sha256", equalTo(sha256(notes)));
    }

    // ---
    // A file on disk: multiPart takes a File. The server checks the extension, the declared type
    // and the real bytes; this one is a real PNG. We keep its id and checksum for the download test.
    @Test
    public void uploadPngFromAFile() throws Exception {
        File png = new File("src/test/resources/campus-badge.png");
        Response created = given().spec(CampusLab.spec())
                .multiPart("file", png, "image/png")
                .when().post("/api/v1/students/{id}/documents", studentId);
        created.then().statusCode(201).body("contentType", equalTo("image/png"))
                .header("Location", containsString("/api/v1/documents/"));
        pngId = created.path("id");
        pngSha256 = sha256(Files.readAllBytes(png.toPath()));
        assertEquals(created.path("sha256"), pngSha256);
    }

    // ---
    // 415 Unsupported Media Type, twice: a file whose name says PNG but whose bytes are text,
    // and an extension the server does not accept. The problem+json lists the types it does accept.
    @Test
    public void wrongTypesGive415() {
        given().spec(CampusLab.spec())
                .multiPart("file", "photo.png", "not really a picture".getBytes(StandardCharsets.UTF_8), "image/png")
                .when().post("/api/v1/students/{id}/documents", studentId)
                .then().statusCode(415).contentType("application/problem+json")
                .body("detail", equalTo("The file content does not look like a .png file."))
                .body("allowed", hasItems("application/pdf", "image/png", "image/jpeg", "text/plain"));
        given().spec(CampusLab.spec())
                .multiPart("file", "setup.exe", new byte[]{77, 90, 0, 0}, "application/octet-stream")
                .when().post("/api/v1/students/{id}/documents", studentId)
                .then().statusCode(415).body("detail", equalTo("Extension '.exe' is not accepted. Use pdf, png, jpg or txt."));
    }

    // ---
    // 413 Payload Too Large: one byte over the 5 MB limit. The problem says how big a file may be.
    @Test
    public void overFiveMegabytesGives413() {
        byte[] big = new byte[5 * 1024 * 1024 + 1];
        java.util.Arrays.fill(big, (byte) 'a');
        given().spec(CampusLab.spec())
                .multiPart("file", "too-big.txt", big, "text/plain")
                .when().post("/api/v1/students/{id}/documents", studentId)
                .then().statusCode(413)
                .body("title", equalTo("Payload Too Large"))
                .body("limitBytes", equalTo(5242880));
    }

    // ---
    // Download: the response body is the file itself. asByteArray() gives the raw bytes, and
    // their SHA-256 must equal the checksum of the file we uploaded: the round trip lost nothing.
    @Test(dependsOnMethods = "uploadPngFromAFile")
    public void downloadAndCompareChecksum() throws Exception {
        Response download = given().spec(CampusLab.spec())
                .when().get("/api/v1/documents/{id}/download", pngId);
        download.then().statusCode(200).contentType("image/png")
                .header("Content-Disposition", containsString("attachment; filename=\"campus-badge.png\""));
        byte[] bytes = download.asByteArray();
        System.out.println("downloaded " + bytes.length + " bytes, sha256 " + sha256(bytes));
        assertEquals(sha256(bytes), pngSha256);
    }

    // --- keep
    // Clean up: deleting the student also deletes its documents (the sandbox allows 20 documents).
    @AfterClass(alwaysRun = true)
    public void deleteStudent() {
        if (studentId > 0) {
            given().spec(CampusLab.spec()).when().delete("/api/v1/students/{id}", studentId).then().statusCode(204);
        }
    }
}
