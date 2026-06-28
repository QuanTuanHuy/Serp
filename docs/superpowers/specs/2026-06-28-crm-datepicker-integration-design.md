# Design Spec: CRM Date Picker Component Integration

**Date:** 2026-06-28
**Author:** Antigravity (AI Coding Assistant)

## Goal
Clone the PM module's date picker component `PMDatePicker` to the CRM module as `CRMDatePicker` and integrate it across all date fields in the CRM module to improve UI/UX consistency.

## Proposed Changes

### 1. Create CRM Date Helpers
We will clone date parsing and formatting utility functions from the PM module to the CRM module.

#### [NEW] [date.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/utils/date.ts)
- Implement `parseLocalDateValue` and `toLocalDateInputValue`.

#### [MODIFY] [index.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/utils/index.ts)
- Export `date.ts` utils.

### 2. Create CRMDatePicker Component
We will clone `PMDatePicker.tsx` to the CRM module and rename all exported components to use the `CRM` prefix.

#### [NEW] [CRMDatePicker.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/shared/CRMDatePicker.tsx)
- Expose `CRMDatePicker`, `CRMDateRangePicker`, and `CRMDateTimePicker`.
- Update imports to use CRM's new local date helpers.

#### [MODIFY] [shared/index.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/shared/index.ts)
- Export `CRMDatePicker`, `CRMDateRangePicker`, and `CRMDateTimePicker`.

### 3. Integrate CRMDatePicker with React Hook Form
We will wrap `CRMDatePicker` in `<Controller>` from `react-hook-form` in forms that use React Hook Form.

#### [MODIFY] [OpportunityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/OpportunityForm.tsx)
- Destructure `control` from `useForm`.
- Replace `<Input type="date">` for `expectedCloseDate` with `<Controller>` rendering `<CRMDatePicker>`.

#### [MODIFY] [ActivityForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/ActivityForm.tsx)
- Destructure `control` from `useForm`.
- Replace `<Input type="date">` for `scheduledDate` with `<Controller>` rendering `<CRMDatePicker>`.

### 4. Integrate CRMDatePicker with Local State Inputs
We will replace standard `<Input type="date">` with `<CRMDatePicker>` in components using local component state.

#### [MODIFY] [LeadForm.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/forms/LeadForm.tsx)
- Replace `<Input id="followUpDate">` with `<CRMDatePicker>`.

#### [MODIFY] [QuickAddActivityDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dialogs/QuickAddActivityDialog.tsx)
- Replace `<Input id="scheduledDate">` with `<CRMDatePicker>`.

#### [MODIFY] [QuickAddLeadDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dialogs/QuickAddLeadDialog.tsx)
- Replace `<Input id="quick-followUpDate">` with `<CRMDatePicker>`.

#### [MODIFY] [QuickAddOpportunityDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/components/dialogs/QuickAddOpportunityDialog.tsx)
- Replace `<Input id="expectedCloseDate">` with `<CRMDatePicker>`.

#### [MODIFY] [ActivityListPage.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/crm/pages/activities/ActivityListPage.tsx)
- Replace `<input type="date">` for `dueDateFrom` and `dueDateTo` with `<CRMDatePicker>`.

## Verification
- Run `npm run type-check` to verify that there are no compilation errors.
