# Audit against the M1–M9 list of 2026-11-10

On 2026-11-10 the teacher shared nine practices ("mejoras" M1–M9) and warned that missing them lowers
the grade a lot. This report checks BarberSaaS against each one, in the format the list itself asks
for: declared scope (M5), severity first (M9) and a verdict computed by a rule (M6).

## Scope (M5)

- **Read:** the 30 code repositories of the polyrepo (`develop`, `qa`, `release.3.0.0`), `barber-saas-docs`
  and this repository: their tests, configuration, seeds, `env/*.example` files and the open issues of
  DOCS. Secrets: **every commit of every branch of the 32 repositories** (gitleaks 8.30.0). Tests of the
  repositories I changed: run in full (`mvn verify`, `pytest`).
- **Not checked:** the tests of my teammates' repositories were read, not run. The system was not yet
  started from a clean folder following only the manual (planned for 2026-11-12 on the Azure host). No
  manual penetration test was done.
- **Left to decide:** the Wompi sandbox account (it depends on a person, not on code), and the five
  security issues assigned to Carlos and Juan Pablo.

## Rule for the verdict (M6)

Each practice gets a level from its evidence: **met** (evidence merged), **partial** (evidence for part
of it, the rest assigned with an owner) or **not met**. The audit **passes** when no practice is
*not met* and no P0 or P1 finding is open without an owner and evidence in progress. The level comes
from the evidence column, not from an opinion.

## Findings, severity first (M9)

### P0 and P1 — complete

| # | Finding | Practice | Status | Evidence |
|---|---|---|---|---|
| 1 | DOCS had 1 open P0 and 8 open P1 security issues, five of them already fixed in code but never closed | M3 | **Fixed for 5**, in progress for 5 | Closed with evidence per criterion: [#155](https://github.com/code-corhuila/barber-saas-docs/issues/155) (P0), [#156](https://github.com/code-corhuila/barber-saas-docs/issues/156), [#165](https://github.com/code-corhuila/barber-saas-docs/issues/165), [#166](https://github.com/code-corhuila/barber-saas-docs/issues/166), [#168](https://github.com/code-corhuila/barber-saas-docs/issues/168). My part of #158, #159, #160 and #164 is merged; the rest has an owner and a prompt (Carlos: #163, #164; Juan Pablo: #158–#160) |
| 2 | `main` could mark a plan as paid through the simulated checkout, with no event from the provider | M8 | **Fixed** | `PAYMENTS_SIMULATOR=false` on `main` ([infra-postgres #51](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/51) → `qa` #52 → `release.3.0.0` #53). Negative tests already in platform-admin-api: a forged event is `401` and changes nothing; without a checkout, paying is `503` |
| 3 | Service tokens lived one year, with no audience and no scope; one leaked worker token could call any internal operation, including the outbox where the reset codes travel | M3 (#158, #159, #160) | **Fixed on the issuing side** | identity-auth-api #39, platform-admin-api #28, worker #25, infra-postgres #48, DOCS #177; promoted to `qa` and `release.3.0.0` |

### P2 and P3 — grouped

| Practice | Status | Evidence |
|---|---|---|
| M1 cross-ownership tests | Met, with its gaps assigned | Table in DOCS `11-quality/cross-ownership-tests.md` ([#179](https://github.com/code-corhuila/barber-saas-docs/pull/179)); new test for #166 criterion 2 (identity-auth-api #42) |
| M2 secrets in code, docs and history | Met | DOCS `11-quality/secret-scan-2026-11-10.md`: 0 real secrets, 54 false positives read one by one |
| M3 grave findings closed | Partial | DOCS `15-project-control/declared-debt-3.0.0.md`: every SEC and OBS item without code has a decision; #160 criteria 2 and 4 downgraded with a written reason |
| M4 manual executed | Partial | README of infra-postgres rewritten (#54); the run from a clean folder is planned for 2026-11-12 |
| M5, M6, M9 report format | Met | This report and section 3 of this week's README |
| M7 seeds | Met | The seeds are only the plans; the SUPER_ADMIN of development comes from an ignored env file and was never in the history; `qa` and `main` create it by hand |

## Verdict

**Not passed yet**, by the rule above: M3 and M4 are *partial*. No practice is *not met*, and every open
P1 has an owner and evidence in progress. It passes when the teammates' issues close and the clean run
of 2026-11-12 succeeds.

## Update 2026-11-11

**Scope added (M5):** the release candidate was deployed on the Azure host and checked there: every
worker token against the four outboxes, a real user token against notifications-api, and the
end-to-end script (31/31). My teammates' security code was reviewed against the token contract.

**New findings, severity first (M9):**

| # | Finding | Practice | Status | Evidence |
|---|---|---|---|---|
| 4 | P1 — notifications-api refused every token that names an audience, so the inbox, the push registration and every event of the worker answered 401, including the e-mailed verification codes | M3 | **Fixed** | notifications-api #30 → #31 → #32; to `main` in #29, waiting for the teacher |
| 5 | P2 — platform-admin-api relayed its outbox and ran its daily jobs for any worker token, and allowed 100 events per read | M3 (#159, #160) | **Fixed** | platform-admin-api #31–#33 and #35–#37; to `main` in #34, waiting for the teacher |
| 6 | P3 — the outbox contracts said 1–100 while the code caps at 50 | — | **Fixed, waiting for the teacher** | DOCS #181 |

**Status of each practice:**

| Practice | 2026-11-10 | 2026-11-11 | Why |
|---|---|---|---|
| M3 grave findings closed | Partial | **Met** | #158, #159, #160, #163 and #164 closed by their owners; every row of the four threat models has a decision (DOCS #179, #180); the only open security issue is #157, a P2 with its reason |
| M4 manual executed | Partial | **Partial** | The host followed the README's update steps without a manual fix, but the platform was not started from a clean folder |
| M8 money | Met | **Met** | `main` keeps the simulator off; the Wompi sandbox needs a merchant account the team does not have (#157); the demo host's simulator is declared |
| M1, M2, M5, M6, M7, M9 | Met | **Met** | No change |

**Verdict (M6), by the rule above:** no practice is *not met* and no P0 or P1 finding is open. M4 stays
*partial* until the clean-folder start is done.
