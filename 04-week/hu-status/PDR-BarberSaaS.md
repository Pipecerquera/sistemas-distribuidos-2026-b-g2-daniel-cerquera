# Product Requirements Document (PRD)
## BarberSaaS — Multi-Tenant Barbershop Management Platform

---

| Field | Details |
|---|---|
| **Document version** | 2.0 |
| **Status** | In Development — Phase 1 Complete, Phase 2 In Progress |
| **Last updated** | August 2026 |
| **Confidentiality** | Internal — Proprietary |
| **Team members** | Carlos Mauricio Leal Medina · Daniel Felipe Cerquera Idrobo · Juan Pablo Borrero Morales · Carolay Arraut Heredia |

---

## Table of Contents

1. Executive Summary
2. Problem Statement
3. Goals & Success Metrics
4. Target Users & Personas
5. Market Context & Competitive Analysis
6. Product Scope
7. Functional Requirements
8. Non-Functional Requirements
9. System Architecture Overview
10. Microservices & Modular Design
11. Data Model & Database
12. API Design Principles
13. Security & Compliance
14. Monetization Model
15. Release Roadmap
16. Risks & Mitigations
17. Open Questions
18. Appendix

---

## 1. Executive Summary

BarberSaaS is a cloud-based, multi-tenant SaaS platform designed to fully digitize the operations of barbershops across Colombia. It provides a complete suite of tools for four distinct user roles — platform administrator, barbershop owner, barber, and client — covering appointment scheduling, staff management, loyalty programs, financial tracking, inventory management, and real-time push notifications, all accessible from a single mobile application.

The platform targets the underserved barbershop market in Colombia, where the vast majority of businesses still rely on WhatsApp groups, paper notebooks, and verbal agreements to manage day-to-day operations. BarberSaaS brings the operational capabilities previously available only to large salon chains within reach of any independent barbershop, at a fraction of the cost.

The business model is a monthly SaaS subscription with a **60-day free trial**, giving shop owners sufficient time to experience the full value of the product before committing to a paid plan. Initial billing is manual (bank transfer / Nequi), with automated payment processing planned for Phase 3.

The backend is implemented as a **modular monolith** (Spring Boot 3 / Java 21 / PostgreSQL) designed with explicit bounded contexts, enabling future extraction of individual services (starting with Notifications) when scaling needs justify it. The mobile frontend uses React Native + Expo SDK 54, targeting Android and iOS from a single codebase.

---

## 2. Problem Statement

### 2.1 The Current Reality

Barbershops in Colombia — particularly in mid-sized cities like Neiva, Ibagué, Pasto, Cúcuta, and Manizales — face a set of recurring operational problems that directly impact revenue, client retention, and business visibility:

**For barbershop owners (ADMIN_BARBERSHOP):**
- No reliable way to track individual barber performance (clients served, revenue generated per barber)
- Manual income and expense tracking on paper or basic spreadsheets, with no consolidated financial view
- No loyalty program to incentivize repeat clients — retention depends entirely on personal relationships
- No appointment scheduling system — clients call, text on WhatsApp, or simply arrive, causing unpredictable queues
- No formal way to track product inventory or generate restock alerts
- No visibility into which services generate the most revenue

**For barbers (BARBER):**
- No visibility into their daily or weekly schedule — they learn about appointments informally
- No personal metrics: how many clients served, total revenue contributed, busiest hours
- Manual coordination with the owner for every schedule exception or day off

**For clients (CLIENT):**
- No way to book an appointment without calling or messaging on WhatsApp
- No appointment reminders — clients forget and the barbershop loses the slot
- No formal loyalty tracking — they rely on the barber's memory for any informal reward

**For walk-in clients:**
- Walk-ins (clients who arrive without prior booking) cannot be tracked in existing tools — their services are invisible to the business metrics

### 2.2 The Gap

Existing digital solutions (Booksy, Fresha, Treatwell) are designed for and priced for Western markets. They lack Spanish-language UX tailored to Colombian colloquialisms, do not support the specific workflows of Colombian barbershops (walk-in client tracking without registration, offline-resilient scenarios, COP currency), and carry pricing that puts them out of reach for small independent shops.

---

## 3. Goals & Success Metrics

### 3.1 Business Goals

| Goal | Metric | Target (12 months post-launch) |
|---|---|---|
| Acquire paying barbershops | Active paid subscriptions | 50 barbershops |
| Validate retention | Monthly churn rate | < 5% |
| Generate recurring revenue | Monthly Recurring Revenue (MRR) | COP $5,000,000 |
| Expand geographically | Cities with ≥1 active barbershop | 5+ cities in Colombia |
| Convert trial users | Trial-to-paid conversion rate | > 40% |

### 3.2 Product Goals

| Goal | Metric | Target |
|---|---|---|
| Appointment digitization | % of appointments booked through the app (vs walk-in) | > 70% within 30 days of onboarding |
| Push notification delivery | FCM delivery rate | > 95% |
| Platform reliability | API uptime | > 99.5% monthly |
| Client booking experience | Time to complete booking (search → confirmation) | < 90 seconds |
| Loyalty engagement | % of barbershops with ≥1 loyalty redemption per month | > 60% |

### 3.3 Leading Indicators (Early Validation)

