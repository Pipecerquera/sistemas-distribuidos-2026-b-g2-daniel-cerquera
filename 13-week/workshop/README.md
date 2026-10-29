# Week 13 Workshop - Threat Modeling and the Security Backlog

Threat model of one trust boundary of BarberSaaS: the edge where requests from the internet
(the mobile app and the Wompi payment events) enter the platform through Caddy and the NGINX
gateway, before they reach any domain service.

| Element | Name in `barber-saas-docs` | Source |
|---|---|---|
| Gateway | `api-gateway` (NGINX, the only published port) | `09-microservices/service-catalog.md` |
| Token entry point | `POST /api/v1/auth/refresh` (`identity-auth-api`) | `07-api/authentication.md` |
| Payment entry point | `POST /api/v1/payments/wompi/events` (`platform-admin-api`, no token) | `05-architecture/decisions/records/ADR-017-plan-payments-wompi-sandbox.md` |

<!-- CONFIG-START -->
- FULL_NAME: Daniel Felipe Cerquera Idrobo
- GITHUB_USER: Pipecerquera
- TEAM: Barbersaas
- DOCS_REPO: https://github.com/code-corhuila/barber-saas-docs
- BOUNDARY: TB1 Internet -> edge (Caddy HTTPS + `barber-saas-api-gateway`)
- CLAIM_ISSUE: https://github.com/code-corhuila/barber-saas-docs/issues/150
- REFLECTION_DOC: https://docs.google.com/document/d/1jg3-w3ClQEw4KATDxsTqX-Xyui4kjdUTNEuh5v2e1P8/edit?usp=sharing
- LIBRARY_BOOK: Nilsson, N. J. - Inteligencia artificial: una nueva síntesis - 1st Spanish ed. - McGraw-Hill/Interamericana de España, 2001 - call number 006.3 N347 Ej. 1 (pp. 175-178, 285)
- LIBRARY_RECORD_TIME: 2026-10-29 08:20
- STRIDE_PR: https://github.com/code-corhuila/barber-saas-docs/pull/153
- STORIES: https://github.com/code-corhuila/barber-saas-docs/issues/155, https://github.com/code-corhuila/barber-saas-docs/issues/156, https://github.com/code-corhuila/barber-saas-docs/issues/157
<!-- CONFIG-END -->

## Contents

| Checkpoint | File |
|---|---|
| CP1 | [`01-attack-surface.md`](01-attack-surface.md) |
| CP2 | STRIDE Pull Request in `barber-saas-docs` |
| CP3 | Security stories in the team board |
| CP4 | [`04-security-ci/`](04-security-ci/) |
