# PM Work Item Board UI Improvement Plan

## Scope

Target screen:

- `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoard.tsx`
- `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoardColumn.tsx`
- `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoardCard.tsx`

Reference direction:

- JIRA-style project board from provided screenshot
- Compact command bar instead of page-like header
- Faster scanning across columns and cards

Main goals:

1. Turn current board into a denser workbench-style board.
2. Reuse the existing board API filter capability that is not exposed in UI yet.
3. Make toolbar, columns, and cards feel closer to JIRA mental model without cloning it exactly.
4. Keep first delivery mostly frontend-only.
5. Keep changes incremental and mergeable.

Non-goals for this plan:

1. Full drag-and-drop ranking implementation.
2. Saved filters, shared filters, or JQL builder.
3. Swimlanes, epic panels, or sprint management.
4. Full work item detail drawer redesign.
5. Broad domain refactor outside board flow.

## Current State Summary

Current implementation already has:

1. Project-scoped board route at `src/app/pm/projects/[projectId]/(detail)/board/page.tsx`.
2. Board data query via `useGetPmWorkItemBoardQuery(...)`.
3. Backend support for board filters:
   - `keyword`
   - `statusIds`
   - `assigneeIds`
   - `issueTypeIds`
   - `priorityIds`
4. Column rendering split into dedicated components.
5. Basic loading, error, and empty states.

Current gaps:

1. Top area still feels like generic page header, not board command bar.
2. Only keyword search is visible even though board API already supports more filters.
3. Active filters are not visible as chips.
4. Board container still reads as card-inside-card instead of workspace surface.
5. Column header hierarchy is weak compared to JIRA reference.
6. Card metadata still feels technical and under-enriched:
   - assignee shows numeric id instead of real user presentation
   - issue type and priority icon fields exist in types but are not used
   - card hover/click affordance is weak
7. Data shape may still be insufficient for truly JIRA-like cards if avatar/name data is required.

## Decision

Default path:

1. Ship a frontend-first board refresh first.
2. Reuse current board endpoint and current query params.
3. Reuse patterns from the PM list filter experience where it helps.
4. Do not expose status filter in board toolbar because board already groups work by status.
5. Add backend support only if card enrichment or filter sources are blocked by missing data.

Why:

1. Current endpoint already supports the most important board filters.
2. Board columns already act as the primary status segmentation.
3. The biggest weakness today is UI structure and information density, not routing.
4. Shipping the board shell first reduces risk before adding richer data or interactions.

## Delivery Strategy

Implement in small phases. Each phase should be mergeable on its own.

Recommended sequence:

1. Lock URL and state contract for board filters.
2. Rebuild the top area into a compact board toolbar.
3. Add focused filter UX and active filter chips.
4. Rework the board surface and column structure.
5. Rework board cards for faster scanning.
6. Add backend enrichment only if real UI gaps remain.
7. Final polish and verification.

## Phase 0 - Baseline And URL Contract

### Goal

Define how board state should be represented before visual changes.

### Tasks

1. Review current responsibilities in `PMWorkItemBoard.tsx`.
2. Decide whether board keyword and filters should move to URL search params like list view.
3. Document target board query contract:
   - `q`
   - `assigneeIds`
   - `issueTypeIds`
   - `priorityIds`
4. Decide which state stays local only:
   - filter panel open state
   - temporary option search text
5. Confirm no route change is needed.

### Recommendation

1. Keep board filters URL-driven for shareability and consistency with list view.
2. Keep toolbar interaction state local.

### Files

1. `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoard.tsx`
2. optional new `serp_web/src/modules/pm/components/work-items/board/pmWorkItemBoard.utils.ts`

### Done Criteria

1. Board query contract is clear.
2. No visual change yet.

## Phase 1 - Convert Header Into Board Toolbar

### Goal

Replace the current page-like intro card with a compact board command bar.

### Tasks

1. Remove the large title treatment and descriptive header block.
2. Build a compact top toolbar with:
   - search input
   - filter trigger button
   - optional quick type filter trigger
   - refresh button
   - lightweight result summary or active filter count
