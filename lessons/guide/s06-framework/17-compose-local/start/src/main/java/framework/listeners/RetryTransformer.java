// Puts the RetryAnalyzer on every test, so nobody has to remember @Test(retryAnalyzer = ...) on each method.
package framework.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

// --- keep
// An IAnnotationTransformer may change a @Test annotation before TestNG reads it.
public class RetryTransformer implements IAnnotationTransformer {

    // --- keep
    // Called for every @Test TestNG finds: give each one our analyzer.
    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
    // --- keep
    // end of the class
}
