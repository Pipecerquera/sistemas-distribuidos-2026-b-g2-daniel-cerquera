# Week 11 Workshop - End-to-End Observability

Observability, SLOs and incident response applied to one critical journey of BarberSaaS,
centred on one microservice and followed across services, the event relay and the database.

| Element | Name in `barber-saas-docs` | Source |
|---|---|---|
| Microservice | `appointment-api` | `09-microservices/service-catalog.md` |
| Entry endpoint | `POST /api/v1/appointments` | `07-api/contracts/openapi/appointment-service.yaml` |
| Table | `appointment.appointment` | `06-data/models.md` §5 |
| Event | `AppointmentCreated` (v2) | `09-microservices/event-catalog.md` |

<!-- CONFIG-START -->
- FULL_NAME: Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- DOCS_REPO: https://github.com/code-corhuila/barber-saas-docs
- REFLECTION_DOC: <REFLECTION_DOC_URL>
- JOURNEY: <JOURNEY>
<!-- CONFIG-END -->

## Contents

| Checkpoint | File |
|---|---|
| CP1 | [`01-journey.md`](01-journey.md) |
| CP2 | [`02-log-contract.md`](02-log-contract.md) |
| CP3 | [`03-instrumentation/`](03-instrumentation/) |
| CP4 | [`04-metrics.md`](04-metrics.md) |
