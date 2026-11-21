// The same student as a Java record: one line instead of forty.
package students;

import java.util.List;

// ---
// A record declares its fields once; Java writes the constructor, the accessors, equals and toString.
// Jackson can read a record from JSON and write one to JSON, with no setters at all.
public record StudentRecord(Integer id, String firstName, String lastName, String email,
                            String programme, List<String> courses) {
}
