// An enrollment as Campus Lab returns it (the fields the tests use; the rest is ignored).
package framework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// --- keep
// Read-only model: we never send it, Campus Lab builds it from {studentId, courseId}.
@JsonIgnoreProperties(ignoreUnknown = true)
public record Enrollment(int id, int studentId, int courseId, String status, String term) {
}
