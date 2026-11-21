# Prompt: triage a red CI run, from logs and Allure to a root cause

Written by the course. The red run is real: `ai/inputs/red-run/` holds the CI log of the PR check
(`ci-log.txt`, the "PR smoke" workflow run with `mvn -B test -Dgroups=smoke`), the surefire reports and the
Allure results of the same command. On a developer machine `mvn -B test` is green.

## 1. From the evidence only

---
The PR check "PR smoke" is red, but `mvn -B test` is green on my machine. Read ai/inputs/red-run/ci-log.txt,
the surefire reports and the Allure results in ai/inputs/red-run/allure-results (the request and response of
each test are attachments). Do not open the test code yet.
Tell me: which test failed, the exact request it sent and the response it got, and your hypotheses for why,
ranked, each with the evidence for it.
---

## 2. Confirm with the code and one run

---
Now read the code of the failing test and of the workflow. Which hypothesis is right? Prove it with one command
that fails the same way on my machine, run it, and show the output.
---

## 3. Fix the cause, not the symptom

---
Fix the root cause so the test passes in any group and in any order, following AGENTS.md. Do not add
dependsOnMethods or a group to hide the problem. Run both `mvn -B test` and `mvn -B test -Dgroups=smoke`.
---
