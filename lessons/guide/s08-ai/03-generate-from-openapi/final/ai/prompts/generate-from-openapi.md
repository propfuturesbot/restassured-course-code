# Prompt: generate tests from the OpenAPI spec, run them, review them

Written by the course. Paste the three prompts one after the other into Claude Code, opened in this folder.
The spec excerpt is `ai/inputs/openapi-courses.json` (Campus Lab's two course paths, cut from its openapi.json).

## 1. Generate

---
Read ai/inputs/openapi-courses.json. It describes /api/v1/courses and /api/v1/courses/{course_id} of Campus Lab.
Write CoursesSmokeTest and CoursesRegressionTest in src/test/java/campus, following AGENTS.md.
Cover each operation once on the happy path and the error responses the spec lists for POST and PATCH.
Do not run anything yet. List every test you wrote, one line each, with the status code it expects.
---

## 2. Run

---
Run mvn -B test -Dtest='Courses*' and show me the real result. For every failing test, show the real
response body and tell me whether the test or the API is wrong. Do not change any test yet.
---

## 3. Review

---
Now review your own tests as a strict reviewer. Which tests are weak: they would still pass if the API were broken?
Which status codes in the spec did you not test, and why? Fix the failing tests whose expectation was wrong
and strengthen the weak ones, run the class again, and show the result.
---
