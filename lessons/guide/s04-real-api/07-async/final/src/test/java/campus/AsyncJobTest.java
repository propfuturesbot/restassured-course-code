// An async job: start it (202 Accepted), poll it with Awaitility until it is done, then download the result.
// Run:  mvn test -Dtest=AsyncJobTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.testng.Assert.assertEquals;

// --- keep
// POST /api/v1/reports/transcripts does not build the report while we wait: it answers 202 Accepted
// at once with a job to poll. The job is pending (~1 s), then running (2-7 s), then done,
// with the result as CSV, JSON or PDF. Campus Lab allows 20 report jobs per key per day.
public class AsyncJobTest {

    // ---
    // Start the job. 202 means "accepted, not finished". Location says where to poll,
    // Retry-After how long to wait before the first poll.
    @Test
    public void startPollAndDownload() throws Exception {
        Response accepted = given().spec(CampusLab.spec()).contentType(ContentType.JSON).body(Map.of("studentId", 4))
                .when().post("/api/v1/reports/transcripts");
        accepted.then().statusCode(202).body("status", equalTo("pending")).header("Retry-After", "2");
        String job = accepted.header("Location");
        System.out.println("202 Accepted, poll " + job);

        // ---
        // Too early: the result answers 409 job-not-finished (with its own Retry-After) until the job is done.
        given().spec(CampusLab.spec()).queryParam("format", "csv")
                .when().get(job + "/result")
                .then().statusCode(409).body("type", endsWith("/problems/job-not-finished"));

        // ---
        // Awaitility instead of Thread.sleep: ask every second, give up after 30 seconds,
        // and stop the moment the status is "done". We record each status we saw, to print the journey.
        List<String> seen = new ArrayList<>();
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).until(() -> {
            String status = given().spec(CampusLab.spec()).when().get(job).then().statusCode(200).extract().path("status");
            seen.add(status);
            return status.equals("done");
        });
        System.out.println("statuses seen: " + seen);

        // ---
        // Done: the job lists its result links and rowCount. The CSV has a header line plus one line per row;
        // the JSON has the same rows. Both must agree with rowCount.
        int rows = given().spec(CampusLab.spec()).when().get(job).then().extract().path("rowCount");
        String csv = given().spec(CampusLab.spec()).queryParam("format", "csv")
                .when().get(job + "/result")
                .then().statusCode(200).contentType("text/csv")
                .header("Content-Disposition", startsWith("attachment; filename=\"transcripts-job_"))
                .extract().asString();
        List<String> lines = csv.lines().toList();
        System.out.println(rows + " rows; CSV header: " + lines.get(0));
        assertEquals(lines.get(0), "studentId,studentName,programme,courseCode,courseTitle,credits,term,status,grade");
        assertEquals(lines.size() - 1, rows);
        given().spec(CampusLab.spec()).queryParam("format", "json")
                .when().get(job + "/result")
                .then().statusCode(200).body("rows.size()", equalTo(rows)).body("rows.studentId[0]", equalTo(4));

        // ---
        // The PDF is binary: asByteArray(), check the magic bytes, and save it so we can open it.
        byte[] pdf = given().spec(CampusLab.spec()).queryParam("format", "pdf")
                .when().get(job + "/result")
                .then().statusCode(200).contentType("application/pdf").extract().asByteArray();
        Path file = Files.write(Path.of("target", "transcript-report.pdf"), pdf);
        System.out.println("saved " + file + " (" + pdf.length + " bytes)");
        assertEquals(new String(pdf, 0, 5), "%PDF-");
    }
    // --- keep
    // end of the class
}
