// Campus Lab's sandbox: every key has its own copy of the sample data, and it can be restored in one call.
package framework.clients;

import io.restassured.response.Response;

// --- keep
// POST /api/v1/sandbox/reset wipes everything this key created and copies the seed data again.
// It changes only YOUR key's data: other people (and other keys) never notice.
public class SandboxApi {

    public Response reset() {
        return ApiClient.given().when().post("/api/v1/sandbox/reset");
    }
}
