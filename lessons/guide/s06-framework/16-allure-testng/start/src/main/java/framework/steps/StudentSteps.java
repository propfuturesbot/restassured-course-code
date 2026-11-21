// The report's vocabulary: business steps on top of the clients. Each @Step becomes a line in the Allure report.
package framework.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import framework.clients.StudentsApi;
import framework.data.Cleanup;
import framework.models.Student;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

// --- keep
// The clients stay plain; steps are where a report-friendly name lives. {student.email} reads the parameter.
public class StudentSteps {

    private final StudentsApi students = new StudentsApi();

    // ---
    // @Step works through AspectJ (the -javaagent in the pom). The text can use the method's parameters.
    // The student we sent is attached as JSON, so the report shows the exact test data.


    // --- keep
    // Allure.addAttachment: name, MIME type, content, file extension.
    private static void attachJson(String name, Object value) {
        try {
            Allure.addAttachment(name, "application/json", new ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value), ".json");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
