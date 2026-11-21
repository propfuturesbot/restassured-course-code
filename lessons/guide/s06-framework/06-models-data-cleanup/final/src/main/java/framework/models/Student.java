// A student as a Java record: the request body we send and the object we read back, in one type.
package framework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

// ---
// A record is an immutable data class: fields, constructor, equals, hashCode and toString for free.
// NON_NULL: a new student has no id yet, so "id" is left out of the JSON we send.
// ignoreUnknown: if a target adds fields we do not model, reading still works.
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Student(Integer id, String firstName, String lastName, String email, String programme,
                      List<String> courses) {

    // ---
    // Records are immutable, so a "change" is a copy: withId and withEmail return a new Student.
    // Because records compare by value, sent.withId(id) equals what the server returns.
    public Student withId(Integer newId) {
        return new Student(newId, firstName, lastName, email, programme, courses);
    }

    public Student withEmail(String newEmail) {
        return new Student(id, firstName, lastName, newEmail, programme, courses);
    }

    // ---
    // A builder for readable test data: set only what the test cares about, the rest has defaults.
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String firstName = "Ada";
        private String lastName = "Lovelace";
        private String email = "ada@example.com";
        private String programme = "Computer Science";
        private List<String> courses = List.of("Java");

        public Builder firstName(String v) { firstName = v; return this; }
        public Builder lastName(String v) { lastName = v; return this; }
        public Builder email(String v) { email = v; return this; }
        public Builder programme(String v) { programme = v; return this; }
        public Builder courses(String... v) { courses = List.of(v); return this; }

        public Student build() {
            return new Student(null, firstName, lastName, email, programme, courses);
        }
    }
    // --- keep
    // end of the record
}
