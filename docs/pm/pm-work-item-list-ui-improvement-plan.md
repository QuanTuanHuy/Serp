# PM Work Item List UI Improvement Plan

## Scope

Target screen:

- `serp_web/src/modules/pm/components/work-items/list/PMWorkItemListTab.tsx`

Reference direction:

- JIRA-style work item workspace from provided screenshots

Main goals:

1. Turn current page-like layout into workbench-like layout.
2. Replace always-visible filter block with focused filter workflow.
3. Improve scan speed in list view.
4. Improve detail view structure without route change.
5. Add backend support only where current data sources are weak.

Non-goals for this plan:

1. Full JQL builder.
2. Saved filters persistence.
3. Hierarchical tree list.
4. Permissions redesign.
5. Large domain refactor outside work item list flow.

## Current State Summary

Current implementation already has:

1. Search by keyword.
2. URL-driven list/detail mode.
3. URL-driven issue selection.
4. Basic filters for:
   - parent
   - assignee
   - work type
   - status
   - priority
   - reporter
5. Project-scoped backend list endpoints for:
   - statuses
   - priorities
   - issue types

Current gaps:

1. Filter UI takes too much page space.
2. Header still feels like standard page header, not command bar.
3. Detail view still reads like one content card, not 3-zone workspace.
4. Assignee/reporter options still use organization users, not project-scoped people.
5. Parent options reuse full search payload, not lightweight option endpoint.
6. Visual density and interaction hierarchy still weaker than JIRA reference.

## Delivery Strategy

Implement in small phases. Each phase should be mergeable on its own.

Recommended sequence:

1. Stabilize structure and supporting helpers.
2. Rebuild header into command bar.
3. Rebuild filter UX.
4. Improve list workspace.
5. Improve detail workspace.
6. Add backend support for filter sources.
7. Final polish and verification.

## Phase 0 - Baseline And Guardrails

### Goal

Freeze current behavior and define safe change boundaries.

### Tasks

1. Review current responsibilities inside `PMWorkItemListTab.tsx`.
2. Identify pure helper logic that should stay in same file first.
3. Document URL contract currently in use:
   - `view`
   - `issueId`
   - `q`
   - `parentId`
   - `assigneeIds`
   - `issueTypeIds`
   - `statusIds`
   - `priorityIds`
   - `reporterIds`
4. Confirm no route change needed.

### Files

1. `serp_web/src/modules/pm/components/work-items/list/PMWorkItemListTab.tsx`

### Done Criteria

1. Current URL behavior understood and preserved.
2. No visual change yet.

## Phase 1 - Internal Refactor For Readability

### Goal

Reduce risk before visual redesign.

### Tasks

1. Split large file into local subcomponents in same module folder.
2. Extract shared helpers for:
   - query param parse
   - query param serialize
   - active filter count
   - date formatting
   - avatar initials
3. Keep behavior unchanged.
4. Keep state URL-driven.

### Suggested File Split

1. `PMWorkItemListTab.tsx`
2. `PMWorkItemListFilters.tsx`
3. `PMWorkItemListTable.tsx`
4. `PMWorkItemCompactList.tsx`
5. `PMWorkItemDetailPanel.tsx`
6. `pmWorkItemList.utils.ts`

### Notes

1. Do not create deep folder tree unless needed.
2. Keep types near feature.

### Done Criteria

1. File size reduced.
2. No UX change.
3. Lint and type-check still pass.

## Phase 2 - Convert Header Into Command Bar

### Goal

Make top section feel like operations toolbar, not page intro.

### Tasks

1. Remove heavy page-title emphasis.
2. Build compact command row with:
   - search input
   - filter trigger button
   - view switch
   - refresh button
   - optional result count or active filter count
3. Move descriptive text out or reduce to subtle breadcrumb/label.
4. Ensure row wraps well on narrow screens.

### UX Requirements

1. Data should appear earlier on viewport.
2. Search remains always visible.
3. Filter trigger becomes primary entry point for filtering.

### Files

1. `PMWorkItemListTab.tsx`
2. optional new `PMWorkItemCommandBar.tsx`

### Done Criteria