- At least 3 barbershops complete their first full week of operations on the platform within 30 days of launch
- At least 80% of onboarded barbershops configure their service catalog and at least one barber schedule within 48 hours of registration
- At least one loyalty sticker granted within the first 7 days for every active barbershop
- Zero double-bookings in the first 90 days of production operation

---

## 4. Target Users & Personas

### Persona 1 — The Barbershop Owner (ADMIN_BARBERSHOP)

**Name:** Carlos, 34 years old, Neiva, Huila
**Context:** Owns a barbershop with 2–4 barbers. Has a smartphone, uses WhatsApp daily, has basic comfort with mobile apps but no technical background. His biggest pain is not knowing his monthly numbers, and not being able to hold barbers accountable for their individual performance.
**Goal:** Understand his business numbers, control his team's schedule, keep clients coming back, and reduce no-shows.
**Frustration:** *"I don't know how much each barber brings in. I just trust them."*
**How BarberSaaS helps:** Dashboard with per-barber revenue, appointment history, loyalty config, and financial records.

---

### Persona 2 — The Barber (BARBER)

**Name:** Andrés, 24 years old, Neiva
**Context:** Works at a barbershop as an employee. Very comfortable with smartphones and social media. Wants to build his own client base and knows his reputation depends on reliability and punctuality.
**Goal:** See his daily agenda clearly, know when clients are arriving, and track his personal service metrics.
**Frustration:** *"Clients show up whenever, and I never know if it's going to be a busy day or empty."*
**How BarberSaaS helps:** Personal agenda screen with date chips, appointment state transitions, and stats (services completed, revenue, busiest hours).

---

### Persona 3 — The Client (CLIENT)

**Name:** María, 28 years old, Neiva
**Context:** Regular client at a barbershop. Books via WhatsApp, sometimes forgets appointments, loves the idea of earning a free haircut after enough visits.
**Goal:** Book easily, receive reminders, and feel valued as a loyal client.
**Frustration:** *"I have to send a message and wait for a reply to know if there's even availability."*
**How BarberSaaS helps:** Browse barbershops, book in < 90 seconds, receive push reminders, track loyalty stickers, and get automatic free appointments when reward is redeemed.

---

### Persona 4 — The Platform Administrator (SUPER_ADMIN)

**Name:** Internal BarberSaaS team member
**Context:** Manages the entire platform. Creates barbershop accounts, manages subscription plans, monitors platform health, and controls trial/billing cycles.
**Goal:** Full visibility over all active barbershops, ability to create/suspend/reactivate accounts, and manage what plans are available.
**How BarberSaaS helps:** Platform dashboard (all barbershops, revenue, active/trial/suspended counts), plan management, direct barbershop creation with first-admin assignment.

---

## 5. Market Context & Competitive Analysis

### 5.1 Market Opportunity

- Colombia has approximately **80,000+ registered barbershops and hair salons** (DANE, 2023)
- The mid-sized city segment (Neiva, Ibagué, Pasto, Manizales, Montería — cities with 100k–500k population) is largely unserved by digital tools
- Average monthly spend on operational tools in this segment: < COP $50,000
- Target Addressable Market (TAM): ~15,000 barbershops in Colombian cities with 100k–500k population
- Serviceable Addressable Market (SAM — reachable digitally in Phase 1): ~2,000 barbershops in Huila department
- Serviceable Obtainable Market (SOM — Year 1 target): 50 barbershops

### 5.2 Competitive Landscape

| Competitor | Strengths | Weaknesses vs. BarberSaaS |
|---|---|---|
| **Booksy** | Strong global brand, client discovery marketplace | English-first UX, USD pricing, not adapted to Colombian workflows, no walk-in tracking |
| **Fresha** | Commission-free model, polished UI | Lacks Colombian Spanish localization, requires stable internet, no offline resilience |
| **WhatsApp + notebooks** | Zero cost, universally familiar | No scheduling logic, no financial tracking, no loyalty, completely manual, human error-prone |
| **Custom Excel sheets** | Flexible, familiar | No real-time capability, no mobile, no automation, no reminders |
| **No solution (verbal)** | Zero friction | No data, no accountability, no client retention tools |

### 5.3 Differentiation

BarberSaaS is the only platform in the Colombian market that combines:
- Appointment booking with **anti-double-booking guarantee** (pessimistic DB lock)
- **Walk-in client tracking** without requiring client registration
- **Loyalty card with automatic coupon** (next booking free, applied automatically)
- **Per-barber financial tracking** giving owners visibility into individual performance
- Native **Spanish (Colombia)** UX with COP currency formatting
- **60-day free trial** with no credit card required

---

## 6. Product Scope

### 6.1 In Scope — MVP (Current)

