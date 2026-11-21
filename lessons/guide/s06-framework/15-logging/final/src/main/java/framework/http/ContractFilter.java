// Checks EVERY response against its JSON Schema, inside the request pipeline: every test becomes a contract test.
package framework.http;

import framework.config.App;
import framework.config.TestConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;

// --- keep
// A REST Assured Filter sees every request and its response. This one picks the schema that the route,
// the status and the app call for, and fails the test the moment a body breaks its contract.
public class ContractFilter implements Filter {

    private static final Map<String, AtomicInteger> CHECKED = new ConcurrentHashMap<>();

    // --- keep
    // Send the request first (ctx.next), then look at what came back. No body (204) means nothing to check;
    // a route we have no schema for is left alone, so the filter never blocks a new endpoint.
    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification spec,
                           FilterContext ctx) {
        Response response = ctx.next(request, spec);
        String schema = schemaFor(request.getMethod(), request.getUserDefinedPath(), response.statusCode());
        if (schema != null && !response.asString().isEmpty()) {
            assertThat(request.getMethod() + " " + request.getUserDefinedPath() + " -> " + response.statusCode()
                    + " must match " + schema, response.asString(), matchesJsonSchemaInClasspath("schemas/" + schema));
            CHECKED.computeIfAbsent(schema, s -> new AtomicInteger()).incrementAndGet();
        }
        return response;
    }

    // --- keep
    // The contract table. getUserDefinedPath() is the path as the client wrote it ("/student/{id}"),
    // so the template matches whatever id was sent. Errors: problem+json on Campus Lab, {"error"} on the Students app.
    static String schemaFor(String method, String path, int status) {
        boolean campus = TestConfig.get().app() == App.CAMPUS;
        if (status >= 400) return campus ? "problem.json" : "students-error.json";
        if (path.equals("/student/list")) return "student-list.json";
        if (path.startsWith("/student")) {
            if (method.equals("GET")) return "student.json";
            return campus ? "student.json" : "message.json";   // POST, PUT, PATCH
        }
        if (path.endsWith("/enrollments") && method.equals("GET")) return "enrollment-page.json";
        if (path.startsWith("/api/v1/enrollments")) return "enrollment.json";
        return null;
    }

    // --- keep
    // How many responses each schema checked in this run, printed at the end of the suite.
    public static String summary() {
        Map<String, Integer> counts = new TreeMap<>();
        CHECKED.forEach((schema, n) -> counts.put(schema, n.get()));
        return counts.values().stream().mapToInt(Integer::intValue).sum() + " responses matched their schema " + counts;
    }
    // --- keep
    // end of the class
}