1. Top area height reduced.
2. Search and actions still obvious.
3. Mobile wrap remains usable.

## Phase 3 - Rebuild Filter UX

### Goal

Replace current multi-dropdown block with single focused filter surface.

### Preferred Direction

One `Filter` trigger opens one larger popover, sheet, or drawer.

### Tasks

1. Replace always-visible filter card.
2. Add filter trigger near search.
3. Open 2-column filter surface:
   - left column: filter criteria list
   - right column: options for selected criterion
4. Add tabs or modes only if low cost:
   - `Basic`
   - reserve `Advanced` and `JQL` for later, disabled or hidden for now
5. Add active filter chips under command bar.
6. Add `Clear all` inside filter surface and near active chips if useful.

### Filter Surface Requirements

1. Parent: searchable single-select.
2. Assignee: searchable multi-select.
3. Work type: searchable multi-select.
4. Status: searchable multi-select.
5. Priority: searchable multi-select.
6. Reporter: searchable multi-select.
7. Show count for current criterion.
8. Show loading and empty states inside right pane.

### Technical Notes

1. Keep selected values in URL.
2. Keep filter UI local state only for current open panel and search text.
3. Fetch options lazily when criterion opens.
4. Preserve currently selected options even if not in first page of fetched options.

### Candidate UI Primitives

1. `Popover`
2. `Dialog`
3. `ScrollArea`
4. `Input`
5. `Badge`

### Done Criteria

1. Main page no longer shows large filter block.
2. Filtering remains fully functional.
3. Active filters visible without reopening panel.

## Phase 4 - Improve List View Scanability

### Goal

Make list view faster to scan and closer to issue tracker mental model.

### Tasks

1. Rebalance columns for information density.
2. Merge key and summary into stronger `Work` presentation if useful.
3. Reduce secondary text noise.
4. Improve selected row style.
5. Add row hover affordance.
6. Consider inline status editing only if existing mutation API already available.
7. Keep bulk checkbox column only if real bulk actions exist soon. Otherwise defer.

### UX Direction

1. Work column should lead eye first.
2. Status and priority should read fast.
3. Assignee should consume less horizontal space.
4. Due date and updated date should be compact and consistent.

### Optional Enhancements

1. Result count in table header.
2. Sort indicator if sortable columns added.
3. Sticky table header if scroll container supports it.

### Done Criteria

1. Table denser but still readable.
2. Visual hierarchy improved.
3. No regression in click-to-select behavior.

## Phase 5 - Improve Detail Workspace

### Goal

Make detail mode feel like workspace, not large info card.

### Preferred Direction

3-zone layout in detail mode:

1. left list navigator
2. center content
3. right metadata panel

### Tasks

1. Keep current split-pane route and URL behavior.
2. Change detail area from single card to internal 2-column content + meta layout.
3. Put in center column:
   - key
   - summary
   - description
   - child items or linked items placeholders when available
4. Put in right column:
   - status
   - issue type
   - priority
   - assignee
   - reporter
   - due date
   - updated
5. Add sticky or semi-sticky metadata panel on wide screens.
6. Tighten loading skeletons to match final shape.

### Notes

1. Do not invent unavailable domain sections if data not ready.
2. Use placeholders only when they map to upcoming real sections.

### Done Criteria

1. Detail mode clearly separated into content and metadata.
2. Layout remains usable on laptop widths.
3. Mobile gracefully stacks.

## Phase 6 - Backend Support For Better Filter Sources

### Goal

Stop overfetching and improve relevance of filter options.

### Problem Areas

1. Assignee/reporter list currently uses organization users.
2. Parent list currently reuses full work item search response.

### Backend Additions

#### 6.1 Project People Options Endpoint

Purpose:

- return users relevant to project for assignee/reporter filters

Suggested shape:

`GET /projects/{projectId}/people/options?search=&page=0&pageSize=20`

Suggested item DTO:

```json
{
  "id": 123,
  "displayName": "Nguyen Van A",
  "email": "a@example.com",
  "avatarUrl": "..."
}
```

Possible source strategies:

1. project role actors
2. project members table if exists
3. fallback to organization users only if no project-scoped source exists

