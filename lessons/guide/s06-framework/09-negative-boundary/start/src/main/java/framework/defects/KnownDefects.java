// Found defects, written down in code: the test states the CORRECT behaviour, and a known defect is reported, not hidden.
// Default: a known defect SKIPS the test with its id. -Ddefects=fail lets it fail, so you can see it fail.
package framework.defects;

import framework.config.App;
import framework.config.TestConfig;
import io.restassured.response.Response;
import org.testng.SkipException;

// --- keep
// Why not just expect 500 on the Students app? Because then the suite would call a bug "correct",
// and nobody would notice when it gets fixed. Here the test keeps asking for the right answer.
public final class KnownDefects {

    private KnownDefects() {
    }

    // ---
    // Call it after sending the request and before asserting. On an app where the defect is known:
    //  - the right answer came back -> the defect is fixed: fail, so someone removes this marker;
    //  - the wrong answer came back -> skip with the defect id and what was really answered;
    //  - with -Ddefects=fail          -> do nothing, and the assertion that follows fails for real.

    // --- keep
    // end of the class
}
