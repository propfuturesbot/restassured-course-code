// Logs every request and response: one INFO line per call, and at DEBUG the headers and bodies - with credentials masked.
package framework.http;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

// --- keep
// The logger is named "http", so logback-test.xml can send these lines to the file but keep the console quiet.
public class HttpLogFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger("http");
    private static final Set<String> SECRET_HEADERS = Set.of("x-api-key", "authorization", "cookie");
    private static final int MAX_BODY = 2000;

    // ---
    // Time the call, then one INFO line: method, full URI, status, milliseconds. Enough to read a run.


        // ---
        // DEBUG: what you need when a test fails in CI and you cannot rerun it - headers and bodies both ways.
        // isDebugEnabled() first, so nothing is formatted when DEBUG is off.


    // ---
    // Never write a secret into a log file: keys, tokens and cookies keep their first 4 characters only.


    // --- keep
    // Long bodies (a list of 100 students) are cut, so the log stays readable.
    private static String cut(String body) {
        return body.length() <= MAX_BODY ? body : body.substring(0, MAX_BODY) + "... (" + body.length() + " chars)";
    }
}
