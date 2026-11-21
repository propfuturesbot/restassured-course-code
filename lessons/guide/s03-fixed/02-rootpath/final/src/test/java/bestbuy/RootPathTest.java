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
    @Test
    public void rootPathOnAPage() {
        given().queryParam("manufacturer", "Duracell").queryParam("$limit", 5)
                .when().get("/products")
                .then().statusCode(200)
                .rootPath("data")
                .body("size()", equalTo(5))
                .body("manufacturer", everyItem(equalTo("Duracell")))
                .body("price", everyItem(lessThan(100f)));
    }

    // ---
    // A root path can point deep: categories[0] of one product.
    // detachRoot("[0]") steps back out to the whole categories list, appendRoot goes back in.
    // The same then() checks the first category, then all of them.
    @Test
    public void detachAndAppendRoot() {
        given().pathParam("id", 43900)
                .when().get("/products/{id}")
                .then().statusCode(200)
                .rootPath("categories[0]")
                .body("id", equalTo("abcat0208002"))
                .body("name", equalTo("Alkaline Batteries"))
                .detachRoot("[0]")
                .body("name", hasItems("Housewares", "Household Batteries"))
                .body("size()", equalTo(4))
                .appendRoot("[1]")
                .body("name", equalTo("Housewares"));
    }

    // ---
    // Chained body() calls stop at the FIRST failure: then() checks each one as it is called.
    // Give one body() several path/matcher pairs and REST Assured checks them all and reports every failure.
    // This test is wrong on purpose, twice (name and price), to see both failures in one report.
    @Test
    public void everyFailureInOneReport() {
        given().pathParam("id", 43900)
                .when().get("/products/{id}")
                .then()
                .body("name", equalTo("Duracell - AA Batteries (4-Pack)"),
                      "price", equalTo(4.99f),
                      "manufacturer", equalTo("Duracell"));
    }

    // ---
    // For checks that are not REST Assured matchers, TestNG has SoftAssert.
    // Each assert is recorded, nothing stops the test, and assertAll() fails once with the full list.
    @Test
    public void softAssertWithTestNG() {
        Response product = given().pathParam("id", 43900).when().get("/products/{id}");
        List<String> categories = product.path("categories.name");
        SoftAssert soft = new SoftAssert();
        soft.assertEquals(product.statusCode(), 200, "status");
        soft.assertEquals(product.path("type"), "HardGood", "type");
        soft.assertTrue(categories.contains("Household Batteries"), "category list " + categories);
        soft.assertEquals(product.path("shipping"), Integer.valueOf(0), "shipping");
        soft.assertAll();
    }
    // --- keep
    // end of the class
}
