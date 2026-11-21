// One student as a Java record: the fields we care about, typed.
package campus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ---
// A record lists its fields once; Jackson fills them from the JSON names (firstName, gpa, ...).
// The API sends more fields than this (fullName, role, createdAt...). ignoreUnknown = true says "skip them";
// without it Jackson stops with UnrecognizedPropertyException on the first field we did not declare.
