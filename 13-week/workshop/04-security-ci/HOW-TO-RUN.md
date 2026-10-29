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

Filled in CP8 with the real run.
