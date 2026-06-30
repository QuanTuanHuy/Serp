# Admin Filter & Combobox Scroll Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Solve scrollability issues in the admin filter panels by making the comboboxes dialog-modal-aware and extracting option lists to a centralized, scroll-enabled shared component.

**Architecture:** Add an optional `modal` prop to the shared `Combobox` (defaulting to `false`) to register nested popovers with Radix UI's modal layer managers. Extract and centralize the duplicated `FilterPane` and `FilterOption` components in the admin module and apply scroll overflow styling.

**Tech Stack:** Next.js, React, Tailwind CSS, Radix UI Popover, Lucide React icons.

---

### Task 1: Add Modal Prop to Shared Combobox Component

**Files:**
- Modify: `src/shared/components/ui/combobox.tsx`

- [ ] **Step 1: Modify Combobox interface and component definition**
  Add the optional `modal` prop to `ComboboxProps` and default it to `false` in `Combobox`. Forward it to the `Popover` component wrapper.
  
  Replace the top section of `src/shared/components/ui/combobox.tsx`:
  ```tsx
  export interface ComboboxProps {
    value?: string | number;
    onChange: (value: string | number | undefined) => void;
    items: ComboboxItem[];
    placeholder?: string;
    emptyText?: string;
    disabled?: boolean;
    loading?: boolean;
    clearable?: boolean;
    onSearch?: (query: string) => void; // if provided, caller controls filtering via items
    className?: string;
    modal?: boolean;
  }
  ```
  And update the component signature:
  ```tsx
  export const Combobox: React.FC<ComboboxProps> = ({
    value,
    onChange,
    items,
    placeholder = 'Search...',
    emptyText = 'No items found',
    disabled,
    loading,
    clearable = true,
    onSearch,
    className,
    modal = false,
  }) => {
  ```
  And pass it down to `Popover`:
  ```tsx
    return (
      <Popover open={open} onOpenChange={setOpen} modal={modal}>
  ```

- [ ] **Step 2: Verify type correctness**
  Run: `npx tsc --noEmit` from `serp_web/`
  Expected: Command runs successfully without errors in `combobox.tsx`.

- [ ] **Step 3: Commit**
  Run:
  ```bash
  git add src/shared/components/ui/combobox.tsx
  git commit -m "feat: add optional modal prop to Combobox component"
  ```

---

### Task 2: Implement Shared FilterPane and FilterOption Components

**Files:**
- Modify: `src/modules/admin/components/shared/AdminFilterDialog.tsx`
- Modify: `src/modules/admin/index.ts`

- [ ] **Step 1: Add CheckCircle import and define components in AdminFilterDialog**
  Add `CheckCircle` to the imports from `lucide-react` at the top of `src/modules/admin/components/shared/AdminFilterDialog.tsx`:
  ```typescript
  import { CheckCircle, ChevronRight, SlidersHorizontal } from 'lucide-react';
  ```
  And define and export `FilterPane` and `FilterOption` at the bottom of `src/modules/admin/components/shared/AdminFilterDialog.tsx`:
  ```tsx
  export interface FilterPaneProps {
    title: string;
    children: React.ReactNode;
    className?: string;
  }

  export function FilterPane({ title, children, className }: FilterPaneProps) {
    return (
      <div className='flex min-h-0 flex-1 flex-col p-4'>
        <div className='mb-3'>
          <h3 className='text-sm font-semibold'>{title}</h3>
          <p className='text-sm text-muted-foreground'>Select one value.</p>
        </div>
        <div className={cn('flex-1 overflow-y-auto space-y-2 pr-1', className)}>{children}</div>
      </div>
    );
  }

  export interface FilterOptionProps {
    label: string;
    selected: boolean;
    onSelect: () => void;
  }

  export function FilterOption({ label, selected, onSelect }: FilterOptionProps) {
    return (
      <button
        type='button'
        onClick={onSelect}
        title={label}
        className={cn(
          'flex w-full min-w-0 items-center justify-between gap-2 rounded-md px-3 py-2 text-left text-sm hover:bg-muted transition-colors',
          selected && 'bg-muted font-medium'
        )}
      >
        <span className='min-w-0 flex-1 truncate'>{label}</span>
        {selected ? (
          <CheckCircle className='h-4 w-4 shrink-0 text-primary' />
        ) : null}
      </button>
    );
  }
  ```

