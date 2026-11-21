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

    // ---
    // Send the request first (ctx.next), then look at what came back. No body (204) means nothing to check;
    // a route we have no schema for is left alone, so the filter never blocks a new endpoint.


    // ---
    // The contract table. getUserDefinedPath() is the path as the client wrote it ("/student/{id}"),
    // so the template matches whatever id was sent. Errors: problem+json on Campus Lab, {"error"} on the Students app.


    // ---
    // How many responses each schema checked in this run, printed at the end of the suite.

    // --- keep
    // end of the class
}