3. Keep toolbar layout horizontally efficient on desktop.
4. Make toolbar wrap cleanly on smaller widths.
5. Consider sticky behavior when board scrolls vertically.

### UX Requirements

1. Work columns should appear earlier in viewport.
2. Search must remain visible at all times.
3. Toolbar must feel operational, not editorial.

### Files

1. `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoard.tsx`
2. optional new `PMWorkItemBoardToolbar.tsx`

### Done Criteria

1. Top area height is reduced.
2. Search and refresh remain obvious.
3. Layout still works on mobile and laptop widths.

## Phase 2 - Add Board Filter UX

### Goal

Expose the existing API filter capability through a focused board filter workflow.

### Preferred Direction

One primary `Filter` trigger opens one compact but capable filter surface.

### Tasks

1. Add filter trigger in the toolbar.
2. Support these criteria in first pass:
   - assignee
   - work type
   - priority
3. Reuse PM list filter interaction patterns where practical.
4. Show active filter chips below the toolbar.
5. Add `Clear all` near chips and inside filter surface.
6. Keep selected values synchronized with URL and board query.

### Filter Surface Requirements

1. Lazy-load options only when the panel opens or criterion becomes active.
2. Show counts for active values.
3. Show loading and empty states.
4. Preserve selected values when fetched option pages change.
5. Avoid taking permanent vertical space in the page.

### Technical Notes

1. Board can reuse the same RTK Query endpoints already used by list filters.
2. Status filter should stay out of the board toolbar unless a later use case proves it is needed.
3. Prefer minimal duplication, but do not force full component sharing if it makes board UX awkward.
4. Keep board-specific query serialization small and explicit.

### Candidate UI Primitives

1. `Dialog` or `Popover`
2. `ScrollArea`
3. `Input`
4. `Badge`
5. `Button`

### Files

1. `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoard.tsx`
2. optional new `PMWorkItemBoardFilters.tsx`
3. optional shared helper reuse from `work-items/list/`

### Done Criteria

1. Board filtering works beyond keyword.
2. Active filters are visible without reopening the filter surface.
3. No large always-visible filter block is introduced.

## Phase 3 - Rebuild Board Surface And Columns

### Goal

Make the board read like a workspace instead of a bordered content card.

### Tasks

1. Reduce heavy outer card framing around the entire board.
2. Improve horizontal scrolling behavior and spacing between columns.
3. Rework column shell for stronger hierarchy:
   - clearer status title
   - stronger count badge
   - quieter status category label
4. Make column header sticky and visually lighter.
5. Revisit empty-column placeholder so it is less visually noisy.
6. Keep column height tuned for laptop viewports.

### UX Direction

1. Columns should feel like lightweight lanes.
2. Counts should be readable at a glance.
3. Empty columns should not dominate visual attention.

### Optional Enhancements

1. Add subtle status-category accent color.
2. Add per-column quick action placeholder only if there is a real next use.
3. Add gradient edge hint for horizontal overflow if needed.

### Files

1. `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoard.tsx`
2. `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoardColumn.tsx`
3. `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoardSkeleton.tsx`

### Done Criteria

1. Board surface feels less like nested cards.
2. Column headers are easier to scan.
3. Loading skeleton still matches new visual structure.

## Phase 4 - Rebuild Board Cards

### Goal

Make each card faster to scan and closer to issue-tracker expectations.

### Tasks

1. Reorder card hierarchy so key metadata reads more naturally:
   - issue type hint
   - key
   - summary
   - compact footer metadata
2. Use issue type icon or fallback indicator if available.
3. Use priority icon/color more intentionally.
4. Replace `Assignee {id}` treatment with better presentation:
   - avatar if data becomes available
   - initials or compact fallback if not
5. Make due date styling more semantic:
   - normal
   - due soon
   - overdue
6. Tune hover, focus, and click affordance.
7. Keep card density high enough to fit more work on screen.

### Current Data Limits To Watch