| Feature | Status |
|---|---|
| Multi-tenant architecture with `barbershop_id` isolation | ✅ Complete |
| Four user roles: SUPER_ADMIN, ADMIN_BARBERSHOP, BARBER, CLIENT | ✅ Complete |
| JWT authentication with TenantContext (ThreadLocal) | ✅ Complete |
| Password recovery via 6-digit email code (15 min expiry) | ✅ Complete |
| Barbershop self-registration (3-step wizard + 60-day trial) | ✅ Complete |
| Subscription plan management (Super Admin) | ✅ Complete |
| Service catalog management per barbershop | ✅ Complete |
| Barber schedule + exceptions | ✅ Complete |
| Appointment booking with real-time availability | ✅ Complete |
| Anti-double-booking (PESSIMISTIC_WRITE DB lock) | ✅ Complete |
| Appointment state machine (6 states) | ✅ Complete |
| Appointment cancellation with policy enforcement | ✅ Complete |
| Appointment rescheduling | ✅ Complete |
| Walk-in client tracking (admin adds without client registration) | 🔄 In progress |
| Loyalty card system (stickers + automatic reward coupon) | ✅ Complete |
| Financial records (income / expense tracking) | ✅ Complete |
| Inventory management with stock alerts | ✅ Complete |
| Push notifications via FCM (appointment events) | ✅ Complete |
| In-app notification history | ✅ Complete |
| Email notification (password recovery via Gmail SMTP) | ✅ Complete |
| Super Admin platform dashboard | ✅ Complete |
| Per-barber stats (services completed, revenue, busiest hours) | ✅ Complete |
| Gallery (barbershop photos) | ✅ Complete |
| Client favorites (saved barbershops) | ✅ Complete |
| Review system | ✅ Complete |

### 6.2 Out of Scope — MVP (Planned for Future Phases)

| Feature | Target Phase |
|---|---|
| In-app payment processing (Stripe, PSE, Nequi) | Phase 3 |
| Automated billing and dunning management | Phase 3 |
| Client-facing web app (QR-based booking, no app install) | Phase 3 |
| Advanced analytics and PDF/Excel report exports | Phase 3 |
| Multi-location chains under one account | Phase 4 |
| Client marketplace / barbershop discovery by map | Phase 4 |
| Google Calendar / Apple Calendar integration | Phase 4 |
| Notification microservice extraction | Phase 2 (trigger-based) |
| Appointment microservice extraction | Phase 3 (trigger-based) |

---

## 7. Functional Requirements

### 7.1 Authentication & Identity

| ID | Requirement | Priority |
|---|---|---|
| AUTH-01 | Users register with full name, email, password (min 8 chars), and phone | Must Have |
| AUTH-02 | All authenticated sessions use JWT (HS512) with claims: `userId`, `role`, `barbershopId` | Must Have |
| AUTH-03 | JWT tokens expire after 24 hours; refresh tokens valid for 7 days | Must Have |
| AUTH-04 | Password recovery via 6-digit code sent to the user's registered email (expires in 15 min) | Must Have |
| AUTH-05 | Code is invalidated after first use; previous codes for same user are invalidated on new request | Must Have |
| AUTH-06 | Barbershop owners self-register in a 3-step wizard: account → barbershop data → plan selection | Must Have |
| AUTH-07 | Barbers are added by the ADMIN_BARBERSHOP — they do not self-register | Must Have |
| AUTH-08 | Welcome/landing screen shown before login with app context and value proposition | Must Have |
| AUTH-09 | Registration type selection screen: Client / Barbershop Owner / Barber (informational only) | Must Have |

### 7.2 Appointment System

| ID | Requirement | Priority |
|---|---|---|
| APPT-01 | Clients can search barbershops by city and view public profile (services, barbers, gallery) | Must Have |
| APPT-02 | Clients can book by selecting: barbershop → service → barber → date → time slot | Must Have |
| APPT-03 | System prevents double-booking using `PESSIMISTIC_WRITE` lock — guaranteed at DB level | Must Have |
| APPT-04 | Appointments follow a 6-state machine: PENDING → CONFIRMED → IN_PROGRESS → COMPLETED / CANCELLED / NO_SHOW | Must Have |
| APPT-05 | Clients can cancel appointments subject to the barbershop's configurable `cancellationPolicyHours` | Must Have |
| APPT-06 | Clients can reschedule, triggering re-confirmation | Must Have |
| APPT-07 | Admins and barbers can mark appointments as NO_SHOW | Must Have |
| APPT-08 | Walk-in clients can be added by the admin directly to a barber's agenda without requiring client registration | Must Have |
| APPT-09 | `priceAtBooking` is captured at reservation time — set to `0` automatically if client has an active `RewardCoupon` | Must Have |
| APPT-10 | Appointment reminders sent via push notification 1 hour before the scheduled time (`@Scheduled` job) | Should Have |

### 7.3 Loyalty & Rewards

| ID | Requirement | Priority |
|---|---|---|
| LOYAL-01 | Each barbershop configures its loyalty program: stickers required and reward description | Must Have |
| LOYAL-02 | Admins and barbers can grant stickers from the completed appointment view or by searching the client by name/email | Must Have |
| LOYAL-03 | When a client accumulates enough stickers, admin/barber can redeem the reward | Must Have |
| LOYAL-04 | Redemption automatically generates a `RewardCoupon` with status `ACTIVE` | Must Have |
| LOYAL-05 | The coupon is automatically applied as 100% discount on the client's next booking at that barbershop (`priceAtBooking = 0`) | Must Have |
| LOYAL-06 | Coupon status changes to `USED` and is linked to the appointment after successful booking | Must Have |
| LOYAL-07 | Clients can view their loyalty card: sticker count, progress grid, reward description, active coupon banner | Must Have |
| LOYAL-08 | Admins and barbers can view a client's loyalty card before granting/redeeming | Must Have |

### 7.4 Staff & Schedule Management

