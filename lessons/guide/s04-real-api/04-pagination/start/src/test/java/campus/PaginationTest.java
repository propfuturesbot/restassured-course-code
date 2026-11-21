// Walk every page of a list two ways: by following the Link header, and by passing the cursor back.
// Run:  mvn test -Dtest=PaginationTest   (Campus Lab at CAMPUS_BASE_URL, key in CAMPUS_API_KEY)
package campus;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// --- keep
// GET /api/v1/students pages its answer: limit (1-100) items per page, plus a Link header (RFC 8288)
// with first / prev / next / last, an X-Total-Count header, and meta.nextCursor in the body.
// We page through the Physics students, 5 at a time.
public class PaginationTest {

    static final String PROGRAMME = "Physics";
    static final int PAGE_SIZE = 5;

    // ---
    // A Link header looks like:  <https://...&offset=5>; rel="next", <https://...&offset=20>; rel="last"
    // This turns it into a map from rel to URL. No next in the map means: this is the last page.


    // ---
    // The first page: 5 students, and the headers tell us the rest. There is a next and a last,
    // but no prev on page one. X-Total-Count says how many students match in all.


    // ---
    // Follow rel="next" until there is none. The Link URLs are complete, so we GET them as they are.
    // Every id must be new (no student twice), and in the end we must have X-Total-Count students.


    // ---
    // Cursor paging: send meta.nextCursor back as the cursor parameter, with the SAME filters,
    // until nextCursor is null. It must visit exactly the same students as the offset walk.


    // ---
    // A cursor belongs to the query that made it. Reuse a Physics cursor for Mathematics and the server
    // refuses with 400, problem type invalid-cursor: start again without a cursor.

    // --- keep
    // end of the class
}
