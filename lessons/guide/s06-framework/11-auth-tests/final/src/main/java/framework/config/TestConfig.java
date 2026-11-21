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

    // --- keep
    // Pick the target: -Denv=campus on the command line, else the TEST_ENV variable, else docker.
    // Each target is a small properties file on the classpath: env/local.properties, env/docker.properties ...
    // An unknown name stops the run at once instead of testing the wrong server.
    private static TestConfig load() {
        String env = System.getProperty("env", System.getenv().getOrDefault("TEST_ENV", "docker"));
        Properties props = new Properties();
        try (InputStream in = TestConfig.class.getResourceAsStream("/env/" + env + ".properties")) {
            if (in == null) {
                throw new IllegalStateException("Unknown env '" + env + "': there is no env/" + env + ".properties");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read env/" + env + ".properties", e);
        }
        return new TestConfig(env, props);
    }

    // --- keep
    // One lookup rule for every setting: -Dbase.uri=... wins, then the variable BASE_URI, then the file.
    // So CI can point the same tests anywhere without editing a file.
    public String value(String key) {
        String fromCommandLine = System.getProperty(key);
        if (fromCommandLine != null) return fromCommandLine;
        String fromEnvironment = System.getenv(key.toUpperCase().replace('.', '_'));
        if (fromEnvironment != null) return fromEnvironment;
        return file.getProperty(key);
    }

    public String env() {
        return env;
    }

    public String baseUri() {
        return value("base.uri");
    }

    public App app() {
        return App.valueOf(value("app").toUpperCase());
    }

    // --- keep
    // Secrets: the file only NAMES the variable (api.key.env=CAMPUS_API_KEY); the key is read from the environment.
    // Never from a file in the repo and never from -D (that would show it in the process list and the logs).
    // A target that needs a key and has none fails here, with a message that says what to do.
    public Optional<String> apiKey() {
        String variable = file.getProperty("api.key.env");
        if (variable == null) return Optional.empty();
        String key = System.getenv(variable);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Target '" + env + "' needs an API key: export " + variable
                    + "=<your key> (get one at " + baseUri() + "/keys). Keys never go into a file.");
        }
        return Optional.of(key);
    }

    // ---
    // Any other secret (Basic password, OAuth client secret): from the environment only, empty when not set,
    // so a test that needs it can skip instead of failing on a machine that does not have it.
    public Optional<String> secret(String variable) {
        return Optional.ofNullable(System.getenv(variable)).filter(v -> !v.isBlank());
    }

    // --- keep
    // One line that says where this run goes. It names the key's variable, never the key.
    public String describe() {
        String variable = file.getProperty("api.key.env");
        return "env=" + env + " app=" + app() + " baseUri=" + baseUri()
                + (variable == null ? " (no key needed)" : " key from $" + variable);
    }
    // --- keep
    // end of the class
}
