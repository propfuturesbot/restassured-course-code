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
    static Map<String, String> links(Response response) {
        Map<String, String> links = new LinkedHashMap<>();
        String header = response.header("Link");
        if (header != null) {
            Matcher m = Pattern.compile("<([^>]+)>;\\s*rel=\"([^\"]+)\"").matcher(header);
            while (m.find()) {
                links.put(m.group(2), m.group(1));
            }
        }
        return links;
    }

    // ---
    // The first page: 5 students, and the headers tell us the rest. There is a next and a last,
    // but no prev on page one. X-Total-Count says how many students match in all.
    @Test
    public void firstPageDescribesTheRest() {
        Response first = given().spec(CampusLab.spec()).queryParam("programme", PROGRAMME).queryParam("limit", PAGE_SIZE)
                .when().get("/api/v1/students");
        first.then().statusCode(200).body("data.size()", equalTo(PAGE_SIZE));
        Map<String, String> links = links(first);
        System.out.println("total " + first.header("X-Total-Count") + ", links " + links.keySet());
        assertTrue(links.containsKey("next"));
        assertFalse(links.containsKey("prev"));
        assertTrue(links.get("last").endsWith("offset=20"), links.get("last"));
    }

    // ---
    // Follow rel="next" until there is none. The Link URLs are complete, so we GET them as they are.
    // Every id must be new (no student twice), and in the end we must have X-Total-Count students.
    @Test
    public void followTheLinkHeader() {
        Response page = given().spec(CampusLab.spec()).queryParam("programme", PROGRAMME).queryParam("limit", PAGE_SIZE)
                .when().get("/api/v1/students");
        int total = Integer.parseInt(page.header("X-Total-Count"));
        List<Integer> ids = new ArrayList<>();
        int pages = 0;
        while (true) {
            page.then().statusCode(200).body("data.programme", everyItem(equalTo(PROGRAMME)));
            ids.addAll(page.path("data.id"));
            pages++;
            String next = links(page).get("next");
            if (next == null) {
                break;
            }
            page = given().spec(CampusLab.spec()).when().get(next);
        }
        System.out.println(pages + " pages, " + ids.size() + " students: " + ids);
        assertEquals(ids.size(), total);
        assertEquals(new HashSet<>(ids).size(), ids.size(), "no student twice");
    }

    // ---
    // Cursor paging: send meta.nextCursor back as the cursor parameter, with the SAME filters,
    // until nextCursor is null. It must visit exactly the same students as the offset walk.
    @Test
    public void walkWithTheCursor() {
        List<Integer> ids = new ArrayList<>();
        String cursor = null;
        do {
            var request = given().spec(CampusLab.spec()).queryParam("programme", PROGRAMME).queryParam("limit", PAGE_SIZE);
            if (cursor != null) {
                request.queryParam("cursor", cursor);
            }
            Response page = request.when().get("/api/v1/students");
            page.then().statusCode(200);
            ids.addAll(page.path("data.id"));
            cursor = page.path("meta.nextCursor");
        } while (cursor != null);
        System.out.println("cursor walk: " + ids.size() + " students");
        assertEquals(new HashSet<>(ids).size(), ids.size(), "no student twice");
        assertEquals(ids.size(), Integer.parseInt(given().spec(CampusLab.spec()).queryParam("programme", PROGRAMME)
                .queryParam("limit", 1).when().get("/api/v1/students").header("X-Total-Count")));
    }

    // ---
    // A cursor belongs to the query that made it. Reuse a Physics cursor for Mathematics and the server
    // refuses with 400, problem type invalid-cursor: start again without a cursor.
    @Test
    public void aCursorOnlyFitsItsOwnQuery() {
        String cursor = given().spec(CampusLab.spec()).queryParam("programme", PROGRAMME).queryParam("limit", PAGE_SIZE)
                .when().get("/api/v1/students").path("meta.nextCursor");
        given().spec(CampusLab.spec()).queryParam("programme", "Mathematics").queryParam("limit", PAGE_SIZE)
                .queryParam("cursor", cursor)
                .when().get("/api/v1/students")
                .then().statusCode(400).body("type", endsWith("/problems/invalid-cursor"));
    }
    // --- keep
    // end of the class
}
