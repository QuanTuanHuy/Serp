# Spec: PM Module Access User Filtering in serp_web

**Author:** QuanTuanHuy  
**Date:** 2026-07-01  
**Goal:** Filter user lists in the `pm` module of `serp_web` to only return users with active access to the Project Management (`PM`) module.

---

## 1. PM Module Integration

We will update the 5 user queries in the `pm` module to resolve the `'PM'` module ID dynamically at runtime and filter user lists by it.

### Target Files
1. `src/modules/pm/components/projects/PMProjectEditForm.tsx`
2. `src/modules/pm/components/settings/resource-calendar/PMResourceCalendarUserCombobox.tsx`
3. `src/modules/pm/pages/PMProjectComponentsPage.tsx`
4. `src/modules/pm/pages/PMProjectCreatePage.tsx`
5. `src/modules/pm/pages/PMProjectPeoplePage.tsx`

### Logic Addition
In each component, we will:
1. Import `useGetMyModulesQuery` from `@/modules/account/services/moduleApi`.
2. Extract the `'PM'` module ID:
   ```typescript
   const { data: myModules } = useGetMyModulesQuery(undefined, {
     skip: !organizationId,
   });
   const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;
   ```
3. Pass `moduleId: pmModuleId` into the `useGetOrganizationUsersQuery` arguments.

---

## 2. Verification Plan

### Manual / Dev Verification
- Verify that compiling the `serp_web` project succeeds without TypeScript errors:
  ```bash
  npm run type-check
  ```
- Run the linter to ensure no syntax/style issues:
  ```bash
  npm run lint
  ```
