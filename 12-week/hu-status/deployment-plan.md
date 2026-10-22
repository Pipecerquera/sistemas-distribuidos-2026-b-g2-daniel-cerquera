# BarberSaaS — Deployment plan for MVP 3

Week 12. Session 1 asks to deploy the system with a tagged image, a zero-downtime strategy and
health checks; session 2 asks for the deployment plan of MVP 3. This document first records how
the platform is deployed **today** (release `v2.0.0`, read from the repositories), then the plan:
artifact promotion, infrastructure as code, the zero-downtime strategy, the rollback and the
deployment stories with testable acceptance criteria.

## 1. How the platform is deployed today (`v2.0.0`)

| Topic | What exists | Where |
|---|---|---|
| Composition | One root `compose.yml` in `infra-postgres` that `include`s the `deploy/compose.yml` of every repository; `scripts/up.sh [dev\|qa\|main]` starts PostgreSQL and MongoDB, runs every `-db` migration, then the rest with `--wait` | `barber-saas-infra-postgres` |
| Environments | `dev`, `qa` and `main` on the same host, separated by `COMPOSE_PROJECT_NAME` in `env/<environment>.env` (own containers and volumes); only `env/*.env.example` is versioned | `infra-postgres/env/` |
| Images | **Built from source on the host** (`build:` in every service); no registry, no image tag, no digest. The `v2.0.0` git tags exist in the 31 repositories, but the image that runs is whatever the host built | every `deploy/compose.yml` |
| Health checks | The 12 back-end services (gateway, 9 APIs, workflow, worker) declare a Docker `healthcheck` on `GET /health` (every 5 s, 30 retries) and `up.sh` waits on them. It is a **liveness** check: it answers `{"status":"ok"}` without touching the database. The 9 front-end containers have none | e.g. `appointment-api/deploy/compose.yml`, `HealthController.java` |
| Routing | Only the NGINX gateway is published; the services use `expose`. The gateway resolves each service name through Docker DNS every 10 s (`resolver 127.0.0.11 valid=10s`), so a new container behind the same name is found without restarting it | `api-gateway/nginx/conf.d/00-resolver.conf` |
| Schema changes | Liquibase per `-db`, every changeset with its rollback, checked in CI (`update` → `update` → `rollback-count` → `update`, ADR-007) | `db-ci.yml` in each `-db` |
| Cloud host | **None.** The platform has only run on the team's laptops | — |

What this means for week 12: we have health checks and per-environment configuration, but no
artifact to promote (every host rebuilds, so `qa` and `main` may not run the same bits) and no
host outside a laptop. A deployment today is `up.sh`, which **recreates** each changed container:
the service is down from the moment the old container stops until the new one is healthy
(about 30 s for a Spring Boot API in the week 11 drill).

DOCS is also behind: `04-requirements/non-functional.md` (NFR-002, NFR-008) and
`10-devops/environments.md` still describe Railway, MySQL, Vault and "no health checks", from the
first prototype. They need to be rewritten from this plan (story DEP-07).

## 2. Target for MVP 3

One **production-like Linux host** (a cloud VM with Docker Engine and the Compose plugin; sizing:
the 12 back-end limits add up to about 6 GB, plus PostgreSQL, MongoDB and the 9 front ends,
so 4 vCPU and 16 GB), running the `main` environment from **images pulled from a registry**, never
built on the host. `qa` runs on the same host with its own project name until there is budget for
a second one. Kubernetes is out of scope: the course norm and ADR-011 (one instance per engine)
fit one host, and the team has no operator for a cluster.

## 3. Artifact promotion — build once, promote the digest

1. On a `vX.Y.Z` tag, the `ci.yml` of each service repository builds its `deploy/Dockerfile` once
   and pushes `ghcr.io/code-corhuila/barber-saas-<repo>:X.Y.Z` (plus the commit SHA as a second
   tag) to GitHub Container Registry, with the default `GITHUB_TOKEN`.
2. The release record (`release-tags-vX.Y.Z.md`, as in `c2-release`) adds the **image digest**
   of each repository next to its tag and commit.
3. `qa` and `main` pin images by version in `env/<environment>.env` (`APPOINTMENT_API_IMAGE=…:2.1.0`).
   Promoting to `main` changes only those lines, to the **same** digests that passed in `qa`:
   nothing is rebuilt between environments.
4. `dev` keeps building from source, so local work does not need a registry.

## 4. Infrastructure as code

| Layer | File (all versioned in `infra-postgres`) | Change |
|---|---|---|
| Host | `deploy/host/cloud-init.yml` | Installs Docker, creates the `platform` network and a deploy user without root login; the same file brings up a new VM from zero |
| Runtime | `compose.yml` + new `compose.release.yml` | The override replaces each `build:` with `image: ${<SERVICE>_IMAGE:?…}` for `qa` and `main` |
| Configuration | `env/qa.env.example`, `env/main.env.example` | Add one `<SERVICE>_IMAGE` per service; secrets stay out of git (Annex J J.5.1) |
| Procedure | `scripts/deploy.sh <environment> <service>` and `scripts/rollback.sh` | The rolling update of §5, the same for every service |

