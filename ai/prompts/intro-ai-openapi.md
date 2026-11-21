# Prompt: tests from Campus Lab's OpenAPI spec

Paste this into Claude Code, opened in the lesson project folder (the one with CLAUDE.md and pom.xml).

---

Read CLAUDE.md first and follow it.

Download the OpenAPI spec of Campus Lab with
`curl -s "$CAMPUS_BASE_URL/openapi.json" -o openapi.json` and use only what it says.

Write a JUnit 6 + REST Assured test class `src/test/java/campus/StudentsApiTest.java` for these operations of
`/api/v1/students`:

1. GET /api/v1/students with `programme` and `limit`: the list holds only that programme and no more than `limit` items.
2. POST /api/v1/students: 201, the body is the created student, and the Location header points to it.
3. GET /api/v1/students/{student_id}: the student just created comes back with the same fields.
4. POST the same email twice: the second answer is the conflict the spec documents.
5. POST with invalid fields: the validation error the spec documents, with one entry per bad field.
6. GET a student id that does not exist: the 404 the spec documents.

Then run `mvn -B test -Dtest=StudentsApiTest`, show me the output, and fix any test that fails because the TEST
is wrong (not the API). Do not change a status code just to make a test pass: if the API disagrees with the spec,
stop and tell me.
