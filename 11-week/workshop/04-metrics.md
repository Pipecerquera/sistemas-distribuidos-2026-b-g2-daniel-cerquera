# CP4 - Golden Signals and Metric Catalog

One table per service of the journey in [`01-journey.md`](01-journey.md). Names are `snake_case`
with a unit suffix; counters end in `_total`. The two metrics of `appointment-api`'s booking
(`appointment_book_requests_total`, `appointment_book_duration_seconds`) are the ones exposed by
[`03-instrumentation/`](03-instrumentation/) on `GET /metrics`; the rest follow the same rules for
the other services. Prometheus and Grafana run in `barber-saas-infra-postgres` under the
`observability` profile.

## 1. `appointment-api` (central service)

| Signal | Metric | Type | Labels | Dashboard PromQL |
|---|---|---|---|---|
| Latency | `appointment_book_duration_seconds` | histogram | - | `histogram_quantile(0.95, sum by (le) (rate(appointment_book_duration_seconds_bucket[5m])))` |
| Traffic | `appointment_book_requests_total` | counter | `outcome` | `sum(rate(appointment_book_requests_total[5m]))` |
| Errors | `appointment_book_requests_total` | counter | `outcome="failed"` | `sum(rate(appointment_book_requests_total{outcome="failed"}[5m])) / sum(rate(appointment_book_requests_total[5m]))` |
| Errors (rule) | `appointment_book_requests_total` | counter | `outcome="rejected"` | `sum(rate(appointment_book_requests_total{outcome="rejected"}[5m]))` - double booking, `INV-APPT-001` |
| Latency (deps) | `appointment_downstream_duration_seconds` | histogram | `downstream`, `outcome` | `histogram_quantile(0.95, sum by (le, downstream) (rate(appointment_downstream_duration_seconds_bucket[5m])))` |
| Saturation | `appointment_outbox_pending_events` | gauge | - | `max(appointment_outbox_pending_events)` - rows of `outbox_event` with `published_at IS NULL` |
| Saturation | `hikaricp_connections_pending` | gauge | `pool` | `max(hikaricp_connections_pending{pool="appointment"})` |

## 2. `schedule-api` (the slow dependency of CP9)

| Signal | Metric | Type | Labels | Dashboard PromQL |
|---|---|---|---|---|
| Latency | `schedule_availability_duration_seconds` | histogram | - | `histogram_quantile(0.95, sum by (le) (rate(schedule_availability_duration_seconds_bucket[5m])))` |
| Traffic | `schedule_availability_requests_total` | counter | `outcome` | `sum(rate(schedule_availability_requests_total[5m]))` |
| Errors | `schedule_availability_requests_total` | counter | `outcome="failed"` | `sum(rate(schedule_availability_requests_total{outcome="failed"}[5m])) / sum(rate(schedule_availability_requests_total[5m]))` |
| Saturation | `schedule_busy_slots_inflight_requests` | gauge | - | `max(schedule_busy_slots_inflight_requests)` - calls waiting on `GET /internal/v1/busy-slots` |

## 3. `barbershop-api`

| Signal | Metric | Type | Labels | Dashboard PromQL |
|---|---|---|---|---|
| Latency | `barbershop_http_request_duration_seconds` | histogram | `route`, `method` | `histogram_quantile(0.95, sum by (le, route) (rate(barbershop_http_request_duration_seconds_bucket{route="/api/v1/services/{id}"}[5m])))` |
| Traffic | `barbershop_http_requests_total` | counter | `route`, `method`, `status_class` | `sum by (route) (rate(barbershop_http_requests_total[5m]))` |
| Errors | `barbershop_http_requests_total` | counter | `status_class="5xx"` | `sum(rate(barbershop_http_requests_total{status_class="5xx"}[5m])) / sum(rate(barbershop_http_requests_total[5m]))` |
| Saturation | `hikaricp_connections_pending` | gauge | `pool` | `max(hikaricp_connections_pending{pool="barbershop"})` |

## 4. `loyalty-api`

