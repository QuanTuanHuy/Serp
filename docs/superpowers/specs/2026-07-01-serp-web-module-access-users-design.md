# Spec: Module Access User Filtering in serp_web

**Author:** QuanTuanHuy  
**Date:** 2026-07-01  
**Goal:** Filter user lists to only return users with active access to a requested module when querying users within the `serp_web` frontend, specifically targeting the CRM module.

---

## 1. API and Types Updates

We will add `moduleId` support to the `getUsers` and `getOrganizationUsers` queries in the API gateway proxy layers of `serp_web`.

### `modules/admin/types/user.types.ts`
- Add `moduleId?: number;` to the `UserFilters` interface.

### `modules/admin/services/users/usersApi.ts`
- Update `buildUserQueryParams` to append the `moduleId` query parameter if present:
  ```typescript
  if (filters.moduleId !== undefined)
    params.append('moduleId', String(filters.moduleId));
  ```

### `modules/settings/types/user.types.ts`
- Add `moduleId?: number;` to the `SettingsUserFilters` interface.

### `modules/settings/services/users/usersApi.ts`
- Update `buildUserQueryParams` to append the `moduleId` query parameter if present:
  ```typescript
  if (filters.moduleId !== undefined)
    params.append('moduleId', String(filters.moduleId));
  ```

---

## 2. CRM Integration

We will update the 5 user queries in the CRM module to resolve the `'CRM'` module ID dynamically at runtime and filter by it.

### Target Files
1. `src/modules/crm/components/shared/CRMUserSelect.tsx`
2. `src/modules/crm/components/forms/TeamForm.tsx`
3. `src/modules/crm/components/meeting-requests/RequestMeetingDialog.tsx`
4. `src/modules/crm/components/shared/CRMNotesTab.tsx`
5. `src/modules/crm/pages/teams/TeamDetailPage.tsx`

### Logic Addition
In each component, we will:
1. Import `useGetMyModulesQuery` from `@/modules/account/services/moduleApi`.
2. Extract the `'CRM'` module ID:
   ```typescript
   const { data: myModules } = useGetMyModulesQuery(undefined, {
     skip: !organizationId,
   });
   const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;
   ```
3. Pass `moduleId: crmModuleId` into the `useGetOrganizationUsersQuery` arguments.

---

## 3. Verification Plan

### Manual / Dev Verification
- Verify that compiling the `serp_web` project succeeds without TypeScript errors:
  ```bash
  npm run type-check
  ```
- Run the linter to ensure no syntax/style issues:
  ```bash
  npm run lint
  ```
