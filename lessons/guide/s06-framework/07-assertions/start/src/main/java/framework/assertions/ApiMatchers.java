// Custom Hamcrest matchers for REST Assured's body(...) checks.
package framework.assertions;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.regex.Pattern;

// --- keep
// REST Assured's then().body(path, matcher) takes any Hamcrest matcher, including our own.
public final class ApiMatchers {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[a-z]{2,}$");

    private ApiMatchers() {
    }

    // ---
    // A TypeSafeMatcher<String> only sees Strings (null or a number simply does not match).
    // matchesSafely decides; describeTo is the "Expected: ..." half of the failure message.

    // --- keep
    // end of the class
}
