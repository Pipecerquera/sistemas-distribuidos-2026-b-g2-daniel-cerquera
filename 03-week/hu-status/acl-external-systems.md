# Anti-Corruption Layer (ACL) — External systems consumed by BarberSaaS

> Week 3, Session 2 deliverable: for MVP 1, identify where BarberSaaS consumes an external
> system, and how the domain is isolated (ACL) from that external system's own model.
>
> BarberSaaS is a modular monolith (ADR-002) — there is no other internal service to
> integrate with yet, so "external system" here means real **third-party** systems, not
> another BarberSaaS module.

## Real external systems in scope

| # | External system | Consumed by | Purpose |
|---|------------------|-------------|---------|
| 1 | Firebase Admin SDK (FCM) | `com.barbersaas.notification` | Push notifications (appointment reminders, loyalty rewards) |
| 2 | Gmail SMTP (Spring Mail) | `com.barbersaas.auth` | Password-reset email delivery |

## 1 — Firebase Admin SDK / FCM

| Aspect | Detail |
|---|---|
| External model | An FCM `Message`: a device token + title/body + a flat key-value `data` payload. No concept of `User`, `Barbershop`, or `Appointment` — Firebase only knows "send this payload to this token". |
| Domain model | A `Notification` belonging to a `User`, with a `type` (`APPOINTMENT_REMINDER`, `LOYALTY_REWARD`, …) and Spanish-localized `title`/`body`, per `02-domain/Domain_Events_Luxury_Barber_EN.md`. |
| Where the translation happens | Only inside `com.barbersaas.notification`, at the point of dispatch — it maps the domain `Notification` to an FCM `Message` right before calling the Firebase SDK. No other module, and no domain entity, imports a Firebase SDK type. |
| Isolation mechanism | **DB-first write** (documented as principle P3 in `05-architecture/overview.md`): the `Notification` row is persisted *before* the FCM call is attempted, so the domain's notion of "the notification exists" never depends on Firebase succeeding — a push failure degrades gracefully instead of corrupting domain state. |
| Credential isolation | `firebase-service-account.json` is excluded from git and injected via a configured file path (`FCM_CREDENTIALS_PATH`) — the domain code never sees raw Firebase credentials. |

## 2 — Gmail SMTP (Spring Mail)

| Aspect | Detail |
|---|---|
| External model | An SMTP message: `to`/`from`/`subject`/`body`, transport-specific (host, port, auth handled by Spring Mail / Gmail). |
| Domain model | A `PasswordResetToken`: a 6-digit code, 15-minute expiry, single use, tied to a `User`. |
| Where the translation happens | `com.barbersaas.auth` generates the code and expiry as pure domain logic, then hands off *"send this code to this email"* to Spring Mail's abstraction — the domain never constructs an SMTP message itself or depends on Gmail-specific behavior. |
| Isolation mechanism | Spring Mail's `JavaMailSender` interface is the seam: swapping Gmail for another SMTP provider (or a transactional email API) would only change configuration, not domain code. |
| Credential isolation | Mail credentials are environment variables, never hardcoded (per the repo's secrets rule). |

## What is NOT an ACL case here

Inter-module calls inside `barbersaas-backend` (e.g., `AppointmentService` calling
`notificationService.notify(...)` directly) are **not** an ACL boundary — they are in-process
calls between bounded contexts of the same modular monolith, governed by ADR-002's
"no direct cross-context imports except through domain entities" rule, not by ACL. ACL
specifically applies to translating a **third-party** system's model into the domain's own
language, which is why only Firebase and Gmail are in scope here.

## Correlations
- Domain events these adapters react to → `02-domain/Domain_Events_Luxury_Barber_EN.md`
- Cross-cutting concerns (push notifications, secrets) → `05-architecture/overview.md` §7
- Architecture style (modular monolith, no message broker) → `ADR-002-modular-monolith.md`
