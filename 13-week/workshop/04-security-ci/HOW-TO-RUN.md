# How to run the security gates and the negative test

Service: `identity-auth-api` (Java 21, Spring Boot 3.5, Maven; modules `identity-auth-core`,
`identity-auth-adapters`, `identity-auth-app`). Boundary: TB1 Internet -> edge.

## What it covers

| File | Story | Acceptance criterion |
|---|---|---|
| `ExpiredAccessTokenHttpTest.java` | [#156](https://github.com/code-corhuila/barber-saas-docs/issues/156) A stolen access token stops working within 15 minutes (STRIDE S, risk 6) | 1: a token with `exp` in the past answers `401` on `GET /api/v1/auth/me` |
| `security.yml` job `secret-scan` | Team backlog SEC-04 | A PR that adds a key fails |
| `security.yml` job `dependency-audit` | Team backlog SEC-05 | A dependency with CVSS >= 7 fails the PR |
| `security.yml` job `authorization-tests` | #156 | The negative test runs on every PR |

The test signs the token with the service's own `Rs256TokenIssuer` (the same key the service
validates with), so the only thing wrong with the token is its age: issued 16 minutes ago with a
15-minute lifetime. The second test is the control case: the same kind of token inside its lifetime
must not get `401`.

## Run the negative test locally

1. Copy `ExpiredAccessTokenHttpTest.java` to
   `identity-auth-api/identity-auth-app/src/test/java/co/edu/corhuila/barbersaas/identityauth/app/`.
2. From the root of `identity-auth-api`:

   ```bash
   mvn -B -pl identity-auth-app -am test -Dtest='ExpiredAccessTokenHttpTest' -Dsurefire.failIfNoSpecifiedTests=false
   ```

3. Expected: `Tests run: 2, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

The same request against the running platform (through the gateway):

```bash
curl -i http://localhost:8000/api/v1/auth/me -H "Authorization: Bearer <expired token>"
# HTTP/1.1 401 ... {"error":"UNAUTHORIZED", ...}
```

## Run the gates locally

```bash
gitleaks detect --source . --redact                                   # secret-scan
mvn -B org.owasp:dependency-check-maven:12.1.0:aggregate -DfailBuildOnCVSS=7   # dependency-audit
```

The workflow is a proposal for the team pipeline; GitHub Actions of the organization is locked by a
billing issue today, so until it is fixed these commands run before each PR and their output goes in
the PR description.

## Console output

Run on 2026-10-30 against `identity-auth-api` (branch `develop`), Maven 3.9.11, JDK 21.0.12, with
the command of step 2. The test file was copied into the service only for the run and then removed.
Spring log lines are trimmed; the two access lines are the requests of the test:

```text
[INFO] Running co.edu.corhuila.barbersaas.identityauth.app.ExpiredAccessTokenHttpTest
... "logger":"access" ... "message":"request method=GET path=/api/v1/auth/me status=404 durationMs=92"
... "logger":"access" ... "message":"request method=GET path=/api/v1/auth/me status=401 durationMs=0"
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.630 s -- in co.edu.corhuila.barbersaas.identityauth.app.ExpiredAccessTokenHttpTest
[INFO] Results:
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Reactor Summary for identity-auth 0.1.0:
[INFO] identity-auth ...................................... SUCCESS [  0.000 s]
[INFO] identity-auth-core ................................. SUCCESS [  2.194 s]
[INFO] identity-auth-adapters ............................. SUCCESS [  1.538 s]
[INFO] identity-auth-app .................................. SUCCESS [  6.140 s]
[INFO] BUILD SUCCESS
[INFO] Total time:  10.217 s
[INFO] Finished at: 2026-10-30T00:31:38-05:00
```

The expired token gets `401`. The control token passes the signature and lifetime checks and gets
`404` only because its random user id is not in the test database, which is what "not `401`" means.

## Workflow validation

`security.yml` checked on 2026-10-30 with actionlint 1.7.7 (Windows build from the rhysd/actionlint
releases):

```text
$ actionlint 13-week/workshop/04-security-ci/security.yml; echo "exit=$?"
exit=0
```

No errors and exit code `0`: the syntax, the job keys and the `${{ }}` expressions are valid.
