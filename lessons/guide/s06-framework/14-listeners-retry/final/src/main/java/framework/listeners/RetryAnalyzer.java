// Re-runs a failed test, but ONLY when the failure was the infrastructure (connection refused, timeout, 502/503/504).
// A wrong answer from the API is a bug: it fails at once and is never retried.
package framework.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

// --- keep
// TestNG makes one RetryAnalyzer per test method, so the attempt counter belongs to that method.
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRIES = 2;
    private int retries = 0;

    // ---
    // retry() is called after each failure: true = run the test again. Two retries at most.
    @Override
    public boolean retry(ITestResult result) {
        if (retries >= MAX_RETRIES || !isTransient(result.getThrowable())) return false;
        retries++;
        System.out.println("RETRY " + retries + "/" + MAX_RETRIES + " " + result.getMethod().getMethodName()
                + " after: " + result.getThrowable());
        return true;
    }

    // ---
    // Why retries are not fixes: retrying an assertion failure turns a real, intermittent bug into a green build.
    // So the allow-list is narrow: the network failed, or the server said it could not answer right now.
    static boolean isTransient(Throwable t) {
        for (Throwable cause = t; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConnectException || cause instanceof SocketTimeoutException) return true;
            String message = String.valueOf(cause.getMessage());
            if (message.matches("(?s).*Expected status code <\\d+> but was <50[234]>.*")) return true;
        }
        return false;
    }
    // --- keep
    // end of the class
}