#### 6.2 Parent Work Item Options Endpoint

Purpose:

- lightweight search for parent selector

Suggested shape:

`GET /projects/{projectId}/work-items/options?search=&page=0&pageSize=20`

Suggested item DTO:

```json
{
  "id": 456,
  "key": "K1-4",
  "summary": "Project Management",
  "issueTypeName": "Epic",
  "statusName": "In Progress"
}
```

#### 6.3 Optional Label/Version/Component Endpoints

Only if next filter wave includes them.

### Frontend Tasks After BE Ready

1. Add RTK Query endpoints.
2. Replace current org-users query for assignee/reporter.
3. Replace current search reuse for parent selector.
4. Add per-criterion search input debounce.

### Done Criteria

1. Filter options more relevant.
2. Smaller payloads.
3. Better scale on large organizations.

## Phase 7 - Active Filter Chips And State Polish

### Goal

Make current filter state easy to inspect and edit.

### Tasks

1. Show chips for active filters below command bar.
2. Each chip supports remove action.
3. Add summary chip for parent if selected.
4. If labels become too long, show count summary like `Assignee: 3`.
5. Keep chip text stable and readable.

### Data Requirement

1. Need label maps for selected ids.
2. If selected option not loaded yet, show fallback like `Status: 2 selected`.

### Done Criteria

1. User can inspect active filters at glance.
2. User can remove one filter without reopening panel.

## Phase 8 - Performance And UX Polish

### Goal

Reduce jitter and improve perceived speed.

### Tasks

1. Use `useDeferredValue` for search inputs where useful.
2. Avoid fetching all filter sources at once.
3. Keep previous data during list refresh when possible.
4. Avoid layout jumps on loading.
5. Add stable heights to scrollable panes.
6. Review keyboard access for:
   - filter open/close
   - criterion selection
   - row selection

### Done Criteria

1. Less flicker.
2. Better keyboard flow.
3. Large project interaction still acceptable.

## Phase 9 - Final Verification

### Frontend Commands

Run from `serp_web/`:

```bash
npm run lint
npm run type-check
npm run format:check
npm run build
```

### Backend Commands

Run from `pm_core/` if backend changes added:

```bash
./mvnw.cmd clean compile
./mvnw.cmd test
```

### Manual QA Checklist

1. Search updates URL correctly.
2. Filter open/close works on desktop.
3. Filter state persists on refresh.
4. Detail mode keeps selected issue in URL.
5. Switching list/detail preserves filter state.
6. Empty result state looks intentional.
7. Long names do not break layout.
8. Mobile stacking still usable.
9. No duplicate network calls from filter open/close loops.

## Suggested Execution Order For Next Sessions

If work will continue step by step across multiple sessions, use this exact order:

1. Phase 1: internal refactor only.
2. Phase 2: command bar.
3. Phase 3: unified filter surface.
4. Phase 7: active chips.
5. Phase 4: list density improvements.
6. Phase 5: detail workspace improvements.
7. Phase 6: backend option endpoints.
8. Phase 8: performance polish.
9. Phase 9: final verification.

Reason:

1. Early phases improve structure and UX fastest.
2. Backend additions should follow confirmed frontend need, not lead it.
3. Chips should land right after filter redesign, while filter state model is fresh.

## Open Decisions To Confirm Before Build

1. Filter surface type:
   - popover
   - side sheet
   - modal dialog
2. Whether to expose placeholder tabs for `Advanced` and `JQL` now.
3. Whether project-scoped people source already exists in backend.
4. Whether inline status editing should be included in this redesign or deferred.
5. Whether saved filters are in current roadmap or later.

## Minimal First Delivery Recommendation

If smallest high-value release needed first, do only:

1. Phase 1
2. Phase 2
3. Phase 3
4. Phase 7

That already gives:

1. better top bar
2. better filter workflow
3. less page clutter
4. stronger JIRA-like interaction pattern

## Change Log

### 2026-05-10

1. Initial detailed step-by-step plan created.
2. Based on current `PMWorkItemListTab.tsx` state and JIRA screenshot analysis.
