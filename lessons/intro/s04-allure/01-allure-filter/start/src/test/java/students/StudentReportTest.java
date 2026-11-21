// Student tests whose every HTTP call lands in the Allure report, with no logging code in the tests.
// Run:  mvn test -Dtest=StudentReportTest   then   npx -y allure@3.22.1 generate target/allure-results -o target/allure-report
package students;

import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

// --- keep
// The base URI comes from an environment variable, as in every lesson.
class StudentReportTest {

    // ---
    // One line makes every request and response part of the report:
    // RestAssured.filters() adds the Allure filter to EVERY call of every test in this run.


    // ---
    // A normal test. In the report it shows the GET with its URL, headers and the JSON answer.


    // ---
    // Allure.step() names a part of the test; the calls made inside it are attached to that step.


    // ---
    // This one FAILS on purpose: student 1 is not called Nobody. The report shows the failure
    // next to the exact response that caused it, so nobody has to rerun the test to see what came back.

    // --- keep
    // end of the class
}
