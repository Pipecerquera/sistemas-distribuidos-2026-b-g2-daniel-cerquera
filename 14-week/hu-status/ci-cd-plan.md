# CI/CD audit, DoD gates and MVP 3 pipeline stories - Week 14

Audited on 2026-11-06 from the workflow files of the 30 code repositories (`develop`) and the GitHub
API. This records what the pipeline **is**, not what it should be; section 5 slices the gap into stories.

## 1. Current pipeline

| Repository type | Workflow | What runs today | Missing against the week's gates |
|---|---|---|---|
| Java `-api`, `workflow` | `ci.yml` | `mvn -B verify` (build, compiler types, unit and HTTP tests) | lint, coverage, dependency and secret scan |
| Python `notifications-api`, `worker` | `ci.yml` | `lint-imports` (hexagonal boundaries), `pytest`; `worker` also builds its image | style lint, type check, coverage, scans |
| `front`, `-app` | `ci.yml` | `npm ci`, `npm test`, `npm run build`; `identity-auth-app` also `tsc --noEmit` | lint, coverage, scans |
| `-db` | `db-ci.yml` | Liquibase changesets against a real engine, path-filtered in `workflow` | scans |
| `api-gateway` | `ci.yml` | image build, `nginx -t`, `tests/smoke.sh` | scans |
| `infra-postgres` | `secrets.yml` | rejects a value in an `env/*.example` file | compose validation |
| `infra-mongo` | none | nothing | a validation workflow |

**Three facts decide the state of the gates:**

1. **No gate blocks a merge.** `required_status_checks` is empty on `develop`, `qa` and `main` in the
   12 repositories queried; a red check does not stop a pull request.
2. **Nothing runs.** GitHub Actions of `code-corhuila` is locked by billing since 2026-10-26: every job
   fails without starting, so local runs replace CI and their output goes in the PR. The 2026-11-06 runs
   of identity-auth-api #33 and #34 show it.
3. **There is no CD.** No workflow reacts to a tag and no image is published; the deployment builds on
   the host (`docker compose up --build`).

## 2. Definition of Done mapped to gates

Items come from [`definition-of-done.md`](https://github.com/code-corhuila/barber-saas-docs/blob/main/00-governance/definition-of-done.md).
That file still says "no repository has a `ci.yml` yet": out of date, fixed by CI-08.

| DoD item | Gate that verifies it | State today | Story |
|---|---|---|---|
| Code follows project standards | Linter per stack, blocking | Only `lint-imports` in Python | CI-02 |
| Types correct | Strict type check | Java compiler; `tsc` not in every app; Python none | CI-02 |
| Unit tests for new logic | Unit tests | Run in the workflow of the code repositories | CI-01 |
| Coverage does not decrease | Coverage floor from a measured baseline | Not measured: no baseline exists | CI-03 |
| All tests pass in CI | Required status check | Run when unlocked; not required | CI-01 |
| Changes do not break other services | Cross-service contract tests | Not automated; end-to-end scripts run by hand | CI-05 |
| OpenAPI contract updated | Provider response checked against `07-api/contracts` | Manual review | CI-05 |
| No secrets or known vulnerabilities | Secret and dependency scan | Env examples in `infra-postgres` only | CI-04 |
| Smoke test through the gateway | `/health` and one authenticated route after deploy | `tests/smoke.sh` for the gateway alone | CI-06 |
| Merged with green CI | Branch protection with required checks | Protection exists, without required checks | CI-01 |
| Promotion with `cherry-pick -x` | Script that checks the trail | Checked by hand in each promotion | CI-07 |
| Declares its story, Conventional Commits, under 400 lines | PR title and body check | `env-tracking.yml` moves the story; the rest by hand | CI-07 |
| Review, report rubric, ADR, consistent docs | Human review, not automatable | Manual, stays manual | none |

## 3. Branch protection

| Branch | Approvals | Required checks | Applies to admins |
|---|---|---|---|
| `develop` | 0 | none | yes |
| `qa` | 0 | none | yes |
| `main` | 1 | none | yes |

Protection is already on in the three branches. **Not done this week:** adding required checks. The
course norm treats changing protection rules as a serious fault outside the teacher's hands, and requiring
a check that cannot run would block every merge while Actions is locked. It needs the billing fix first,
then the teacher's change.

## 4. Pipeline design for the polyrepo

Each of the 30 repositories is one service, so **a repository's own workflow already is the per-service
pipeline**: a push to `schedule-api` runs nothing in `loyalty-api`. Path filters only save minutes inside
a repository (`docs/**` and `*.md` skip the build); `workflow` already filters its `db/**` job.

```
PR -> build -> lint + types -> tests -> coverage -> scans -> required check -> merge to develop
develop -> cherry-pick -x -> qa -> contract tests + smoke -> tag vX.Y.Z -> image in GHCR -> deploy -> smoke
```

Contract tests go in `infra-postgres`: it already composes every repository, so one job can bring the
platform up and check each public route against its OpenAPI file. The manual promotion keeps this
**continuous delivery, not continuous deployment**: `main` needs the teacher's approval.

## 5. CI/CD stories for MVP 3

| ID | Story | Acceptance criteria (testable) | Depends on |
|---|---|---|---|
| CI-01 | As the team, we want a red check to block the merge, so `develop` stays releasable | With a failing test, the PR's merge button is disabled on `develop`, `qa` and `main` | Billing; teacher |
| CI-02 | As a developer, I want lint and strict types in every stack, so style and type errors stop at the PR | An unused import or a wrong type fails the job in a Java, a Python and a TypeScript repository | none |
| CI-03 | As the team, we want a coverage floor from a measured baseline, so tests cannot quietly shrink | JaCoCo and `pytest-cov` report per repository; a PR below the recorded floor fails | none |
| CI-04 | As the operator, I want secret and dependency scans, so a leaked key or a known CVE stops the PR | A test secret fails the secret scan; a dependency with a known high CVE fails the scan | HU-000-061 |
| CI-05 | As the team, we want cross-service contract tests, so a provider cannot break its callers silently | Renaming a field of a public response without updating its OpenAPI file fails the job | CI-01 |
| CI-06 | As the operator, I want a smoke test after each deploy, so a bad release is seen in a minute | The script fails when `/health` or one authenticated route through the gateway fails | none |
| CI-07 | As the team, we want the norm's checks automated, so a promotion cannot lose its trail | A commit in a `qa` PR without `cherry picked from` fails; a PR without its story fails | none |
| CI-08 | As the team, we want a tagged image deployed by one action, so `qa` and `main` run the same bits (extends DEP-01, DEP-02) | Tag `v3.0.1` publishes the image to GHCR and the manual deploy pulls it; the DoD's "no CI" notes are removed | CI-01; a host |

**Order:** CI-02, CI-03, CI-06 and CI-07 need nobody else and can be written now, running locally until
Actions is unlocked. CI-01, CI-05 and CI-08 wait for billing, the teacher or the host.

## 6. Delivery health (DORA)

Only what was measured: **time to restore** 40 s for one service (rollback rehearsal on the laptop host,
2026-11-06); **deployment frequency** two to the public host, the release candidate and the sign-up fix.
**Lead time** and **change failure rate** have no data yet: there is one deployment day and no production
traffic. They start counting with the first tagged release.

## 7. Risks

- The billing lock is outside the team's control and blocks CI-01, CI-05 and CI-08.
- Adding gates to 30 repositories in one go would stall the 3.0.0 release: they go one stack at a time.
- Local runs are honest but not enforced: a merge relies on the author having run the tests.
