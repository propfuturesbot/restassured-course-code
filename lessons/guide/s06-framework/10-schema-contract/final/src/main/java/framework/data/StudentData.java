// Random but valid students from Datafaker. Every email is unique, so tests never collide.
package framework.data;

import framework.models.Student;
import net.datafaker.Faker;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

// --- keep
// One Faker for the run, seeded: -Dseed=42 replays exactly the same names when a failure must be reproduced.
public final class StudentData {

    private static final long SEED = Long.getLong("seed", System.currentTimeMillis());
    private static final Faker FAKER = new Faker(Locale.ENGLISH, new Random(SEED));
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final List<String> PROGRAMMES = List.of("Computer Science", "Mathematics", "Physics", "Business");
    private static final List<String> COURSES = List.of("Java", "Algebra", "Statistics", "Databases", "Networks");

    private StudentData() {
    }

    public static long seed() {
        return SEED;
    }

    // --- keep
    // A complete, valid student. Names come from Datafaker; the email is built from them plus a counter
    // and the clock, so two runs (or two threads) never create the same email. Duplicate emails are a
    // bug trigger in the Students app, so uniqueness is not optional.
    // synchronized: Faker's Random is shared, and parallel tests will call this at the same time.
    public static synchronized Student randomStudent() {
        String first = FAKER.name().firstName();
        String last = FAKER.name().lastName();
        String email = (first + "." + last).toLowerCase().replaceAll("[^a-z.]", "")
                + "." + Long.toString(System.nanoTime(), 36) + COUNTER.incrementAndGet() + "@example.com";
        return Student.builder()
                .firstName(first).lastName(last).email(email)
                .programme(FAKER.options().nextElement(PROGRAMMES))
                .courses(FAKER.options().nextElement(COURSES), "Testing")
                .build();
    }
    // --- keep
    // end of the class
}
