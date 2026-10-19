# CP2 - Structured Log Contract

Contract for every log line written by `appointment-api` while serving the journey of
[`01-journey.md`](01-journey.md) (`POST /api/v1/appointments`). One JSON object per line, UTF-8,
written to stdout. Field names are `snake_case`; unknown fields are not added ad hoc.

Sources in `barber-saas-docs`: `06-data/models.md` §5 and §10, `07-api/guidelines.md`
(`Idempotency-Key`, `X-Correlation-Id`), `07-api/contracts/openapi/appointment-service.yaml`
(`DEC-APPT-01` to `DEC-APPT-09`), `09-microservices/event-catalog.md`.

## 1. Fields

| Name | Type | Required | Example | Why |
|---|---|---|---|---|
| `timestamp` | string, ISO-8601 UTC with ms | required | `2026-10-19T14:03:21.412Z` | Orders lines across services without time-zone guessing |
| `level` | string: `ERROR`, `WARN`, `INFO`, `DEBUG` | required | `INFO` | Filters noise and drives alerts (section 5) |
| `service` | string | required | `appointment-api` | Tells which container wrote the line |
| `version` | string, semver | required | `2.0.0` | Ties a regression to the release tag that shipped it |
| `environment` | string: `dev`, `qa`, `prod` | required | `dev` | Keeps test traffic out of production dashboards |
| `trace_id` | string, 32 lowercase hex | required | `4bf92f3577b34da6a3ce929d0e0e4736` | Joins every line of one booking across services (W3C `traceparent`) |
| `span_id` | string, 16 lowercase hex | required | `00f067aa0ba902b7` | Places the line on one span of the tree in CP1 |
| `correlation_id` | string | required | `c-7f3e2a91` | Value of `X-Correlation-Id`, returned as `traceId` in the error envelope |
| `event` | string, dotted | required | `appointment.book.received` | Stable name to count and search, unlike free text |
| `message` | string, English | required | `Booking request received` | Human summary of the line |
| `duration_ms` | integer, milliseconds | required | `184` | Duration of the operation the line closes (call, request); on other lines, time elapsed since the request started |
| `barbershop_id` | string, UUID | required inside a request | `9b1d...` | Tenant from the token's `barbershopId` claim; isolates one barbershop's incidents |
| `appointment_id` | string, UUID | optional, once created | `5c0e...` | Links the line to the `appointment` row and the `AppointmentCreated` outbox event |
| `barber_id` | string, UUID | optional | `a71c...` | `barber_profile` id; shows overloaded barbers and double-booking hot spots |
| `service_id` | string, UUID | optional | `e2f4...` | Catalog service booked; explains price and duration snapshots |
| `downstream` | string: `barbershop-api`, `schedule-api`, `loyalty-api` | required on `downstream.*` | `schedule-api` | Names the dependency that answered, failed or timed out |
| `http_status` | integer | required on `http.request.completed`, and on `downstream.*` when an answer arrived | `422` | Outcome of the call without parsing `message` |
| `error_code` | string | required when `level` is `ERROR` or a rule rejects | `BUSINESS_RULE_VIOLATION` | Closed code of `_shared.yaml` `ErrorResponse` (`BUSINESS_RULE_VIOLATION`, `SERVICE_UNAVAILABLE`, ...) |
| `coupon_applied` | boolean | optional | `false` | Whether an `ACTIVE` `reward_coupon` paid the booking (`DEC-APPT-09`) |
| `outcome` | string: `created`, `replayed`, `rejected`, `failed` | required on `appointment.book.*` final lines | `rejected` | Same values as the CP3/CP4 metric label |

## 2. Never log

| Data | Where it appears | How it is masked |
|---|---|---|
| User JWT and the worker/workflow service token | `Authorization` header | Header dropped; only `auth_scheme: "Bearer"` may be logged |
| `Idempotency-Key` | Booking request header | First 12 hex of its SHA-256 as `idempotency_key_hash` |
| `client_id` | Token `sub`, `appointment.client_id` | First 12 hex of HMAC-SHA-256 with a per-environment salt as `client_ref` |
| `notes` | `BookAppointmentRequest.notes`, `appointment.notes` (free text, up to 500 chars) | Never logged; only `notes_length` |
| E-mail, full name and phone of an `app_user` | Snapshots read from other domains | Never logged; the UUID is enough |
| Password-reset `code` and passwords | `PasswordResetRequested` payload, `/api/v1/auth/*` bodies | Never logged, not even masked |
| Full request and response bodies | Every endpoint | Never logged; only the fields of section 1 |

