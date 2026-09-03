<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       01-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 01

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME:Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- SPRINT_GOAL: Understand and document the main distributed architecture styles and their trade-offs.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-000-001 | As a team, we want to document the BarberSaaS PRD (product scope, personas, functional/non-functional requirements) so that the rest of the project has a shared source of truth | done | https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/b74c7f0594723fe06d3bebf4c8c7a312bc403384 |
| HU-000-002 | Session 1 — As a team, for each core BarberSaaS operation (booking, payment, inventory, loyalty, reminders, tenant onboarding, plan changes) we want to define the required consistency model and delivery semantics so that these choices can be defended in the MVP 1 design | done | [consistency-and-delivery-semantics.md](consistency-and-delivery-semantics.md) |
| HU-000-003 | Session 2 — As a team, we want the `docs` repo created, the chosen architecture style recorded as an ADR, and the backlog drafted with testable (Gherkin) acceptance criteria so that MVP 1 has a documented, defensible design basis | done | docs repo: https://github.com/code-corhuila/barber-saas-docs — ADR: https://github.com/code-corhuila/barber-saas-docs/blob/main/05-architecture/decisions/records/ADR-002-modular-monolith.md — backlog with ACs: https://github.com/code-corhuila/barber-saas-docs/blob/main/04-requirements/user-stories.md |

## 2. My individual contribution
-Researched and documented the main distributed architecture styles, including client-server, peer-to-peer, SOA and microservices.
-Prepared the weekly visual summary to consolidate the concepts and their main trade-offs.
-Organized and updated the Week 01 documentation in the repository.
-Defined, per core BarberSaaS operation, the required consistency model and delivery semantics (`hu-status/consistency-and-delivery-semantics.md`), grounding the modular-monolith decision in which operations actually need strong consistency vs. which can tolerate eventual consistency.
-As a team: created the `docs` repo (`barber-saas-docs`), recorded the chosen architecture style as an ADR, and drafted the backlog with Gherkin acceptance criteria per HU.

## 3. Blockers and risks
-No major blockers during the week.
-The main risk was ensuring that the architectural concepts and their trade-offs were clearly summarized and represented in the weekly documentation.
-Naming note: the ADR that records the chosen architecture style (modular monolith) is numbered **ADR-002**, not ADR-001 — ADR-001 was already used earlier for the documentation-language decision (English for all docs/code). Flagging this so the numbering mismatch against the activity wording is explicit, not silently glossed over.

## 4. Plan for next week
-Study containerization with Docker.
-Understand the differences between images, containers and registries.
-Practice writing Dockerfiles and using Docker Compose for multi-service applications.
-Document the concepts and evidence for Week 02.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — not applicable this week: work was committed directly to `main`, no HU branch/PR flow used yet.
- [ ] Testable acceptance criteria — not applicable: this week's deliverable was conceptual/documentation (architecture styles, PRD), not a testable product HU.
- [ ] Tests added/updated (unit / integration) — not applicable: no code was written this week.
- [ ] DDD / hexagonal boundaries respected (domain has no I/O) — not applicable: no code was written this week.
- [x] No secrets; config via environment variables

## 6. Evidence links
- Repo: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera.git
- PDR-BarberSaaS.md added: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/b74c7f0594723fe06d3bebf4c8c7a312bc403384
- Week 1 visual summary added: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/6f704186cb10a1454b000411812619033c0274cb
- Session 1 deliverable — consistency & delivery semantics per operation: [`consistency-and-delivery-semantics.md`](consistency-and-delivery-semantics.md)
- Session 2 — `docs` repo created: https://github.com/code-corhuila/barber-saas-docs
- Session 2 — ADR, chosen architecture style (modular monolith): https://github.com/code-corhuila/barber-saas-docs/blob/main/05-architecture/decisions/records/ADR-002-modular-monolith.md
- Session 2 — backlog with testable (Gherkin) acceptance criteria: https://github.com/code-corhuila/barber-saas-docs/blob/main/04-requirements/user-stories.md

## Resumen visual de la semana 1

![Resumen Semana 1](Week-01.jpg)
## PDR de la semana 1
[Ver PDR - BarberSaaS](PDR-BarberSaaS.md)
-
