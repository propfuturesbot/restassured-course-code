// Reads a server-sent events stream with the JDK's HttpClient and turns it into events a test can wait for.
// Used by SseTest.
package campus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

// --- keep
// One event of the stream: its id, its name ("event:" line), its data, and the retry hint if one came.
public class SseStream implements AutoCloseable {

    public record Event(String id, String name, String data, String retry) {}

    public final List<Event> events = new CopyOnWriteArrayList<>();
    public volatile HttpResponse<Stream<String>> response;
    private final CompletableFuture<Void> reading;

    // ---
    // REST Assured reads a whole body before it returns, and this body never ends. So we open the stream
    // with HttpClient and BodyHandlers.ofLines(): lines arrive as the server writes them.
    // The reading runs in the background; the test thread stays free to cause events.
    public SseStream(String url, String apiKey) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("X-API-Key", apiKey).header("Accept", "text/event-stream").build();
        reading = HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(r -> { response = r; r.body().forEach(this::line); });
    }

    // ---
    // The SSE format: "field: value" lines, and a blank line ends one event.
    // Lines starting with ":" are comments (Campus Lab sends ": keep-alive" every 15 seconds).
    private String id, name, data, retry;

    private void line(String line) {
        if (line.isEmpty()) {
            if (data != null) {
                events.add(new Event(id, name, data, retry));
                System.out.println("event: " + name + "  id: " + id + "  retry: " + retry);
            }
            id = name = data = retry = null;
        } else if (!line.startsWith(":")) {
            String[] kv = line.split(": ?", 2);
            switch (kv[0]) {
                case "id" -> id = kv[1];
                case "event" -> name = kv[1];
                case "data" -> data = data == null ? kv[1] : data + "\n" + kv[1];
                case "retry" -> retry = kv[1];
                default -> { }
            }
        }
    }

    // --- keep
    // Closing cancels the reading: the connection drops and the server unsubscribes us.
    @Override
    public void close() {
        reading.cancel(true);
        if (response != null) response.body().close();
    }
}
