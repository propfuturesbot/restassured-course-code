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

    // --- keep
    // Time the call, then one INFO line: method, full URI, status, milliseconds. Enough to read a run.
    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification spec,
                           FilterContext ctx) {
        long start = System.nanoTime();
        Response response = ctx.next(request, spec);
        long ms = (System.nanoTime() - start) / 1_000_000;
        log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getURI(), response.statusCode(), ms);

        // --- keep
        // DEBUG: what you need when a test fails in CI and you cannot rerun it - headers and bodies both ways.
        // isDebugEnabled() first, so nothing is formatted when DEBUG is off.
        if (log.isDebugEnabled()) {
            log.debug("request headers: {}", headers(request.getHeaders().asList()));
            if (request.getBody() != null) log.debug("request body: {}", cut(redact(String.valueOf((Object) request.getBody()))));
            log.debug("response headers: {}", headers(response.getHeaders().asList()));
            log.debug("response body: {}", cut(redact(response.asString())));
        }
        return response;
    }

    // --- keep
    // Never write a secret into a log file: keys, tokens and cookies keep their first 4 characters only.
    private static String headers(java.util.List<Header> headers) {
        return headers.stream()
                .map(h -> h.getName() + "=" + (SECRET_HEADERS.contains(h.getName().toLowerCase()) ? mask(h.getValue()) : h.getValue()))
                .collect(Collectors.joining(", "));
    }

    private static String mask(String value) {
        return value.length() <= 4 ? "****" : value.substring(0, 4) + "****(" + value.length() + " chars)";
    }

    // Bodies can carry secrets too: a token endpoint answers with the token itself.
    private static String redact(String body) {
        return body.replaceAll("\"(access_token|accessToken|refresh_token|client_secret|password)\"\\s*:\\s*\"[^\"]*\"",
                "\"$1\":\"****\"");
    }

    // --- keep
    // Long bodies (a list of 100 students) are cut, so the log stays readable.
    private static String cut(String body) {
        return body.length() <= MAX_BODY ? body : body.substring(0, MAX_BODY) + "... (" + body.length() + " chars)";
    }
}
