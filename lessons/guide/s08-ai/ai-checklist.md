# AI in API testing: the guardrails checklist

Use it before you paste anything into an AI tool, and before you merge anything it wrote.

## Before the prompt: what goes in

- [ ] No secrets. No API keys, passwords, tokens, client secrets, cookies or `.env` files in the prompt, in a
      pasted log or in a file the agent can read. Tests read them from environment variables; the agent never
      needs the value, only the variable name (`CAMPUS_API_KEY`).
- [ ] No production data. No real customer names, emails, IDs, payment data or production responses. Use a
      sandbox (Campus Lab gives every key its own copy of fake data) or data you generated.
- [ ] Logs and reports are scrubbed before you share them. Search them for the key value first
      (`grep -rF "$CAMPUS_API_KEY" target/`). An Allure report records every request header: without
      `LogConfig.blacklistHeader("X-API-Key")` our key was in 8 attachment files of one run.
- [ ] Check what the tool may do with your data (training, retention) and that your company allows it.
- [ ] Give it the real contract (the OpenAPI spec, a JSON schema) and your conventions file (CLAUDE.md /
      AGENTS.md), not a description from memory.

## After the answer: what comes out

- [ ] Every claim about the API is verified with a real request. The spec can be wrong: Campus Lab accepts
      `year: "2"` although its schema says integer, and rejects `"not-an-email"` although its schema has no format.
- [ ] Every test it wrote has been RUN by you, and you saw the real `Tests run:` line. "All tests pass" is not a
      result until you ran them.
- [ ] Each test can fail. Break the API's answer on purpose (or the expectation) once: a test that stays green
      checks nothing (`assertTrue(status < 500)`, status-only checks, a 422 that comes before the lookup).
- [ ] Run it twice, and run it by group (`-Dgroups=smoke`, `-Dgroups=regression`): fixed test data and hidden
      dependencies between tests only show up then.
- [ ] No new secrets or hard-coded URLs in the diff; requests go through the shared spec.
- [ ] A failing test is not "fixed" by changing the assertion to what the API returns, by `dependsOnMethods`
      that turns it into a skip, or by a retry. Find the cause.
- [ ] Review the diff like a colleague's pull request: you are responsible for what you merge, not the tool.
- [ ] Say what the AI did and what you did, in the PR description.
