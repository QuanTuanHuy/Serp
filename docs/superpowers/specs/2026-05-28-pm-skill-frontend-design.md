# PM Skill Frontend Design

## Context

`pm_core` already exposes the skill catalog and assignment endpoints needed by the PM optimization flow:

- `GET/POST/PATCH/DELETE /api/v1/skills`
- `GET/PUT /api/v1/users/{userId}/skills`
- `GET/PUT /api/v1/projects/{projectId}/work-items/{workItemId}/skills`

The PM frontend currently consumes optimization run skill-fit fields, but it does not let users maintain the skill data that feeds those recommendations.

## Scope

Build frontend wiring for the existing backend skill APIs.

The v1 feature includes:

- PM Settings skill catalog management.
- Project People user skill profile editing.
- Work item skill requirement editing in the detail dialog.
- RTK Query hooks, cache tags, and PM module types for these endpoints.
- Client-side joining of `skillId` responses with the skill catalog for labels.

The v1 feature excludes backend API changes, bulk import, skill suggestions, automatic skill extraction, and changes to the optimization solver.

## Frontend Design

Add a PM-owned skill API slice:

- `serp_web/src/modules/pm/api/skillApi.ts`
- `serp_web/src/modules/pm/types/skill-api.types.ts`

RTK Query endpoints:

- `getPmSkills`
- `createPmSkill`
- `updatePmSkill`
- `archivePmSkill`
- `getPmUserSkills`
- `replacePmUserSkills`
- `getPmWorkItemSkills`
- `replacePmWorkItemSkills`

Add shared PM skill UI:

- `PMSkillSelector` for choosing catalog skills.
- `PMSkillCatalogTable` for Settings.
- `PMUserSkillDialog` for user profiles.
- `PMWorkItemSkillPanel` and `PMWorkItemSkillDialog` for work item requirements.

PM Settings gets a new `skills` section in the settings shell. It lists active skills, supports create/edit/archive, and uses the same PM settings visual density as work types, workflows, and priorities.

Project People adds an `Edit skills` row action. The dialog lists the selected user's skills with proficiency, confidence, and catalog labels. Saving replaces the user's active skill profile.

Work Item detail adds a `Skills` panel in the right sidebar. It shows required/preferred requirements and opens a dialog to replace requirements with skill, requirement type, minimum proficiency, and weight.

## Data Flow

1. PM pages load skill catalog through `getPmSkills`.
2. People row action opens `PMUserSkillDialog`, which fetches `getPmUserSkills(userId)`.
3. Work item detail opens `PMWorkItemSkillPanel`, which fetches `getPmWorkItemSkills(projectId, workItemId)`.
4. Dialog edits local rows and submits a replace payload.
5. RTK Query invalidates user/work item skill tags and optimization-relevant work item cache.
6. Optimization generation reads backend skill data through existing `pm_core` logic.

## Error Handling

All mutations use `.unwrap()` in `try/catch`, normalize errors with `getErrorMessage(error)`, and show toast feedback. Empty rows are allowed and intentionally replace the server-side profile/requirements with an empty set.

## Testing

The frontend currently has no configured test runner. Verification uses:

- `npm run lint`
- `npm run type-check`
- `npm run format:check`

No backend verification is required unless backend files are changed.

## Open Decisions

None. The implementation keeps backend contracts unchanged and joins skill labels client-side.
