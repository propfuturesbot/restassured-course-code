// A REST Assured filter that gets an OAuth2 bearer token, caches it, and gets a new one when it runs out.
// Used by BearerTokenFilterTest.
package campus;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.time.Instant;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;

// --- keep
// The filter needs where the token endpoint is, the client's id and secret, and a clock.
// The clock is a Supplier so a test can move time forward instead of waiting an hour.
public class BearerTokenFilter implements Filter {

    private final String baseUrl, clientId, clientSecret, scope;
    private final Supplier<Instant> clock;
    private String token;
    private Instant expiresAt = Instant.EPOCH;
    private int fetches;

    public BearerTokenFilter(String baseUrl, String clientId, String clientSecret, String scope, Supplier<Instant> clock) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.clock = clock;
    }

    // ---
    // Fetch a token with the client credentials grant: the client id and secret go in a Basic header,
    // the grant type and scope go as form fields. The answer says how long the token lives (expires_in),
    // so we remember the moment it stops working.
    private void fetchToken() {
        Response r = given().baseUri(baseUrl)
                .auth().preemptive().basic(clientId, clientSecret)
                .formParam("grant_type", "client_credentials")
                .formParam("scope", scope)
                .when().post("/oauth/token")
                .then().statusCode(200).extract().response();
        token = r.path("access_token");
        expiresAt = clock.get().plusSeconds(r.<Integer>path("expires_in"));
        fetches++;
        System.out.println("Fetched a new token, it expires at " + expiresAt);
    }

    // ---
    // The cache: reuse the token until one minute before it expires, then fetch a new one.
    // synchronized, because parallel tests may share one filter.
    public synchronized String currentToken() {
        if (token == null || clock.get().isAfter(expiresAt.minusSeconds(60))) {
            fetchToken();
        }
        return token;
    }

    // ---
    // The filter itself: put the token on every request, send it with ctx.next().
    // If the server still answers 401 (the token was revoked), throw the token away, fetch a new one
    // and send the request once more with ctx.send(req): ctx.next() can be called only once per request.
    @Override
    public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
        req.removeHeader("Authorization");
        req.header("Authorization", "Bearer " + currentToken());
        Response response = ctx.next(req, res);
        if (response.statusCode() == 401) {
            synchronized (this) { token = null; }
            req.removeHeader("Authorization");
            req.header("Authorization", "Bearer " + currentToken());
            response = ctx.send(req);
        }
        return response;
    }

    // --- keep
    // Two helpers for the tests: how many tokens we fetched, and a way to plant a token the server will refuse.
    public int fetches() { return fetches; }

    public synchronized void replaceTokenWith(String badToken) { token = badToken; }
}
