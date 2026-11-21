// The first test of the framework: the Students app answers and lists its students.
// Run:  mvn test   (the whole suite from testng.xml)   or   mvn test -Dtest=StudentListTest
package tests;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

// ---
// A TestNG test class is a plain class; every method marked @Test is one test.
// For now the address comes straight from an environment variable; the config layer replaces this soon.


    // ---
    // The list endpoint answers 200 with a JSON array that is not empty.


    // ---
    // One student by id: the first seeded student has an id, a first name and an email.

    // --- keep
    // end of the class
}
