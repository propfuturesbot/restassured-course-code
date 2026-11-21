// A TestNG listener: one clear line per test (start, pass, fail, skip with the reason) and a summary at the end.
package framework.listeners;

import framework.config.TestConfig;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.concurrent.atomic.AtomicInteger;

// --- keep
// Listeners are TestNG's hooks: TestNG calls these methods, the tests do not know they exist.
// Registered in the pom (surefire's "listener" property), so every run uses them.
public class TestListener implements ITestListener, ISuiteListener {

    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();

    // ---
    // ISuiteListener: once before the suite, once after. The summary names the target, so a CI log
    // says at a glance where the run went and how it ended.


    // ---
    // ITestListener: one line per result, with the time the test took and the thread it ran on
    // (useful the first time a parallel run misbehaves).


    // ---
    // A skip has a reason: a skipped class, a known defect, or an attempt that the retry analyzer re-ran.


    // --- keep
    // Class.method plus the parameters of a data-driven run.
    private static String name(ITestResult r) {
        String params = r.getParameters().length == 0 ? "" : java.util.Arrays.toString(r.getParameters());
        return r.getTestClass().getRealClass().getSimpleName() + "." + r.getMethod().getMethodName() + params;
    }

    private static long millis(ITestResult r) {
        return r.getEndMillis() - r.getStartMillis();
    }
}
