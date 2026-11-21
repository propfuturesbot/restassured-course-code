// One call per route, so every schema is used in every run. The checks themselves happen in ContractFilter.
// Run:  mvn test -Dtest=ContractTest -Denv=docker   (or campus)
package tests;

import framework.config.App;
import framework.data.StudentData;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

// --- keep
// Notice how little these tests assert: the status. The body's shape is checked for us on every response.
@Test(groups = {"smoke", "functional"})
public class ContractTest extends BaseTest {

    // ---
    // Reads: the list and one student. Each response passes through the schema check before we see it.


    // ---
    // Writes and errors: a create (student.json on Campus Lab, message.json on the Students app) and a 404.


    // ---
    // The same schema can also be asserted by hand, when one test wants to make the contract explicit.

    // --- keep
    // end of the class
}
