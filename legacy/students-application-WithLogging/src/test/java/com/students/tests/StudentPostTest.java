package com.students.tests;


import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import com.student.base.TestBase;
import com.student.model.Student;
public class StudentPostTest extends TestBase {

/**
 * Test for creating new student	
 */
	@Test
	public void createNewStudent(){
		ArrayList<String> courses = new ArrayList<>();
		courses.add("Java");
		courses.add("C++");
		
		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("xyz@gmail.com");
		student.setProgramme("Computer Science");
		student.setCourses(courses);
		
		given()
		.contentType(ContentType.JSON)
		.when()
		.body(student)
		.post()
		.then()
		.statusCode(201);
		
	
	}
}
