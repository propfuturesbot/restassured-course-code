// Remembers what a test created and deletes it afterwards, newest first, even when the test failed.
package framework.data;

import java.util.ArrayDeque;
import java.util.Deque;

// --- keep
// Per thread, like the client: a parallel test only ever cleans up its own data.
public final class Cleanup {

    private static final ThreadLocal<Deque<Runnable>> ACTIONS = ThreadLocal.withInitial(ArrayDeque::new);

    private Cleanup() {
    }

    // ---
    // A test registers HOW to undo what it made, right after making it: a lambda such as () -> api.delete(id).
    // push() puts it on top, so things are removed in reverse order (enrollments before their student).


    // ---
    // After every test: run every undo. One failing undo must not stop the others, so each is caught.
    // Returns how many ran, so the log can say what was cleaned.

    // --- keep
    // end of the class
}