| ID | Requirement | Priority |
|---|---|---|
| STAFF-01 | Admins can add, edit, and deactivate barbers (employees) | Must Have |
| STAFF-02 | Each barber has a configurable weekly schedule (day of week + start/end time) | Must Have |
| STAFF-03 | Schedule exceptions (day off, modified hours) can be added per barber per date | Must Have |
| STAFF-04 | Availability algorithm considers schedule + existing appointments to compute open slots | Must Have |
| STAFF-05 | Admins can view the full barbershop agenda (all barbers, any date) | Must Have |

### 7.5 Financial Tracking

| ID | Requirement | Priority |
|---|---|---|
| FIN-01 | Admins can manually register income and expense records | Must Have |
| FIN-02 | Dashboard shows: total revenue, total expenses, net profit for a configurable period | Must Have |
| FIN-03 | Revenue from completed appointments is automatically recorded as an INCOME entry | Should Have |
| FIN-04 | Per-barber revenue breakdown available in Barber Stats | Should Have |

### 7.6 Notifications

| ID | Requirement | Priority |
|---|---|---|
| NOTIF-01 | Push notifications sent for: appointment booked, confirmed, cancelled, completed, reminder | Must Have |
| NOTIF-02 | All notifications are persisted to DB before FCM delivery attempt — FCM failure does not block the operation | Must Have |
| NOTIF-03 | Users can view in-app notification history | Should Have |
| NOTIF-04 | Email is used exclusively for password recovery in MVP | Must Have |
| NOTIF-05 | Device tokens (FCM registration tokens) are registered per user per device at login | Must Have |

### 7.7 Platform Administration (Super Admin)

| ID | Requirement | Priority |
|---|---|---|
| SA-01 | Super Admin can view all barbershops with status and plan | Must Have |
| SA-02 | Super Admin can create, update, and soft-delete subscription plans | Must Have |
| SA-03 | Super Admin can manually create a barbershop and assign its first ADMIN_BARBERSHOP | Must Have |
| SA-04 | Super Admin can activate, suspend, or cancel a barbershop account | Must Have |
| SA-05 | Platform dashboard: total barbershops, ACTIVE/TRIAL/SUSPENDED counts, total clients, total revenue | Must Have |
| SA-06 | Trial expiration tracked via `trial_ends_at`; Super Admin manually transitions to ACTIVE on payment | Must Have |

---

## 8. Non-Functional Requirements

### 8.1 Performance

| Requirement | Target |
|---|---|
| API response time (p95) — read operations | < 300 ms |
| API response time (p95) — appointment creation (with lock) | < 500 ms |
| Concurrent users supported (MVP) | 500 simultaneous users |
| Double-booking rate under concurrent requests | 0% (guaranteed by pessimistic lock) |

### 8.2 Availability & Reliability

| Requirement | Target |
|---|---|
| API uptime | > 99.5% monthly |
| FCM unavailability impact on operations | Zero — graceful degradation implemented |
| Database backups | Daily automated backups, 30-day retention (Railway managed) |
| Recovery Time Objective (RTO) | < 1 hour |
| Recovery Point Objective (RPO) | < 24 hours |

### 8.3 Scalability

The system is a **modular monolith** designed for horizontal scaling behind a load balancer. Stateless JWT authentication enables scaling without session affinity. The architecture explicitly defines extraction triggers for each bounded context (see Section 10).

### 8.4 Security

| Requirement | Implementation |
|---|---|
| Authentication | JWT (HS512), 24h expiry, refresh token 7 days |
| Tenant isolation | `barbershop_id` validated in every service method via `TenantContext` |
| Password storage | BCrypt (strength 10) |
| Transport security | HTTPS enforced — Railway auto-TLS |
| Secrets management | All secrets via environment variables (`$env:VAR=value`) — never hardcoded |
| Firebase credentials | `firebase-service-account.json` excluded from VCS via `.gitignore` |
| Rate limiting | Planned for `/api/auth/**` before production (prevent brute-force) |

### 8.5 Internationalization

| Field | Value |
|---|---|
| Language | Spanish (Colombia) — all UI strings, error messages, emails |
| Currency | Colombian Peso (COP) — `toLocaleString('es-CO')` |
| Default timezone | `America/Bogota` (UTC-5) |
| Date format | DD/MM/YYYY, 12-hour clock |

---

## 9. System Architecture Overview

### 9.1 Technology Stack

| Layer | Technology | Version | Rationale |
|---|---|---|---|
| Mobile frontend | React Native + Expo | SDK 54 | Single codebase for Android/iOS; fast iteration |
| Navigation | Expo Router | v3 | File-based routing; role-based route groups |
| State management | Zustand + TanStack Query | Latest | Lightweight auth store; automatic server-state cache |
| Backend | Spring Boot + Java | 3.3.4 / 21 | Production-grade; excellent JPA + Security ecosystem |
| Authentication | jjwt + Spring Security | 0.12.6 | Stateless JWT; no server-side session |
| Database | PostgreSQL | 16 | ACID, superior JSONB, better scale than MySQL |
| Cache | Redis | 7 | Future rate limiting; session revocation list |
| Push notifications | Firebase Admin SDK (FCM) | 9.4.1 | Native push for Android + iOS in one integration |
| Email | Gmail SMTP (Spring Mail) | — | Zero-cost for MVP volume |
| Mobile builds | EAS Build (Expo) | — | Cloud-based native APK/IPA without local toolchain |
| Backend hosting | Railway | — | Zero-config Spring Boot + PostgreSQL; automatic TLS |
| API documentation | SpringDoc OpenAPI (Swagger) | 2.6.0 | Auto-generated from annotations |

