# PM Project List Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the PM projects list with compact dialog filters, list/grid views, and no fake progress data.

**Architecture:** Keep `PMProjectsPage` as the state and data owner. Move filter selection into a project-specific dialog component, keep toolbar controls compact, and render either the existing table view or a new grid view from the same `PMProjectListItem` model. No backend API changes are included.

**Tech Stack:** Next.js 15, React 19, TypeScript, RTK Query, Tailwind CSS, shared Shadcn-style UI primitives.

---

## File Structure

- Modify `serp_web/src/modules/pm/pages/PMProjectsPage.tsx` to own `viewMode`, filter dialog state, and conditional list/grid rendering.
- Modify `serp_web/src/modules/pm/components/projects/PMProjectListToolbar.tsx` to show search, filter trigger, sort, view toggle, result count, and clear action.
- Create `serp_web/src/modules/pm/components/projects/PMProjectListFilters.tsx` for status/category dialog filters.
- Create `serp_web/src/modules/pm/components/projects/PMProjectListGrid.tsx` for responsive project cards.
- Modify `serp_web/src/modules/pm/components/projects/PMProjectListRow.tsx` to remove fake progress calculations and UI.
- Modify `serp_web/src/modules/pm/types/project-list.types.ts` to add `PMProjectViewMode`.

## Testing Note

`serp_web` currently has no checked-in frontend test framework or supported `test` script. Do not invent test commands. Verify with lint, type-check, and format checks.

### Task 1: Add Shared View Type

**Files:**

- Modify: `serp_web/src/modules/pm/types/project-list.types.ts`

- [ ] Add `export type PMProjectViewMode = 'list' | 'grid';`.
- [ ] Confirm no existing type names conflict by running `rg "PMProjectViewMode" serp_web/src/modules/pm`.

### Task 2: Build Dialog Filters

**Files:**

- Create: `serp_web/src/modules/pm/components/projects/PMProjectListFilters.tsx`

- [ ] Implement a client component that accepts `open`, `categoryFilter`, `categoryOptions`, `statusFilter`, `onOpenChange`, `onCategoryFilterChange`, `onStatusFilterChange`, and `onClear`.
- [ ] Use a two-column dialog with criterion buttons on the left and values on the right, matching the shape of `PMWorkItemListFilters.tsx`.
- [ ] Support criteria `status` and `category`.
- [ ] Show active-count badges for selected status/category filters.
- [ ] Use only existing shared UI primitives.

### Task 3: Compact Toolbar

**Files:**

- Modify: `serp_web/src/modules/pm/components/projects/PMProjectListToolbar.tsx`

- [ ] Remove inline status/category controls.
- [ ] Add `viewMode`, `onViewModeChange`, `activeFilterCount`, and `onOpenFilters` props.
- [ ] Keep search and sort visible.
- [ ] Add a filter button with `SlidersHorizontal` and active-count badge.
- [ ] Add list/grid icon buttons using Lucide icons.
- [ ] Keep clear filters available when filters/search are active.

### Task 4: Remove Fake Progress From Table Row

**Files:**

- Modify: `serp_web/src/modules/pm/components/projects/PMProjectListRow.tsx`

- [ ] Remove `Progress` import.
- [ ] Remove `useMemo` import.
- [ ] Remove deterministic `mockProgress`.
- [ ] Replace category/status/progress cell with real category and status display only.

### Task 5: Add Grid View

**Files:**

- Create: `serp_web/src/modules/pm/components/projects/PMProjectListGrid.tsx`

- [ ] Render responsive project cards for the same project list data.
- [ ] Include loading and empty states equivalent to table view.
- [ ] Include pagination footer equivalent to table view.
- [ ] Reuse open/edit/archive/unarchive action behavior in card actions.

### Task 6: Wire Page State

**Files:**

- Modify: `serp_web/src/modules/pm/pages/PMProjectsPage.tsx`

- [ ] Add `filterOpen` and `viewMode` local state.
- [ ] Compute active filter count from status/category.
- [ ] Render `PMProjectListFilters`.
- [ ] Pass compact-toolbar props.
- [ ] Render table for `list` and grid for `grid`.
- [ ] Keep existing API query parameters unchanged.

### Task 7: Verify

**Files:**

- Check: `serp_web`

- [ ] Run `npm run lint`.
- [ ] Run `npm run type-check`.
- [ ] Run `npm run format:check`.
- [ ] If formatting fails only for touched files, run `npx prettier --write` on those files and re-run checks.
