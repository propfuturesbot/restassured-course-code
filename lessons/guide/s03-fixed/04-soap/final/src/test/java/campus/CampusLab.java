// Where Campus Lab lives and how every request proves who we are. Shared by the tests of this project.
package campus;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;

public final class CampusLab {

    // The server: an environment variable, with the local Docker port as the default.
    public static final String BASE_URL = System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000");

    private CampusLab() {
    }

    // Every request carries the key in X-API-Key. The key comes from the CAMPUS_API_KEY environment
    // variable (POST /api/keys gives you one) and is never written in the code or printed in a log.
    public static RequestSpecification spec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .addHeader("X-API-Key", apiKey())
                .setConfig(RestAssuredConfig.config().logConfig(LogConfig.logConfig().blacklistHeader("X-API-Key")))
                .build();
    }

    public static String apiKey() {
        String key = System.getenv("CAMPUS_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Set CAMPUS_API_KEY first (POST " + BASE_URL + "/api/keys gives you one)");
        }
        return key;
    }
}
