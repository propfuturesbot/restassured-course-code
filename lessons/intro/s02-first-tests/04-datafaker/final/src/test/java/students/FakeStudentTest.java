// Unique, realistic test students from Datafaker, sent to the Students app.
// Run:  mvn test -Dtest=FakeStudentTest   (the app at STUDENTS_BASE_URI, default http://localhost:8080)
package students;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

// --- keep
// The base URI comes from an environment variable, as in every lesson.
class FakeStudentTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080");
        RestAssured.basePath = "/student";
    }

    // ---
    // One Faker builds every value. Names and programmes look real, so the data reads well in reports.
    // The email gets a random UUID part: the app rejects a duplicate email, so every run must use new ones.
    // Names like O'Connell carry an apostrophe, so we keep only letters for the email.
    static Student fakeStudent(Faker faker) {
        String first = faker.name().firstName();
        String last = faker.name().lastName();
        String email = (first + "." + last).toLowerCase().replaceAll("[^a-z.]", "")
                + "." + faker.internet().uuid().substring(0, 8) + "@example.com";
        String programme = faker.options().option("Computer Science", "Mathematics", "Physics", "Economics");
        List<String> courses = List.of(faker.educator().course(), faker.educator().course());
        return new Student(first, last, email, programme, courses);
    }

    // ---
    // Create three different students and prove each one is really in the app afterwards.
    @Test
    void createThreeFakeStudents() {
        Faker faker = new Faker();
        for (int i = 0; i < 3; i++) {
            Student student = fakeStudent(faker);
            System.out.println("Creating " + student.getFirstName() + " " + student.getLastName()
                    + " <" + student.getEmail() + "> in " + student.getProgramme());
            given().contentType(ContentType.JSON).body(student)
                    .when().post()
                    .then().statusCode(201).body("msg", equalTo("Student added"));
            given().when().get("/list")
                    .then().body("email", hasItem(student.getEmail()));
        }
    }

    // ---
    // A seed makes Faker repeatable: the same seed gives the same "random" student every time.
    // Handy when a failing test must be replayed with exactly the same data.
    @Test
    void sameSeedSameData() {
        String first = new Faker(new Random(42)).name().fullName();
        String again = new Faker(new Random(42)).name().fullName();
        System.out.println("Seed 42 gives: " + first);
        assertEquals(first, again);
    }
    // --- keep
    // end of the class
}
