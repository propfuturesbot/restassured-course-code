// A student as a Java record: the request body we send and the object we read back, in one type.
package framework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

// ---
// A record is an immutable data class: fields, constructor, equals, hashCode and toString for free.
// NON_NULL: a new student has no id yet, so "id" is left out of the JSON we send.
// ignoreUnknown: if a target adds fields we do not model, reading still works.


    // ---
    // Records are immutable, so a "change" is a copy: withId and withEmail return a new Student.
    // Because records compare by value, sent.withId(id) equals what the server returns.


    // ---
    // A builder for readable test data: set only what the test cares about, the rest has defaults.

    // --- keep
    // end of the record
}
