// Writes the Environment widget of the Allure report: where this run went. Never a secret.
package framework.report;

import framework.config.TestConfig;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// --- keep
// Allure reads allure-results/environment.properties and shows it on the report's front page.
public class AllureEnvironment implements ISuiteListener {

    // ---
    // At the start of the suite: target, app, base URI, Java and REST Assured versions. The key is NOT here:
    // only the name of the variable it came from.

    // --- keep
    // end of the class
}
