# ADR-C3-02: Build each image once and promote it from SWR

## Status

Proposed

## Context

Today the images of the services are built directly on the virtual machine for every deployment (ADR-020: the first
build took about 6 minutes), and there is no image registry. The image that runs in production can differ from the one
tested in qa, and a rollback has to rebuild the previous version from the source. Section 2 (Operations) and section 5
of the design note need the same artifact in qa and in production and a fast way back.

## Decision

Build the image of each service once, when the change is merged into `qa`, push it to SWR with a tag, and deploy that
same image to qa and later to production. A rollback deploys the previous image already stored in SWR.

## Alternatives considered

- Keep building the images on the virtual machine for each deployment: qa and production can end up with different
  builds, and a rollback is slower because it rebuilds.
- Docker Hub or GitHub Container Registry: they work, but we prefer to keep the images inside Huawei Cloud, next to CCE,
  with SWR.

## Consequences

- Positive: qa and production run exactly the same image, and an earlier version can be restored without rebuilding it.
- Negative: the tags must be managed and the old images cleaned up regularly to control the storage and its cost.
