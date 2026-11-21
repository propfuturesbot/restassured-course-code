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


    // ---
    // ONE assertion: body() takes a path into the JSON and a Hamcrest matcher.
    // "total" is how many Duracell products exist; equalTo() checks the exact number.


    // ---
    // Checks on a LIST. "data.name" collects the name of every product on the page.
    // hasItem() wants one name in the list, hasItems() several, hasSize() counts the list.


    // ---
    // everyItem() applies a matcher to EVERY element: each manufacturer is Duracell, each price is above zero.
    // hasKey() checks the first product has a "categories" field at all.


    // ---
    // MULTIPLE assertions in ONE body() call: path, matcher, path, matcher...
    // If several of them are wrong, REST Assured reports all of them together, not just the first.

    // --- keep
    // end of the class
}
