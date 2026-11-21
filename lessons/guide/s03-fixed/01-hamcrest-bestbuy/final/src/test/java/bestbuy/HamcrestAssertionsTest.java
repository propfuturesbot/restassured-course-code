// Hamcrest assertions on live data: the BestBuy playground's products.
// Run:  mvn -B test -Dtest=HamcrestAssertionsTest   (BestBuy at BESTBUY_BASE_URI, default http://localhost:3030)
package bestbuy;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

// --- keep
// Every test asks for the same thing: the first ten Duracell products.
class HamcrestAssertionsTest {

    // ---
    // Where BestBuy runs. One environment variable, so the same tests work on any machine.
    @BeforeClass
    void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("BESTBUY_BASE_URI", "http://localhost:3030");
    }

    // ---
    // ONE assertion: body() takes a path into the JSON and a Hamcrest matcher.
    // "total" is how many Duracell products exist; equalTo() checks the exact number.
    @Test
    void totalNumberOfDuracellProducts() {
        given().queryParam("manufacturer", "Duracell").queryParam("$limit", 10)
                .when().get("/products")
                .then().statusCode(200).body("total", equalTo(44));
    }

    // ---
    // Checks on a LIST. "data.name" collects the name of every product on the page.
    // hasItem() wants one name in the list, hasItems() several, hasSize() counts the list.
    @Test
    void namesOnTheFirstPage() {
        given().queryParam("manufacturer", "Duracell").queryParam("$limit", 10)
                .when().get("/products")
                .then().body("data.name", hasItem("Duracell - AAA Batteries (4-Pack)"))
                .body("data.name", hasItems("Duracell - AAA Batteries (4-Pack)",
                        "Duracell - AA 1.5V CopperTop Batteries (4-Pack)"))
                .body("data", hasSize(10));
    }

    // ---
    // everyItem() applies a matcher to EVERY element: each manufacturer is Duracell, each price is above zero.
    // hasKey() checks the first product has a "categories" field at all.
    @Test
    void everyProductOnThePage() {
        given().queryParam("manufacturer", "Duracell").queryParam("$limit", 10)
                .when().get("/products")
                .then().body("data.manufacturer", everyItem(equalTo("Duracell")))
                .body("data.price", everyItem(greaterThan(0f)))
                .body("data[0]", hasKey("categories"));
    }

    // ---
    // MULTIPLE assertions in ONE body() call: path, matcher, path, matcher...
    // If several of them are wrong, REST Assured reports all of them together, not just the first.
    @Test
    void severalChecksInOneCall() {
        given().queryParam("manufacturer", "Duracell").queryParam("$limit", 10)
                .when().get("/products")
                .then().body("limit", equalTo(10),
                        "skip", equalTo(0),
                        "data[0].name", startsWith("Duracell"),
                        "data[0].price", lessThan(10f));
    }
    // --- keep
    // end of the class
}
