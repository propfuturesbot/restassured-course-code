// Puts the RetryAnalyzer on every test, so nobody has to remember @Test(retryAnalyzer = ...) on each method.
package framework.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

// --- keep
// An IAnnotationTransformer may change a @Test annotation before TestNG reads it.
public class RetryTransformer implements IAnnotationTransformer {

    // ---
    // Called for every @Test TestNG finds: give each one our analyzer.

    // --- keep
    // end of the class
}
