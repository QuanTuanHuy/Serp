# CRM Opportunity Display Enrichment Design

## Context

The current CRM opportunities UI has a useful pipeline/list/detail structure, but the displayed data is too sparse for daily sales work. Backend opportunity responses already include core deal fields such as stage, estimated value, actual value, probability, close dates, account id, lead id, assignee id, notes, loss reason, reopen reason, and metadata. The frontend maps these into cards and detail tabs, but key relationship data still appears as fallback labels such as `Account #id` and `User #id`.

The target audience for this phase is both sales reps and sales managers:

- Sales reps need to know which opportunities need action today, which ones are overdue, and whether a next activity exists.
- Sales managers need enough pipeline context to scan value, weighted value, stage health, owner, and risk signals without opening every opportunity.

This phase uses a light backend enrichment approach. It improves the payload and display without adding new database tables, forecasting logic, products, competitors, or a full opportunity history engine.

## Goals

- Make opportunity lists and pipeline cards useful as a sales cockpit.
- Show real account, owner, and linked lead names where available.
- Add activity health signals so stale or neglected deals stand out.
- Keep the detail page focused on deal context and next action readiness.
- Reuse existing frontend components and backend enrichment patterns where possible.

## Non-Goals

- No database schema migration for this phase.
- No product-line integration for opportunity products.
- No competitors, tags, forecast categories, or manager scoring engine.
- No dedicated timeline/history API. The activities tab remains the source of real chronological context.
- No split `nextTaskAt` / `nextMeetingAt`; this phase uses one generic `nextActivityAt`.

## Backend Contract

Extend `OpportunityResponse` with display-only enrichment fields:

```ts
accountName?: string;
assignedToName?: string;
leadName?: string;
lastActivityAt?: number;
nextActivityAt?: number;
openActivityCount?: number;
overdueActivityCount?: number;
```

These fields are derived at response time and are not persisted on the `opportunities` table.

### Field Semantics

- `accountName`: name of the linked account. Frontend falls back to `Account #id` if missing.
- `assignedToName`: display name of the assigned owner. Frontend falls back to `User #id` or `Unassigned`.
- `leadName`: name of the linked lead, if the opportunity has `leadId`.
- `lastActivityAt`: most recent related activity timestamp. Prefer `activityDate`, fallback to `dueDate`, then `updatedAt`.
- `nextActivityAt`: nearest future related activity timestamp for an activity that is not `COMPLETED` or `CANCELLED`.
- `openActivityCount`: count of related activities that are not `COMPLETED` or `CANCELLED`.
- `overdueActivityCount`: count of open related activities whose `activityDate` or `dueDate` is before now.

### Backend Flow

Add an enrichment step in `OpportunityUseCase` after mapping `OpportunityEntity` to `OpportunityResponse`.

Apply enrichment consistently to:

- `GET /api/v1/opportunities/{id}`
- `POST /api/v1/opportunities/search`
- `GET /api/v1/opportunities/pipeline`

Use the existing batch-enrichment style already present in CRM use cases:

- Batch load account names from account ids.
- Batch load lead names from lead ids.
- Resolve owner names from existing user/team-member profile mechanisms.
- Load related activities for the returned opportunity ids and compute activity summary fields.

The detail endpoint can enrich a single response through the same helper path with a one-item list to keep behavior consistent.

## Frontend Data Model

Extend `Opportunity` in `serp_web/src/modules/crm/types/opportunity.ts` with the new optional fields:

```ts
accountName?: string;
assignedToName?: string;
leadName?: string;
lastActivityAt?: string;
nextActivityAt?: string;
openActivityCount?: number;
overdueActivityCount?: number;
```

The RTK Query mapper should convert backend timestamps into the frontend's existing date string convention. Existing fallback behavior remains, but it should prefer enriched names:

- account display: `accountName || customerName || Account #accountId`
- owner display: `assignedToName || User #assignedTo || Unassigned`
- lead display: `leadName || Lead #leadId`

## Opportunity List and Pipeline Display

Each opportunity card should expose three layers of information.

### Identity

- Opportunity name
- Account name
- Owner name

### Commercial Signal

- Estimated value
- Weighted value
- Probability
- Stage
- Expected close date with overdue or due-soon state

### Action Signal

- `nextActivityAt`: show as the next action date.
- Missing `nextActivityAt` on an active opportunity: show `No next action`.
- `openActivityCount`: show compactly, such as `3 open`.
- `overdueActivityCount > 0`: show an overdue activity badge.

Pipeline cards should remain compact and add only one action signal line. Grid/list cards may show the full set of badges and counts.

## Opportunity Detail Display

Render the existing `OpportunityDealMetricsStrip` below the header so deal value, weighted value, probability, and days to close are visible before the tabs.

Update the overview tab into these conceptual sections:

- Deal Summary: stage, estimated value, weighted value, probability, expected close date, actual value/date when present.
- Account & Owner: account name, owner name, linked lead name.
- Activity Health: last activity, next activity, open activity count, overdue activity count.
- Description & Notes: existing description and notes.
- Close Outcome: loss reason, reopen reason, actual close information when relevant.

The activities tab remains the detailed source for activity rows.

## Badge Rules

Use badges sparingly and prioritize operational signals:

- `overdueActivityCount > 0`: red badge, highest priority.
- Active opportunity with no `nextActivityAt`: amber badge, `No next action`.
- Active opportunity with `expectedCloseDate` before today: red badge, `Close overdue`.
- Active opportunity with `expectedCloseDate` within 7 days: neutral or amber badge, `Closing soon`.
- High value: optional badge using a fixed initial threshold, for example VND 100,000,000, until the product has a better percentile-based rule.

Closed opportunities should not show `No next action` as a problem.

## Error Handling and Fallbacks

- Missing enrichment fields must not break current UI.
- Name fields fall back to id-based labels.
- Activity summary fields default to empty or zero.
- If backend enrichment partially fails for profile/account/lead names, still return base opportunity data.
- If activity summary cannot be computed, omit activity fields rather than failing the opportunity list.

## Testing

Backend:

- Unit test opportunity enrichment for list responses.
- Unit test single opportunity enrichment.
- Unit test activity summary logic for no activities, future activities, overdue open activities, completed activities, and cancelled activities.
- Regression test that pipeline responses include enriched opportunities.

Frontend:

- Type-check mapper changes.
- Verify card display fallback behavior when enriched names are absent.
- Verify badges for overdue activities, missing next action, close overdue, and closing soon.
- Verify detail page renders the metrics strip and activity health section.

## Implementation Notes

- Keep the backend enrichment helper private to `OpportunityUseCase` unless reuse pressure appears.
- Avoid new persistent fields and migrations.
- Avoid broad refactors in opportunity cards; add small formatting helpers where needed.
- Do not implement Products or Timeline tabs in this phase beyond their existing placeholder behavior.
