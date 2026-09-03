<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       04-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 04

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- SPRINT_GOAL: Build a service using hexagonal architecture and patterns, and plan MVP 1 with a contract-first API, sprint board, story points and a Definition of Done.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-000-008 | Session 1 — As a team, we want a walking skeleton of one service (composition root, one endpoint, one persisted entity, running against a real database in a container) so that MVP 1 endpoints can be scoped on top of it in Session 2 | partial | Composition root: https://github.com/Pipecerquera/barbersaas-code/blob/main/barbersaas-backend/barbersaas-backend/src/main/java/com/barbersaas/BarberSaasApplication.java — real DB in container: https://github.com/Pipecerquera/barbersaas-code/blob/main/barbersaas-backend/barbersaas-backend/docker-compose.yml — persisted entities: https://github.com/Pipecerquera/barbersaas-code/tree/main/barbersaas-backend/barbersaas-backend/src/main/java/com/barbersaas/domain/entity |
| HU-000-009 | Session 2 — As a team, we want to finalize the MVP 1 sprint (API contract, task board with testable stories, a MoSCoW-fitting commitment, and a written Definition of Done) so that MVP 1 can ship next week | done | API contract (generated, always in sync with code): https://github.com/Pipecerquera/barbersaas-code/blob/main/barbersaas-backend/barbersaas-backend/src/main/java/com/barbersaas/config/SwaggerConfig.java — task board automation: https://github.com/code-corhuila/barber-saas-docs/blob/main/.github/workflows/board-sync.yml — MoSCoW priorities per story: https://github.com/code-corhuila/barber-saas-docs/blob/main/04-requirements/user-stories.md — Definition of Done: https://github.com/code-corhuila/barber-saas-docs/blob/main/00-governance/definition-of-done.md |

## 2. My individual contribution
- Verified the walking-skeleton pieces against the real backend: a Spring Boot composition root (`BarberSaasApplication.java`), a real PostgreSQL/MySQL database running in a Docker container (`docker-compose.yml`), and persisted JPA entities (`com.barbersaas.domain.entity`).
- Confirmed, honestly, that the folder layout is **not** hexagonal ports/adapters — `05-architecture/hexagonal-architecture.md` itself documents this gap: the real code is `Controller → Service → Repository` per bounded-context package, with no `port/in`, `port/out`, or `application/` subpackages. Treating this as a known, documented trade-off rather than silently claiming hexagonal compliance.
- Confirmed there is **no dedicated `/health` endpoint** — the Docker healthcheck instead targets `/api-docs` (springdoc). Flagging this as a real, small gap for a future sprint.
- Verified the MVP 1 API contract is produced by springdoc-openapi directly from the running code (`SwaggerConfig.java`, exposed at `/swagger-ui.html`) rather than hand-written — this is the trustworthy contract, unlike the stale/generic static files in `07-api/contracts/openapi/` already flagged in Week 03.
- Verified a live task board exists and is automated via `board-sync.yml` (GitHub Issues → Status field sync) in the DOCS repo.
- Confirmed MoSCoW prioritization is applied per story (`Priority: Must Have` field in `user-stories.md`) and a full, real Definition of Done checklist exists (`00-governance/definition-of-done.md`).
- Prepared the Week 04 visual summary consolidating these concepts.

## 3. Blockers and risks
- The `CODE` repo (BarberSaaS backend/mobile) was not yet under version control when this week's material was first studied; it is now (`barbersaas-code`), so this week's evidence links directly to the real code instead of staying purely conceptual.
- **Real gap found, not fixed this week:** no dedicated `/health` endpoint exists (Spring Boot Actuator is not a dependency); the Docker healthcheck uses `/api-docs` as a stand-in.
- Main risk: MVP scope creep if the MoSCoW matrix and Definition of Done aren't enforced when planning MVP 1 — mitigated by both already existing as real, applied documents (not just planned).

## 4. Plan for next week
- Containerize the service with Docker: Dockerfile → image → container flow, registry push/pull, multi-stage builds, ENV vs. volumes.
- Design the Docker Compose networking diagram and apply Docker security practices (least privilege, image scanning, signed images, network isolation).
- Release/ship MVP 1: Git promotion flow (develop → QA → main), Release Definition of Done checklist, MVP scope vs. quality trade-off.
- Run the MVP 1 demo flow (prep, live demo, Q&A, feedback) and hold a retrospective (what went well / what can improve / action items).
- Document the concepts and evidence for Week 05.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary` — commit `4ec13d5` ("feat: update deliverables for weeks 1 to 5") follows the format, though it's a single batch commit covering weeks 1-5, not week-04-specific.
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — not applicable: work was committed directly to `main`, no HU branch/PR flow used yet.
- [x] Testable acceptance criteria — met: MVP 1 stories in `user-stories.md` carry Gherkin `Given/When/Then` criteria and a MoSCoW priority.
- [ ] Tests added/updated (unit / integration) — not verified this week; out of scope for this hu-status pass.
- [ ] DDD / hexagonal boundaries respected (domain has no I/O) — **not met, and documented as such**: the real code uses a flatter `Controller → Service → Repository` layout per module, not ports/adapters (see `hexagonal-architecture.md`'s own "Reality check" note).
- [x] No secrets; config via environment variables

## 6. Evidence links
- Repo: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera.git
- PDR-BarberSaaS.md updated to v2.0 + Week 4 visual summary: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/4ec13d58e4bb9c5b9752e15a5d203fd0c479399a
- Session 1 — walking skeleton (composition root, DB in container, persisted entities): https://github.com/Pipecerquera/barbersaas-code
- Session 1 — honest gap: no hexagonal ports/adapters layout: https://github.com/code-corhuila/barber-saas-docs/blob/main/05-architecture/hexagonal-architecture.md
- Session 2 — generated API contract (springdoc): https://github.com/Pipecerquera/barbersaas-code/blob/main/barbersaas-backend/barbersaas-backend/src/main/java/com/barbersaas/config/SwaggerConfig.java
- Session 2 — task board automation: https://github.com/code-corhuila/barber-saas-docs/blob/main/.github/workflows/board-sync.yml
- Session 2 — Definition of Done: https://github.com/code-corhuila/barber-saas-docs/blob/main/00-governance/definition-of-done.md

![Resumen Semana 4](Week-04.jpg)
