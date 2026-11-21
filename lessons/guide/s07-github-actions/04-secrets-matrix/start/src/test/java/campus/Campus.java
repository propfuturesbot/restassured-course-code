// The request every Campus Lab test starts from: the base URL and the API key, both from environment variables.
package campus;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public final class Campus {

    private Campus() {
    }

    public static RequestSpecification spec() {
        String key = System.getenv("CAMPUS_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("CAMPUS_API_KEY is not set (locally: export it; in CI: a repository secret)");
        }
        return new RequestSpecBuilder()
                .setBaseUri(System.getenv().getOrDefault("CAMPUS_BASE_URL", "http://localhost:8000"))
                .addHeader("X-API-Key", key)
                .build();
    }
}
