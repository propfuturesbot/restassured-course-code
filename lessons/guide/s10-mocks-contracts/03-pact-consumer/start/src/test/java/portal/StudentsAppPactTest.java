// Consumer-driven contract with Pact: the portal writes down what it needs from the Students app,
// tests itself against Pact's mock server, and Pact saves the contract to target/pacts/.
// Run:  mvn test -Dtest=StudentsAppPactTest   (no real app needed)
package portal;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;

// --- keep
// The consumer is "student-portal", the provider is "students-app": those names go into the pact file name.
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "students-app")
class StudentsAppPactTest {

    // ---
    // An interaction: in the provider state "student 1 exists", GET /student/1 answers 200 with a student.
    // The matchers say what MATTERS, not exact values: firstName is some string, id is an integer,
    // email looks like an email, courses is a list of strings. The values are only examples.


    // ---
    // The consumer test runs against Pact's mock server (it plays the provider from the interaction).
    // If the client called a different path or header, the mock would answer 500 and the test would fail.


    // ---
    // The unhappy path is part of the contract too: a missing student is 404 with an "error" message.


    // ---
    // A write: POST /student with this body answers 201 and the message "Student added".
    // The provider state tells the provider to make sure the email is free before the request is replayed.
