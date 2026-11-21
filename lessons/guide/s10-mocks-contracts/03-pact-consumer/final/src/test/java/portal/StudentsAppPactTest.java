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
    @Pact(consumer = "student-portal")
    V4Pact studentOne(PactDslWithProvider builder) {
        return builder.given("student 1 exists")
                .uponReceiving("a request for student 1")
                .path("/student/1").method("GET").headers("Accept", "application/json")
                .willRespondWith().status(200)
                .body(new PactDslJsonBody()
                        .integerType("id", 1)
                        .stringType("firstName", "Vernon")
                        .stringType("lastName", "Harper")
                        .stringMatcher("email", "^\\S+@\\S+\\.\\S+$", "egestas.rhoncus.Proin@massaQuisqueporttitor.org")
                        .stringType("programme", "Financial Analysis")
                        .eachLike("courses", PactDslJsonRootValue.stringType("Accounting"), 1))
                .toPact(V4Pact.class);
    }

    // ---
    // The consumer test runs against Pact's mock server (it plays the provider from the interaction).
    // If the client called a different path or header, the mock would answer 500 and the test would fail.
    @Test
    @PactTestFor(pactMethod = "studentOne")
    void theClientReadsAStudent(MockServer mock) {
        var response = new StudentClient(mock.getUrl()).getStudent(1);
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.path("firstName"), not(emptyString()));
    }

    // ---
    // The unhappy path is part of the contract too: a missing student is 404 with an "error" message.
    @Pact(consumer = "student-portal")
    V4Pact missingStudent(PactDslWithProvider builder) {
        return builder.given("student 9999 does not exist")
                .uponReceiving("a request for a student that does not exist")
                .path("/student/9999").method("GET").headers("Accept", "application/json")
                .willRespondWith().status(404)
                .body(new PactDslJsonBody().stringType("error", "Not Found!"))
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "missingStudent")
    void theClientSeesA404(MockServer mock) {
        assertThat(new StudentClient(mock.getUrl()).getStudent(9999).statusCode(), equalTo(404));
    }

    // ---
    // A write: POST /student with this body answers 201 and the message "Student added".
    // The provider state tells the provider to make sure the email is free before the request is replayed.
    @Pact(consumer = "student-portal")
    V4Pact addStudent(PactDslWithProvider builder) {
        return builder.given("no student has the email pact.ada@example.com")
                .uponReceiving("a request to add a student")
                .path("/student").method("POST").headers("Content-Type", "application/json")
                .body(new PactDslJsonBody().stringValue("firstName", "Ada").stringValue("lastName", "Lovelace")
                        .stringValue("email", "pact.ada@example.com").stringValue("programme", "Computer Science")
                        .array("courses").stringValue("Java").closeArray())
                .willRespondWith().status(201)
                .body(new PactDslJsonBody().stringValue("msg", "Student added"))
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "addStudent")
    void theClientAddsAStudent(MockServer mock) {
        var response = new StudentClient(mock.getUrl()).addStudent(Map.of("firstName", "Ada", "lastName", "Lovelace",
                "email", "pact.ada@example.com", "programme", "Computer Science", "courses", List.of("Java")));
        assertThat(response.statusCode(), equalTo(201));
        assertThat(response.path("msg"), equalTo("Student added"));
    }
}
