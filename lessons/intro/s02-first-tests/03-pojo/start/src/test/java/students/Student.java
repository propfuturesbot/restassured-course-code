// A student as a classic Java class (a POJO): private fields, a no-argument constructor, getters and setters.
// Jackson turns it into JSON when we send it and fills it from JSON when we read it.
package students;

import java.util.List;

public class Student {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String programme;
    private List<String> courses;

    public Student() {
    }

    public Student(String firstName, String lastName, String email, String programme, List<String> courses) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.programme = programme;
        this.courses = courses;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
    public List<String> getCourses() { return courses; }
    public void setCourses(List<String> courses) { this.courses = courses; }
}
