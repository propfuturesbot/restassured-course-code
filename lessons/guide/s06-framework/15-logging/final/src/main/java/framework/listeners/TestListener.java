// A TestNG listener: one clear line per test (start, pass, fail, skip with the reason) and a summary at the end.
package framework.listeners;

import framework.config.TestConfig;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicInteger;

// --- keep
// Listeners are TestNG's hooks: TestNG calls these methods, the tests do not know they exist.
// Registered in the pom (surefire's "listener" property), so every run uses them.
public class TestListener implements ITestListener, ISuiteListener {

    // ---
    // SLF4J instead of System.out: one logger, and logback-test.xml decides where each line goes.
    // MDC (mapped diagnostic context) is per thread: put the test's name there when it starts, and every
    // log line written on that thread - including each HTTP request - carries it, even in a parallel run.
    private static final Logger log = LoggerFactory.getLogger("tests");

    @Override
    public void onTestStart(ITestResult result) {
        MDC.put("test", name(result));
    }

    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();

    // --- keep
    // ISuiteListener: once before the suite, once after. The summary names the target, so a CI log
    // says at a glance where the run went and how it ended.
    @Override
    public void onStart(ISuite suite) {
        log.info("=== " + suite.getName() + " on " + TestConfig.get().describe());
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("=== " + passed + " passed, " + failed + " failed, " + skipped + " skipped");
    }

    // --- keep
    // ITestListener: one line per result, with the time the test took and the thread it ran on
    // (useful the first time a parallel run misbehaves).
    @Override
    public void onTestSuccess(ITestResult result) {
        passed.incrementAndGet();
        log.info("PASS " + name(result) + " (" + millis(result) + " ms, " + Thread.currentThread().getName() + ")");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failed.incrementAndGet();
        log.error("FAIL " + name(result) + ": " + result.getThrowable());
    }

    // --- keep
    // A skip has a reason: a skipped class, a known defect, or an attempt that the retry analyzer re-ran.
    @Override
    public void onTestSkipped(ITestResult result) {
        skipped.incrementAndGet();
        String reason = result.wasRetried() ? "retried" : String.valueOf(result.getThrowable());
        log.warn("SKIP " + name(result) + ": " + reason);
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
