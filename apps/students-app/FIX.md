# studentApp-java21.jar: what changed

`studentApp-java21.jar` is the course's own `studentApp-java17.jar` (Spring Boot 3.1.2, the same compiled classes)
with ONE line added to `BOOT-INF/classes/import.sql`:

    ALTER SEQUENCE student_seq RESTART WITH 150;

Why: the 100 seeded students are inserted with ids 1 to 100, but Hibernate's id sequence started at 1, so every
POST collided with a seeded row and the app answered 500 "Email address already exists" until the 100th try.
Hibernate hands out ids in blocks of 50 below the sequence value, so 150 makes the first new student id 101.

Nothing else changed (no source was needed). Still as before, on purpose (lessons test these as found defects):
- a duplicate email, and validation errors, answer 500 instead of 409 / 400;
- PATCH changes the email only; GET /student/list filters by programme (and limit) only.

Rebuild it yourself: unzip import.sql from the java17 jar, append the line, `zip studentApp-java21.jar BOOT-INF/classes/import.sql`.