### 9.2 Architecture Pattern: Modular Monolith

```
┌──────────────────────────────────────────────────────────────────────┐
│                    barbersaas-backend (Spring Boot)                  │
│                                                                      │
│  ┌───────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │   auth    │  │ barbershop  │  │ appointment │  │   loyalty   │  │
│  │           │  │  + employee │  │  + schedule │  │  + rewards  │  │
│  └───────────┘  └─────────────┘  └─────────────┘  └─────────────┘  │
│  ┌───────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │  finance  │  │  inventory  │  │notification │  │    plan     │  │
│  │           │  │             │  │ FCM + email │  │ super-admin │  │
│  └───────────┘  └─────────────┘  └─────────────┘  └─────────────┘  │
│                                                                      │
│            shared: domain entities · security · TenantContext        │
└──────────────────────────────┬───────────────────────────────────────┘
                               │ JDBC / HikariCP
                    ┌──────────▼──────────┐
                    │     PostgreSQL 16    │
                    │    (barbersaas DB)   │
                    └─────────────────────┘
```

### 9.3 Request lifecycle

```
iPhone / Android App
        │
        │ HTTPS (JWT in Authorization header)
        ▼
┌─────────────────────────┐
│  JwtAuthenticationFilter│  ← validates JWT, populates TenantContext
│  (Spring Security)      │     (userId, barbershopId, role)
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  @RestController        │  ← @PreAuthorize(hasRole(...))
│  (bounded context)      │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  @Service               │  ← validates tenant scope
│  (business logic)       │     via TenantContext.getTenantId()
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  @Repository (JPA)      │  ← queries scoped by barbershop_id
│  PostgreSQL             │
└─────────────────────────┘
             │
             │ (async, non-blocking)
             ▼
┌─────────────────────────┐
│  NotificationService    │  ← writes to DB first, then attempts FCM
│  FcmService (Firebase)  │     failure never blocks main operation
└─────────────────────────┘
```

---

## 10. Microservices & Modular Design

### 10.1 Current state: Modular Monolith (1 deployable unit)

BarberSaaS is intentionally implemented as a modular monolith in MVP. Each bounded context is a self-contained Java package with its own controller, service, DTOs, and no direct imports from other contexts except through domain entities and shared security utilities.

**Why not microservices now?**
- Team size: 1 developer — operational overhead of distributed systems (service discovery, tracing, versioned contracts, network failures) would consume all available capacity
- Current load: tens of barbershops — no scaling need exists
- FLP impossibility and distributed consistency trade-offs are not worth paying for at this scale
- A well-structured monolith extracts into services faster than a poorly structured one

### 10.2 Current service: `barbersaas-backend`

| Property | Value |
|---|---|
| Type | Modular Monolith |
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Deployment | Single JAR on Railway |
| Database | PostgreSQL 16 (shared schema, tenant-isolated by `barbershop_id`) |
| Port | 8080 |
| API prefix | `/api/**` |

**Internal modules (proto-microservices):**

| Module | Package | Endpoints prefix | Bounded context |
|---|---|---|---|
| Auth | `com.barbersaas.auth` | `/api/auth/**` | Identity & Auth |
| Barbershop | `com.barbersaas.barbershop` | `/api/admin/barbershop` · `/api/super-admin/barbershops` | Barbershop Management |
| Employee | `com.barbersaas.employee` | `/api/admin/employees` | Barbershop Management |
| Appointment | `com.barbersaas.appointment` | `/api/client/appointments` · `/api/barber/appointments` · `/api/admin/appointments` | Appointment |
| Schedule | `com.barbersaas.schedule` | `/api/admin/schedules` | Schedule |
| Loyalty | `com.barbersaas.loyalty` | `/api/client/loyalty` · `/api/admin/loyalty` | Loyalty & Rewards |
| Finance | `com.barbersaas.finance` | `/api/admin/finance` | Finance & Inventory |
| Inventory | `com.barbersaas.inventory` | `/api/admin/inventory` | Finance & Inventory |
| Notification | `com.barbersaas.notification` | `/api/notifications` | Notifications |
| Plan | `com.barbersaas.plan` | `/api/super-admin/plans` · `/api/public/plans` | Platform Administration |
| Dashboard | `com.barbersaas.dashboard` | `/api/admin/dashboard` · `/api/super-admin/dashboard` | Supporting |

### 10.3 Future services — extraction roadmap

Extraction is **trigger-based**, not schedule-based. No service is extracted until the trigger condition is measured in production.

| Service | Extract from | Trigger condition | Estimated phase |
|---|---|---|---|
| **notification-service** | `com.barbersaas.notification` | FCM/email calls add >200ms to p95 latency of appointment creation OR >5,000 active barbershops | Phase 2 |
| **appointment-service** | `com.barbersaas.appointment` | CPU >70% sustained during peak hours in production OR concurrent booking failures under load | Phase 3 |
| **auth-service** | `com.barbersaas.auth` | A second client application (web app, external API) needs to authenticate against the same identity store | Phase 3 |
| **loyalty-service** | `com.barbersaas.loyalty` | Loyalty program sold as standalone product to businesses outside the barbershop sector | Phase 4 |
| **analytics-service** | `com.barbersaas.dashboard` | Advanced reporting, PDF exports, data warehouse needs — when dashboards require queries >2s | Phase 4 |

