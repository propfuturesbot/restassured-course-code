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

    // --- keep
    // At the start of the suite: target, app, base URI, Java and REST Assured versions. The key is NOT here:
    // only the name of the variable it came from.
    @Override
    public void onStart(ISuite suite) {
        TestConfig config = TestConfig.get();
        Properties env = new Properties();
        env.setProperty("Target", config.env());
        env.setProperty("App", config.app().name());
        env.setProperty("Base URI", config.baseUri());
        env.setProperty("Java", System.getProperty("java.version"));
        env.setProperty("REST Assured", "6.3.0");
        env.setProperty("Suite", suite.getName());
        Path dir = Path.of("target", "allure-results");
        try {
            Files.createDirectories(dir);
            try (Writer out = Files.newBufferedWriter(dir.resolve("environment.properties"))) {
                env.store(out, "written by AllureEnvironment");
            }
        } catch (IOException e) {
            throw new IllegalStateException("cannot write the Allure environment", e);
        }
    }
    // --- keep
    // end of the class
}
