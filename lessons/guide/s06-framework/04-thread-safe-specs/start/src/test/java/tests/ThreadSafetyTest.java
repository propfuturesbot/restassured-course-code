// Proves the client is per thread: eight calls on four threads, and each thread sends with its own specification.
// Run:  mvn test -Dtest=ThreadSafetyTest -Denv=docker
package tests;

import framework.clients.ApiClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.greaterThan;
import static org.testng.Assert.assertEquals;

// --- keep
// Thread name -> identity of the specification that thread used. ConcurrentHashMap: written by four threads.
public class ThreadSafetyTest {

    private final Map<String, Set<Integer>> specsByThread = new ConcurrentHashMap<>();

    // ---
    // invocationCount = 8 runs this method eight times; threadPoolSize = 4 spreads them over four threads.
    // Each call records which specification object its thread used, then sends a real request.


    // ---
    // After the eight calls: every thread used exactly ONE specification, and no two threads shared one.

    // --- keep
    // end of the class
}
