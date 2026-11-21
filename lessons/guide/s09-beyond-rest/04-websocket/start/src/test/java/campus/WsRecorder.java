// An OkHttp WebSocket listener that records everything that happens, so a test can wait for it.
// Used by WebSocketTest.
package campus;

import io.restassured.path.json.JsonPath;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

// --- keep
// OkHttp calls these methods on its own thread, so the lists are thread-safe.
public class WsRecorder extends WebSocketListener {

    public final List<String> messages = new CopyOnWriteArrayList<>();
    public volatile Integer closeCode;
    public volatile String closeReason;

    // ---
    // Every text frame the server sends is stored as it arrives. Nothing is asserted here:
    // the listener only records, the test decides what to wait for.


    // ---
    // When the server closes the connection we keep its close code and reason (1008 = policy violation).


    // --- keep
    // Connection errors are recorded as a close too, so a test never waits for nothing.
    @Override
    public void onFailure(WebSocket ws, Throwable t, Response response) {
        closeReason = "failure: " + t;
        closeCode = -1;
    }

    // The first message whose "type" field equals the given type, parsed with REST Assured's JsonPath.
    public Optional<JsonPath> first(String type) {
        return messages.stream().map(JsonPath::from).filter(j -> type.equals(j.getString("type"))).findFirst();
    }
}
