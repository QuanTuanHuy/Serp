# PM Skill Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire the PM frontend to existing PM Core skill APIs so users can maintain skill catalog data, user skill profiles, and work item skill requirements for optimization.

**Architecture:** Add a PM-owned skill API slice and typed contracts, then reuse focused PM skill editor components from Settings, People, and Work Item detail. Backend contracts stay unchanged; frontend joins `skillId` records with the loaded skill catalog.

**Tech Stack:** Next.js App Router, React 19, TypeScript, RTK Query, Tailwind, shared Shadcn UI, lucide-react.

---

## File Map

Create:
- `serp_web/src/modules/pm/types/skill-api.types.ts` - skill catalog, user skill, work item skill request/response types.
- `serp_web/src/modules/pm/api/skillApi.ts` - RTK Query endpoints for the existing backend skill APIs.
- `serp_web/src/modules/pm/components/skills/PMSkillCatalogSection.tsx` - Settings catalog table and dialogs.
- `serp_web/src/modules/pm/components/skills/PMUserSkillDialog.tsx` - People row user skill profile editor.
- `serp_web/src/modules/pm/components/skills/PMWorkItemSkillPanel.tsx` - Work item detail sidebar panel and requirement dialog.
- `serp_web/src/modules/pm/components/skills/skill-ui.utils.ts` - small label and row helper functions.

Modify:
- `serp_web/src/lib/store/api/apiSlice.ts` - add PM skill tag types.
- `serp_web/src/modules/pm/types/api.ts` - export skill types.
- `serp_web/src/modules/pm/api/index.ts` - export skill API hooks and types.
- `serp_web/src/modules/pm/components/settings/settings-page.types.ts` - add `skills` setting section.
- `serp_web/src/modules/pm/pages/PMSettingsPage.tsx` - render skill catalog section.
- `serp_web/src/modules/pm/pages/PMProjectPeoplePage.tsx` - add Edit skills action/dialog while preserving existing user-search changes.
- `serp_web/src/modules/pm/components/work-items/detail/PMWorkItemDetailDialog.tsx` - add work item skill requirement panel.
- `serp_web/src/modules/pm/index.ts` - export skill components only if needed by module boundary.

## Tasks

### Task 1: API and Types

- [ ] Add `skill-api.types.ts` with exact backend request/response contracts.
- [ ] Add `skillApi.ts` with catalog, user skill, and work item skill endpoints using `extraOptions: { service: 'pm' }`.
- [ ] Add cache tags: `pm/Skill`, `pm/UserSkill`, `pm/WorkItemSkill`.
- [ ] Export hooks/types through PM API barrels.

### Task 2: Shared Skill UI

- [ ] Add utility functions for proficiency labels, requirement labels, and joining skill records to catalog labels.
- [ ] Add catalog section with create/edit/archive dialogs.
- [ ] Add user skill dialog with add/remove rows, proficiency select, confidence input, and replace save.
- [ ] Add work item skill panel/dialog with requirement type, minimum proficiency, and weight.

### Task 3: Settings Integration

- [ ] Add `skills` to settings section types and navigation.
- [ ] Render the skill catalog section when `section=skills`.
- [ ] Keep existing settings sections unchanged.

### Task 4: People Integration

- [ ] Add `Edit skills` row action.
- [ ] Mount `PMUserSkillDialog` for the selected person.
- [ ] Preserve the existing lazy org-user search behavior in this dirty worktree.

### Task 5: Work Item Detail Integration

- [ ] Add `PMWorkItemSkillPanel` to the work item detail sidebar.
- [ ] Fetch only when the dialog has a real work item id.
- [ ] Invalidate work item skill cache after replace.

### Task 6: Verification

- [ ] Run `npm run lint` from `serp_web`.
- [ ] Run `npm run type-check` from `serp_web`.
- [ ] Run `npm run format:check` from `serp_web`.
- [ ] If formatting fails only for touched files, run Prettier on touched frontend files and repeat checks.

## Self-Review

The plan covers all approved scope: skill catalog in Settings, user skill profiles in People, work item skill requirements in detail, and RTK Query wiring. Frontend tests are not listed because this repo has no checked-in frontend test runner.
