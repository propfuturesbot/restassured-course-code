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
// Functional: slower, deeper checks. Methods inherit the class-level group.
@Test(groups = "functional")
// --- keep
// Thread name -> identity of the specification that thread used. ConcurrentHashMap: written by four threads.
public class ThreadSafetyTest {

    private final Map<String, Set<Integer>> specsByThread = new ConcurrentHashMap<>();

    // --- keep
    // invocationCount = 8 runs this method eight times; threadPoolSize = 4 spreads them over four threads.
    // Each call records which specification object its thread used, then sends a real request.
    @Test(invocationCount = 8, threadPoolSize = 4)
    public void listFromManyThreads() {
        specsByThread.computeIfAbsent(Thread.currentThread().getName(), t -> ConcurrentHashMap.newKeySet())
                .add(System.identityHashCode(ApiClient.spec()));
        ApiClient.given().when().get("/student/list").then().statusCode(200).body("size()", greaterThan(0));
    }

    // --- keep
    // After the eight calls: every thread used exactly ONE specification, and no two threads shared one.
    @AfterClass
    public void eachThreadHadItsOwnSpec() {
        long specs = specsByThread.values().stream().flatMap(Set::stream).distinct().count();
        System.out.println("8 calls on " + specsByThread.size() + " threads used " + specs + " specifications");
        specsByThread.values().forEach(set -> assertEquals(set.size(), 1, "a thread changed specification"));
        assertEquals(specs, (long) specsByThread.size(), "two threads shared a specification");
    }
    // --- keep
    // end of the class
}
