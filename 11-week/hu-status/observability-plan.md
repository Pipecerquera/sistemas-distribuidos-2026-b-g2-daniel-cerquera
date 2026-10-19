# BarberSaaS — Observability plan (week 11)

Week 11 topic: observability in distributed systems. Session 1 asks for structured logs with a
propagated trace id, basic metrics (rate, errors, latency) and one distributed trace across a
multi-service request. Session 2 asks for 2–3 SLOs with windows, an error-budget policy, a
symptom-based alert with owner and runbook, and the work sliced as stories with testable
acceptance criteria.

This file is the result of session 2. It starts from what the code of release `v2.0.0` already
does, so every SLO below can be measured with data the platform produces today or after the
stories in section 5.

## 1. What the platform already has (`v2.0.0`)

| Signal | State | Where |
|---|---|---|
| Trace id at the edge | **done** — the gateway reuses the client's `X-Correlation-Id` or creates one (`$request_id`), sends it to the service and back to the client, and returns it as `traceId` in every gateway error | [`10-security.conf`](https://github.com/code-corhuila/barber-saas-api-gateway/blob/v2.0.0/nginx/conf.d/10-security.conf) |
| Structured access log | **done** — one JSON object per request: `time`, `correlationId`, `method`, `uri`, `status`, `upstream`, `durationMs` | [`nginx.conf`](https://github.com/code-corhuila/barber-saas-api-gateway/blob/v2.0.0/nginx/nginx.conf) |
| Structured service logs | **done** — Java services log ECS JSON (`logging.structured.format.console: ecs`) with `correlationId` in the MDC; error envelopes carry it as `traceId` | e.g. workflow [`application.yml`](https://github.com/code-corhuila/barber-saas-workflow/blob/v2.0.0/workflow-app/src/main/resources/application.yml), [`CorrelationFilter.java`](https://github.com/code-corhuila/barber-saas-workflow/blob/v2.0.0/workflow-adapters/src/main/java/co/edu/corhuila/barbersaas/workflow/adapter/in/http/CorrelationFilter.java) |
| Propagation across services (sync) | **done** — the workflow sends the request's `X-Correlation-Id` to every saga step (barbershop, platform-admin, identity-auth) | [`ParticipantClient.java`](https://github.com/code-corhuila/barber-saas-workflow/blob/v2.0.0/workflow-adapters/src/main/java/co/edu/corhuila/barbersaas/workflow/adapter/out/http/ParticipantClient.java) |
| Propagation across the outbox (async) | **partial** — each outbox row stores the request's `correlation_id` and the envelope carries it, but the worker sends its own per-run id in the `X-Correlation-Id` header | [`client.py`](https://github.com/code-corhuila/barber-saas-worker/blob/v2.0.0/src/worker/adapter/outbound/http/client.py), [`scheduler.py`](https://github.com/code-corhuila/barber-saas-worker/blob/v2.0.0/src/worker/adapter/inbound/scheduler.py) |
| Metrics (rate / errors / latency) | **not yet** — OpenTelemetry Collector, Prometheus and Grafana exist in `infra-postgres` under the `observability` profile ([`e7cea59`](https://github.com/code-corhuila/barber-saas-infra-postgres/commit/e7cea59d8a34222b4f273d5b7919c4436bc5f5ad)), but no service exports OTLP yet | [`otel-collector.yaml`](https://github.com/code-corhuila/barber-saas-infra-postgres/blob/v2.0.0/observability/otel-collector.yaml) |
| Distributed traces (spans) | **not yet** — the collector has a traces pipeline but only to the `debug` exporter; there is no trace backend | same file |

**Defect found while reviewing:** the gateway writes `"durationMs":$request_time`, but nginx's
`$request_time` is in **seconds** with millisecond resolution. Any latency SLO computed from that
field would be off by 1000×. Fixed by story OBS-01.

## 2. SLOs

Window for all three: **28 days, rolling**. Measured at the edge (what the user sees), not inside
each service.

| ID | Type | SLI (good events / valid events) | Objective | Source |
|---|---|---|---|---|
| **SLO-1** | Availability | Requests through the gateway to `/api/v1/**` answered with a status other than 5xx (429 counts as good: it is a deliberate limit) / all requests to `/api/v1/**`, health checks excluded | **99.5 %** (error budget 0.5 % ≈ 3 h 22 min of full outage in 28 days) | gateway access log now; `http.server.request` metric after OBS-02 |
| **SLO-2** | Latency | `GET /api/v1/**` answered in < **300 ms** and `POST /api/v1/appointments` in < **800 ms** / all such requests that did not fail with 5xx | **95 %** | gateway access log (`durationMs`, after OBS-01) |
| **SLO-3** | Freshness | Outbox events with `published_at - occurred_at` < **60 s** / all events with `occurred_at` in the window (an event still unpublished after 60 s counts as bad) | **99 %** | the `outbox_event` tables of appointment, loyalty and identity-auth (`occurred_at`, `published_at`, `failed_at`) |

Why these three: SLO-1 and SLO-2 cover the booking flow, the core of the product; SLO-3 covers
the asynchronous half (loyalty stickers, notifications, password-reset e-mail), which can be
broken while every HTTP request still answers 200.

## 3. Error-budget policy

| Budget consumed in the 28-day window | What the team does |
|---|---|
| < 50 % | Normal work: features and fixes as planned. |
| 50 – 100 % | The owner of the SLO opens an issue; the next sprint plans at least one reliability story from the cause found. |
| > 100 % (budget exhausted) | Feature freeze on the affected path: only fixes and reliability work merge to `develop` until the SLI is back above the objective over the last 7 days. |
| One incident burns > 20 % | Written post-incident review (cause, timeline, action items) linked from the issue, whatever the total. |

Planned maintenance announced in advance and failures of the developer's own machine do not
count against the budget.

## 4. Symptom-based alerts

Alerts fire on what users feel (errors, delay), never on causes (CPU, a restarted container).

| Alert | Condition | Severity | Owner | Runbook |
|---|---|---|---|---|
| **`EventsNotDelivered`** | Oldest unpublished outbox event older than **5 min** in any producer, **or** SLO-3 burn rate > 14.4× over 1 h and over 5 min | page | Daniel Cerquera (worker owner) | [RB-01](#6-runbook-rb-01--domain-events-are-not-being-delivered) |
| `GatewayErrorBudgetBurn` | SLO-1 burn rate > 14.4× over 1 h and over 5 min (2 % of the budget in one hour) | page | Daniel Cerquera (api-gateway owner) | RB-02 (to write with OBS-02) |

The burn-rate thresholds follow the multi-window rule: 14.4× means the whole 28-day budget would
be gone in under two days; requiring both windows avoids paging for a short spike that already
stopped.

## 5. Observability stories (with testable acceptance criteria)

| ID | Story | Acceptance criteria |
|---|---|---|
| **OBS-01** | As an operator, I want the gateway's latency field in milliseconds, so latency SLOs are computed correctly | Given a request that takes 1.2 s, the access-log line has `durationMs` between 1200 and 1300 (or the field is renamed `durationSeconds` with the raw value); the gateway CI test checks the format |
| **OBS-02** | As an operator, I want rate, errors and latency of every HTTP service, so SLO-1 and SLO-2 have metrics | Each service exports `http.server.request.duration` (histogram, labels `route`, `method`, `status`) over OTLP to the collector; Prometheus shows them for the 8 APIs after `up.sh dev --profile observability`; a request answered 500 increases the error series of that route by 1 |
| **OBS-03** | As an operator, I want one trace across the owner-onboarding saga, so I see which step was slow or failed | With a trace backend (Jaeger or Tempo) in the `observability` profile, one `POST /api/v1/sagas/owner-onboarding` produces one trace with spans for gateway → workflow → `create-barbershop` → `assign-plan` → `create-owner`; with `identity-auth-api` stopped, the trace shows the 3 retries and the compensation; the trace id equals the `correlationId` in the logs |
| **OBS-04** | As an operator, I want the original correlation id to survive the outbox, so a booking and the notice it caused share one id | When the worker delivers an event it sends that event's own `correlationId` as `X-Correlation-Id` (and keeps the run id in its own log line as `runId`); a test books an appointment with `X-Correlation-Id: corr-test` and finds `corr-test` in notifications-api's log for `AppointmentCreated` |
| **OBS-05** | As the worker owner, I want the outbox lag as a metric, so SLO-3 and `EventsNotDelivered` can be evaluated | The worker exports `outbox_oldest_unpublished_seconds{producer}` and `outbox_published_total{producer,result}`; with loyalty-api stopped for 6 min, the alert rule evaluates to firing; when it comes back, the gauge drops to < 10 s within one run |
| **OBS-06** | As the team, we want the SLOs and alerts versioned next to the code | `observability/rules.yml` in `infra-postgres` holds the recording and alert rules of section 4, loaded by Prometheus; `promtool check rules` runs in CI; DOCS records the SLOs in a decision |

## 6. Runbook RB-01 — Domain events are not being delivered

**Alert:** `EventsNotDelivered`. **Owner:** Daniel Cerquera. **User impact:** bookings work, but
clients do not get the confirmation notice, completed appointments do not add a loyalty sticker,
and password-reset codes are not e-mailed. No data is lost: events wait in the outbox.

**1. Confirm the symptom (2 min)**

```bash
docker exec barber-saas-dev-postgres-1 sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "
  select ''appointment'' p, count(*) pending, min(occurred_at) oldest from appointment.outbox_event  where published_at is null and failed_at is null
  union all
  select ''identity_auth'', count(*), min(occurred_at) from identity_auth.outbox_event where published_at is null and failed_at is null;"'
```

Pending events older than 5 minutes confirm the alert. A count that drops between two runs means
the worker is catching up: watch, do not act.

**2. Is the worker running? (2 min)**

```bash
docker ps --filter name=worker --format '{{.Names}} {{.Status}}'
docker logs --since 10m barber-saas-dev-worker-1 | tail -50
```

- No container, or `Exited` → `docker compose --env-file env/dev.env up -d worker` and go to step 5.
- Lines with `"job": "relay-..."` and `"done": 0` every 5 s while events are pending → the worker
  runs but cannot read or deliver: step 3.

**3. Which side fails? (5 min)** Take the `correlationId` of a failing run from the worker log
and search it in the producer's and consumers' logs:

```bash
docker logs --since 10m barber-saas-dev-appointment-api-1 2>&1 | grep <correlationId>
docker logs --since 10m barber-saas-dev-loyalty-api-1      2>&1 | grep <correlationId>
docker logs --since 10m barber-saas-dev-notifications-api-1 2>&1 | grep <correlationId>
```

| What you see | Cause | Action |
|---|---|---|
| Producer answers `401` to the worker | the worker's service token expired (dev tokens last 24 h) | `./scripts/dev-keys.sh` then `docker compose --env-file env/dev.env up -d` |
| A consumer is down or answers `5xx`/`503` | consumer unavailable (e.g. notifications without SMTP answers 503 on purpose) | restart or fix that consumer; the worker retries with back-off, nothing else to do |
| A consumer answers `4xx` and events get `failed_at` | contract mismatch: the event is set aside, not retried | read `last_error`; fix the consumer or the producer; open an issue |
| Producer answers `5xx` on `/internal/v1/outbox` | producer or its database down | check `postgres` health and the producer's log |

**4. Events set aside (`failed_at` not null).** They are never retried automatically. After the
cause is fixed, list them with `select id, event_type, last_error from <schema>.outbox_event where failed_at is not null order by occurred_at;`
and re-queue only the ones whose cause is fixed (`update ... set failed_at = null, last_error = null where id in (...)`).
Consumers are idempotent (`processed_event`), so a re-sent event is answered `DUPLICATE` and causes
no second effect.

**5. Verify recovery.** Re-run the query of step 1: pending events must fall to zero and the oldest
must be younger than 60 s within a few runs. Book a test appointment and check the client's inbox
shows "Cita confirmada".

**6. Close.** Note the start and end time, the cause and the budget burned in the issue. If more
than 20 % of SLO-3's budget burned, write the post-incident review (section 3).

**Escalate** to the owner of the failing consumer (loyalty, notifications) when the cause is in
their service and step 3 does not resolve it within 30 minutes.
