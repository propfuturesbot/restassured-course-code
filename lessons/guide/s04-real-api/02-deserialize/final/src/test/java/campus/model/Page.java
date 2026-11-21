// A page of any kind of item: the shape every Campus Lab list returns ({"data": [...], "meta": {...}}).
package campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// ---
// One generic record for every list: Page<Student>, Page<Course>... T is the type of the items.
// Java erases T at run time, so to read a Page<Student> we must hand REST Assured the full type (TypeRef).
@JsonIgnoreProperties(ignoreUnknown = true)
public record Page<T>(List<T> data, Meta meta) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(int total, int limit, int count, String nextCursor) {
    }
}
