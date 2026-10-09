<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       10-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 10

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- SPRINT_GOAL: Persistence in distributed systems, applied to BarberSaaS: close phase 1 of the migration as a working Android app, then build my part of phase 2 — events through the outbox pattern without a broker (ADR-016 and the `worker`), the platform administration domain (plans, barbershops, trial expiration), the owner-onboarding saga with a new compensated step, the single MongoDB instance, and the shell loading the Angular domain apps — each specified in DOCS first, built in small pull requests, and tested end to end on the running platform.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-000-030 | Carried over — As a prospective barbershop owner, I want to register my barbershop myself from the app, so I start a 60-day trial (HU-AUTH-003, DOCS [#7](https://github.com/code-corhuila/barber-saas-docs/issues/7)) | **done — tested end to end on the platform and in the Android emulator** | DOCS #73–#75 approved and merged (2026-10-05); the saga completes since barbershop's internal operations landed; see HU-000-040 for the plan step |
| HU-000-032 | As a CLIENT, I want my session bound to the barbershop I pick, so I book only where I chose (closes OQ-07) | **done** | DOCS [#77](https://github.com/code-corhuila/barber-saas-docs/pull/77) · identity-auth-api [#13](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/13) · front [#6](https://github.com/code-corhuila/barber-saas-front/pull/6) |
| HU-000-033 | As the team, we want how one domain shows another's data decided, so no service reads another's schema (ADR-014 snapshot, ADR-015 busy slots; closes OQ-08, OQ-09) | **done** | DOCS [#78](https://github.com/code-corhuila/barber-saas-docs/pull/78) · identity-auth-api [#14](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/14) (`GET /internal/v1/users/{id}`) |
| HU-000-034 | As a user, I want the app installed on Android with every domain app inside, so it works without development servers (ADR-013) — closes phase 1 | **done** | front [#7](https://github.com/code-corhuila/barber-saas-front/pull/7) [#8](https://github.com/code-corhuila/barber-saas-front/pull/8) [#10](https://github.com/code-corhuila/barber-saas-front/pull/10) [#11](https://github.com/code-corhuila/barber-saas-front/pull/11) · identity-auth-app [#7](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/7) · schedule-app [#12](https://github.com/code-corhuila/barber-saas-schedule-app/pull/12) |
| HU-000-035 | As the team, we want the infrastructure Annex J asks for: `barber-saas-infra-postgres` (renamed) and the single MongoDB instance in `barber-saas-infra-mongo`, and every README naming BarberSaaS | **done** | DOCS [#79](https://github.com/code-corhuila/barber-saas-docs/pull/79) · infra-mongo [#1](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/1) [#2](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/2) [#3](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/3) · infra-postgres [#16](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/16) [#23](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/23) · header PRs in my 11 repositories (e.g. worker [#2](https://github.com/code-corhuila/barber-saas-worker/pull/2)) |
| HU-000-036 | As the team, we want how domain events travel decided and contracted, so loyalty and notifications receive appointment's events reliably (outbox pattern; closes AT-004, D-3) | **done — 6 PRs approved by the teacher and merged** | ADR-016 [#82](https://github.com/code-corhuila/barber-saas-docs/pull/82) · envelope and consumers [#83](https://github.com/code-corhuila/barber-saas-docs/pull/83) · OQ-10 and trial expiry [#84](https://github.com/code-corhuila/barber-saas-docs/pull/84) · routing table [#85](https://github.com/code-corhuila/barber-saas-docs/pull/85) · appointment's outbox operations [#86](https://github.com/code-corhuila/barber-saas-docs/pull/86) · loyalty and identity-auth outboxes, `processed_event` [#87](https://github.com/code-corhuila/barber-saas-docs/pull/87) |
| HU-000-037 | As the platform, I want a worker that relays every outbox and runs the daily jobs, so events reach their consumers at least once and reminders, no-shows and trial expiry run on time (HU-NOTIF-001 [#5](https://github.com/code-corhuila/barber-saas-docs/issues/5), HU-APPT-002 [#8](https://github.com/code-corhuila/barber-saas-docs/issues/8), HU-SADMIN-002 [#24](https://github.com/code-corhuila/barber-saas-docs/issues/24)) | **done** | worker [#4](https://github.com/code-corhuila/barber-saas-worker/pull/4) [#5](https://github.com/code-corhuila/barber-saas-worker/pull/5) [#6](https://github.com/code-corhuila/barber-saas-worker/pull/6) [#7](https://github.com/code-corhuila/barber-saas-worker/pull/7) [#8](https://github.com/code-corhuila/barber-saas-worker/pull/8) · infra-postgres [#21](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/21) |
| HU-000-038 | As a SUPER_ADMIN, I want to manage subscription plans and barbershops (status, plan, trial), and have expired trials suspended automatically (HU-SADMIN-001 [#12](https://github.com/code-corhuila/barber-saas-docs/issues/12), HU-SADMIN-002 [#24](https://github.com/code-corhuila/barber-saas-docs/issues/24)) | **done** | platform-admin-db [#3](https://github.com/code-corhuila/barber-saas-platform-admin-db/pull/3) · platform-admin-api [#3](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/3)–[#7](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/7) · platform-admin-app [#3](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/3)–[#7](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/7) · api-gateway [#10](https://github.com/code-corhuila/barber-saas-api-gateway/pull/10) · front [#12](https://github.com/code-corhuila/barber-saas-front/pull/12) · infra-postgres [#15](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/15) [#19](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/19) [#20](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/20) |
| HU-000-039 | As a user who forgot the password, I want a 6-digit code to set a new one, sent without losing it if a service is down (HU-AUTH-002, DOCS [#6](https://github.com/code-corhuila/barber-saas-docs/issues/6)) | **done — the code now arrives by e-mail** | identity-auth-db [#7](https://github.com/code-corhuila/barber-saas-identity-auth-db/pull/7) · identity-auth-api [#16](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/16) [#17](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/17) [#18](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/18) [#19](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/19) · worker [#7](https://github.com/code-corhuila/barber-saas-worker/pull/7) · e-mail (`DEC-NOTIF-01`): notifications-db [#4](https://github.com/code-corhuila/barber-saas-notifications-db/pull/4) · notifications-api [#10](https://github.com/code-corhuila/barber-saas-notifications-api/pull/10) [#11](https://github.com/code-corhuila/barber-saas-notifications-api/pull/11) (merged by Carlos with two review fixes of his) · DOCS [#107](https://github.com/code-corhuila/barber-saas-docs/pull/107) |
| HU-000-040 | As a prospective owner, I want to pick an active plan when I register, as in the prototype (FR-004, HU-AUTH-003) | **done** | DOCS [#89](https://github.com/code-corhuila/barber-saas-docs/pull/89) (approved and merged) · platform-admin-api [#8](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/8) · workflow [#13](https://github.com/code-corhuila/barber-saas-workflow/pull/13) · identity-auth-app [#8](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/8) [#9](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/9) |
| HU-000-041 | As an owner or a client, I want a tab per Angular domain app, so I reach finances and loyalty from the app (ADR-013) | **done — tested in the emulator** | front [#12](https://github.com/code-corhuila/barber-saas-front/pull/12) (Angular host, Plataforma) · [#15](https://github.com/code-corhuila/barber-saas-front/pull/15) (Finanzas) · [#16](https://github.com/code-corhuila/barber-saas-front/pull/16) (Fidelidad for the client; "Programa de fidelidad" in the owner's profile, as in the prototype) |
| HU-000-042 | As the team, we want DOCS to describe the system that runs: service catalog, C4-02 and SEQ-03 with the worker, and the traceability matrix with the real tests (closes AT-006, AT-007, AT-008, D-4) | **done — approved and merged** | DOCS [#90](https://github.com/code-corhuila/barber-saas-docs/pull/90) · [#92](https://github.com/code-corhuila/barber-saas-docs/pull/92) · [#93](https://github.com/code-corhuila/barber-saas-docs/pull/93) |
| HU-000-043 | As a client, I want a sticker every time a barber completes my appointment, without anyone granting it by hand (HU-LOY-001 [#9](https://github.com/code-corhuila/barber-saas-docs/issues/9); Juan Pablo built loyalty's side, L-3) | **done — the whole chain tested on the platform** | worker [#9](https://github.com/code-corhuila/barber-saas-worker/pull/9) (`CONSUMERS: loyalty,notifications`); four-role check 30 OK, 0 FAIL: completing an appointment puts a sticker on the client's card, granted by who completed it, and the inbox shows "Cita completada" and "Ganaste un sello". `completedBy` is proposed in DOCS [#91](https://github.com/code-corhuila/barber-saas-docs/pull/91) |
| HU-000-044 | As a client with an earned reward, I want my active coupon applied when I book, so the service is free without asking staff (FR-010, HU-APPT-001 [#4](https://github.com/code-corhuila/barber-saas-docs/issues/4)) | **done — 171 tests in appointment-api, 74 in loyalty-api** | Decision first: DOCS [#108](https://github.com/code-corhuila/barber-saas-docs/pull/108) (`DEC-APPT-09`, `DEC-LOY-06`) and [#110](https://github.com/code-corhuila/barber-saas-docs/pull/110) (technical debt AT-009) · appointment-db [#7](https://github.com/code-corhuila/barber-saas-appointment-db/pull/7) · appointment-api [#19](https://github.com/code-corhuila/barber-saas-appointment-api/pull/19) · loyalty-api [#15](https://github.com/code-corhuila/barber-saas-loyalty-api/pull/15) · worker [#10](https://github.com/code-corhuila/barber-saas-worker/pull/10) |
| HU-000-045 | As the team, we want MVP 2 released: every repository promoted `develop` → `qa` → `release.2.0.0` → `main` with `cherry-pick -x` and tagged `v2.0.0`, with a CHANGELOG and the CI rules the teacher set (tracking DOCS [#109](https://github.com/code-corhuila/barber-saas-docs/issues/109)) | **done for my 12 repositories and DOCS; Carlos and Juan Pablo are finishing theirs** | 48 PRs: `hu-mvp2-dev` → `develop` (CI on `develop`/`qa` only, `timeout-minutes: 10`, `concurrency`; `CHANGELOG.md`), `hu-mvp2-qa` → `qa`, `hu-mvp2-release` → `release.2.0.0`, `release.2.0.0` → `main` (approved by the teacher) — e.g. worker [#11](https://github.com/code-corhuila/barber-saas-worker/pull/11) [#12](https://github.com/code-corhuila/barber-saas-worker/pull/12) [#13](https://github.com/code-corhuila/barber-saas-worker/pull/13) [#14](https://github.com/code-corhuila/barber-saas-worker/pull/14); tag and release [`v2.0.0`](https://github.com/code-corhuila/barber-saas-worker/releases/tag/v2.0.0) in the 12 repositories and in [DOCS](https://github.com/code-corhuila/barber-saas-docs/releases/tag/v2.0.0); every promoted commit carries its trail and the tree is identical to `develop`. Review findings answered on every PR; code ones recorded as MVP 3 backlog in DOCS [#111](https://github.com/code-corhuila/barber-saas-docs/issues/111) |
| HU-000-046 | As the team, we want the code repositories public (the teacher's decision on Actions minutes) without leaking any secret | **done** | Scanned the full history of the 30 repositories, DOCS and the prototype (gitleaks + own patterns): no real credential in the 30 repositories or DOCS; the prototype's leaked Gmail app password was revoked and replaced the same day; `barber-saas-infra-postgres` and `barber-saas-infra-mongo` made public after checking every `.env.example` is empty and `env/*.env`/`keys/` were never committed |
| HU-000-017 | Carried over — hardened config (`.env.example`, fail-fast, secret scan, a feature flag) | doing (unchanged in the prototype) | Every new repository this week ships only `.env.example`/`env/*.env.example`, fails fast on a missing secret (`${VAR:?}`) and keeps keys out of git (`dev-keys.sh` writes them locally); still no pre-commit secret scan or feature flag |
| HU-000-011 | Carried over — ship MVP 1 to `main` with a tag | **superseded by HU-000-045** | The first promotion to `main` is release `v2.0.0`, which carries MVP 1 and MVP 2 together |

## 2. My individual contribution
- **Closed phase 1 as a real Android app.** Packaged the Angular shell with every React domain app inside the APK, so it runs without development servers; gave the shell the prototype's look (dark theme, bottom tabs per role) and the Android back button; built and tested it on the emulator with JDK 21.
- **Specified events before building them (SDD).** ADR-016: no broker — each producer keeps its outbox, and a worker reads it through an internal operation of the producer and delivers it over internal HTTP, at least once, with idempotent consumers. Then the contracts: the shared `EventEnvelope`, the consumers' `POST /internal/v1/events`, the outbox operations of appointment, loyalty and identity-auth, the routing table and `processed_event` (DOCS #82–#87, all approved).
- **Built the worker** (Python 3.12, standard library only, hexagonal with `import-linter`): outbox relay every 5 s (50 events, 30 s per run, 8 attempts with exponential back-off and jitter, a 4xx fails at once, `published` only when every consumer answered 2xx), the daily jobs (`reminders-due` 18:00, `no-shows` 01:00, `trials/expire` 02:00), and `CONSUMERS` so a service can publish before it consumes. 21 tests.
- **Built the platform-admin domain end to end**: the `platform_admin` schema with its plan seed, the Java service (plans, barbershops through barbershop's internal operations, `DEC-PLAT-02`, trial expiry; 46 tests), and the first Ionic Angular domain app, which became the template Carlos and Juan Pablo copied.
- **Stood up MongoDB** as Annex J asks: one instance (replica set `rs0` with a key file) in `infra-mongo`, the `notifications_app` user, included by `infra-postgres`, with the notifications database migrated by its own runner. Checked that notifications lives only in MongoDB and the seven other domains only in PostgreSQL.
- **Password reset through the outbox** (`DEC-AUTH-08`): the code is stored only as a hash, its event commits in the same transaction, and the code is erased from the outbox row once published. 74 tests in identity-auth-api.
- **Found and fixed a gap between the prototype and the contracts**: the prototype asked for a plan at sign-up, the saga had lost it. Specified a new saga step, `assign-plan`, run by platform-admin with its compensation (`PLAN_NOT_AVAILABLE` removes the barbershop), and built it in the workflow, platform-admin and the sign-up screen.
- **Tested everything on the running platform** (13 containers): an appointment confirmation and a loyalty sticker reach the client's inbox through the worker; a sign-up on the Pro plan completes and a retired plan is compensated; the SUPER_ADMIN suspends and assigns plans; the reset code works once and disappears from the outbox.
- Kept every applied Liquibase changeset untouched (new changesets only), and merged 94 pull requests since 2026-10-05 (the first ones of that Monday were already listed in week 09), each with CI green and under 400 changed lines.
- **Finished two items of my teammates, with their agreement**, after the team decided I would: the password-reset e-mail (SMTP adapter with the standard library, the `processed_event` collection so a redelivery never e-mails twice, `503` so the worker retries; Carlos merged it and applied two review fixes) and the reward coupon at booking. For the coupon I wrote the decision before the code (`DEC-APPT-09`, `DEC-LOY-06`): the coupon cannot be consumed synchronously while booking — loyalty would check an appointment not yet stored — so booking reads the coupon, stores the appointment at price 0, and loyalty marks it `USED` from `AppointmentCreated`. Enforced in the schema too (`uq_appointment_coupon`, `chk_appointment_coupon_price`).
- **Answered every recommendation of the automated review** on my pull requests, in writing: applied the valid ones as new commits (e.g. a TTL and an `eventType` enum for `processed_event`, rollback order in appointment-db), and justified the rest; code findings of the release went to the MVP 3 backlog (DOCS #111).
- **Released MVP 2 in my 12 repositories**: CI rules the teacher set, a generated `CHANGELOG.md`, and the promotion `develop` → `qa` → `release.2.0.0` → `main` with `cherry-pick -x` (340 commits), checking each time that no commit lacks its trail, that every cited SHA exists in its source branch (norm 10.5) and that the tree is identical; then tagged `v2.0.0` with a GitHub Release. Wrote the step-by-step prompts so Carlos and Juan Pablo release their 18 repositories the same way, after a local dry run showed none of them conflicts.
- **Secret audit before the repositories went public**: full-history scan of 32 repositories; found and had revoked the Gmail app password in the prototype's history; published `infra-postgres` and `infra-mongo` only after verifying nothing sensitive is versioned.

## 3. Blockers and risks
- **Release of the team not finished**: Carlos's and Juan Pablo's 18 repositories still need their `release.2.0.0` → `main` merge and the `v2.0.0` tag before the single delivery zip can be built (delivery 2026-10-13).
- **The live demo with an injected failure** (Release rubric, 1.5 points) is not rehearsed yet.
- **The reset e-mail needs real SMTP credentials** in each developer's `env/dev.env` (a Gmail app password); without them the code waits in the outbox, which is safe but sends nothing.
- **Push notifications need a Firebase project** and its `google-services.json`, which is never versioned; F-4 waits for that decision.
- MVP 1/2 not promoted to `qa`/`main` yet.

## 4. Plan for next week
- Close the team's release: Carlos's and Juan Pablo's tags, then one zip of the 31 repositories at `v2.0.0` for the delivery.
- Rehearse the demo end to end with the four roles, including an injected failure (a stopped service whose events wait in the outbox and arrive when it comes back).
- Start MVP 3 from the backlog of DOCS #111 (refresh-token revocation and logout first), and push notifications (F-4) once the Firebase project exists.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary` — every commit of this week's pull requests (e.g. `feat(relay): relay loyalty's outbox before loyalty consumes events`, `feat(saga): assign the plan the owner picked during onboarding`).
- [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...) — the release used exactly that flow in my 12 repositories: `hu-mvp2-dev` → `develop`, `hu-mvp2-qa` → `qa`, `hu-mvp2-release` → `release.2.0.0` → `main`, with `cherry-pick -x`; day-to-day work goes through child branches (`feat/`, `fix/`, `docs/NNN-slug`) and a PR.
- [x] Testable acceptance criteria — each contract change carries its decision and the cases it must answer (e.g. `DEC-WF-05`: a retired plan ends `COMPENSATED` with `PLAN_NOT_AVAILABLE`), and each one has a test.
- [x] Tests added/updated (unit / integration) — worker 21, platform-admin-api 46, identity-auth-api 74, workflow 43, identity-auth-app 23, shell 30; schema rebuilds in CI; all run on every PR.
- [x] DDD / hexagonal boundaries respected (domain has no I/O) — three Maven modules per Java service (the core has no Spring) and `import-linter` contracts in the worker.
- [x] No secrets; config via environment variables — only `.env.example` files are versioned; tokens and keys are generated locally by `dev-keys.sh`.

## 6. Evidence links
- Repo: https://github.com/Pipecerquera/sistemas-distribuidos-2026-b-g2-daniel-cerquera.git
- This week's infographic: `10-week/hu-status/Week-10.jpg` (in this same folder)
- DOCS, events (ADR-016 and contracts): https://github.com/code-corhuila/barber-saas-docs/pull/82 … https://github.com/code-corhuila/barber-saas-docs/pull/87
- DOCS, plan at sign-up: https://github.com/code-corhuila/barber-saas-docs/pull/89
- DOCS, awaiting approval: https://github.com/code-corhuila/barber-saas-docs/pull/90 · https://github.com/code-corhuila/barber-saas-docs/pull/92 · https://github.com/code-corhuila/barber-saas-docs/pull/93
- Worker: https://github.com/code-corhuila/barber-saas-worker/pulls?q=is%3Amerged
- Platform admin: https://github.com/code-corhuila/barber-saas-platform-admin-api/pulls?q=is%3Amerged · https://github.com/code-corhuila/barber-saas-platform-admin-app/pulls?q=is%3Amerged · https://github.com/code-corhuila/barber-saas-platform-admin-db/pulls?q=is%3Amerged
- MongoDB: https://github.com/code-corhuila/barber-saas-infra-mongo/pulls?q=is%3Amerged
- Shell and Android app: https://github.com/code-corhuila/barber-saas-front/pulls?q=is%3Amerged
- **Complete record of my individual work this week** (every pull request I opened, by repository; generated from GitHub):
  - Pull requests: **157** (155 merged, 2 closed without merge).
  - `barber-saas-docs` (19):
    - [#73](https://github.com/code-corhuila/barber-saas-docs/pull/73) docs(architecture): accept adr-009, saga state in a workflow schema — merged 2026-10-05
    - [#74](https://github.com/code-corhuila/barber-saas-docs/pull/74) docs(api): add the onboarding operations for owners and barbers — merged 2026-10-05
    - [#75](https://github.com/code-corhuila/barber-saas-docs/pull/75) docs(api): specify the owner-onboarding saga and close oq-12 — merged 2026-10-05
    - [#77](https://github.com/code-corhuila/barber-saas-docs/pull/77) docs(api): bind a client token to the barbershop they pick — merged 2026-10-05
    - [#78](https://github.com/code-corhuila/barber-saas-docs/pull/78) docs(architecture): close oq-08 and oq-09 with adr-014 and adr-015 — merged 2026-10-05
    - [#79](https://github.com/code-corhuila/barber-saas-docs/pull/79) docs: barber-saas-infra-postgres rename and barber-saas-infra-mongo creation — merged 2026-10-06
    - [#82](https://github.com/code-corhuila/barber-saas-docs/pull/82) docs(adr): ADR-016 — the worker relays each outbox over internal HTTP — merged 2026-10-06
    - [#83](https://github.com/code-corhuila/barber-saas-docs/pull/83) docs(api): event envelope and the consumers' POST /internal/v1/events — merged 2026-10-06
    - [#84](https://github.com/code-corhuila/barber-saas-docs/pull/84) docs(api): close OQ-10 — platform-admin operates barbershops internally; trial expiry — merged 2026-10-06
    - [#85](https://github.com/code-corhuila/barber-saas-docs/pull/85) docs(domain): event transport and routing table in domain-events.md (D-3) — merged 2026-10-06
    - [#86](https://github.com/code-corhuila/barber-saas-docs/pull/86) docs(api): appointment's outbox relay and daily jobs for the worker — merged 2026-10-06
    - [#87](https://github.com/code-corhuila/barber-saas-docs/pull/87) docs(api): loyalty and identity-auth outboxes, e-mailed reset code, processed_event — merged 2026-10-07
    - [#89](https://github.com/code-corhuila/barber-saas-docs/pull/89) docs(api): the owner picks an active plan at sign-up — merged 2026-10-07
    - [#90](https://github.com/code-corhuila/barber-saas-docs/pull/90) docs: catalog the 30 repositories and close AT-006, AT-007 and AT-008 — merged 2026-10-07
    - [#92](https://github.com/code-corhuila/barber-saas-docs/pull/92) docs(diagrams): draw the worker relay in C4-02 and SEQ-03 — merged 2026-10-07
    - [#93](https://github.com/code-corhuila/barber-saas-docs/pull/93) docs(requirements): trace every FR to the tests of the polyrepo — merged 2026-10-07
    - [#107](https://github.com/code-corhuila/barber-saas-docs/pull/107) docs(notifications): the password-reset e-mail is implemented — merged 2026-10-08
    - [#108](https://github.com/code-corhuila/barber-saas-docs/pull/108) docs(appointment,loyalty): apply the reward coupon at booking — merged 2026-10-08
    - [#110](https://github.com/code-corhuila/barber-saas-docs/pull/110) docs(architecture): register the appointment-loyalty cycle as technical debt — merged 2026-10-09
  - `barber-saas-api-gateway` (7):
    - [#7](https://github.com/code-corhuila/barber-saas-api-gateway/pull/7) feat(routes): route the workflow's saga operations — merged 2026-10-05
    - [#8](https://github.com/code-corhuila/barber-saas-api-gateway/pull/8) chore: Barber Saas header and the barber-saas-infra-postgres name — merged 2026-10-06
    - [#10](https://github.com/code-corhuila/barber-saas-api-gateway/pull/10) feat(routes): route platform-admin through the gateway — merged 2026-10-06
    - [#13](https://github.com/code-corhuila/barber-saas-api-gateway/pull/13) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#14](https://github.com/code-corhuila/barber-saas-api-gateway/pull/14) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#15](https://github.com/code-corhuila/barber-saas-api-gateway/pull/15) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#16](https://github.com/code-corhuila/barber-saas-api-gateway/pull/16) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-appointment-api` (1):
    - [#19](https://github.com/code-corhuila/barber-saas-appointment-api/pull/19) feat(booking): apply the client's active reward coupon — merged 2026-10-08
  - `barber-saas-appointment-db` (1):
    - [#7](https://github.com/code-corhuila/barber-saas-appointment-db/pull/7) feat(ddl): add the reward coupon of an appointment — merged 2026-10-08
  - `barber-saas-front` (16):
    - [#6](https://github.com/code-corhuila/barber-saas-front/pull/6) feat(session): bind a client session to a barbershop — merged 2026-10-05
    - [#7](https://github.com/code-corhuila/barber-saas-front/pull/7) feat(native): add the Android app with the domain apps inside — merged 2026-10-05
    - [#8](https://github.com/code-corhuila/barber-saas-front/pull/8) fix(native): give the packaged domain apps root-relative addresses — merged 2026-10-05
    - [#9](https://github.com/code-corhuila/barber-saas-front/pull/9) docs(readme): point the header to Barber Saas and barber-saas-docs — merged 2026-10-06
    - [#10](https://github.com/code-corhuila/barber-saas-front/pull/10) feat(shell): the look of the prototype — dark theme, bottom tabs per role, welcome and profile — merged 2026-10-06
    - [#11](https://github.com/code-corhuila/barber-saas-front/pull/11) fix(android): back button goes back; buttons written as sentences — merged 2026-10-06
    - [#12](https://github.com/code-corhuila/barber-saas-front/pull/12) feat(shell): load Ionic Angular domain apps; Plataforma for SUPER_ADMIN — merged 2026-10-06
    - [#15](https://github.com/code-corhuila/barber-saas-front/pull/15) feat(navigation): mount the finance domain app for the owner — merged 2026-10-07
    - [#16](https://github.com/code-corhuila/barber-saas-front/pull/16) feat(navigation): mount the loyalty domain app — merged 2026-10-07
    - [#17](https://github.com/code-corhuila/barber-saas-front/pull/17) chore(git): never version the Firebase files — merged 2026-10-07
    - [#18](https://github.com/code-corhuila/barber-saas-front/pull/18) feat(native): register the device for push after each sign-in — merged 2026-10-07
    - [#19](https://github.com/code-corhuila/barber-saas-front/pull/19) ci: refuse Firebase files and keys in any pull request — merged 2026-10-07
    - [#20](https://github.com/code-corhuila/barber-saas-front/pull/20) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#21](https://github.com/code-corhuila/barber-saas-front/pull/21) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#22](https://github.com/code-corhuila/barber-saas-front/pull/22) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#23](https://github.com/code-corhuila/barber-saas-front/pull/23) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-identity-auth-api` (12):
    - [#12](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/12) feat(http): add POST /api/v1/auth/barbers for owners — merged 2026-10-05
    - [#13](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/13) feat(http): add POST /api/v1/auth/barbershop-token for clients — merged 2026-10-05
    - [#14](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/14) feat(internal): add GET /internal/v1/users/{id} for barbershop — merged 2026-10-05
    - [#15](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/15) chore: Barber Saas header and the barber-saas-infra-postgres name — merged 2026-10-06
    - [#16](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/16) feat(core): reset a password with an e-mailed code — merged 2026-10-07
    - [#17](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/17) feat(persistence): relay the identity-auth outbox to the worker — merged 2026-10-07
    - [#18](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/18) feat(http): add password reset and the outbox relay operations — merged 2026-10-07
    - [#19](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/19) test(http): specify password reset through the outbox — merged 2026-10-07
    - [#20](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/20) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#21](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/21) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#22](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/22) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#23](https://github.com/code-corhuila/barber-saas-identity-auth-api/pull/23) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-identity-auth-app` (9):
    - [#5](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/5) feat(ui): add the three-step barbershop sign-up screen — merged 2026-10-05
    - [#6](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/6) chore: Barber Saas header and the barber-saas-infra-postgres name — merged 2026-10-06
    - [#7](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/7) fix(sign-up): shorten the password hint so it fits on a phone — merged 2026-10-06
    - [#8](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/8) feat(owner): pick an active plan in the last sign-up step — merged 2026-10-07
    - [#9](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/9) fix(owner): word the barber cap of a plan as platform-admin-app — merged 2026-10-07
    - [#10](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/10) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#11](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/11) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#12](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/12) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#13](https://github.com/code-corhuila/barber-saas-identity-auth-app/pull/13) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-identity-auth-db` (7):
    - [#5](https://github.com/code-corhuila/barber-saas-identity-auth-db/pull/5) chore: Barber Saas header and the barber-saas-infra-postgres name — merged 2026-10-06
    - [#6](https://github.com/code-corhuila/barber-saas-identity-auth-db/pull/6) fix(migrations): restore the applied roles changeset byte for byte — merged 2026-10-06
    - [#7](https://github.com/code-corhuila/barber-saas-identity-auth-db/pull/7) feat(ddl): set aside outbox events the worker cannot deliver — merged 2026-10-07
    - [#8](https://github.com/code-corhuila/barber-saas-identity-auth-db/pull/8) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#9](https://github.com/code-corhuila/barber-saas-identity-auth-db/pull/9) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#10](https://github.com/code-corhuila/barber-saas-identity-auth-db/pull/10) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#11](https://github.com/code-corhuila/barber-saas-identity-auth-db/pull/11) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-infra-mongo` (7):
    - [#1](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/1) feat(mongo): the single MongoDB instance and its domain users — merged 2026-10-06
    - [#2](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/2) fix(mongo): run mongo-init only on demand, in the tooling profile — merged 2026-10-06
    - [#3](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/3) fix: keep the container scripts with LF line endings — merged 2026-10-06
    - [#4](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/4) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#5](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/5) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#6](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/6) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#7](https://github.com/code-corhuila/barber-saas-infra-mongo/pull/7) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-infra-postgres` (17):
    - [#11](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/11) feat(compose): include the workflow in the platform — merged 2026-10-05
    - [#12](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/12) feat(keys): issue the barbershop and schedule service tokens — merged 2026-10-05
    - [#13](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/13) docs(readme): use the new repository name barber-saas-infra-postgres — merged 2026-10-06
    - [#14](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/14) docs(readme): point the header to Barber Saas and barber-saas-docs — merged 2026-10-06
    - [#15](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/15) feat(keys): issue the platform-admin service token — merged 2026-10-06
    - [#16](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/16) feat(compose): include the single MongoDB instance in the platform — merged 2026-10-06
    - [#19](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/19) feat(compose): include platform-admin in the platform — merged 2026-10-06
    - [#20](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/20) feat(scripts): create the development SUPER_ADMIN — merged 2026-10-06
    - [#21](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/21) feat(compose): include the worker in the platform — merged 2026-10-06
    - [#23](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/23) feat(compose): migrate the notifications database with the platform — merged 2026-10-07
    - [#26](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/26) chore(env): name the FCM service account variable — merged 2026-10-07
    - [#27](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/27) ci: refuse env files, Firebase files and keys in any pull request — merged 2026-10-07
    - [#28](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/28) chore(env): list the smtp settings of the password-reset e-mail — closed without merge 2026-10-08
    - [#30](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/30) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#31](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/31) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#32](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/32) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#33](https://github.com/code-corhuila/barber-saas-infra-postgres/pull/33) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-loyalty-api` (1):
    - [#15](https://github.com/code-corhuila/barber-saas-loyalty-api/pull/15) feat(events): use the coupon applied at booking from AppointmentCreated — merged 2026-10-08
  - `barber-saas-notifications-api` (2):
    - [#10](https://github.com/code-corhuila/barber-saas-notifications-api/pull/10) feat(events): e-mail the password-reset code instead of an inbox notice — merged 2026-10-08
    - [#11](https://github.com/code-corhuila/barber-saas-notifications-api/pull/11) feat(email): send the password-reset code through smtp — merged 2026-10-08
  - `barber-saas-notifications-db` (1):
    - [#4](https://github.com/code-corhuila/barber-saas-notifications-db/pull/4) feat(ddl): add the processed_event collection — merged 2026-10-08
  - `barber-saas-platform-admin-api` (11):
    - [#2](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/2) docs(readme): point the header to Barber Saas and barber-saas-docs — merged 2026-10-06
    - [#3](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/3) chore: the platform-admin-api foundation (modules, tokens, errors, CI) — merged 2026-10-06
    - [#4](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/4) feat(plans): the subscription plan, its use cases and its persistence — merged 2026-10-06
    - [#5](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/5) feat(http): serve the plans of platform-admin-service — merged 2026-10-06
    - [#6](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/6) feat(barbershops): operate barbershops through barbershop-api; trial expiry — merged 2026-10-06
    - [#7](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/7) feat(http): barbershops of the platform, plan deactivation and trial expiry — merged 2026-10-06
    - [#8](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/8) feat(internal): assign the plan chosen at sign-up for the workflow — merged 2026-10-07
    - [#9](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/9) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#10](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/10) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#11](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/11) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#12](https://github.com/code-corhuila/barber-saas-platform-admin-api/pull/12) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-platform-admin-app` (10):
    - [#2](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/2) docs(readme): point the header to Barber Saas and barber-saas-docs — merged 2026-10-06
    - [#3](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/3) chore: set up the Ionic Angular domain app with Native Federation — merged 2026-10-06
    - [#4](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/4) feat(platform): shell contract, view states and the platform-admin calls — merged 2026-10-06
    - [#5](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/5) feat(platform): barbershop screens of the SUPER_ADMIN — merged 2026-10-06
    - [#6](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/6) feat(platform): plan screens, deploy and the Angular domain app guide — merged 2026-10-06
    - [#7](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/7) fix(plans): say 'Hasta 1 barbero' for a one-barber plan — merged 2026-10-06
    - [#8](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/8) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#9](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/9) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#10](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/10) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#11](https://github.com/code-corhuila/barber-saas-platform-admin-app/pull/11) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-platform-admin-db` (6):
    - [#2](https://github.com/code-corhuila/barber-saas-platform-admin-db/pull/2) docs(readme): point the header to Barber Saas and barber-saas-docs — merged 2026-10-06
    - [#3](https://github.com/code-corhuila/barber-saas-platform-admin-db/pull/3) feat: the platform_admin schema, its plans and its permissions — merged 2026-10-06
    - [#4](https://github.com/code-corhuila/barber-saas-platform-admin-db/pull/4) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#5](https://github.com/code-corhuila/barber-saas-platform-admin-db/pull/5) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#6](https://github.com/code-corhuila/barber-saas-platform-admin-db/pull/6) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#7](https://github.com/code-corhuila/barber-saas-platform-admin-db/pull/7) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-schedule-app` (1):
    - [#12](https://github.com/code-corhuila/barber-saas-schedule-app/pull/12) fix(ui): keep the save bar above the cards' time inputs — merged 2026-10-06
  - `barber-saas-worker` (13):
    - [#2](https://github.com/code-corhuila/barber-saas-worker/pull/2) docs(readme): point the header to Barber Saas and barber-saas-docs — merged 2026-10-06
    - [#3](https://github.com/code-corhuila/barber-saas-worker/pull/3) feat: the worker's domain, routing, retry policy, relay and daily jobs — closed without merge 2026-10-06
    - [#4](https://github.com/code-corhuila/barber-saas-worker/pull/4) feat: the worker's HTTP adapters, scheduler, health and image — merged 2026-10-06
    - [#5](https://github.com/code-corhuila/barber-saas-worker/pull/5) fix(jobs): wait before calling a service that did not answer — merged 2026-10-06
    - [#6](https://github.com/code-corhuila/barber-saas-worker/pull/6) feat(deploy): deliver events to notifications-api — merged 2026-10-07
    - [#7](https://github.com/code-corhuila/barber-saas-worker/pull/7) feat(deploy): relay the identity-auth outbox — merged 2026-10-07
    - [#8](https://github.com/code-corhuila/barber-saas-worker/pull/8) feat(relay): relay loyalty's outbox before loyalty consumes events — merged 2026-10-07
    - [#9](https://github.com/code-corhuila/barber-saas-worker/pull/9) feat(deploy): deliver events to loyalty-api — merged 2026-10-07
    - [#10](https://github.com/code-corhuila/barber-saas-worker/pull/10) feat(routing): deliver AppointmentCreated to loyalty — merged 2026-10-08
    - [#11](https://github.com/code-corhuila/barber-saas-worker/pull/11) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#12](https://github.com/code-corhuila/barber-saas-worker/pull/12) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#13](https://github.com/code-corhuila/barber-saas-worker/pull/13) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#14](https://github.com/code-corhuila/barber-saas-worker/pull/14) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
  - `barber-saas-workflow` (16):
    - [#2](https://github.com/code-corhuila/barber-saas-workflow/pull/2) chore(app): lay out the hexagonal workflow service — merged 2026-10-05
    - [#3](https://github.com/code-corhuila/barber-saas-workflow/pull/3) chore(repo): add the pull request template, board tracking and readme — merged 2026-10-05
    - [#4](https://github.com/code-corhuila/barber-saas-workflow/pull/4) feat(db): version the workflow schema and its migration runner — merged 2026-10-05
    - [#5](https://github.com/code-corhuila/barber-saas-workflow/pull/5) feat(domain): model the owner-onboarding saga and its steps — merged 2026-10-05
    - [#6](https://github.com/code-corhuila/barber-saas-workflow/pull/6) feat(usecase): orchestrate owner onboarding with its compensation — merged 2026-10-05
    - [#7](https://github.com/code-corhuila/barber-saas-workflow/pull/7) feat(http): saga participants over HTTP and the JDBC saga store — merged 2026-10-05
    - [#8](https://github.com/code-corhuila/barber-saas-workflow/pull/8) feat(http): validate tokens on every saga route but the sign-up — merged 2026-10-05
    - [#9](https://github.com/code-corhuila/barber-saas-workflow/pull/9) feat(http): start owner onboarding and read a saga over HTTP — merged 2026-10-05
    - [#10](https://github.com/code-corhuila/barber-saas-workflow/pull/10) feat(deploy): package the workflow and compose it with its runner — merged 2026-10-05
    - [#11](https://github.com/code-corhuila/barber-saas-workflow/pull/11) chore: Barber Saas header and the barber-saas-infra-postgres name — merged 2026-10-06
    - [#12](https://github.com/code-corhuila/barber-saas-workflow/pull/12) fix(migrations): restore the applied roles changeset byte for byte — merged 2026-10-06
    - [#13](https://github.com/code-corhuila/barber-saas-workflow/pull/13) feat(saga): assign the plan the owner picked during onboarding — merged 2026-10-07
    - [#14](https://github.com/code-corhuila/barber-saas-workflow/pull/14) chore(release): prepare 2.0.0 — CI rules and CHANGELOG — merged 2026-10-08
    - [#15](https://github.com/code-corhuila/barber-saas-workflow/pull/15) chore(release): promote MVP 2 to qa with cherry-pick -x — merged 2026-10-08
    - [#16](https://github.com/code-corhuila/barber-saas-workflow/pull/16) chore(release): fill release 2.0.0 from qa with cherry-pick -x — merged 2026-10-08
    - [#17](https://github.com/code-corhuila/barber-saas-workflow/pull/17) release: 2.0.0 — MVP 2 (corte 2) — merged 2026-10-09
- *End of the complete record.*

![Resumen Semana 10](Week-10.jpg)
