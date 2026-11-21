// A transcript as a classic POJO: private fields, a no-argument constructor, getters and setters.
package campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// ---
// The older style, still everywhere in real projects: Jackson calls the empty constructor, then the setters.
// Nested JSON becomes nested classes: a transcript has a student summary and a list of course results.


    // --- keep
    // The two nested shapes are records: short, and Jackson reads records as well as POJOs.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StudentSummary(int id, String name, String programme, int year) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CourseResult(String code, String title, int credits, String term, String status,
                               String grade, Double points) {
    }
}
