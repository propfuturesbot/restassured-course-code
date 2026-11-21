// Waits and resends when the server says "too many requests" (429) or "overloaded" (503) with a Retry-After header.
package framework.http;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.ArrayList;

// --- keep
// Campus Lab allows 120 requests a minute per key and sheds load with 503 + Retry-After.
// This is not a retry of failed tests: it only honours what the server asked for, at most three times.
public class RateLimitFilter implements Filter {

    private static final int MAX_WAITS = 3;

    // --- keep
    // The first version of the filter: when the server says "busy", wait as long as it asks, then send the same request again.
    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification response,
                           FilterContext ctx) {
        Response answer = ctx.next(request, response);
        for (int attempt = 1; attempt <= MAX_WAITS && isBusy(answer); attempt++) {
            long seconds = retryAfter(answer);
            // ---
            // Say in the log that we are waiting, and for how long: a silent pause of a minute looks like a hang.
            System.out.println("rate limited (" + answer.statusCode() + "): waiting " + seconds + " s before resending "
                    + request.getMethod() + " " + request.getUserDefinedPath());
            // --- keep
            // Wait as long as the server asked.
            sleepSeconds(seconds);
            // ---
            // Found by the first parallel run on Campus Lab: four threads went past 120 requests a minute, a GET
            // /student/{id} was resent, and REST Assured failed with "Invalid number of path parameters. Expected 0,
            // was 1": the resent request already has its path filled in (/student/1) AND still carries id=1.
            // So before resending, forget the path parameters.
            new ArrayList<>(request.getNamedPathParams().keySet()).forEach(request::removeNamedPathParam);
            new ArrayList<>(request.getUnnamedPathParamValues()).forEach(request::removeUnnamedPathParamByValue);
            // --- keep
            // Send it again, and look at the answer.
            answer = ctx.send(request);
        }
        return answer;
    }

    // --- keep
    // Busy = 429 or 503 that says when to come back.
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