- [ ] **Step 2: Export FilterPane and FilterOption from index barrel**
  Modify exports in `src/modules/admin/index.ts` at line 32:
  ```typescript
  export {
    AdminFilterDialog,
    FilterPane,
    FilterOption,
  } from './components/shared/AdminFilterDialog';
  ```

- [ ] **Step 3: Verify build**
  Run: `npm run build` or `npx tsc --noEmit` from `serp_web/`
  Expected: Clean compilation.

- [ ] **Step 4: Commit**
  Run:
  ```bash
  git add src/modules/admin/components/shared/AdminFilterDialog.tsx src/modules/admin/index.ts
  git commit -m "feat: export scrollable FilterPane and FilterOption components"
  ```

---

### Task 3: Refactor Users Page and Create User Dialog

**Files:**
- Modify: `src/app/admin/users/page.tsx`
- Modify: `src/modules/admin/components/users/UserDialog.tsx`

- [ ] **Step 1: Import shared components and clean up users page**
  Modify imports at the top of `src/app/admin/users/page.tsx`:
  ```typescript
  import {
    AdminActionMenu,
    AdminFilterChips,
    AdminFilterDialog,
    AdminStatusBadge,
    UserAccessDialog,
    UserDetailsDrawer,
    UserDialog,
    UserStatusDialog,
    useUsers,
    FilterPane,
    FilterOption,
  } from '@/modules/admin';
  ```
  Delete the local definitions of `FilterPane` and `FilterOption` from the bottom of `src/app/admin/users/page.tsx`.
  Remove the duplicate `CheckCircle` import from `lucide-react` if it is no longer used locally.

- [ ] **Step 2: Add modal prop to Comboboxes in Users filter dialog**
  Update the `<Combobox>` usages for **Organization** and **Role** in `src/app/admin/users/page.tsx`:
  For Organization:
  ```tsx
            <Combobox
              value={filters.organizationId}
              modal={true}
              onChange={(value) => {
  ```
  For Role:
  ```tsx
            <Combobox
              value={filters.roleId}
              modal={true}
              onChange={(value) =>
  ```

- [ ] **Step 3: Add modal prop to Organization picker in Create User Dialog**
  Update the `<Combobox>` usage in `src/modules/admin/components/users/UserDialog.tsx`:
  ```tsx
              <Combobox
                value={organizationId}
                modal={true}
                onChange={(value) => {
  ```

- [ ] **Step 4: Verify type safety**
  Run: `npx tsc --noEmit` from `serp_web/`
  Expected: No errors.

- [ ] **Step 5: Commit**
  Run:
  ```bash
  git add src/app/admin/users/page.tsx src/modules/admin/components/users/UserDialog.tsx
  git commit -m "refactor: use shared filter components and enable modal combobox in users"
  ```

---

### Task 4: Refactor Menu Displays Page

**Files:**
- Modify: `src/app/admin/menu-displays/page.tsx`

- [ ] **Step 1: Import shared components and clean up menu displays page**
  Modify imports at the top of `src/app/admin/menu-displays/page.tsx`:
  ```typescript
  import { AdminFilterChips, AdminFilterDialog, FilterPane, FilterOption } from '@/modules/admin';
  ```
  Delete the local definitions of `FilterPane` and `FilterOption` from the bottom of `src/app/admin/menu-displays/page.tsx`.

- [ ] **Step 2: Add modal prop to Combobox in Menu Displays filter dialog**
  Update the `<Combobox>` usage for **Module** in `src/app/admin/menu-displays/page.tsx`:
  ```tsx
            <Combobox
              value={filters.moduleId}
              modal={true}
              onChange={(value) =>
  ```

- [ ] **Step 3: Commit**
  Run:
  ```bash
  git add src/app/admin/menu-displays/page.tsx
  git commit -m "refactor: use shared filter components and enable modal combobox in menu-displays"
  ```

---

### Task 5: Refactor Plans Page