1. Board card currently has `assigneeId` but no guaranteed assignee display name.
2. `issueType.iconUrl` and `priority.iconUrl` exist in types but may not be reliable yet.
3. If enrichment is missing, first pass should still improve layout using current data.

### UX Direction

1. Summary is the primary line of sight.
2. Metadata should support the summary, not compete with it.
3. The card should feel actionable even before drag-and-drop exists.

### Files

1. `serp_web/src/modules/pm/components/work-items/board/PMWorkItemBoardCard.tsx`
2. optional small helper file for date and badge formatting

### Done Criteria

1. Cards are more compact and scannable.
2. Assignee and priority are easier to understand.
3. No regression in current card rendering for sparse data.

## Phase 5 - Backend Enrichment Only If Needed

### Goal

Add backend support only if the refreshed board still lacks critical card or filter data.

### Likely Triggers

1. Board needs assignee display name and avatar, not just numeric id.
2. Board needs reporter, labels, epic, or component hints for planned card variants.
3. Board filter sources need project-scoped people rather than organization-wide users.
4. Board cards need richer status or issue type icon metadata than current payload provides.

### Candidate Backend Additions

1. Enrich board card DTO with:
   - assignee display name
   - assignee avatar url
   - reporter display name if needed
2. Add lightweight option endpoints for project people if current source is noisy.
3. Add board-specific aggregation only if the UI needs server-computed counts.

### Default Recommendation

1. Do not expand backend before phase 1 to phase 4 are complete.
2. Let UI gaps prove the backend need first.

### Possible Files If BE Changes

1. `pm_core/src/main/java/serp/project/pmcore/ui/rest/workitem/*`
2. `pm_core/src/main/java/serp/project/pmcore/application/workitem/query/board/*`
3. `serp_web/src/modules/pm/types/api.ts`
4. `serp_web/src/modules/pm/api/workItemApi.ts`

### Done Criteria

1. Any backend change is justified by a concrete UI gap.
2. Frontend no longer relies on technical placeholders for key user-facing fields.

## Phase 6 - Polish And Verification

### Goal

Stabilize the new board experience and verify it holds up across states.

### Tasks

1. Align loading skeleton with new toolbar and column/card structure.
2. Tighten empty state copy for:
   - no columns
   - no items for active filters
   - empty individual column
3. Verify responsive behavior for:
   - desktop workspace
   - laptop widths
   - stacked mobile layout
4. Verify focus states and keyboard access on toolbar controls.
5. Verify horizontal scroll remains usable with many columns.
6. Verify filter chip removal updates URL and data correctly.

### Verification

FE:

1. `npm run lint`
2. `npm run type-check`
3. `npm run format:check`

Manual:

1. Open project board with several statuses.
2. Search by keyword and confirm results narrow correctly.
3. Apply each supported filter and confirm board query updates.
4. Remove chips one by one and verify data refresh.
5. Verify empty-filter result message appears when no cards match.
6. Verify toolbar wrap and horizontal board scroll on narrower widths.

### Done Criteria

1. Board feels denser and more operational.
2. Filtering is visible, compact, and understandable.
3. Lint, type-check, and format check pass.

## Suggested File Structure

Recommended minimal split if current board file grows:

1. `PMWorkItemBoard.tsx`
2. `PMWorkItemBoardToolbar.tsx`
3. `PMWorkItemBoardFilters.tsx`
4. `PMWorkItemBoardColumn.tsx`
5. `PMWorkItemBoardCard.tsx`
6. `PMWorkItemBoardSkeleton.tsx`
7. `pmWorkItemBoard.utils.ts`

Notes:

1. Keep helper surface small.
2. Avoid deep folder nesting.
3. Reuse list filter logic selectively, not mechanically.

## Recommended First Merge

Safest first merge:

1. Move board state to URL contract if chosen.
2. Replace header with compact toolbar.
3. Add active filter chips.
4. Keep card and column visuals mostly intact in that first PR.

Why:

1. It unlocks the biggest usability gain early.
2. It reduces risk by separating state/filter work from visual card redesign.
