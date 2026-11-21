# Prompt: AI review of a pull request of tests

Written by the course. The pull request is `ai/inputs/pr-patch-tests.patch` (a git format-patch of one commit)
with its description in `ai/inputs/pr-description.md`. On GitHub the same commit is the open pull request
"Add PATCH tests for students; drop the flaky total check" (branch add-patch-tests).

Make the real pull request (owner, once, in the private course repository):
`git checkout -b add-patch-tests && git am ai/inputs/pr-patch-tests.patch && git push -u origin add-patch-tests`,
then open the PR with the text of pr-description.md.

## 1. Review

---
Review the pull request add-patch-tests (gh pr diff, or ai/inputs/pr-patch-tests.patch) against AGENTS.md.
For every problem give the file and line, why it matters, and how sure you are (sure / likely / guess).
Separate problems that make tests fail sometimes from problems that make tests pass when they should not.
Do not run anything yet.
---

## 2. Prove it

---
Check out the branch and prove your two most important findings with real runs, for example running the suite
twice in a row, or only the regression group as the nightly job does. Show the real output.
---

## 3. What did you miss?

---
Read the diff again, line by line, as if your first review missed something. What is left?
---
