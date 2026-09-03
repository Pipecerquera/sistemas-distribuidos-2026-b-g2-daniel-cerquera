<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       05-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 05

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- SPRINT_GOAL: Research cross-cutting domains and the Strangler Fig migration pattern, and study containerization with Docker plus the MVP 1 release/shipping process.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| N/A | As a team, we want to document cross-cutting domains (security, observability, configuration, resilience, auditing) and the Strangler Fig pattern so that we have written guidance for distributed/microservices concerns and legacy migration | done | Docx: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/4ec13d58e4bb9c5b9752e15a5d203fd0c479399a — Infographic: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/1c9e285f0444bf20bca48c2eba54e47b9bf44781 |
| HU-000-010 | Session 1 — As a team, we want to containerize our services (multi-stage Dockerfile per service, `.dockerignore`, and a `docker-compose.yml` bringing up all services + the database on one network, config via env, data in a volume) so that this is the runtime base for the MVP 1 release | done | Backend Dockerfile (multi-stage): https://github.com/code-corhuila/barber-saas/blob/develop/barbersaas-backend/barbersaas-backend/Dockerfile — Mobile Dockerfile (multi-stage): https://github.com/code-corhuila/barber-saas/blob/develop/barbersaas-frontend%20(2)/barbersaas-frontend/barbersaas-mobile/Dockerfile — docker-compose.yml (all services, one network, env config, volume): https://github.com/code-corhuila/barber-saas/blob/develop/docker-compose.yml |
| HU-000-011 | Session 2 — As a team, we want to ship MVP 1 (promote to main, tag v1.0.0, verify the DoD checklist with evidence, demo, retrospective) so that we close Corte 1 and move on to Corte 2 | doing | See "Blockers and risks" below — most items are honestly still pending, not yet done |

## 2. My individual contribution
- Wrote "Dominios Transversales", a research document on cross-cutting concerns (security, observability, configuration management, resilience, auditing/traceability) in distributed and microservices architectures — concept, difference from business domains, and centralization strategies (API Gateway, service mesh, sidecars, DevSecOps).
- Built an infographic explaining the Strangler Fig pattern for incrementally migrating a legacy system.
- Studied Docker containerization (Dockerfile → image → container, registry push/pull, multi-stage builds, ENV vs. volumes, Compose networking diagram, Docker security) and the MVP 1 release/shipping process (Git promotion flow develop→QA→main, Release Definition of Done checklist, MVP scope vs. quality, demo flow, retrospective) from this week's session material.
- Verified the containerization deliverable against the real, official code repo (`barber-saas`, `code-corhuila` org): multi-stage Dockerfiles for both backend and mobile, a `docker-compose.yml` bringing up PostgreSQL/MySQL + Redis + backend + Nginx on one network, config via environment variables, and Postgres data in a named volume.
- Started shipping MVP 1 on the real GitFlow-style repo: pushed the current backend + mobile code to the `develop` branch of `barber-saas` (previously empty scaffold), and wrote/updated the README on both `main` and `develop` (project description, team, branch workflow) so a visitor lands on a repo that explains itself.

## 3. Blockers and risks
- The `CODE` repo was not yet under version control when this week's material was first studied; it now exists for real as `barber-saas` (`code-corhuila` org, branches `main`/`qa`/`develop`), with the backend + mobile code pushed to `develop` this week.
- **Session 2 is honestly incomplete, not silently marked done:**
  - Promote to `main`: not done — code lives on `develop` only; no PR/promotion to `qa` or `main` has happened yet.
  - Tag `v1.0.0`: not created — the team decided the current code isn't yet the version to declare as MVP 1's v1.0.0.
  - DoD checklist verified with evidence: **blocked by local environment** — no `mvn`/`mvnw` available and Docker Desktop's daemon wasn't running, so `mvn test` could not be executed this week. Pending a retry once the environment is available.
  - Demo of the running system: has not happened yet — the professor hasn't asked for it, work so far has been documentation and git setup.
  - Retrospective: has not happened yet, for the same reason.
- Main risk: treating cross-cutting concerns as ad hoc code inside each business module instead of centralizing them (security, observability, config) — noted explicitly in the research document as the source of duplication and inconsistency.
- Secondary risk: reporting Session 2 as fully "done" would misrepresent real progress — tracked instead as "doing" with each sub-item's real status listed above.

## 4. Plan for next week
- Close the pending Session 2 items honestly listed above: verify the DoD checklist with real evidence (`mvn test` once the local environment allows it), decide when the code on `develop` is ready to promote to `qa`/`main` and tag `v1.0.0`, and run the demo + retrospective once the professor asks for them.
- Continue with the Unit 2 course topics per the syllabus; specific content for Week 06 is not yet defined.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary` — commit `4ec13d5` ("feat: update deliverables for weeks 1 to 5") follows the format; the other week-05 commit (`1c9e285`, "Update week 5") does not.
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — not applicable: work was committed directly to `main`, no HU branch/PR flow used yet.
- [ ] Testable acceptance criteria — not applicable: this week's deliverable was research/documentation plus containerization/shipping setup, not a testable product HU.
- [ ] Tests added/updated (unit / integration) — **pending, not met**: `mvn test` could not be run this week (no Maven/Docker daemon available locally). Real gap, not silently skipped.
- [ ] DDD / hexagonal boundaries respected (domain has no I/O) — not applicable: no code was written this week (this week's work was containerization and repo setup, not domain code).
- [x] No secrets; config via environment variables

## 6. Evidence links
- Repo: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera.git
- Dominios_transversales.docx + Week 5 visual summary added: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/4ec13d58e4bb9c5b9752e15a5d203fd0c479399a
- strangler-fig-infografia.html added: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/1c9e285f0444bf20bca48c2eba54e47b9bf44781
- Session 1 — official code repo (`barber-saas`), `develop` branch with backend + mobile + Docker setup: https://github.com/code-corhuila/barber-saas/tree/develop
- Session 1 — root `docker-compose.yml` (all services, one network, env config, volume): https://github.com/code-corhuila/barber-saas/blob/develop/docker-compose.yml
- Session 2 — `main` branch README (project, team, branch workflow): https://github.com/code-corhuila/barber-saas/blob/main/README.md
- Session 2 — pending items honestly tracked in "Blockers and risks" above (no tag, DoD not verified, demo/retro not yet done)

![Resumen Semana 5](Week-05.jpg)
