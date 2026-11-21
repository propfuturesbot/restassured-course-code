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


        // ---
        // Too early: the result answers 409 job-not-finished (with its own Retry-After) until the job is done.


        // ---
        // Awaitility instead of Thread.sleep: ask every second, give up after 30 seconds,
        // and stop the moment the status is "done". We record each status we saw, to print the journey.


        // ---
        // Done: the job lists its result links and rowCount. The CSV has a header line plus one line per row;
        // the JSON has the same rows. Both must agree with rowCount.


        // ---
        // The PDF is binary: asByteArray(), check the magic bytes, and save it so we can open it.

    // --- keep
    // end of the class
}
