# Failure path — injected failure and graceful degradation (v3.0.0)

**What was run:** the 31 repositories unpacked from their `v3.0.0` tags (the same source zips of this delivery), started from scratch with `barber-saas-infra-postgres/scripts/up.sh dev` (13 containers healthy in 167 s, own Compose project `barber-saas-v300`), then `failure-path.sh` injected two failures with `docker stop`. Mail went to a local SMTP sink; nothing left the machine.

## Results

| Failure | What the user sees | Measured |
|---|---|---|
| A · `loyalty-api` stopped | only `/api/v1/loyalty/*` answers `503 SERVICE_UNAVAILABLE` with a `traceId`; the catalog and `/health` keep answering `200` | 503 in 2.0 s (gateway connect timeout) · other route 200 in 13 ms · route back 4 s after `docker start` |
| B · `notifications-api` stopped | sign-up still answers `201`; the `EmailVerificationRequested` event waits in the `identity-auth` outbox | register 201 in 135 ms · event kept 60 s, not lost · delivered 7 s after `docker start`, e-mail received |

**Why it degrades this way:** the gateway resolves each service per request (`nginx/conf.d/00-resolver.conf`), so a dead upstream only fails its own route, and turns 502/503/504 into the shared error envelope with the correlation id (`20-server.conf`). The producer writes the event in the same transaction as the user (outbox); the worker relays it and retries 5xx/network errors up to 8 times with exponential backoff (5 s → 300 s, jitter), so the consumer's outage only delays the e-mail (ADR-016).

**Extra observation (first run):** with `SMTP_HOST` empty, `notifications-api` answers `503` to `EmailVerificationRequested` on purpose; the worker kept retrying for 13 minutes without losing or dead-lettering the event, and delivered it as soon as SMTP was configured.

## Run log

```text
# Failure path — BarberSaaS v3.0.0 (local, 2026-11-12)

## 0. Baseline
23:18:21 GET /health -> 200 0.004287
23:18:21 GET /api/v1/barbershops (public catalog) -> 200 0.016085
23:18:21 GET /api/v1/loyalty/cards/me (bad token, reaches the service) -> 401 0.004970

## 1. Inject failure A: stop loyalty-api
23:18:22 docker stop loyalty-api
23:18:24 GET /api/v1/loyalty/cards/me -> 503 2.004334
    body: {"error":"SERVICE_UNAVAILABLE","message":"the service is not available, retry later","traceId":"88870236e7f81afe49ed3bbf60021596"}
23:18:24 GET /api/v1/barbershops (other route) -> 200 0.013046
23:18:24 GET /health -> 200 0.027073
23:18:25 docker start loyalty-api
23:18:29 GET /api/v1/loyalty/cards/me -> 401 0.089571 (recovered)

## 2. Inject failure B: stop notifications-api, register a user
23:18:30 docker stop notifications-api
23:18:31 POST /api/v1/auth/register -> 201 0.135146 (the user is not blocked by the outage)
23:18:34 outbox event EmailVerificationRequested published_at=NULL
23:18:35 waiting 60 s with notifications down…
23:19:35 outbox event published_at=NULL (kept, not lost)
    worker log while down:
      {"job": "relay-loyalty", "correlationId": "7c19584b36494525ad2fb0bed99bb017", "done": 0, "failed": 0, "waiting": 0, "notes": ["loyalty pending: 0 http://loyalty-api:8080 unreachable: URLError"]}
23:19:36 docker start notifications-api
23:19:43 outbox event published_at=2026-11-13 04:19:40.943981+00 — delivered 7 s after recovery
23:19:43 GET /health -> 200 0.005933
```

Script: `failure-path.sh` (in this folder).
