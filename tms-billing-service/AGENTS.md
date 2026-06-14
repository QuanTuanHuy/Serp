# AGENTS.md - TMS Billing Service Guide for Coding Agents

This guide is for coding agents working inside `tms-billing-service/` (shipping fee calculation, tariffs, surcharges, VAS rules).
Use it together with the repository-root `AGENTS.md`. For TMS UI (billing pages), see `serp_web/src/modules/first-mile/AGENTS.md`.

## Service Snapshot

- **Module:** `tms-billing-service` — Spring Boot, package `serp.project.tms_billing_service`.
- **Scope:** Public calculate-shipping-fee API and admin CRUD for pricing rules.
- **Default port:** `8104` (`SERVER_PORT`).
- **Gateway path:** `/tms-billing-service/api/v1/...` (proxied by `api_gateway`).

## File headers (required)

- **Author must be `Nguyen The Anh`** on all new or touched Java/SQL files. Do **not** use `QuanTuanHuy` from the root `AGENTS.md` example.
- When editing a file that already has a header, **preserve** `Author: Nguyen The Anh`.
- New Java files:

```text
/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/
```

- SQL migrations: `-- Author: Nguyen The Anh` when sibling scripts use author comments.

## Build and Test

Run from `tms-billing-service/`. On Windows use `mvnw.cmd`.

```bash
./mvnw clean compile
./mvnw test
./mvnw -Dtest=TieuChuanPricingStrategyTest test
```

## Layering

- `ui/controller/` → `core/service/` → `repository/`
- Pricing strategies under `core/service/impl/`; shared helpers under `core/service/support/`
- Do not expose JPA entities from controllers; use DTOs in `dto/request` and `dto/response`

## Before You Finish

- `./mvnw clean compile` and relevant tests for touched pricing logic.
- Keep `serp_web` `billingApi.ts` and `types/billing.types.ts` in sync when API contracts change.
- Never commit `.env` or secrets.
