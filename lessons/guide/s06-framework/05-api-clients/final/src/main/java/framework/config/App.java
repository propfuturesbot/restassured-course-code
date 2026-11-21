// Which application a target runs: the Students app and Campus Lab speak slightly different contracts.
package framework.config;

// --- keep
// STUDENTS: the Spring Boot Students app (jar or Docker). CAMPUS: Campus Lab, our practice API.
public enum App {
    STUDENTS,
    CAMPUS
}
