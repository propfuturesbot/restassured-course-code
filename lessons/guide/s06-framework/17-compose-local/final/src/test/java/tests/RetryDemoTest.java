// What the retry analyzer does and does not do. Skipped unless -Ddemo=true.
// Run:  mvn test -Dtest=RetryDemoTest -Ddemo=true   (one test recovers on retry, one fails for real)
package tests;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicInteger;

// --- keep
// The connection failure is SIMULATED (thrown by the test the first time), so the demo is the same on every run.
@Test(groups = "demo")
public class RetryDemoTest extends BaseTest {

    private static final AtomicInteger attempts = new AtomicInteger();

    @BeforeClass(alwaysRun = true)
    public void demoOnly() {
        if (!Boolean.getBoolean("demo")) throw new SkipException("demo only: run with -Ddemo=true");
    }

    // A transient failure: the first attempt "cannot connect", the retry succeeds.
    public void recoversFromAConnectionBlip() throws ConnectException {
        if (attempts.incrementAndGet() == 1) throw new ConnectException("Connection refused (simulated)");
        students.list().then().statusCode(200);
    }

    // A real wrong answer: GET is not a create, so 201 never comes. Not retried: it fails at once.
    public void wrongAnswerIsNotRetried() {
        students.list().then().statusCode(201);
    }
}
