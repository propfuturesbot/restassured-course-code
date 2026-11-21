# Instructions for Claude Code

@AGENTS.md

## Claude specifics

- Before writing a test, read `Campus.java` and one existing test class, and copy their style.
- Work in small steps: write one test class, run it with `mvn -B test -Dtest=<Class>`, fix, then go on.
- When a test fails, say whether the test or the API is wrong, with the real response as evidence.
  Never change an assertion only to make a test green.
- Do not edit `pom.xml`, `Campus.java` or `.github/` unless the task asks for it.
