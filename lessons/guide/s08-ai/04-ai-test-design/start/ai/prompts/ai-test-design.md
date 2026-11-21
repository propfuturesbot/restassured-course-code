# Prompt: test-case design from a JSON schema (boundaries, negatives, auth matrix)

Written by the course. Inputs: `ai/inputs/student-create.schema.json` (the body of POST /api/v1/students)
and `ai/inputs/auth.json` (how Campus Lab lets a caller in). Paste the prompts one after the other.

## 1. Design first, no code

---
Read ai/inputs/student-create.schema.json and ai/inputs/auth.json.
Design the test cases for POST /api/v1/students as three tables, no code yet:
1. Boundaries: for every field with a limit, the value just inside and just outside each limit, and the expected status.
2. Negatives: missing required fields, wrong types, values outside the enum, an unknown field, null.
3. Auth matrix: no credentials, a wrong API key, a valid API key, Basic, a Bearer token with only students:read,
   a Bearer token with students:write. Expected status for each.
Mark every expectation that the schema alone does not prove (you are guessing) with "?".
---

## 2. Code it, data-driven

---
Turn tables 1 and 2 into one TestNG class StudentCreateValidationTest with a @DataProvider (one row per case:
a name, the body change, the expected status, the expected errors[0].field). Turn table 3 into StudentAuthMatrixTest.
Get the Basic user and password from CAMPUS_BASIC_USER / CAMPUS_BASIC_PASSWORD and the OAuth client from
CAMPUS_CLIENT_ID / CAMPUS_CLIENT_SECRET (POST /oauth/token, grant_type client_credentials, scope ...). Follow AGENTS.md.
Run both classes and show me the real result.
---

## 3. Where was the schema wrong?

---
For every row that failed: show the real response, and say whether the schema, the API or your expectation is wrong.
Update the expectations only where the real behaviour is the documented or sensible one, and list the rows
where the API and the schema disagree, so I can report them.
---
