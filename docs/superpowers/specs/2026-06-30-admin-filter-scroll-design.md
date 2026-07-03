# Technical Specification: Admin Filter & Combobox Scroll Fix

## Status
- **Author**: Antigravity
- **Date**: 2026-06-30
- **Status**: Approved

## Context & Problem Statement
In the `@admin` module of the `serp_web` project, users face two major scroll and event interaction issues:
1. **Unscrollable Comboboxes inside Dialogs**: The filters on various admin pages (e.g., Users, Menu Displays) use a `Combobox` (which is a Popover-based element). When a `Combobox` is opened inside a `Dialog` (which is a modal dialog), the popover is portalled to the `body` root (making it a sibling to the Dialog). Because the Dialog locks background scroll and disables pointer events outside itself, mouse wheel events and hover/clicks on the portalled `Combobox` options are blocked.
2. **Unscrollable Options Lists**: The `FilterPane` component is locally duplicated in 7 page files. If a filter list grows very long (such as 50+ departments or organization types), the container overflows without vertical scrollbars, making some options unreachable.

## Proposed Changes

### 1. Combobox Component Update
We will introduce an optional `modal` prop to the shared `Combobox` component, forwarding it directly to the Radix UI `Popover` component. This enables callers inside modal Dialogs to pass `modal={true}`, which registers the popover with Radix's layer manager and keeps pointer-events/wheel events active.

**Target File**: `src/shared/components/ui/combobox.tsx`
- Add `modal?: boolean` to `ComboboxProps`.
- Pass it to the `<Popover>` wrapper: `<Popover open={open} onOpenChange={setOpen} modal={modal}>`.

### 2. Centralized Scrollable Filter Components
We will extract the duplicated `FilterPane` and `FilterOption` code into shared components in the admin module and implement vertical scrolling for the option items.

**Target File**: `src/modules/admin/components/shared/AdminFilterDialog.tsx`
- Implement and export `FilterPane` and `FilterOption`.
- Add `flex-1 overflow-y-auto pr-1` styling to the children container in `FilterPane`.

**Target File**: `src/modules/admin/index.ts`
- Export `FilterPane` and `FilterOption` from `./components/shared/AdminFilterDialog`.

### 3. Refactoring Admin Pages & Dialogs
We will clean up page files by importing the shared scrollable components and adding `modal={true}` to nested comboboxes:

- **src/app/admin/users/page.tsx**
  - Delete local `FilterPane` and `FilterOption`.
  - Import them from `@/modules/admin`.
  - Add `modal={true}` to Organization and Role filters `<Combobox>`.
- **src/app/admin/menu-displays/page.tsx**
  - Delete local `FilterPane` and `FilterOption`.
  - Import them from `@/modules/admin`.
  - Add `modal={true}` to Module filter `<Combobox>`.
- **src/app/admin/plans/page.tsx**
  - Delete local `FilterPane` and `FilterOption`.
  - Import them from `@/modules/admin`.
  - Add `modal={true}` to Organization filter `<Combobox>`.
- **src/app/admin/subscriptions/page.tsx**
  - Delete local `FilterPane` and `FilterOption`.
  - Import them from `@/modules/admin`.
  - Add `modal={true}` to Organization and Plan filters `<Combobox>`.
- **src/app/admin/modules/page.tsx**
  - Delete local `FilterPane` and `FilterOption`.
  - Import them from `@/modules/admin`.
- **src/app/admin/organizations/page.tsx**
  - Delete local `FilterPane` and `FilterOption`.
  - Import them from `@/modules/admin`.
- **src/app/admin/roles/page.tsx**
  - Delete local `FilterPane` and `FilterOption`.
  - Import them from `@/modules/admin`.
- **src/modules/admin/components/users/UserDialog.tsx**
  - Add `modal={true}` to the organization picker `<Combobox>`.

## Verification Plan

### Automated Checks
- Run `npm run type-check` from `serp_web/` to make sure all types align.
- Run `npm run lint` from `serp_web/` to verify ESLint compliance.
- Run `npm run build` to verify production compilation.

### Manual Verification
1. Open the filter dialog in the Users tab. Open the Organization or Role combobox and verify that mouse wheel scrolling is functional.
2. Verify that clicking outside the combobox correctly dismisses the combobox but keeps the filter dialog open.
3. Simulate a long list of departments or organizations to verify that a scrollbar is present on the right side of the filter dialog.
4. Verify the Create User dialog combobox works and scrolls.