## 5. Zero-downtime strategy: rolling, one service at a time

**Chosen: rolling update per service.** Blue-green would double the 30 containers and needs two
copies of the single PostgreSQL and MongoDB instances, or a schema both colours accept at once;
on one host that is cost without benefit. Rolling works because the services are stateless,
the gateway re-resolves names every 10 s and every service already has a health check.

`scripts/deploy.sh main appointment-api`:

1. Change `APPOINTMENT_API_IMAGE` in `env/main.env` to the new version.
2. `docker compose … up -d --no-deps --no-recreate --scale appointment-api=2 appointment-api` —
   the old container keeps serving and a second one starts **with the new image**.
3. Wait until the new container is `healthy` (fail after 120 s → remove it and stop: nothing changed
   for the users).
4. Wait 15 s (one and a half gateway DNS refreshes) so both containers receive traffic, and check
   that the new one answers through the gateway.
5. Stop and remove the old container, then scale back to 1.

**Database changes go first and are backward compatible** (expand/contract): the migration that
adds a column runs before the new image; the one that drops the old column waits for the next
release. The old and new versions must work on the same schema during step 4.

Health checks to add: a **readiness** check (`GET /health/ready`, checks the database connection)
used by step 3, while `/health` stays as liveness; and a health check for the 9 front-end
containers (`wget -q --spider http://localhost/`).

## 6. Rollback

- **Image:** the rollback is the same rolling update pointing to the previous version that the
  release record pins: `scripts/rollback.sh main appointment-api 2.0.0`. No rebuild, no git revert.
- **Schema:** thanks to expand/contract, the previous image still works on the new schema, so the
  rollback normally does not touch the database. A changeset is only rolled back
  (`rollback-count 1`) after a backup (Annex J J.5.4).
- **Target:** one service back to the previous version in **under 2 minutes**, with zero failed
  requests at the gateway.
- **Rehearsal:** not done yet — it needs the images of §3. It will be timed in `dev` with two
  locally tagged images (`2.0.0` and `2.0.1`), with a loop of 1 request per second through the
  gateway during the deploy and the rollback, and the times and failed requests recorded here.

## 7. Deployment stories (MVP 3)

| ID | Story | Acceptance criteria (testable) |
|---|---|---|
| DEP-01 | As the team, we want every service image built once on its version tag and pushed to GHCR, so `qa` and `main` run the same bits | Pushing tag `v2.1.0` publishes `ghcr.io/code-corhuila/barber-saas-appointment-api:2.1.0`; `docker pull` of it on another machine returns the same digest the CI log printed |
| DEP-02 | As the operator, I want `qa` and `main` to run pinned images, never builds, so a promotion only changes versions | `docker compose -f compose.yml -f compose.release.yml --env-file env/main.env config` shows no `build:` and one `image:` with a version per service; an unset `<SERVICE>_IMAGE` fails with its name |
| DEP-03 | As the operator, I want a readiness check per service, so traffic only reaches a container that can serve | With PostgreSQL stopped, `GET /health/ready` answers 503 and `/health` still 200; when PostgreSQL is back, ready answers 200 within 10 s |
| DEP-04 | As a client of a barbershop, I want a deploy not to fail my booking, so releases can happen during opening hours | During `deploy.sh main appointment-api` with 1 booking request per second through the gateway, 0 responses are 5xx and the new version answers at the end |
| DEP-05 | As the operator, I want a rehearsed and timed rollback, so a bad release is undone in minutes | `rollback.sh` from `2.0.1` to `2.0.0` ends in under 120 s with 0 5xx in the same loop; the time is written in §6 |
| DEP-06 | As the team, we want the production-like host created from versioned files, so it can be rebuilt from zero | A new VM with `deploy/host/cloud-init.yml` and `up.sh main` serves `GET /health` through the gateway; the steps fit in the README |
| DEP-07 | As the team, we want DOCS to describe the real deployment, so the documents match the code | NFR-002, NFR-008 and `10-devops/environments.md` mention no Railway, MySQL or Vault, and link this strategy through an ADR |

## 8. Risks

- **No host and no budget decided yet**: DEP-04 to DEP-06 can be shown on a laptop, but "cloud" needs
  a VM; the free tiers we know are too small for 16 GB.
- **`--scale` leaves two containers with the same name prefix**: the scripts must pick the old one by
  creation time; a mistake there removes the new one. Test it in `dev` first.
- **Liveness only today**: until DEP-03, a container is `healthy` while its database is down.
- **The workflow and worker hold work in progress** (saga steps, outbox batches): stopping the old
  container must wait for the in-flight step (graceful shutdown, `stop_grace_period`).
- **Security and CI/CD are the next weeks' topics**: GHCR access, the deploy user and the pipeline
  that runs `deploy.sh` will be reviewed there.
