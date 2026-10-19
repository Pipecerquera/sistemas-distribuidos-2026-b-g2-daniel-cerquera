# CP9 - Incident Drill: Slow `schedule-api`

## 1. Scenario

Step 6 of [`01-journey.md`](01-journey.md), `GET /api/v1/availability` on `schedule-api`, answers
in **3 s for 25 % of the calls during 40 minutes**. The drill models a hypothetical `schedule-api`
build `2.0.1` whose read of `schedule_exception` filters by `barbershop_id` only, so it uses
`idx_schedule_exception_barbershop_id` and scans the whole day-off history of the barbershop
instead of the `(barber_profile_id, exception_date)` index of `uq_schedule_exception_barber_date`.
One barbershop in four has a long history, so one call in four is slow. The query still finishes
(schedule's `statement_timeout` is 5 s), but `appointment-api` gives up at its 3 s request
timeout and retries once ([`02-log-contract.md`](02-log-contract.md), line 4 of the example).

Inputs: Saturday 15:00, the peak of [`05-slo.md`](05-slo.md): **6 bookings/min**.

```
bookings in 40 min                  = 6 x 40                    = 240
first call fast                     = 0.75                      -> 180 bookings, ~0.2 s
first call slow, retry fast         = 0.25 x 0.75 = 0.1875      ->  45 bookings, ~3.2 s, answered
both calls slow                     = 0.25 x 0.25 = 0.0625      ->  15 bookings, ~6.1 s, 503
calls to schedule-api               = 240 + 60 retries          = 300, of which 60 + 15 = 75 slow (25 %)
```

## 2. Trace of one failed booking

`trace_id` `7c2a9f1e4b8d4a6c9e3f1b5d7a2c4e60`, both attempts slow. Offsets from the start of `a1`.

- `POST /api/v1/appointments` - server, `appointment-api` (`a1`) - 0 ms, **6,142 ms**, `503`
  - `GET barbershop-api /api/v1/services/{id}` - client (`a2`) - 4 ms, 38 ms
  - `GET barbershop-api /api/v1/barbershops/me` - client (`a3`) - 43 ms, 31 ms
  - `GET loyalty-api /api/v1/loyalty/coupons` - client (`a4`) - 76 ms, 27 ms
  - `GET schedule-api /api/v1/availability` - client (`a5`, attempt 1) - 106 ms, **3,000 ms timeout**
    - `GET /api/v1/availability` - server, `schedule-api` - 109 ms, 3,021 ms (ends after the caller left)
      - `GET appointment-api /internal/v1/busy-slots` - client (`s1`) - 114 ms, 16 ms
      - `SELECT schedule_exception` - DB span, `schedule` schema - 133 ms, **2,989 ms**
  - `GET schedule-api /api/v1/availability` - client (`a5r`, retry) - 3,108 ms, **3,000 ms timeout**
    - `GET /api/v1/availability` - server, `schedule-api` - 3,111 ms, 3,018 ms
      - `GET appointment-api /internal/v1/busy-slots` - client - 3,115 ms, 15 ms
      - `SELECT schedule_exception` - DB span, `schedule` schema - 3,133 ms, **2,994 ms**
  - no `INSERT` span: no `appointment`, `idempotency_key` or `outbox_event` row, so no
    `AppointmentCreated` reaches `barber-saas-worker`

38 + 31 + 27 + 3,000 + 3,000 = 6,096 ms in calls; the other 46 ms are `appointment-api` itself.

## 3. Four log lines of that trace

```json
{"timestamp":"2026-10-24T15:21:07.204Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"7c2a9f1e4b8d4a6c9e3f1b5d7a2c4e60","span_id":"3d9e1a7c5b2f4e80","correlation_id":"c-91b04d2e","event":"appointment.book.received","message":"Booking request received","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","duration_ms":0}
{"timestamp":"2026-10-24T15:21:10.310Z","level":"WARN","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"7c2a9f1e4b8d4a6c9e3f1b5d7a2c4e60","span_id":"8f4c2b6a1e9d3c57","correlation_id":"c-91b04d2e","event":"downstream.call.timeout","message":"Availability request timed out, retrying once","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","downstream":"schedule-api","duration_ms":3000}
{"timestamp":"2026-10-24T15:21:13.312Z","level":"ERROR","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"7c2a9f1e4b8d4a6c9e3f1b5d7a2c4e60","span_id":"a61e5d0b7c3f9248","correlation_id":"c-91b04d2e","event":"downstream.call.timeout","message":"Availability request timed out after the retry","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","downstream":"schedule-api","error_code":"SERVICE_UNAVAILABLE","duration_ms":3000}
{"timestamp":"2026-10-24T15:21:13.346Z","level":"ERROR","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"7c2a9f1e4b8d4a6c9e3f1b5d7a2c4e60","span_id":"3d9e1a7c5b2f4e80","correlation_id":"c-91b04d2e","event":"http.request.completed","message":"POST /api/v1/appointments answered","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","http_status":503,"error_code":"SERVICE_UNAVAILABLE","outcome":"failed","duration_ms":6142}
```

## 4. SLIs during the 40 minutes

| SLI | Value | Target | Burn rate |
|---|---|---|---|
| SLO-A availability | (240 - 15) / 240 = **93.75 %** | 99.5 % | 0.0625 / 0.005 = **12.5** |
| SLO-L latency | (240 - 45 - 15) / 240 = **75 %** under 0.8 s | 95 % | 0.25 / 0.05 = **5** |
| SLO-F freshness | **100 %**: the 15 failed bookings write no `outbox_event` row, the others publish on time | 99 % | 0 |

## 5. Which alert fires first

Neither alert of [`06-alerts.yml`](06-alerts.yml) fires during the 40 minutes.

```
AppointmentBookingFastBurn  needs failed share > 0.072 over 1h AND 5m
  1h window at minute 40    = 15 failed / (6 x 60) = 0.0417   -> false
  even a full hour inside   = 0.0625 (burn 12.5 < 14.4)       -> never fires for this fault
AppointmentBookingSlowBurn  needs failed share > 0.03 over 6h AND 30m
  6h window at minute 40    = 15 / (6 x 360)       = 0.0069   -> false
  6h window at minute t     = 0.0625 x t / 360 > 0.03  -> t > 172.8 min
  plus for: 15m             -> would fire at minute ~188 if the fault kept going
```

The first alert that would fire is `AppointmentBookingSlowBurn`, at **minute ~188**. The burn
rate is a ratio and ignores volume: the 2 % per hour of `14.4 x 1h` assumes the average
1.94 bookings/min, but at the Saturday peak of 6/min the 15 failed bookings spend
15 / 210 = **7.1 % of the budget in 40 minutes**, about 10.7 % per hour, while the ratio stays at
12.5. The incident is found by a person at minute 18 (postmortem, timeline).

## 6. What stops the damage

Runbook [`07-runbook.md`](07-runbook.md): step 2.2 shows `downstream="schedule-api"` with
`outcome="timeout"` near 25 % of calls and a p95 of 3 s; step 2.3 on the `trace_id` above shows
two `downstream.call.timeout` lines. In section 3, mitigation 1 (scale out) does not apply,
`hikaricp_connections_pending{pool="schedule"}` is 0 and a second copy would run the same slow
query; mitigation 2 does not apply, the container is `healthy`. **Mitigation 3, rollback of
`schedule-api` to `v2.0.0`**, matches "failures started right after a deploy" and stops the
damage.

---

# Postmortem: slow availability in `schedule-api` (drill)

**Status:** closed. **Severity:** minor (7.1 % of SLO-A's budget). **Blameless:** the review
looks at the system and its signals, not at who deployed. **Day:** Saturday 2026-10-24,
15:00-15:40, drill environment `dev`.

## Timeline

| Time | Minute | Event |
|---|---|---|
| 15:00 | 0 | `schedule-api` `2.0.1` is deployed; its container turns `healthy` (the check only calls `GET /health`) |
| 15:05 | 5 | `appointment_book:error_ratio:rate5m` around 0.06; no rule fires (section 5) |
| 15:18 | 18 | A barbershop owner writes to the team that clients say the booking screen "hangs": **detection** |
| 15:20 | 20 | The owner of `appointment-api` opens `07-runbook.md`; steps 2.1 to 2.3 point at `schedule-api` and trace `7c2a9f1e...` |
| 15:24 | 24 | Step 2.4: every container `healthy`; saturation gauges at 0, mitigations 1 and 2 discarded |
| 15:26 | 26 | Escalated to the owner of `schedule-api` (Tech Lead), who decides the rollback |
| 15:28 | 28 | `git checkout v2.0.0` in `barber-saas-schedule-api`, `docker compose up -d --build --wait schedule-api` |
| 15:36 | 36 | Maven build and start-up finish, the `v2.0.0` copy is `healthy` (8 min: no image registry) |
| 15:40 | 40 | Last slow call; `rate5m` of failures back to 0, booking p95 back under 0.8 s at 15:45 |

Detection 18 min, mitigation decided at 28 min, recovery at 40 min.

## Impact

```
attempted bookings                 = 240
failed (503, no appointment row)   = 15
SLO-A budget spent                 = 15 / 210              = 7.1 %
SLO-A budget in minutes            = 15 / 42,000 x 43,200  = 15.4 min   (or 7.1 % x 216 min)
slow bookings (> 0.8 s)            = 45 + 15               = 60
SLO-L budget spent                 = 60 / 2,100            = 2.9 %
SLO-L budget in minutes            = 60 / 42,000 x 43,200  = 61.7 min   (or 2.9 % x 2,160 min)
SLO-F budget spent                 = 0
extra load on schedule-api         = 60 retries            = +25 % calls
SLO-A budget left                  = 210 - 15              = 195 failed bookings (92.9 %)
```

The 7.1 % stays under the 20 % that makes a postmortem mandatory in the policy of `05-slo.md`;
this one is written because it is a drill.

## Root cause

The availability read of `2.0.1` loads `schedule_exception` rows by `barbershop_id` alone. For
barbershops with a long day-off history the query takes about 3 s, which is exactly the request
timeout of `appointment-api`, so the call is abandoned even though `schedule-api` answers.

## Contributing factors

1. **The page is blind to volume.** The burn-rate ratio was 12.5, under 14.4, while the peak of
   6 bookings/min spent the budget about 3 times faster than the average the alert assumes.
2. **No alert on SLO-L.** Latency burned at 5x with no rule watching it; 25 % of the clients
   waited more than 3 s.
3. **The retry doubles the wait.** Without a circuit breaker each failed booking waited 6 s and
   `schedule-api` received 25 % more calls while it was slow.
4. **The deploy check is `/health` only.** Nothing compares latency before and after a deploy,
   and no test of `GET /api/v1/availability` runs with a realistic `schedule_exception` volume.
5. **Rollback is a build.** Images come from `build:`, so going back to `v2.0.0` took 8 minutes.

## Action items

**OBS-07 - Availability stays fast with a long exception history.** As the owner of
`schedule-api`, I want a performance test of `GET /api/v1/availability` with realistic data, so a
query that skips the barber and date index fails CI.

- Given a barbershop with 5,000 `schedule_exception` rows spread over its barbers, when the CI
  test calls `GET /api/v1/availability` 50 times for one barber and one date, then the p95 is
  under 300 ms (SLO-2 for `GET`).
- Given the same data, when `EXPLAIN` runs the exception query, then the plan uses
  `uq_schedule_exception_barber_date`.

**OBS-08 - An alert catches a slow dependency before a client does.** As the owner of
`appointment-api`, I want a `ticket` alert on the burn of SLO-L, so a fault like this drill is
found by a rule and not by a message from a barbershop.

- Given the drill replayed as `promtool test rules` series (6 bookings/min, 6.25 % failed, 25 %
  over 0.8 s), when the test runs in CI, then a `ticket` alert fires before minute 18.
- Given normal traffic with one failed and one slow booking per hour, when the same test runs,
  then no alert fires.

**OBS-09 - Rollback is a pull, not a build.** As the team, we want every `-api` image published
with its version tag, so mitigation 3 of `07-runbook.md` takes minutes, not a build.

- Given `schedule-api` `v2.0.0` published as an image with tag `2.0.0`, when the responder runs
  the rollback of mitigation 3 with that tag, then `docker compose ps` shows `schedule-api`
  `healthy` in under 2 minutes from the command.
- Given the image is published, when `07-runbook.md` is read, then mitigation 3 names the image
  tag instead of `git checkout` and `--build`.
