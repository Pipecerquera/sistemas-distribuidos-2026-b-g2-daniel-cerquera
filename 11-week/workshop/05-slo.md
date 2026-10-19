# CP6 - SLOs and Error Budget: Book an Appointment

Three SLOs for the journey of [`01-journey.md`](01-journey.md), computed only from the metrics of
[`04-metrics.md`](04-metrics.md). Window: **30 days, rolling**. The targets are the ones of SLO-1,
SLO-2 and SLO-3 in `11-week/hu-status/observability-plan.md`, narrowed from the whole gateway to
`POST /api/v1/appointments` and its `AppointmentCreated` event.

## 1. Expected traffic

No load figure has been agreed yet (`04-requirements/non-functional.md`, NFR-001, "Minimum
throughput: not yet defined"), so the estimate is built from the product documents of
`barber-saas-docs`:

| Input | Value | Source |
|---|---|---|
| Paying barbershops | 50 | `03-product/vision.md`, KR1 (12 months after launch) |
| Barbers per barbershop | 3 | `03-product/problem-framing.md`: shops with 2-4 barbers |
| Services per barber per day | 12 | Assumption: 12 opening hours, about one 45-minute service plus a gap per hour |
| Share booked through the app | 70 % | North Star Metric, `03-product/problem-framing.md` |
| Rejected attempts | 10 % of attempts | Assumption: slot taken meanwhile (`INV-APPT-001`), outside the booking window |

```
created bookings / day = 50 x 3 x 12 x 0.70           = 1,260
requests / day         = 1,260 / 0.90                 = 1,400   (140 rejected with 422)
requests / 30 days     = 1,400 x 30                   = 42,000
AppointmentCreated / 30 days = 1,260 x 30             = 37,800
average rate (12 opening hours) = 1,400 / 720 min     = 1.94 requests/min
peak (Friday and Saturday afternoon, about 3x)        = ~6 requests/min
```

Window length used for minute budgets: 30 x 24 x 60 = **43,200 minutes**.

## 2. The three SLOs

| SLO | SLI (good events / valid events) | Target | Window |
|---|---|---|---|
| **SLO-A** Availability | Bookings answered `201` or `422` (`outcome` `created` or `rejected`) / all bookings counted by `appointment_book_requests_total` | **99.5 %** | 30 d |
| **SLO-L** Latency | Bookings answered in <= 0.8 s / all bookings | **95 %** | 30 d |
| **SLO-F** Freshness | Minutes in which the oldest unpublished `outbox_event` row of `appointment-api` is younger than 60 s / all minutes | **99 %** | 30 d |

- A `422 BUSINESS_RULE_VIOLATION` is a correct answer (the slot was taken), so it is good for
  SLO-A. `failed` covers `503 SERVICE_UNAVAILABLE` (a dependency such as `schedule-api` did not
  answer) and `500 INTERNAL_ERROR`.
- `400 VALIDATION_ERROR` and `401 UNAUTHORIZED` never reach `BookingController`'s counter, so they
  are not valid events: a malformed request is the client's error, not the service's.
- `appointment_book_duration_seconds` has no `outcome` label, so SLO-L counts failed bookings too.
  A booking that waits 3 s on `schedule-api` and then fails burns SLO-A and SLO-L at once; that is
  intended, the client waited either way.
- SLO-F is time-based: the loyalty coupon of the booking (`reward_coupon` moved to `USED` by
  `loyalty-api`) depends on `barber-saas-worker` delivering `AppointmentCreated`, and that can be
  broken while every `POST /api/v1/appointments` still answers `201`.

### PromQL

SLO-A, availability:

```promql
sum(increase(appointment_book_requests_total{outcome=~"created|rejected"}[30d]))
/
sum(increase(appointment_book_requests_total[30d]))
```

SLO-L, latency (`le="0.8"` is an exact bucket of `BookingMetrics.java`):

```promql
sum(increase(appointment_book_duration_seconds_bucket{le="0.8"}[30d]))
/
sum(increase(appointment_book_duration_seconds_count[30d]))
```

SLO-F, freshness (one sample per minute; the worker reports `0` when no row is pending):

```promql
avg_over_time(
  (max(outbox_oldest_unpublished_seconds{producer="appointment-api"}) < bool 60)[30d:1m]
)
```

## 3. Error budget

| SLO | Budget share | Budget in events | Budget in minutes |
|---|---|---|---|
| SLO-A | 1 - 0.995 = 0.5 % | 42,000 x 0.005 = **210 failed bookings** | 43,200 x 0.005 = **216 min** (3 h 36 min) |
| SLO-L | 1 - 0.95 = 5 % | 42,000 x 0.05 = **2,100 slow bookings** | 43,200 x 0.05 = **2,160 min** (36 h) |
| SLO-F | 1 - 0.99 = 1 % | 37,800 x 0.01 = **~378 late events** | 43,200 x 0.01 = **432 min** (7 h 12 min) |

The minute budget of SLO-A assumes traffic spread over the whole day. Bookings only arrive in the
12 opening hours, so a total outage of `appointment-api` during opening hours spends the 210
requests in 210 / 1.94 = **108 min**, and at the Saturday peak in 210 / 6 = **35 min**. The
alerts of CP7 work on the request ratio, not on wall-clock minutes, for that reason.

NFR-002 asks 99.9 % for the whole platform in production. For this journey 99.9 % would leave
42,000 x 0.001 = 42 failed bookings a month, about one slow deploy of `appointment-api`; 99.5 %
is the target the team can measure and hold with today's single-instance services.

## 4. Error-budget policy

Thresholds apply to the rolling 30-day budget of each SLO, shown here for SLO-A.

| Consumed | SLO-A requests | Actions |
|---|---|---|
| **50 %** | 105 failed | The owner of `barber-saas-appointment-api` opens a GitHub issue labelled `reliability` with the top `error_code` and `downstream` values from the logs. Deploys continue, but each deploy of `appointment-api` or `schedule-api` is watched on the dashboard for 30 min. |
| **75 %** | 158 failed | The next sprint plans at least one reliability story from the cause found. Deploys of the journey's services (`appointment-api`, `schedule-api`, `barbershop-api`, `loyalty-api`) only Monday to Thursday, never before the Friday and Saturday peak, and no new changeset on the `appointment` schema. |
| **100 %** | 210 failed | Feature freeze on the journey: only fixes and reliability work merge to `develop` of those four repositories until SLO-A is back above 99.5 % over the last 7 days. A blameless postmortem is written. The Tech Lead (Carlos Leal) approves any exception. |

Planned maintenance announced in advance does not count against the budget. A single incident that
burns more than 20 % (42 requests) gets a postmortem whatever the total.
