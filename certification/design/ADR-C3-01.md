# ADR-C3-01: Backup and Restore for the PostgreSQL instance

## Status

Proposed

## Context

All the data of BarberSaaS that matters most lives in one PostgreSQL instance: the clients' appointments (`appointment`
schema) and the loyalty stamps and coupons (`loyalty` schema). Today it runs on one virtual machine with a daily backup
that stays on the same machine (ADR-020), so a failure can lose up to a whole day of bookings. Section 4 of the design
note sets the targets for the Huawei Cloud design: RPO 15 minutes and RTO 120 minutes.

## Decision

Protect PostgreSQL with Backup and Restore (backup-based disaster recovery): automated RDS backups plus a copy of the
transaction logs to OBS every 15 minutes. When the database fails, PostgreSQL is restored on a new instance, the
microservices are started again and the app is checked, all within 2 hours.

## Alternatives considered

- Keep only the daily backup used today: it cannot meet an RPO of 15 minutes, because a failure could lose up to
  1,440 minutes of bookings.
- Primary/standby in two availability zones: it recovers in minutes and loses almost nothing, but keeping a second
  active instance raises the cost considerably, and our targets do not need it.

## Consequences

- Positive: the RPO of 15 minutes and the RTO of 2 hours are met at a much lower infrastructure cost than a standby
  database.
- Negative: the restore must be tested regularly; if it is never practised, nobody knows if it really takes less than
  two hours.