### 10.4 Inter-module communication (current)

All inter-module communication happens **in-process** (direct Java method calls). There is no network hop between modules. When a service is extracted, the in-process call becomes an HTTP/REST call or an async event (Kafka/RabbitMQ).

| From module | To module | Communication | Future pattern when extracted |
|---|---|---|---|
| Appointment | Notification | `notificationService.notify(...)` — in-process | HTTP POST or event publish |
| Appointment | Loyalty | `rewardCouponRepository` check in `create()` | HTTP GET or event subscribe |
| Loyalty | Notification | `notificationService.notify(...)` — in-process | HTTP POST or event publish |
| Auth | All modules | `TenantContext` ThreadLocal | JWT propagation via HTTP header |

---

## 11. Data Model & Database

### 11.1 Database selection: PostgreSQL

PostgreSQL was selected over MySQL for the following reasons:

| Factor | PostgreSQL | MySQL |
|---|---|---|
| JSONB support | Native JSONB with indexing | JSON stored as text, limited indexing |
| Concurrent writes | MVCC — no read/write lock contention | Can exhibit table-level locks |
| Full-text search | Built-in `tsvector`/`tsquery` | Requires MyISAM or external tooling |
| Standards compliance | Closest to SQL standard | Some deviations |
| Cloud ecosystem | First-class on Railway, Render, Supabase, AWS RDS | Supported but PostgreSQL preferred |
| Future analytics | Compatible with Timescale, Citus, read replicas | Limited analytical ecosystem |

> **Migration note:** Development environment currently uses MySQL. Migration to PostgreSQL is planned before the first production deployment. The JPA/Hibernate abstraction layer (dialect swap + schema re-generation) minimizes required code changes.

### 11.2 Multi-tenancy enforcement

Every table that stores barbershop-scoped data includes a `barbershop_id` column (FK to `barbershops.id`). The `TenantContext` ThreadLocal is validated in every service method before any data access. No cross-tenant data leak is possible through the API layer.

### 11.3 Core schema (abbreviated)

```sql
-- Identity
users                   (id, full_name, email, password_hash, phone, role, barbershop_id FK, is_active)
password_reset_tokens   (id, user_id FK, token VARCHAR(6), expires_at, used)

-- Barbershop
barbershops             (id, name, address, city, lat, lng, phone, status, plan_id FK,
                         timezone, cancellation_policy_hours, trial_ends_at, created_at)
subscription_plans      (id, name, price DECIMAL, max_barbers, features_json, is_active)

-- Staff & Schedule
barber_profiles         (id, user_id FK UNIQUE, bio, photo_url)
barber_services         (id, barbershop_id FK, name, price, duration_minutes, is_active)
barber_schedules        (id, barber_profile_id FK, day_of_week, start_time, end_time)
schedule_exceptions     (id, barber_profile_id FK, exception_date, is_day_off, start_time, end_time)

-- Appointment
appointments            (id, barbershop_id FK, client_id FK, barber_id FK, service_id FK,
                         appointment_date, start_time, end_time, status ENUM,
                         price_at_booking DECIMAL, notes, cancelled_reason, created_at)

-- Loyalty
loyalty_cards           (id, client_id FK, barbershop_id FK, stickers_count, total_rewards_redeemed)
loyalty_rewards_config  (id, barbershop_id FK, stickers_required, reward_description, is_active)
loyalty_transactions    (id, loyalty_card_id FK, type ENUM, granted_by_id FK, appointment_id FK)
reward_coupons          (id, client_id FK, barbershop_id FK, status ENUM('ACTIVE','USED'),
                         appointment_id FK nullable, created_at, used_at)

-- Finance & Inventory
finance_records         (id, barbershop_id FK, type ENUM('INCOME','EXPENSE'), amount, description, date)
inventory_products      (id, barbershop_id FK, name, stock, min_stock_alert, unit)
inventory_movements     (id, product_id FK, type, quantity, notes, created_at)

-- Notifications
notifications           (id, user_id FK, title, body, type ENUM, is_read, created_at)
device_tokens           (id, user_id FK, token, platform ENUM('android','ios'), created_at)

-- Supporting
reviews                 (id, barbershop_id FK, client_id FK, barber_id FK, rating, comment, created_at)
gallery_images          (id, barbershop_id FK, image_url, caption, created_at)
client_favorites        (id, client_id FK, barbershop_id FK, created_at)
```

---

## 12. API Design Principles

### 12.1 REST conventions

