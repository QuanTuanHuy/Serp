# Design Spec: CRM User Selection Dropdown Component Integration

**Date:** 2026-06-28
**Author:** Antigravity (AI Coding Assistant)

## Goal
Implement a reusable component `CRMUserSelect` that fetches organization users from the API and displays them in a standard dropdown selection box. Integrate this component across all CRM forms and dialogs to replace manual User ID text inputs.

## Proposed Changes

### 1. Create Reusable CRMUserSelect Component

#### [NEW] [CRMUserSelect.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/shared/CRMUserSelect.tsx)
- Create the reusable component that:
  - Selects `organizationId` from Redux state.
  - Queries active organization users via `useGetOrganizationUsersQuery`.
  - Exposes props for controlled value/onChange, fallbackUserName (for orphans), disabled state, etc.
  - Renders a Shadcn UI `<Select>` component with an "Unassigned" option, orphan option (if any), and active organization users list.

#### [MODIFY] [shared/index.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/shared/index.ts)
- Export `CRMUserSelect` from `shared`.

### 2. Integrate CRMUserSelect into CRM Forms and Dialogs

#### [MODIFY] [LeadForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/LeadForm.tsx)
- Import `CRMUserSelect`.
- Replace the `assignedTo` input field with `<CRMUserSelect>`.

#### [MODIFY] [QuickAddActivityDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx)
- Import `CRMUserSelect`.
- Replace the `assignedTo` input field with `<CRMUserSelect>`.

#### [MODIFY] [QuickAddOpportunityDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx)
- Import `CRMUserSelect`.
- Replace the `assignedTo` input field with `<CRMUserSelect>`.

#### [MODIFY] [ActivityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/ActivityForm.tsx)
- Import `CRMUserSelect`.
- Wrap `<CRMUserSelect>` inside react-hook-form `<Controller>` for the `assignedTo` field.

#### [MODIFY] [OpportunityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/OpportunityForm.tsx)
- Import `CRMUserSelect`.
- Refactor the existing custom organization users select logic to use the new `<CRMUserSelect>` wrapped inside react-hook-form `<Controller>` for cleaner code.

## Verification
- Run `npm run type-check` to verify that there are no compilation errors.
