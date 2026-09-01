<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       03-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 03

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- SPRINT_GOAL: Design services and distributed architecture: apply DDD building blocks, hexagonal architecture, data ownership/service contracts and the Anti-Corruption Layer pattern, and outline a vertical-slice MVP.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| N/A | Conceptual session (DDD building blocks, hexagonal architecture, data ownership, service contracts, Anti-Corruption Layer, MVP vertical slice) — no code or standalone document produced this week | done | https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/e79a2aea06424dc878401a10c869e856082f4266 |

## 2. My individual contribution
- Studied Domain-Driven Design building blocks (Entity, Value Object, Aggregate, Aggregate Root, Domain Event) and the rule that the domain must stay free of infrastructure.
- Studied hexagonal architecture (adapters → application → domain) and dependency direction.
- Worked through data ownership ("one datum = one owning service") and service contracts (sync request/response vs. async domain events) between bounded contexts.
- Studied the Anti-Corruption Layer (ACL) pattern for translating/isolating external or legacy systems from the internal domain.
- Practiced the MVP 1 vertical-slice example (create order → check inventory → verify stock → persist order → respond) applying these rules end to end.
- Prepared the Week 03 visual summary consolidating these concepts.

## 3. Blockers and risks
- The `CODE` repo (BarberSaaS backend/mobile) is still not under version control, so this week's DDD/hexagonal concepts could not yet be validated against real code.
- Main risk: mixing infrastructure concerns into the domain layer or skipping service contracts between bounded contexts — mitigated by following this week's golden rules (domain independent of infrastructure, dependencies point inward, one owner per datum).

## 4. Plan for next week
- Build a service using hexagonal architecture and patterns: request journey (API → application logic → database), Dependency Inversion Principle, and walking-skeleton vs. big-bang integration.
- Plan MVP 1 with a contract-first API (OpenAPI spec before coding).
- Set up the sprint board (To Do / In Progress / Testing / Done), estimate with story points (Fibonacci) and prioritize with the MoSCoW matrix.
- Define the sprint goal and Definition of Done (code reviewed, unit tests passed, acceptance criteria met, documentation updated) for MVP 1.
- Document the concepts and evidence for Week 04.

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary` — not met: this week's commits (`e79a2ae`, `15dc1e4`) don't follow `type(scope): summary`.
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — not applicable: work was committed directly to `main`, no HU branch/PR flow used yet.
- [ ] Testable acceptance criteria — not applicable: this week's deliverable was conceptual, not a testable product HU.
- [ ] Tests added/updated (unit / integration) — not applicable: no code was written this week.
- [ ] DDD / hexagonal boundaries respected (domain has no I/O) — studied this week but not yet applied to real code (no code exists yet in the `CODE` repo).
- [x] No secrets; config via environment variables

## 6. Evidence links
- Repo: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera.git
- Week 3 visual summary added: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera/commit/e79a2aea06424dc878401a10c869e856082f4266

![Resumen Semana 3](Week-03.jpg)