- Resources as nouns, HTTP verbs for actions (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`)
- Versioning: currently unversioned (MVP); `/api/v1/` prefix to be added before public API release
- All responses in JSON; all timestamps in ISO-8601 format (`America/Bogota`)

### 12.2 Route naming convention

| Prefix | Accessible by | Example |
|---|---|---|
| `/api/public/**` | No authentication required | `GET /api/public/barbershops`, `POST /api/auth/register` |
| `/api/auth/**` | No authentication required | `POST /api/auth/login`, `POST /api/auth/forgot-password` |
| `/api/client/**` | `CLIENT` role only | `GET /api/client/appointments` |
| `/api/barber/**` | `BARBER` (and `ADMIN_BARBERSHOP` where shared) | `GET /api/barber/appointments` |
| `/api/admin/**` | `ADMIN_BARBERSHOP` role only | `GET /api/admin/dashboard` |
| `/api/super-admin/**` | `SUPER_ADMIN` role only | `GET /api/super-admin/barbershops` |

### 12.3 Error format

```json
{
  "error": "Human-readable message in Spanish",
  "fields": {
    "fieldName": "Validation error message"
  }
}
```

### 12.4 API documentation

Swagger UI available at `/swagger-ui.html` (dev profile only). Every endpoint annotated with `@Operation` and `@Tag`. OpenAPI spec auto-generated at `/api-docs`.

---

## 13. Security & Compliance

### 13.1 Security controls implemented

| Control | Implementation |
|---|---|
| Authentication | JWT (HS512), 24h expiry |
| Authorization | Spring Security `@PreAuthorize` per endpoint |
| Tenant isolation | `barbershop_id` validated in every service method |
| Password storage | BCrypt strength 10 |
| Transport | HTTPS (Railway auto-TLS) |
| Secrets | Environment variables only — never in source code |
| Firebase credentials | Excluded from VCS via `.gitignore` |
| Password reset | 6-digit code, 15-min expiry, single use, invalidates previous tokens |

### 13.2 Pre-production security checklist

- [ ] Move `JWT_SECRET` to Railway environment secrets
- [ ] Add rate limiting on `/api/auth/**` (prevent brute-force)
- [ ] Implement token revocation list (Redis) for logout
- [ ] Add audit logging for: account suspension, plan changes, loyalty redemptions
- [ ] Penetration test on tenant isolation before opening to public

### 13.3 Data privacy

- Client personal data (name, email, phone) is scoped exclusively to the barbershop's tenant
- No cross-tenant data access is possible through any API endpoint
- Firebase service account credentials are never committed to any repository

---

## 14. Monetization Model

### 14.1 Subscription plans

| Plan | Price (COP/month) | Max barbers | Target customer |
|---|---|---|---|
| Starter | $39,900 | 2 | Solo barber or micro-shop |
| Profesional | $79,900 | 5 | Standard 2–5 barber shop |
| Premium | $149,900 | Unlimited | Multi-barber shops and chains |

### 14.2 Free trial

- All new barbershops start on a **60-day free trial** (`BarbershopStatus.TRIAL`)
- `trial_ends_at = created_at + 60 days` — set automatically at registration
- After trial: Super Admin manually transitions to `ACTIVE` upon payment confirmation
- Non-converting accounts are transitioned to `SUSPENDED`
- MVP billing method: bank transfer / Nequi — manual confirmation by Super Admin

### 14.3 Future monetization

| Feature | Target phase |
|---|---|
| Automated billing via PayU / Stripe / PSE | Phase 3 |
| Annual plan (2 months free) | Phase 3 |
| Add-on: advanced analytics + PDF exports | Phase 4 |
| Add-on: multi-location management | Phase 4 |
| White-label platform for other service industries | Phase 5 |

---

## 15. Release Roadmap

### Phase 1 — Private Beta (Current — August 2026)

| Item | Status |
|---|---|
| Backend: 8 phases (auth, barbershop, employees, services, schedule, appointments, loyalty, finance) | ✅ Complete |
| Mobile: 4 roles fully functional end-to-end | ✅ Complete |
| Self-registration wizard (3 steps + 60-day trial) | ✅ Complete |
| FCM push notifications (development build) | ✅ Complete |
| Password recovery via email (Gmail SMTP) | ✅ Complete |
| Loyalty card + automatic reward coupon | ✅ Complete |
| Welcome / onboarding screens | ✅ Complete |
| Walk-in client tracking | 🔄 In progress |
| Trial expiration automation | 🔄 In progress |
| Terms & Conditions screen | 🔄 In progress |

### Phase 2 — Controlled Launch (Q4 2026)

- [ ] MySQL → PostgreSQL migration
- [ ] Backend deployment on Railway (production environment)
- [ ] Production EAS Build (Google Play Store + Apple App Store)
- [ ] Custom barbershop icon and splash screen
- [ ] Terms & Conditions and Privacy Policy (legal)
- [ ] Trial-to-paid conversion flow (notification + Super Admin action)
- [ ] Notification service extraction (if trigger condition met)

### Phase 3 — Growth (Q1–Q2 2027)

- [ ] Client web app (QR-based booking without app install)
- [ ] Automated billing (PayU / Stripe / PSE)
- [ ] Advanced reporting and PDF export
- [ ] Appointment service extraction (if trigger condition met)
- [ ] Auth service extraction (if second client app deployed)

### Phase 4 — Scale (Q3–Q4 2027)

- [ ] Multi-location chain management
- [ ] Client discovery marketplace
- [ ] Google/Apple Calendar integration
- [ ] Analytics service (data warehouse)

---

## 16. Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Low adoption — unfamiliarity with digital tools | High | High | < 5-minute onboarding; in-person setup support for first barbershops in Neiva |
| Double-booking under high concurrency | Low | High | Pessimistic WRITE lock at DB level — tested and guaranteed |
| FCM delivery failures (poor network) | Medium | Medium | All notifications persisted in DB before FCM attempt; in-app inbox always available |
| Gmail SMTP rate limit exceeded | Low | Medium | Monitor volume; migrate to Resend/SES before reaching 500 emails/day |
| Trial abuse (re-registering to reset trial) | Medium | Medium | Email uniqueness constraint; Super Admin review for suspicious patterns |
| MySQL → PostgreSQL migration issues | Low | Medium | JPA abstraction minimizes changes; schema tested in PostgreSQL env before migration |
| App Store rejection | Low | Medium | Privacy Policy, permissions justification, and demo account ready for review |
| Single developer bottleneck | High | High | Thorough documentation (PRD, ADR, domain map); modular architecture enables onboarding a second developer within 1 sprint |
| Railway outage | Low | High | Daily backups; RTO < 1 hour; monitor via Railway health checks |
| JWT secret leaked | Very Low | Critical | Secret in environment variable; rotate immediately if compromised; add revocation list |
| Premature microservice extraction | Medium | Medium | Extraction triggers documented and measurable — no extraction without production data |

---

## 17. Open Questions

| # | Question | Owner | Target resolution |
|---|---|---|---|
| 1 | What is the exact pricing strategy for each Colombian city? | Product | Before Phase 2 launch |
| 2 | Should barbers receive a personal revenue breakdown within the app? | Product | Phase 2 |
| 3 | Should the client web app support full booking or only registration + redirect to the app? | Engineering | Phase 3 scoping |
| 4 | How will the Super Admin be proactively notified when a trial is about to expire (e.g., 7 days before)? | Engineering | Phase 2 |
| 5 | Is there a legal requirement to store Colombian user data within Colombian territory? | Legal | Before production |
| 6 | Will the platform support iOS at Phase 2 launch, or Android only initially? | Product | Phase 2 scoping |
| 7 | When should the notification service be extracted — what monitoring will be used to measure the trigger? | Engineering | Phase 2 planning |
| 8 | Will walk-in client records require any personal data, or just a service record linked to the barber? | Product | Sprint 1 — In progress |

---

## 18. Appendix

### A. Glossary

| Term | Definition |
|---|---|
| **Tenant** | A barbershop registered on the platform. All their data is scoped by `barbershop_id` |
| **TenantContext** | A ThreadLocal-based Java class that stores `userId`, `barbershopId`, and `role` for the duration of a single HTTP request. Cleared in `finally` block of `JwtAuthenticationFilter` |
| **Walk-in client** | A client who arrives without prior booking. Tracked as an appointment record without requiring client app registration |
| **Sticker** | A loyalty point granted by a barber or admin after a completed service |
| **RewardCoupon** | An automatically generated coupon (status: `ACTIVE`) created on loyalty redemption. Applied as 100% discount on next booking |
| **Trial** | A 60-day free period for new barbershops. Status: `TRIAL`. Tracked via `trial_ends_at` column |
| **WalkIn** | An appointment created by the admin for a client who arrived without prior booking |
| **Pessimistic lock** | A `PESSIMISTIC_WRITE` database lock acquired on active appointments for a barber/date during appointment creation, preventing concurrent double-bookings |
| **EAS Build** | Expo Application Services Build — cloud-based compilation of the React Native app into a native APK (Android) or IPA (iOS) |
| **PRD** | Product Requirements Document — this document |
| **ADR** | Architecture Decision Record — documents a specific architectural decision, its alternatives, and consequences |
| **Modular Monolith** | A single deployable application internally organized by bounded contexts, designed for future service extraction |
| **Bounded Context** | A DDD concept — a boundary within which a domain model has a consistent meaning and its own ubiquitous language |

### B. Related Documents

| Document | Location |
|---|---|
| Architecture Decision Record | `05-architecture/decisions/ADR-001-modular-monolith.md` |
| Domain Map (Bounded Contexts) | `02-domain/domain-map.md` |
| Service Catalog | `09-microservices/service-catalog.md` |
| Definition of Done | `00-governance/definition-of-done.md` |
| Definition of Ready | `00-governance/definition-of-ready.md` |
| Git Conventions | `00-governance/git-conventions.md` |
| Agile Conventions | `00-governance/agile-conventions.md` |
| API Contracts | `07-api/contracts/` |
| Data Model | `06-data/data-model.md` |
| Risk Register | `15-project-control/risks.md` |
| Database Init Script | `barbersaas-backend/db/init.sql` |
| Backend Configuration | `barbersaas-backend/src/main/resources/application.yml` |
| Mobile Build Configuration | `barbersaas-mobile/app.json` |

### C. Contact & Project References

| Field | Value |
|---|---|
| Team members | Carlos Mauricio Leal Medina, Daniel Felipe Cerquera Idrobo, Juan Pablo Borrero Morales, Carolay Arraut Heredia |
| Transactional email | sasbarberias@gmail.com |
| Expo project | carlosleal / barbersaas-mobile |
| Firebase project | barbersaas (Spark plan) |
| Deployment target | Railway (barbersaas.up.railway.app) |
| Package name (Android) | com.barbersaas.mobile |
| Bundle identifier (iOS) | com.barbersaas.mobile |
