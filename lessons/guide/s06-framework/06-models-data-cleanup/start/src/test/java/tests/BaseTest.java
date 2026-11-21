// What every test class shares: the clients, the target banner, the cleanup after each test, the optional sandbox reset.
package tests;

import framework.clients.ApiClient;
import framework.clients.EnrollmentsApi;
import framework.clients.SandboxApi;
import framework.clients.StudentsApi;
import framework.config.App;
import framework.config.TestConfig;
import framework.data.Cleanup;
import framework.data.StudentData;
import framework.models.Student;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;

import java.util.Map;

// --- keep
// Test classes extend BaseTest; it holds no data of its own, only clients (which hold no state either).
public abstract class BaseTest {

    protected final TestConfig config = TestConfig.get();
    protected final StudentsApi students = new StudentsApi();
    protected final EnrollmentsApi enrollments = new EnrollmentsApi();

    // ---
    // Once per run: say where it goes and with which data seed.
    // On Campus Lab, -Dsandbox.reset=true first restores this key's sample data: a known start, every time.
    // alwaysRun: the suite setup must run even when the run selects only some groups.


    // ---
    // Create a student AND register its deletion in one call: tests can never forget to clean up.


    // ---
    // After EVERY test, pass or fail: undo what it created. Then the next test starts from clean data.


    // --- keep
    // Thread pools reuse threads: drop this thread's request specification when the class is done.
    @AfterClass(alwaysRun = true)
    public void releaseClient() {
        ApiClient.release();
    }
}
