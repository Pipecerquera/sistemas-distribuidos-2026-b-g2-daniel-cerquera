<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       16-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 16

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- SPRINT_GOAL: Close the semester on what was delivered: keep `v3.0.0` on `main` of the 31 repositories exactly as released, answer the teacher's review of the release and the final project with evidence, defend the Huawei HCCDP certification orally on 2026-11-19, and record the week item by item.
<!-- CONFIG-END -->

> **Plan written on 2026-11-12, before the week starts (2026-11-16 to 2026-11-22).** The work of the
> final project — presentation, `v3.0.0` tags and Releases, `c3-release` and `c3-project` — was done and
> delivered on 2026-11-12 and is recorded in [week 15](../../15-week/hu-status/README.md) (HU-000-095 to
> HU-000-098), not repeated here. The stories below are `todo` until they happen; their evidence is added
> item by item when they do, never in advance.

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-000-099 | As a student, I want to defend the Huawei HCCDP-Solution Architectures certification orally on 2026-11-19 with the evidence already in this repository | todo — 2026-11-19 | Certificate ICT20261111000006 and the delivery in [`certification/`](../../certification/) (delivered on 2026-11-10, week 15 HU-000-065) |
| HU-000-100 | As the team, we want every observation of the teacher on `c3-release`, `c3-project` or `c3-sprints` answered with evidence, by branch and pull request if it needs a change | todo | Moodle feedback and GitHub reviews, when they arrive |
| HU-000-101 | As the team, we want `main` and the `v3.0.0` Releases of the 31 repositories to stay exactly as delivered, so the local run the teacher grades is the released one | todo — checked during the week | Check per repository: `main` == latest tag (31 of 31 on 2026-11-12, week 15 HU-000-095) |
| HU-000-102 | As a student, I want this week recorded item by item from git and GitHub, like weeks 14 and 15 | todo — 2026-11-22 | The complete evidence block below, generated at the end of the week |

## 2. My individual contribution
- Planned: the oral defense of the certification, the answers to the teacher's review and this record. The individual work of the final project is in week 15.

## 3. Blockers and risks
Severity first (M9):
- **P2 — declared, not planned for this week:** the load test against SLO-1/SLO-2 and integration tests with Testcontainers (week 15, section 3). They are recorded as debt, not promised here.
- **P2 — GitHub Actions of the organization locked by billing since 2026-10-26:** any change asked by the review is tested locally and the result goes in its pull request.
- **P3 — the Azure host is stopped to save credit:** the teacher grades the local run; the host starts on request.
- **P3 — the reviewer bot stopped answering on 2026-11-12:** a change to `main` this week would need the teacher's review by hand.

## 4. Plan for next week
- End of the course: no new scope after week 16. Keep the repositories and their Releases as delivered.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary` — applies to any change of this week; none yet.
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — any fix from the review goes `develop` → `qa` → `main` by pull request; none yet.
- [x] Testable acceptance criteria — each story above has a check: the defense held on 2026-11-19, each observation answered with a link, `main` == latest tag in 31 of 31, the evidence block generated.
- [ ] Tests added/updated (unit / integration) — no code change planned.
- [x] DDD / hexagonal boundaries respected (domain has no I/O) — no code change planned; `v3.0.0` stays as released.
- [x] No secrets; config via environment variables — nothing secret in this record; the secret scan of week 15 found none in any history.

## 6. Evidence links
- Repo: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera.git
- Final project and release of the cut: [week 15](../../15-week/hu-status/README.md) (HU-000-095 to HU-000-098)
- Releases: https://github.com/code-corhuila/barber-saas-docs/releases/tag/v3.0.0
- Certification: [`certification/`](../../certification/)

<!-- FULL-EVIDENCE-START -->
### Complete evidence of the week, item by item

Generated at the end of the week (2026-11-22) from git and GitHub, like weeks 14 and 15.
<!-- FULL-EVIDENCE-END -->
