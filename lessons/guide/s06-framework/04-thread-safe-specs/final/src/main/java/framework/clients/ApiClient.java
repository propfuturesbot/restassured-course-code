// The entry point of every request: one request specification per thread, created on first use.
package framework.clients;

import framework.http.SpecFactory;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

// --- keep
// Tests call ApiClient.given() instead of RestAssured.given(): same fluent API, but the defaults
// come from this thread's own specification, never from a static field another thread can change.
public final class ApiClient {

    private ApiClient() {
    }

    // ---
    // ThreadLocal: each thread that sends a request gets its own specification, built by the factory.
    // withInitial runs the factory the first time a thread asks, and never again for that thread.
    private static final ThreadLocal<RequestSpecification> SPEC =
            ThreadLocal.withInitial(SpecFactory::requestSpec);

    // ---
    // given() starts a new request from this thread's defaults. spec() merges the defaults into the
    // new request, so a test can add headers or params without touching the shared defaults.
    public static RequestSpecification given() {
        return RestAssured.given().spec(SPEC.get());
    }

    public static RequestSpecification spec() {
        return SPEC.get();
    }

    // ---
    // Thread pools reuse threads: drop this thread's specification when a test class is done with it.
    public static void release() {
        SPEC.remove();
    }
    // --- keep
    // end of the class
}
