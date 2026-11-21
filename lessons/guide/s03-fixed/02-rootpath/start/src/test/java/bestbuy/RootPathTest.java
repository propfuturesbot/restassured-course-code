// Root paths and soft assertions on live BestBuy data: shorter paths, and every failure reported at once.
// Run:  mvn test -Dtest=RootPathTest   (BestBuy at BESTBUY_BASE_URI, default http://localhost:3030)
package bestbuy;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.lessThan;

// --- keep
// The tests read one product, 43900 (Duracell AAA batteries), and a page of Duracell products.
public class RootPathTest {

    // --- keep
    // Where BestBuy lives: an environment variable, with the local Docker port as the default.
    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("BESTBUY_BASE_URI", "http://localhost:3030");
    }

    // ---
    // Without a root path every check repeats "data." in front of the field.
    // rootPath("data") says: from now on, every path in this then() starts inside data.
    // "manufacturer" now means the manufacturer of every product in the page.


    // ---
    // A root path can point deep: categories[0] of one product.
    // detachRoot("[0]") steps back out to the whole categories list, appendRoot goes back in.
    // The same then() checks the first category, then all of them.


    // ---
    // Chained body() calls stop at the FIRST failure: then() checks each one as it is called.
    // Give one body() several path/matcher pairs and REST Assured checks them all and reports every failure.
    // This test is wrong on purpose, twice (name and price), to see both failures in one report.


    // ---
    // For checks that are not REST Assured matchers, TestNG has SoftAssert.
    // Each assert is recorded, nothing stops the test, and assertAll() fails once with the full list.

    // --- keep
    // end of the class
}
