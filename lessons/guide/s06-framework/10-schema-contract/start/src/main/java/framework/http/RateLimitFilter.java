// Waits and resends when the server says "too many requests" (429) or "overloaded" (503) with a Retry-After header.
package framework.http;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

// --- keep
// Campus Lab allows 120 requests a minute per key and sheds load with 503 + Retry-After.
// This is not a retry of failed tests: it only honours what the server asked for, at most three times.
public class RateLimitFilter implements Filter {

    private static final int MAX_WAITS = 3;

    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification response,
                           FilterContext ctx) {
        Response answer = ctx.next(request, response);
        for (int attempt = 1; attempt <= MAX_WAITS && isBusy(answer); attempt++) {
            sleepSeconds(retryAfter(answer));
            answer = ctx.send(request);
        }
        return answer;
    }

    private static boolean isBusy(Response r) {
        return (r.statusCode() == 429 || r.statusCode() == 503) && r.header("Retry-After") != null;
    }

    private static long retryAfter(Response r) {
        try {
            return Math.min(Long.parseLong(r.header("Retry-After").trim()), 60);
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    private static void sleepSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
