# How to Run - appointment-api Instrumentation

Instrumented slice of `appointment-api` for the journey of [`../01-journey.md`](../01-journey.md):
`POST /api/v1/appointments` checks the slot with `schedule-api` (`GET /api/v1/availability`, CP1
step 6). Persistence in the `appointment` schema and the `outbox_event` relay are out of scope.

| File | Role |
|---|---|
| `trace/TraceContext.java` | Parses or starts a W3C `traceparent`; builds child spans |
| `trace/TraceContextFilter.java` | Server span per request; returns `traceparent` and `X-Correlation-Id` |
| `trace/JsonLogFormatter.java` | One JSON line per event with the fields of [`../02-log-contract.md`](../02-log-contract.md) |
| `booking/ScheduleApiClient.java` | Outgoing call with a child `traceparent` (same `trace_id`) |
| `booking/BookingController.java` | Booking slice: `201`, `422 BUSINESS_RULE_VIOLATION` or `503 SERVICE_UNAVAILABLE` |
| `booking/BookingMetrics.java` | `appointment_book_requests_total{outcome}`, `appointment_book_duration_seconds` and `GET /metrics` |
| `TracePropagationTest.java` | Proves the incoming `trace_id` is the one sent to `schedule-api` |

## Requirements

- JDK 21
- Maven 3.9 (`mvn -v`)

## Install and test

```bash
cd 11-week/hu-status/workshop/03-instrumentation/appointment-api
mvn -B test
```

The test starts the service on a random port and a stub of `schedule-api` on another one, sends
`traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01`, and checks that:

1. the stub received a `traceparent` with the same `trace_id` and a new span id;
2. the response carries a `traceparent` with the same `trace_id`;
3. `GET /metrics` shows the booking in the counter and in the `le="0.8"` bucket.

## Run

```bash
mvn -B package -DskipTests
SCHEDULE_API_URL=http://localhost:8083 java -jar target/appointment-api-observability-2.0.0.jar
curl -i -X POST http://localhost:8080/api/v1/appointments \
  -H 'Content-Type: application/json' \
  -H 'traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01' \
  -d '{"barberId":"a71c2f0e-6c1e-4d8a-9a56-0f3b8c2d9e11","serviceId":"e2f4b6a8-1c3d-4e5f-8a9b-0c1d2e3f4a5b","date":"2026-10-20","startTime":"10:00"}'
curl -s http://localhost:8080/metrics | grep appointment_book
```

`SCHEDULE_API_URL` points to a running `schedule-api`; without it the booking answers `503` and
`appointment_book_requests_total{outcome="failed"}` grows. `APP_ENVIRONMENT` sets `environment`
in the logs (default `dev`).

## Test output (real run, 2026-10-19)

Spring startup lines and JVM warnings are left out; every other line is as printed.

```text
[INFO] --- surefire:3.5.4:test (default-test) @ appointment-api-observability ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running barbersaas.appointment.TracePropagationTest
{"timestamp":"2026-10-19T14:36:21.602Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"31a9ce4600b814eb","correlation_id":"640d3f27-1eef-496a-b0c8-1bd8d08eb3ac","event":"appointment.book.received","message":"Booking request received","duration_ms":86,"barber_id":"a71c2f0e-6c1e-4d8a-9a56-0f3b8c2d9e11","service_id":"e2f4b6a8-1c3d-4e5f-8a9b-0c1d2e3f4a5b"}
{"timestamp":"2026-10-19T14:36:21.635Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"5288adef0c9507c7","correlation_id":"640d3f27-1eef-496a-b0c8-1bd8d08eb3ac","event":"downstream.call.succeeded","message":"Availability answered","duration_ms":31,"downstream":"schedule-api","http_status":200}
{"timestamp":"2026-10-19T14:36:21.640Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"31a9ce4600b814eb","correlation_id":"640d3f27-1eef-496a-b0c8-1bd8d08eb3ac","event":"appointment.book.created","message":"Appointment booked","duration_ms":124,"outcome":"created","appointment_id":"492ee8f1-a4c3-47dc-82a2-ca0c0a62021b"}
{"timestamp":"2026-10-19T14:36:21.655Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"31a9ce4600b814eb","correlation_id":"640d3f27-1eef-496a-b0c8-1bd8d08eb3ac","event":"http.request.completed","message":"POST /api/v1/appointments answered","duration_ms":139,"http_status":201}
{"timestamp":"2026-10-19T14:36:21.725Z","level":"INFO","service":"appointment-api","version":"2.0.0","environment":"dev","trace_id":"dd1fb7893ff4108e6a61e534bb190468","span_id":"5fb166532f5e7bfe","correlation_id":"5608f77a-6e39-49bd-9c05-7975da8779b2","event":"http.request.completed","message":"GET /metrics answered","duration_ms":13,"http_status":200}
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.084 s -- in barbersaas.appointment.TracePropagationTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.808 s
[INFO] Finished at: 2026-10-19T09:36:22-05:00
[INFO] ------------------------------------------------------------------------
```
