<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       02-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 02

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- SPRINT_GOAL: Map the BarberSaaS domain into bounded contexts, decide the architecture style (modular monolith vs. microservices) and record the decision in an ADR.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-000-004 | Session 1 — As a team, we want to sketch the candidate architecture of BarberSaaS (list the bounded contexts, which could become their own service, which stay together, and why) so that the decision can be recorded in Session 2 | done | Bounded contexts: https://github.com/code-corhuila/barber-saas-docs/blob/main/02-domain/domain-map.md — extraction candidates & why: https://github.com/code-corhuila/barber-saas-docs/blob/main/05-architecture/overview.md (§9 Planned evolution) |
| HU-000-005 | Session 2 — As a team, we want to draw the context map, choose the architecture via the decision path, record it as an ADR, and slice it into the first backlog stories with testable acceptance criteria for MVP 1 | done | Context map: https://github.com/code-corhuila/barber-saas-docs/blob/main/02-domain/domain-map.md (§3) — ADR: https://github.com/code-corhuila/barber-saas-docs/blob/main/05-architecture/decisions/records/ADR-002-modular-monolith.md — backlog with ACs: https://github.com/code-corhuila/barber-saas-docs/blob/main/04-requirements/user-stories.md |

## 2. My individual contribution
- Mapped the real BarberSaaS domain into 8 bounded contexts — Identity & Auth, Barbershop Management, Appointment, Schedule, Loyalty & Rewards, Notifications, Finance & Inventory, and Platform Administration — defining boundaries by business capability and data ownership, not by technical layers (`02-domain/domain-map.md`, DOCS repo).
- Drew the context map (§3 of `domain-map.md`): upstream/downstream relationships between the 8 contexts, e.g. Identity & Auth is upstream to all of them via JWT/TenantContext.
- Worked through the architecture decision tree (modular monolith vs. extract-service vs. microservices from day one vs. layered monolith) and recorded the decision, with the full evaluated-alternatives table, in an ADR (`05-architecture/decisions/records/ADR-002-modular-monolith.md`).
- Identified, per bounded context, which stay together now and which are extraction candidates with a measurable trigger — notification-service, appointment-service, auth-service, loyalty-service and analytics-service — each with its own trigger condition, documented in `05-architecture/overview.md` §9 "Planned evolution". Everything else stays inside the modular monolith until a trigger is measured in production.
- Sliced the architecture decision into backlog stories with Gherkin acceptance criteria (`04-requirements/user-stories.md`), traceable to the epics in `03-product/product-backlog.md`.
- Prepared the Week 02 visual summary consolidating these concepts.

## 3. Blockers and risks
- The `CODE` repo (BarberSaaS backend/mobile) was not yet under version control at the time this bounded-context/architecture work was first drafted; it is now (see Week 06+ for the `barbersaas-code` repo), so future weeks can validate the domain map directly against the real module packages (`com.barbersaas.*`).
- Main risk: picking an architecture prematurely without a clear business boundary or real scaling need — mitigated by following the "start modular, extract only when there is a real need" rule from this week's material, and by making every extraction candidate trigger-based (measured in production) rather than schedule-based.
- Naming note (same as Week 01): the ADR recording the chosen architecture style is numbered **ADR-002**, not ADR-001 — ADR-001 was already used for the documentation-language decision.

## 4. Plan for next week
- Study Domain-Driven Design building blocks (Entity, Value Object, Aggregate, Aggregate Root, Domain Event).
- Study hexagonal architecture (adapters → application → domain) and dependency direction.
- Understand data ownership and service contracts between bounded contexts, plus sync vs. async communication.
- Study the Anti-Corruption Layer (ACL) pattern for isolating the domain from external/legacy systems.
- Document the concepts and evidence for Week 03 (vertical-slice MVP).

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary` — not met: this week's commits (`e79a2ae`, `15dc1e4`) don't follow `type(scope): summary`.
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — not applicable: work was committed directly to `main`, no HU branch/PR flow used yet.
- [x] Testable acceptance criteria — met: the backlog stories sliced from this week's architecture decision (`04-requirements/user-stories.md`) carry Gherkin `Given/When/Then` acceptance criteria per HU.
- [ ] Tests added/updated (unit / integration) — not applicable: no code was written this week.
- [ ] DDD / hexagonal boundaries respected (domain has no I/O) — not applicable: no code was written this week (DDD/hexagonal are next week's topic).
- [x] No secrets; config via environment variables

## 6. Evidence links
- Repo: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera.git
- Week 2 visual summary added: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/e79a2aea06424dc878401a10c869e856082f4266
- Session 1 — bounded contexts (8 real contexts): https://github.com/code-corhuila/barber-saas-docs/blob/main/02-domain/domain-map.md
- Session 1 — which stays together / extraction candidates & why: https://github.com/code-corhuila/barber-saas-docs/blob/main/05-architecture/overview.md
- Session 2 — context map: https://github.com/code-corhuila/barber-saas-docs/blob/main/02-domain/domain-map.md
- Session 2 — architecture decision (ADR, chosen via decision path): https://github.com/code-corhuila/barber-saas-docs/blob/main/05-architecture/decisions/records/ADR-002-modular-monolith.md
- Session 2 — backlog sliced into stories with testable acceptance criteria: https://github.com/code-corhuila/barber-saas-docs/blob/main/04-requirements/user-stories.md

![Resumen Semana 2](Week-02.jpg)