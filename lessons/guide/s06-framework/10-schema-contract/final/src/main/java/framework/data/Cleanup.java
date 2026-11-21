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

    // --- keep
    // A test registers HOW to undo what it made, right after making it: a lambda such as () -> api.delete(id).
    // push() puts it on top, so things are removed in reverse order (enrollments before their student).
    public static void register(Runnable undo) {
        ACTIONS.get().push(undo);
    }

    // --- keep
    // After every test: run every undo. One failing undo must not stop the others, so each is caught.
    // Returns how many ran, so the log can say what was cleaned.
    public static int runAll() {
        Deque<Runnable> actions = ACTIONS.get();
        int count = 0;
        while (!actions.isEmpty()) {
            try {
                actions.pop().run();
                count++;
            } catch (RuntimeException e) {
                System.out.println("cleanup failed: " + e.getMessage());
            }
        }
        return count;
    }
    // --- keep
    // end of the class
}
