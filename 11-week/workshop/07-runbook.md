# CP8 - Runbook: `AppointmentBookingFastBurn`

**Alert:** `AppointmentBookingFastBurn` ([`06-alerts.yml`](06-alerts.yml)), `severity="page"`,
`service="appointment-api"`, `slo="SLO-A"`. **Fires when** more than 7.2 % of
`POST /api/v1/appointments` failed over the last 1 h and the last 5 min (burn rate 14.4: the
30-day budget of 210 failed bookings is gone in 50 h). **First responder:** owner of
`barber-saas-appointment-api`. Commands run from `barber-saas-infra-postgres`, as in
`09-microservices/services/appointment/runbook.md` of `barber-saas-docs`.

## 1. Symptoms

- Clients tapping "book" get an error screen; the API answers `503 SERVICE_UNAVAILABLE` (a
  dependency did not answer within 3 s and its retry) or `500 INTERNAL_ERROR`.
- `appointment_book_requests_total{outcome="failed"}` grows; bookings that do succeed are slow,
  stuck in the `3` and `6` s buckets of `appointment_book_duration_seconds`.
- No new `appointment` rows and no new `AppointmentCreated` rows in `outbox_event`, so the barber's
  agenda stops filling while the `GET` screens keep working.

## 2. Confirm

**2.1 The burn is real** (Prometheus, `observability` profile):

```promql
appointment_book:error_ratio:rate5m                     # > 0.072 confirms the page
sum(increase(appointment_book_requests_total{outcome="failed"}[1h]))   # failed bookings in the hour
```

**2.2 Which dependency** (step 3 to 6 of [`01-journey.md`](01-journey.md)):

```promql
sum by (downstream, outcome) (rate(appointment_downstream_duration_seconds_count[5m]))
histogram_quantile(0.95, sum by (le, downstream) (rate(appointment_downstream_duration_seconds_bucket[5m])))
```

A `downstream` with `outcome="timeout"` or a p95 near 3 s is the cause; `schedule-api` is the
usual one, since it also calls `GET /internal/v1/busy-slots` back on `appointment-api`.

**2.3 One failed booking end to end** (log contract of [`02-log-contract.md`](02-log-contract.md)).
The client's error body carries `traceId` (the `correlation_id`); without it, take the latest
failure:

```bash
docker compose --env-file env/dev.env logs --no-log-prefix --since 15m appointment-api \
  | grep '"outcome":"failed"' | tail -3                  # read its "trace_id"
docker compose --env-file env/dev.env logs --no-log-prefix --since 15m \
  appointment-api schedule-api barbershop-api loyalty-api \
  | grep '"trace_id":"4bf92f3577b34da6a3ce929d0e0e4736"' # every line of that booking, all services
```

Read the lines in `timestamp` order: two `downstream.call.timeout` with the same `downstream` and
`duration_ms` 3000, then `error_code` `SERVICE_UNAVAILABLE`, prove the dependency. A `500` with
`INTERNAL_ERROR` and no `downstream.*` line points at `appointment-api` itself (last deploy, or
the `appointment` schema).

**2.4 Health of the containers:**

```bash
docker compose --env-file env/dev.env ps appointment-api schedule-api barbershop-api loyalty-api
```

## 3. Mitigate (lowest risk first)

| # | Mitigation | When | Command | Risk |
|---|---|---|---|---|
| 1 | **Scale out** the slow dependency | A dependency is `healthy` but slow (saturation: `schedule_busy_slots_inflight_requests` or `hikaricp_connections_pending` high) | `docker compose --env-file env/dev.env up -d --no-recreate --scale schedule-api=2 schedule-api` | Low: no code change. The services are stateless and use `expose`, not fixed ports or `container_name`, so Docker DNS spreads `http://schedule-api:8080` over both. Each copy opens up to `DB_POOL_MAX` = 10 connections to the single PostgreSQL. Never tried under load. |
| 2 | **Restart** the failing container | A dependency is `unhealthy` or stopped | `docker compose --env-file env/dev.env restart schedule-api` | Low to medium: in-flight requests to it fail during about 30 s of JVM start-up. |
| 3 | **Roll back** the last deploy | Failures started right after a deploy of `appointment-api` or a dependency | In that repository: `git checkout v2.0.0`, then `docker compose --env-file env/dev.env up -d --build --wait schedule-api`; the lasting fix is a revert pull request on `develop` | Medium: images are built from source (`build:`) and no registry keeps the previous image. A change to `main` needs the teacher's approval (`CODEOWNERS`, `@ariel5253`). Never roll back an applied Liquibase changeset by editing it. |

**Not available today:**

- **Feature flags:** no flag system exists (`05-architecture/README.md` still lists them as
  "Fill in"), so a new behaviour of `ManageAppointments.book` cannot be switched off without a
  deploy.
- **Circuit breaker:** none in any service (`pattern-guide.md`: "not required at current
  scale"). The 3 s timeout with one retry is the only protection, so every booking waits up to
  6 s on a dead `schedule-api` before answering `503`.

## 4. Escalate

Roles from `09-microservices/service-catalog.md` and `00-governance/agile-conventions.md`.

| After | Who | Role | Why |
|---|---|---|---|
| 0 min | Juan Pablo Borrero | Owner of `appointment-api` and `loyalty-api` | First responder: confirms (section 2) and applies mitigation 1 or 2 |
| 15 min without improvement | Carlos Leal | Tech Lead, owner of `schedule-api` and `barbershop-api` | Owns the usual slow dependency; decides a rollback (mitigation 3) |
| 15 min, if a container or the database is the cause | Daniel Cerquera | Owner of `infra-postgres`, `api-gateway` and `worker` | PostgreSQL connections, the gateway, the `platform` network, outbox relay |
| A rollback to `main` | Teacher (`@ariel5253`) | Approver of `main` in every repository | `CODEOWNERS` blocks any merge to `main` without approval |

## 5. Verify recovery

1. `appointment_book:error_ratio:rate5m` drops below 0.072 and the alert resolves about 5 min
   later (its short window); then the ratio stays below 0.005, the SLO-A target.
2. `histogram_quantile(0.95, sum by (le) (rate(appointment_book_duration_seconds_bucket[5m])))`
   is back under 0.8 s (SLO-L).
3. A test booking answers `201` and its `trace_id` shows `downstream.call.succeeded` for
   `schedule-api` with `duration_ms` in the tens, then `appointment.book.created`.
4. `max(outbox_oldest_unpublished_seconds{producer="appointment-api"})` < 60: the new
   `AppointmentCreated` events reach `loyalty-api` (SLO-F).
5. Budget left, for the issue:
   `1 - sum(increase(appointment_book_requests_total{outcome="failed"}[30d])) / (0.005 * sum(increase(appointment_book_requests_total[30d])))`.
   If the incident burned more than 20 % (42 failed bookings), a postmortem is written
   (policy of [`05-slo.md`](05-slo.md)).
6. Scale back to one copy once the cause is fixed:
   `docker compose --env-file env/dev.env up -d --scale schedule-api=1 schedule-api`.
