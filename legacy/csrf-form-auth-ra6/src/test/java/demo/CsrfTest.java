package demo;
import io.restassured.filter.session.SessionFilter;
import io.restassured.authentication.FormAuthConfig;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
public class CsrfTest {
  @Test public void formLoginWithCsrf() {
    SessionFilter s = new SessionFilter();
    given().baseUri("http://localhost:8080").csrf("/").auth().form("user","user", new FormAuthConfig("/login","uname","pwd")).filter(s)
      .when().get("/student/list").then().statusCode(200).body(containsString("<table"));
  }
}
