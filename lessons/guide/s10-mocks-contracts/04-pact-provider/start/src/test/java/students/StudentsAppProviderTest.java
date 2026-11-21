// Pact provider verification: replay the student portal's contract against the real Students app.
// Run:  mvn test -Dtest=StudentsAppProviderTest   (the app at STUDENTS_BASE_URI; the pact in src/test/resources/pacts)
package students;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.List;

import static io.restassured.RestAssured.given;

// --- keep
// We are the provider "students-app". The contract file the consumer produced sits in src/test/resources/pacts.
@Provider("students-app")
@PactFolder("pacts")
class StudentsAppProviderTest {

    static final URI APP = URI.create(System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080"));

    // ---
    // Where the real provider runs. Pact sends every request from the contract there
    // and compares the real response with what the consumer said it needs.


    // ---
    // One test template, run once per interaction in the pact: three interactions, three tests.


    // ---
    // Provider states: before each interaction Pact calls the method with the matching name,
    // so the data is what the consumer assumed. Student 1 is part of the seed, so we only check it is there.


    // ---
    // The write needs a free email: the app answers 500 for a duplicate (a known defect).
    // So the state removes any student that already has it, which makes the verification repeatable.
