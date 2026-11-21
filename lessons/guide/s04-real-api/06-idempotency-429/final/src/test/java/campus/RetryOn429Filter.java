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
public class RetryOn429Filter implements Filter {

    private final int maxRetries;
    public int retries;
    public long waitedMillis;

    public RetryOn429Filter(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
        Response answer = ctx.next(req, res);
        while (answer.statusCode() == 429 && retries < maxRetries) {
            long seconds = Long.parseLong(answer.header("Retry-After"));
            System.out.println("429: Retry-After " + seconds + " s, waiting");
            sleep(seconds * 1000);
            waitedMillis += seconds * 1000;
            retries++;
            answer = ctx.send(given().spec(req).noFilters());
        }
        return answer;
    }

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
