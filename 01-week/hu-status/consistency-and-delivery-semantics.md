# Consistency and delivery semantics per core operation — BarberSaaS

> Week 1, Session 1 deliverable: for each core operation of the system, define the
> consistency model it requires and the delivery semantics it must guarantee. These
> choices will be defended during the MVP 1 design.

## How to read this table

- **Consistency** — *Strong* (must be correct immediately, inside one transaction) vs.
  *Eventual* (a short delay before every reader sees the same value is acceptable).
- **Delivery semantics** — what happens if the operation (or the message that triggers it)
  is retried or delivered more than once: *Exactly-once (effective)*, *At-least-once*, or
  *Best-effort*.

## Core operations

| # | Operation | Consistency required | Delivery semantics | Why |
|---|-----------|----------------------|---------------------|-----|
| 1 | Register a new account | Strong | Exactly-once (effective), via a unique constraint on email | Two accounts for the same email would break login and role assignment |
| 2 | Log in / issue JWT | Strong | Exactly-once per request | A stale or duplicated session token is a security issue, not just a UX one |
| 3 | Book an appointment | Strong | Exactly-once (effective), via an idempotency key per booking attempt + a uniqueness constraint on (barber, time slot) | Two bookings for the same barber/slot is the double-booking bug the whole domain exists to prevent |
| 4 | Cancel / reschedule an appointment | Strong | Exactly-once per request | Must never leave the slot in an ambiguous state (both "cancelled" and "occupied") |
| 5 | Decrement product stock on a completed service | Strong | Exactly-once, in the same transaction as the sale/invoice | Overselling stock (or double-decrementing it) is a direct financial/inventory error |
| 6 | Process payment / generate invoice | Strong | Exactly-once (effective), via an idempotency key per payment attempt | A retried request must never charge or invoice the client twice |
| 7 | Accrue loyalty points | Eventual (a few seconds/minutes of delay is acceptable) | At-least-once, with idempotent crediting (dedupe by appointment ID) | Losing a point accrual is a minor annoyance; the system must tolerate the reminder/points job re-running without double-crediting |
| 8 | Send appointment reminder | Eventual | At-least-once (better to remind twice than not at all) | The consumer (client's device/notification center) must be idempotent per reminder ID |
| 9 | Update favorites / reviews / gallery | Eventual | Best-effort | Not safety- or money-critical; a lost or duplicated write here has no real business impact |
| 10 | Onboard a new barbershop (tenant) | Strong | Exactly-once, atomic (barbershop + owner user + default plan created together or not at all) | A half-created tenant (shop without an owner, or owner without a shop) breaks the multi-tenant model from day one |
| 11 | Change a subscription plan | Strong | Exactly-once per request | Billing must never apply a plan change twice or leave the tenant on an undefined plan |

## Consequence for MVP 1 design

Operations 1–6 and 10–11 are why the system needs strong consistency by default for
anything that touches money, capacity (a barber's calendar), or tenant identity — this is
consistent with choosing a modular monolith (one database, real ACID transactions) over an
early split into microservices with eventual consistency between them.

Operations 7–9 are the only ones that can tolerate eventual consistency and at-least-once
or best-effort delivery — these are the natural candidates for background jobs / async
processing without putting correctness at risk.

## Correlations
- Architecture style decision → `barber-saas-docs` repo, `05-architecture/decisions/records/`
- Backlog this depends on → `03-product/product-backlog.md` (DOCS repo)
