// The configuration of a run: -Denv picks a target file, -D and environment variables override it, secrets come from the environment only.
// Run:  mvn test -Denv=local | -Denv=docker | -Denv=campus
package framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

// --- keep
// One object per run, read once and never changed afterwards, so every test (and every thread) can share it.
public final class TestConfig {

    private static final TestConfig INSTANCE = load();

    private final String env;
    private final Properties file;

    private TestConfig(String env, Properties file) {
        this.env = env;
        this.file = file;
    }

    public static TestConfig get() {
        return INSTANCE;
    }

    // ---
    // Pick the target: -Denv=campus on the command line, else the TEST_ENV variable, else docker.
    // Each target is a small properties file on the classpath: env/local.properties, env/docker.properties ...
    // An unknown name stops the run at once instead of testing the wrong server.


    // ---
    // One lookup rule for every setting: -Dbase.uri=... wins, then the variable BASE_URI, then the file.
    // So CI can point the same tests anywhere without editing a file.


    // ---
    // Secrets: the file only NAMES the variable (api.key.env=CAMPUS_API_KEY); the key is read from the environment.
    // Never from a file in the repo and never from -D (that would show it in the process list and the logs).
    // A target that needs a key and has none fails here, with a message that says what to do.


    // ---
    // One line that says where this run goes. It names the key's variable, never the key.

    // --- keep
    // end of the class
}
