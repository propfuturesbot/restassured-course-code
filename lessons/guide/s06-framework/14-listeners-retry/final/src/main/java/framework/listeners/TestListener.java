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
    @Override
    public void onStart(ISuite suite) {
        System.out.println("=== " + suite.getName() + " on " + TestConfig.get().describe());
    }

    @Override
    public void onFinish(ISuite suite) {
        System.out.println("=== " + passed + " passed, " + failed + " failed, " + skipped + " skipped");
    }

    // ---
    // ITestListener: one line per result, with the time the test took and the thread it ran on
    // (useful the first time a parallel run misbehaves).
    @Override
    public void onTestSuccess(ITestResult result) {
        passed.incrementAndGet();
        System.out.println("PASS " + name(result) + " (" + millis(result) + " ms, " + Thread.currentThread().getName() + ")");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failed.incrementAndGet();
        System.out.println("FAIL " + name(result) + ": " + result.getThrowable());
    }

    // ---
    // A skip has a reason: a skipped class, a known defect, or an attempt that the retry analyzer re-ran.
    @Override
    public void onTestSkipped(ITestResult result) {
        skipped.incrementAndGet();
        String reason = result.wasRetried() ? "retried" : String.valueOf(result.getThrowable());
        System.out.println("SKIP " + name(result) + ": " + reason);
    }

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