**Files:**
- Modify: `src/app/admin/plans/page.tsx`

- [ ] **Step 1: Import shared components and clean up plans page**
  Modify imports at the top of `src/app/admin/plans/page.tsx`:
  ```typescript
  import {
    AdminFilterChips,
    AdminFilterDialog,
    AdminStatusBadge,
    FilterPane,
    FilterOption,
    usePlans,
  } from '@/modules/admin';
  ```
  Delete local definitions of `FilterPane` and `FilterOption` from the bottom of `src/app/admin/plans/page.tsx`.

- [ ] **Step 2: Add modal prop to Combobox in Plans filter dialog**
  Update the `<Combobox>` usage for **Organization** in `src/app/admin/plans/page.tsx`:
  ```tsx
            <Combobox
              value={filters.organizationId}
              modal={true}
              onChange={(value) =>
  ```

- [ ] **Step 3: Commit**
  Run:
  ```bash
  git add src/app/admin/plans/page.tsx
  git commit -m "refactor: use shared filter components and enable modal combobox in plans"
  ```

---

### Task 6: Refactor Subscriptions Page

**Files:**
- Modify: `src/app/admin/subscriptions/page.tsx`

- [ ] **Step 1: Import shared components and clean up subscriptions page**
  Modify imports at the top of `src/app/admin/subscriptions/page.tsx`:
  ```typescript
  import {
    AdminFilterChips,
    AdminFilterDialog,
    AdminStatusBadge,
    FilterPane,
    FilterOption,
    useSubscriptions,
  } from '@/modules/admin';
  ```
  Delete local definitions of `FilterPane` and `FilterOption` from the bottom of `src/app/admin/subscriptions/page.tsx`.

- [ ] **Step 2: Add modal prop to Comboboxes in Subscriptions filter dialog**
  Update the `<Combobox>` usages for **Organization** and **Plan** in `src/app/admin/subscriptions/page.tsx`:
  For Organization:
  ```tsx
            <Combobox
              value={filters.organizationId}
              modal={true}
              onChange={(value) =>
  ```
  For Plan:
  ```tsx
            <Combobox
              value={filters.planId}
              modal={true}
              onChange={(value) =>
  ```

- [ ] **Step 3: Commit**
  Run:
  ```bash
  git add src/app/admin/subscriptions/page.tsx
  git commit -m "refactor: use shared filter components and enable modal combobox in subscriptions"
  ```

---

### Task 7: Refactor Modules, Organizations, and Roles Pages

**Files:**
- Modify: `src/app/admin/modules/page.tsx`
- Modify: `src/app/admin/organizations/page.tsx`
- Modify: `src/app/admin/roles/page.tsx`

- [ ] **Step 1: Refactor modules page**
  Import `FilterPane` and `FilterOption` from `@/modules/admin` in `src/app/admin/modules/page.tsx`.
  Delete local definitions of `FilterPane` and `FilterOption` from the bottom.

- [ ] **Step 2: Refactor organizations page**
  Import `FilterPane` and `FilterOption` from `@/modules/admin` in `src/app/admin/organizations/page.tsx`.
  Delete local definitions of `FilterPane` and `FilterOption` from the bottom.

- [ ] **Step 3: Refactor roles page**
  Import `FilterPane` and `FilterOption` from `@/modules/admin` in `src/app/admin/roles/page.tsx`.
  Delete local definitions of `FilterPane` and `FilterOption` from the bottom.

- [ ] **Step 4: Commit**
  Run:
  ```bash
  git add src/app/admin/modules/page.tsx src/app/admin/organizations/page.tsx src/app/admin/roles/page.tsx
  git commit -m "refactor: clean up modules, organizations, and roles page filters to use shared components"
  ```

---

### Task 8: Verification

**Files:**
- None

- [ ] **Step 1: Check typescript compilations**
  Run: `npx tsc --noEmit` from `serp_web/`
  Expected: Clean compilation with no type errors.

- [ ] **Step 2: Check lint checks**
  Run: `npm run lint` from `serp_web/`
  Expected: ESLint output passes.

- [ ] **Step 3: Build package**
  Run: `npm run build` from `serp_web/`
  Expected: Clean next build completion.
