// A small custom filter: one line per call with the method, the path, the status and the time it took.
package students;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

// ---
// A filter sits between REST Assured and the server: it sees every request before it goes out.
// ctx.next() really sends it and returns the response, so code after it sees the answer.
public class TimingFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification response,
                           FilterContext ctx) {
        long start = System.nanoTime();
        Response answer = ctx.next(request, response);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("[timing] " + request.getMethod() + " " + request.getURI() + " -> "
                + answer.getStatusCode() + " in " + ms + " ms");
        return answer;
    // --- keep
    // the method and the class end here
    }
}