| Signal | Metric | Type | Labels | Dashboard PromQL |
|---|---|---|---|---|
| Latency | `loyalty_coupon_lookup_duration_seconds` | histogram | - | `histogram_quantile(0.95, sum by (le) (rate(loyalty_coupon_lookup_duration_seconds_bucket[5m])))` |
| Traffic | `loyalty_events_received_total` | counter | `event_type`, `result` | `sum by (event_type) (rate(loyalty_events_received_total[5m]))` - `AppointmentCreated` from the worker |
| Errors | `loyalty_events_received_total` | counter | `result="failed"` | `sum(rate(loyalty_events_received_total{result="failed"}[5m]))` |
| Saturation | `hikaricp_connections_pending` | gauge | `pool` | `max(hikaricp_connections_pending{pool="loyalty"})` |

`result` takes the answers of `POST /internal/v1/events`: `processed`, `duplicate`, `ignored`, `failed`.

## 5. `barber-saas-worker` (outbox relay)

| Signal | Metric | Type | Labels | Dashboard PromQL |
|---|---|---|---|---|
| Latency | `worker_event_delivery_duration_seconds` | histogram | `consumer` | `histogram_quantile(0.95, sum by (le, consumer) (rate(worker_event_delivery_duration_seconds_bucket[5m])))` |
| Traffic | `worker_events_delivered_total` | counter | `producer`, `event_type`, `result` | `sum by (event_type) (rate(worker_events_delivered_total[5m]))` |
| Errors | `worker_events_delivered_total` | counter | `result="failed"` | `sum(rate(worker_events_delivered_total{result="failed"}[5m]))` - rows ending with `failed_at` |
| Saturation | `outbox_oldest_unpublished_seconds` | gauge | `producer` | `max by (producer) (outbox_oldest_unpublished_seconds)` - SLO-3 freshness (< 60 s) |

## 6. Label cardinality

| Label | Values | Cardinality |
|---|---|---|
| `outcome` | `created`, `rejected`, `failed` (booking); `ok`, `timeout`, `failed` (downstream) | 3 |
| `downstream` | `barbershop-api`, `schedule-api`, `loyalty-api` | 3 |
| `route` | Route template, never the raw path: `/api/v1/services/{id}`, `/api/v1/barbershops/me`, ... | ~15 per service |
| `method` | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` | 5 |
| `status_class` | `2xx`, `4xx`, `5xx` | 3 |
| `event_type` | The 9 events of `09-microservices/event-catalog.md` | 9 |
| `producer` / `consumer` | `appointment-api`, `loyalty-api`, `identity-auth-api` / `loyalty-api`, `notifications-api` | 3 / 2 |
| `result` | `processed`, `duplicate`, `ignored`, `failed` | 4 |
| `pool` | One per schema | 1 per service |

Worst series count: `barbershop_http_requests_total` = 15 x 5 x 3 = 225 per instance, far below
the thousands a single Prometheus handles per metric.

**Forbidden labels and their replacement:**

| Unbounded label | Why | Replaced by |
|---|---|---|
| `user_id`, `client_id`, `barber_id` | One series per user or barber: grows with every sign-up | Logs (`client_ref`, `barber_id`) and traces, found by `trace_id` |
| `barbershop_id` | Grows with every tenant | Logs and traces; per-tenant reports come from the database, not from metrics |
| `appointment_id`, `coupon_id`, envelope `id` | One series per row | `trace_id` in logs |
| Raw URL (`/api/v1/services/e2f4...`) | One series per UUID in the path | `route` template |
| `email`, `correlation_id` | Unbounded and, for e-mail, personal data | Never in metrics; `correlation_id` only in logs |

## 7. Histogram buckets

`appointment_book_duration_seconds` uses
`0.05, 0.1, 0.2, 0.3, 0.5, 0.8, 1, 2, 3, 6` seconds (`BookingMetrics.java`).

- **0.8 s** is the SLO-2 target for `POST /api/v1/appointments` (`observability-plan.md`): a client
  waiting on the booking screen after picking a slot accepts under one second. A bucket exactly at
  the target makes "share of bookings under 800 ms" an exact count, not an interpolation.
- **0.05 to 0.5 s** split the normal path: four internal calls (barbershop twice, loyalty,
  schedule) of tens of milliseconds each, plus one transaction.
- **0.3 s** matches the SLO-2 target of the `GET` reads, so the same buckets serve the reads of
  `barbershop-api` and `schedule-api`.
- **3 s** is the request timeout to each dependency (`communication-patterns.md` §3) and **6 s**
  is the timeout plus its single retry: a request in those buckets is waiting on a slow
  `schedule-api`, the failure CP9 injects.
- Ten buckets keep each histogram at 12 series (10 + `+Inf` + count/sum per instance).
