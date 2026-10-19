# CP7 - Alert Policy for SLO-A

[`06-alerts.yml`](06-alerts.yml) pages on the availability SLO of `POST /api/v1/appointments`
([`05-slo.md`](05-slo.md), SLO-A: 99.5 % over 30 days). Both alerts read only
`appointment_book_requests_total`, exposed by `appointment-api` on `GET /metrics`
([`03-instrumentation/`](03-instrumentation/)). `promtool check rules 06-alerts.yml` answers
`SUCCESS: 6 rules found` (Prometheus 2.53.2).

## 1. Inputs from CP6

| Value | Number |
|---|---|
| Error budget share | 1 - 0.995 = 0.005 |
| Budget in requests | **210** failed bookings in 30 days |
| Budget in minutes | **216** min |
| Window | 30 d = 720 h |
| Traffic in opening hours | 1,400 / 12 h = **116.7** requests/h (1.94/min) |

Burn rate = observed failed share / 0.005. A burn rate of 1 spends the budget in exactly 30 days.

## 2. Budget consumed before each alert fires

Share of budget consumed = burn rate x long window / 720 h.

| Alert | Severity | Burn rate | Windows | Failed share threshold | Budget consumed | In requests | In minutes | Budget gone in |
|---|---|---|---|---|---|---|---|---|
| `AppointmentBookingFastBurn` | `page` | 14.4 | 1h, confirmed 5m, `for: 2m` | 14.4 x 0.005 = **7.2 %** | 14.4 x 1 / 720 = **2 %** | 0.02 x 210 = **4.2** | 0.02 x 216 = **4.3 min** | 720 / 14.4 = **50 h** |
| `AppointmentBookingSlowBurn` | `ticket` | 6 | 6h, confirmed 30m, `for: 15m` | 6 x 0.005 = **3 %** | 6 x 6 / 720 = **5 %** | 0.05 x 210 = **10.5** | 0.05 x 216 = **10.8 min** | 720 / 6 = **120 h** (5 days) |

The 2 % and 5 % assume traffic spread evenly over 24 hours. Bookings only arrive during the
12 opening hours, so in a real opening hour the same thresholds cost twice as much:

```
fast: 116.7 requests x 0.072 = 8.4 failed in 1 h   -> 8.4 / 210  = 4 % of the budget
slow: 700 requests (6 h) x 0.03 = 21 failed in 6 h -> 21 / 210   = 10 % of the budget
```

Both are still far from the 20 % (42 failed bookings) that forces a postmortem in the policy of
CP6, so the team is warned before the incident becomes one.

## 3. Why two windows per alert

- **Long window (1h, 6h)** decides that the budget is really burning. At 116.7 requests/h the
  fast alert needs at least 8.4 failed bookings in the hour, so one `503 SERVICE_UNAVAILABLE`
  never pages anybody.
- **Short window (5m, 30m)** confirms it is still happening. Five minutes hold only
  1.94 x 5 = 9.7 bookings, so on its own one failure (1 / 9.7 = 10.3 %) would cross 7.2 %;
  joined with `and` to the 1h window it only makes the alert stop about 5 minutes after the fix
  instead of staying red for the rest of the hour.
- At night no booking arrives, the ratio is `0 / 0` (no sample) and neither alert fires: a
  closed barbershop is not an incident.

## 4. Routing

| Severity | Who | How |
|---|---|---|
| `page` | Owner of `barber-saas-appointment-api`, then the Tech Lead | Immediate notification during opening hours; open the runbook [`07-runbook.md`](07-runbook.md) |
| `ticket` | Owner of `barber-saas-appointment-api` | GitHub issue labelled `reliability`, handled in working hours |

Labels on both alerts: `team="barbersaas"`, `service="appointment-api"`, `slo="SLO-A"`.

## 5. Cause-based alerts the team will not create

| Rejected alert | Why not |
|---|---|
| CPU of the `appointment-api` container above 80 % for 5 min | The JVM spikes CPU on JIT warm-up after a deploy and during garbage collection while bookings keep answering `201` in time. A client never feels CPU; if it ever slows bookings, SLO-L and SLO-A already show it with the real user impact. |
| Any single `500 INTERNAL_ERROR` from `barbershop-api` on `GET /api/v1/services/{id}` | One failed call is at most one failed booking, 1 / 210 = 0.5 % of the budget; a `500` is not even retried (`communication-patterns.md` §3 retries only `502`/`503`/`504`), so it is the bug of one request, not an outage. Paging on each one would wake the owner for errors that cost nothing and teach the team to ignore pages. |

Causes like these stay on the dashboards of [`04-metrics.md`](04-metrics.md)
(`hikaricp_connections_pending`, `barbershop_http_requests_total{status_class="5xx"}`) for the
diagnosis step of the runbook, never as pages.
