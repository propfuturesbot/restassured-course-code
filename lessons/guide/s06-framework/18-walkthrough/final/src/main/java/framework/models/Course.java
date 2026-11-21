// A Campus Lab course, the fields the tests use. Extending the framework, step 1 of 4: the model.
package framework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// ---
// A read-only record, like Enrollment: Campus Lab sends more fields, ignoreUnknown skips them.
@JsonIgnoreProperties(ignoreUnknown = true)
public record Course(int id, String code, String title, int credits, int capacity, int enrolled, int seatsAvailable, List<String> prerequisites) {

    public boolean isFull() {
        return seatsAvailable == 0;
    }
}