## 3. Example lines of one booking

Same `trace_id` on all seven lines; timestamps grow. Line 4 is a timeout to `schedule-api`
(recovered by the single retry of `communication-patterns.md` §3) and line 6 is a business-rule
rejection: the slot was taken by another booking (`ex_appointment_no_double_booking`, `INV-APPT-001`).

```json
{"timestamp":"2026-10-19T14:03:21.412Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"00f067aa0ba902b7","correlation_id":"c-7f3e2a91","event":"appointment.book.received","message":"Booking request received","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","barber_id":"a71c2f0e-6c1e-4d8a-9a56-0f3b8c2d9e11","service_id":"e2f4b6a8-1c3d-4e5f-8a9b-0c1d2e3f4a5b","client_ref":"3f9a1c0b7e2d","idempotency_key_hash":"b5d41402abc4","notes_length":0,"duration_ms":0}
{"timestamp":"2026-10-19T14:03:21.468Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"b7ad6b7169203331","correlation_id":"c-7f3e2a91","event":"downstream.call.succeeded","message":"Service price and duration read","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","downstream":"barbershop-api","http_status":200,"duration_ms":41}
{"timestamp":"2026-10-19T14:03:21.512Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"5fb397be34d26b51","correlation_id":"c-7f3e2a91","event":"downstream.call.succeeded","message":"No active coupon for the client","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","downstream":"loyalty-api","http_status":200,"duration_ms":29,"coupon_applied":false}
{"timestamp":"2026-10-19T14:03:24.515Z","level":"WARN","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"e457b5a2e4d86bd1","correlation_id":"c-7f3e2a91","event":"downstream.call.timeout","message":"Availability request timed out, retrying once","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","downstream":"schedule-api","duration_ms":3000}
{"timestamp":"2026-10-19T14:03:24.633Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"0b2a5c9d7e1f3a64","correlation_id":"c-7f3e2a91","event":"downstream.call.succeeded","message":"Free slots read on retry","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","downstream":"schedule-api","http_status":200,"duration_ms":112}
{"timestamp":"2026-10-19T14:03:24.651Z","level":"WARN","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"00f067aa0ba902b7","correlation_id":"c-7f3e2a91","event":"appointment.book.rejected","message":"Slot already taken for the barber","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","barber_id":"a71c2f0e-6c1e-4d8a-9a56-0f3b8c2d9e11","error_code":"BUSINESS_RULE_VIOLATION","outcome":"rejected","duration_ms":3239}
{"timestamp":"2026-10-19T14:03:24.655Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"00f067aa0ba902b7","correlation_id":"c-7f3e2a91","event":"http.request.completed","message":"POST /api/v1/appointments answered","barbershop_id":"9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d","http_status":422,"duration_ms":3243,"outcome":"rejected"}
```

The `span_id` of lines 2 to 5 is the client span of each outgoing call (`a2`, `a4`, `a5` and
its retry in CP1); lines 1, 6 and 7 belong to the root server span `a1`.

## 4. Levels

| Level | When | Example in this journey |
|---|---|---|
| `ERROR` | The service could not do its job and someone must act: a dependency failed after the retry, a `5xx` is returned, the transaction did not commit | `schedule-api` times out twice; the booking answers `503` with `error_code` `SERVICE_UNAVAILABLE` and no `appointment` row is written |
| `WARN` | Something went wrong but was handled or was the caller's doing: a recovered timeout, a rule rejection, an idempotency replay with a different body | Slot taken (`422 BUSINESS_RULE_VIOLATION`, `INV-APPT-001`), or the first `schedule-api` timeout before the retry |
| `INFO` | One line per business step and per request end, enough to rebuild the journey | `appointment.book.created` with `appointment_id` after the `appointment`, `idempotency_key` and `outbox_event` rows commit |
| `DEBUG` | Detail for a developer, off in `qa` and `prod` | The list of free `startTime` values returned by `GET /api/v1/availability` before checking the requested one |
