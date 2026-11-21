// Tests for the new client. Extending the framework, step 4 of 4: the tests, in a group, Campus Lab only.
// Run:  mvn test -Dtest=CoursesTest -Denv=campus
package tests;

import framework.clients.CoursesApi;
import framework.config.App;
import framework.models.Course;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// --- keep
// The four steps of an extension: model (Course), client (CoursesApi), schemas + contract table, tests.
@Test(groups = "functional")
public class CoursesTest extends BaseTest {

    private final CoursesApi courses = new CoursesApi();

    @BeforeClass(alwaysRun = true)
    public void campusOnly() {
        if (config.app() != App.CAMPUS) throw new SkipException("The course catalog exists only on Campus Lab");
    }

    // ---
    // Walk every page of 15 until a short page: the courses add up to X-Total-Count, and no id repeats.
    public void pagesAddUpToTheTotal() {
        int total = Integer.parseInt(courses.page(1, 0).then().statusCode(200).extract().header("X-Total-Count"));
        List<Course> all = new ArrayList<>();
        for (int offset = 0; ; offset += 15) {
            List<Course> page = courses.pageAsCourses(15, offset);
            all.addAll(page);
            if (page.size() < 15) break;
        }
        assertThat(all).hasSize(total).extracting(Course::id).doesNotHaveDuplicates();
    }

    // ---
    // Domain facts through the record: CS101 has no prerequisites, CS499 (id 9) is full.
    public void catalogFacts() {
        Course cs101 = courses.get(1).then().statusCode(200).extract().as(Course.class);
        assertThat(cs101.code()).isEqualTo("CS101");
        assertThat(cs101.prerequisites()).isEmpty();
        assertThat(courses.get(9).then().statusCode(200).extract().as(Course.class).isFull()).isTrue();
    }
    // --- keep
    // end of the class
}
