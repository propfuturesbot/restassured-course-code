// A REST Assured filter that waits and retries when the server says 429 Too Many Requests.
package campus;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import static io.restassured.RestAssured.given;

// ---
// A filter sees every request before it is sent and every response after. ctx.next(...) sends the request
// once (a second ctx.next returns null in REST Assured 6). To send it again we hand ctx.send(...) a copy
// of the same request, without filters so this one does not run inside itself.
// On 429 we read Retry-After (seconds), sleep that long, and try again, a few times at most.


    // --- keep
    // Thread.sleep without the checked exception.
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
