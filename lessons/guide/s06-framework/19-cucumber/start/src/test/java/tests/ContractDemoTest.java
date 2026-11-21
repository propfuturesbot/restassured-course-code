// A deliberate contract failure: the Students app's 404 body checked against Campus Lab's problem+json schema.
// Run:  mvn test -Dtest=ContractDemoTest -Denv=docker -Ddemo=true   (meant to fail)
package tests;

import org.testng.SkipException;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

// --- keep
// The two apps really do have different error contracts: this is what a broken contract looks like.
@Test(groups = "demo")
public class ContractDemoTest extends BaseTest {

    public void studentsAppErrorIsNotAProblemDocument() {
        if (!Boolean.getBoolean("demo")) throw new SkipException("demo only: run with -Ddemo=true");
        students.get(999_999).then().statusCode(404).body(matchesJsonSchemaInClasspath("schemas/problem.json"));
    }
}
