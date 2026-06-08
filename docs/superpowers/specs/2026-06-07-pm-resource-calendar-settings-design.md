# PM Resource Calendar Settings Design

## Context

PM optimization already consumes `resource_calendar_slots` as the read model for
resource capacity. The current backend can resolve capacity from persisted slots
and falls back to a weekday 8-hour calendar when a user has no slot coverage.

The missing product capability is a PM settings UI and backend API for
workspace-level resource calendar configuration. Projects should reuse this
workspace configuration; project settings should not own calendar definitions.

## Decisions

- Calendar configuration is workspace/PM settings-level and tenant-scoped.
- MVP uses calendar profiles with user assignment.
- Weekly working blocks support a capacity factor.
- User exceptions support unavailable time and capacity override.
- Persisted `resource_calendar_slots` remain the materialized read model for
  optimization.
- Saving profile, assignment, or exception changes materializes slots
  immediately for a rolling window.
- A scheduled refresh keeps the rolling window populated.
- Materialization replaces old generated slots with hard deletes, not soft
  deletes.
- PM settings/admin permission owns edits for MVP.
- Backend should introduce a separate `domain.resourcecalendar` capability
  instead of expanding `domain.optimization` with settings concerns.

## Backend Design

Add resource calendar settings as a new capability in `pm_core`.

Core tables:

- `resource_calendar_profiles`
  - `tenant_id`, `name`, `description`, `timezone`, `is_default`
  - Represents reusable calendars such as "VN Full-time" or
    "Part-time morning".
- `resource_calendar_profile_blocks`
  - `profile_id`, `day_of_week`, `start_time`, `end_time`, `capacity_factor`
  - A profile can have multiple weekly working blocks.
- `resource_calendar_assignments`
  - `tenant_id`, `user_id`, `profile_id`, `effective_from`, `effective_to`
  - MVP allows only one active assignment per user at a time.
- `resource_calendar_exceptions`
  - `tenant_id`, `user_id`, `exception_type`, `start_at`, `end_at`,
    `capacity_factor`, `reason`
  - `exception_type` is `UNAVAILABLE` or `CAPACITY_OVERRIDE`.

Keep `resource_calendar_slots` as the read-optimized materialized table used by
optimization. The existing `ResourceCalendarService` can continue reading slots
through `IResourceCalendarSlotReadPort`.

Layering:

- `ui.rest.resourcecalendar`: thin REST controllers.
- `application.resourcecalendar.command/query`: transactions, tenant context,
  orchestration, permission checks.
- `domain.resourcecalendar`: entities, validation, materialization service
  interfaces, slot generation rules.
- `infrastructure.store.adapter`: JPA-backed port implementations and slot
  replacement persistence.

## Materialization

When an admin changes profiles, weekly blocks, assignments, or exceptions:

1. Save the configuration in a transaction.
2. Determine affected user IDs.
3. Generate slots for the rolling window, for example today through the next
   90 days.
4. Hard delete previously generated slots for affected users and overlapping
   window where `source IN ('PROFILE', 'EXCEPTION')`.
5. Insert regenerated slots.

External slots must not be deleted by profile materialization. Future external
calendar sync can own its own source value and replacement policy.

Merge rules:

- Weekly profile blocks create base slots.
- `UNAVAILABLE` exceptions remove capacity for the overlapping range and may
  split slots.
- `CAPACITY_OVERRIDE` exceptions replace capacity factor for the overlapping
  range and may split slots.
- Weekly block `capacity_factor` should satisfy `0 < factor <= 1`.
- Capacity override should allow `0 <= factor <= 2` to support unavailable,
  reduced capacity, and overtime.

A scheduled job should refresh active assignments nightly so the rolling window
stays populated. If refresh fails, optimization fallback still protects
generation, but UI should expose stale or missing coverage.

## API Design

Use PM service routes and the existing `GeneralResponse<?>` envelope.

Endpoints:

- `GET /resource-calendar-settings/overview`
- `POST /resource-calendar-profiles`
- `PUT /resource-calendar-profiles/{id}`
- `DELETE /resource-calendar-profiles/{id}`
- `PUT /resource-calendar-profiles/{id}/blocks`
- `PUT /resource-calendar-profiles/{id}/assignments`
- `POST /resource-calendar-exceptions`
- `PUT /resource-calendar-exceptions/{id}`
- `DELETE /resource-calendar-exceptions/{id}`

`POST /resource-calendar-slots/preview` is out of scope for the first MVP. It
can be added later after the CRUD and materialization flow are stable.

## Frontend Design

Add a new section under `serp_web/src/app/pm/settings` named
`Resource calendars`.

Frontend files should follow the existing PM module pattern:

- API: `src/modules/pm/api/resourceCalendarApi.ts`.
- Types: `src/modules/pm/types/resource-calendar-api.types.ts`.
- Components under `src/modules/pm/components/settings/resource-calendar/`.

Suggested components:

- `PMResourceCalendarSettingsSection`
- `PMResourceCalendarProfileTable`
- `PMResourceCalendarProfileDialog`
- `PMResourceCalendarAssignmentPanel`
- `PMResourceCalendarExceptionPanel`

UI areas:

- Profiles: create, edit, delete, set default, show assignment count.
- Weekly blocks: edit day of week, start/end time, capacity factor.
- Assignments and exceptions: assign users to profiles and manage unavailable or
  override periods by user.

MVP should be table/form-based. A complex calendar visual is not required.

The overview should make these states visible:

- Users without a calendar profile.
- Profiles and assignment counts.
- Upcoming exceptions.
- Last materialized time and materialization coverage window.

## Validation And Errors

Backend validation:

- Weekly block start time must be before end time.
- Weekly block capacity factor must be greater than 0 and no more than 1.
- Exception start must be before end.
- `UNAVAILABLE` does not require capacity factor.
- `CAPACITY_OVERRIDE` uses a factor from 0 through 2.
- A user cannot have overlapping active assignments.
- Profile delete fails if active assignments still use it.

Expected errors should use PM domain exceptions and map through the existing
global exception handler.

## Testing

Backend:

- Domain unit tests for weekly block slot generation.
- Domain unit tests for unavailable and override exception split behavior.
- Application tests that profile/block/assignment/exception saves trigger
  materialization for affected users.
- Store adapter tests for hard delete and insert replacement by tenant, user,
  source, and window.
- Existing optimization tests should continue passing because optimization reads
  the same slot read model.

Frontend:

- Run `npm run lint`.
- Run `npm run type-check`.
- Run `npm run format:check`.
- There is no frontend test runner configured today.

## Out Of Scope For MVP

- Project-level calendar overrides.
- User self-service calendar editing and approval workflow.
- HR-grade leave types.
- External calendar sync.
- Slot preview API and preview UI.
- Visual calendar editor.
- Multi-profile assignment overlap policies beyond "no overlap".
