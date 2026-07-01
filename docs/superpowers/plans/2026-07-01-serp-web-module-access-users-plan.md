# Module Access User Filtering in serp_web Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Filter user query parameters in the API gateway layers of `serp_web` using `moduleId`, and update the CRM module components to fetch and pass the CRM module ID when listing users.

**Architecture:** Extend the `UserFilters` and `SettingsUserFilters` interfaces to accept an optional `moduleId` filter parameter. Update `buildUserQueryParams` functions in both `usersApi.ts` endpoints to serialize it. Inject the `useGetMyModulesQuery` hook in all 5 CRM user query points to dynamically extract the module ID of the `'CRM'` module and filter the users.

**Tech Stack:** Next.js 15, React 19, TypeScript, Redux Toolkit Query (RTK Query).

---

### Task 1: Update User Filter Types

**Files:**
- Modify: `serp_web/src/modules/admin/types/user.types.ts:114-123`
- Modify: `serp_web/src/modules/settings/types/user.types.ts:118-132`

- [ ] **Step 1: Add `moduleId` parameter to `UserFilters` in Admin Types**
  In `serp_web/src/modules/admin/types/user.types.ts`, update `UserFilters`:
  ```typescript
  export interface UserFilters extends SearchParams {
    status?: UserStatus;
    organizationId?: number;
    userType?: UserType;
    roleId?: number;
    departmentId?: number;
    moduleId?: number;
    search?: string;
  }
  ```

- [ ] **Step 2: Add `moduleId` parameter to `SettingsUserFilters` in Settings Types**
  In `serp_web/src/modules/settings/types/user.types.ts`, update `SettingsUserFilters`:
  ```typescript
  export interface SettingsUserFilters {
    organizationId: number;
    search?: string;
    status?: string;
    userType?: string;
    roleId?: number;
    departmentId?: number;
    moduleId?: number;
    page?: number;
    pageSize?: number;
    sortBy?: string;
    sortDir?: 'ASC' | 'DESC';
  }
  ```

- [ ] **Step 3: Run type check to verify the type additions compile**
  Run from `serp_web/`:
  ```bash
  npm run type-check
  ```
  Expected output: Compilation success (no errors).

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/admin/types/user.types.ts serp_web/src/modules/settings/types/user.types.ts
  git commit -m "feat(serp_web): add moduleId to UserFilters interfaces"
  ```

---

### Task 2: Implement `moduleId` in API Query Parameter Serialization

**Files:**
- Modify: `serp_web/src/modules/admin/services/users/usersApi.ts:25-45`
- Modify: `serp_web/src/modules/settings/services/users/usersApi.ts:32-52`

- [ ] **Step 1: Append `moduleId` to query parameters in Admin `usersApi.ts`**
  In `serp_web/src/modules/admin/services/users/usersApi.ts`, update `buildUserQueryParams`:
  ```typescript
  const buildUserQueryParams = (filters: UserFilters): string => {
    const params = new URLSearchParams();

    if (filters.search) params.append('search', filters.search);
    if (filters.status) params.append('status', filters.status);
    if (filters.userType) params.append('userType', filters.userType);
    if (filters.roleId !== undefined)
      params.append('roleId', String(filters.roleId));
    if (filters.departmentId !== undefined)
      params.append('departmentId', String(filters.departmentId));
    if (filters.organizationId !== undefined)
      params.append('organizationId', String(filters.organizationId));
    if (filters.moduleId !== undefined)
      params.append('moduleId', String(filters.moduleId));
    if (filters.page !== undefined) params.append('page', String(filters.page));
    if (filters.pageSize !== undefined)
      params.append('pageSize', String(filters.pageSize));
    if (filters.sortBy) params.append('sortBy', filters.sortBy);
    if (filters.sortDir) params.append('sortDir', filters.sortDir);

    return params.toString();
  };
  ```

- [ ] **Step 2: Append `moduleId` to query parameters in Settings `usersApi.ts`**
  In `serp_web/src/modules/settings/services/users/usersApi.ts`, update `buildUserQueryParams`:
  ```typescript
  const buildUserQueryParams = (filters: SettingsUserFilters): string => {
    const params = new URLSearchParams();
    if (filters.search) params.append('search', filters.search);
    if (filters.status) {
      params.append(
        'status',
        filters.status === 'PENDING' ? 'INVITED' : filters.status
      );
    }
    if (filters.userType) params.append('userType', filters.userType);
    if (filters.roleId !== undefined)
      params.append('roleId', String(filters.roleId));
    if (filters.departmentId !== undefined)
      params.append('departmentId', String(filters.departmentId));
    if (filters.moduleId !== undefined)
      params.append('moduleId', String(filters.moduleId));
    if (filters.page !== undefined) params.append('page', String(filters.page));
    if (filters.pageSize !== undefined)
      params.append('pageSize', String(filters.pageSize));
    if (filters.sortBy) params.append('sortBy', filters.sortBy);
    if (filters.sortDir) params.append('sortDir', filters.sortDir);
    return params.toString();
  };
  ```

- [ ] **Step 3: Run type check**
  Run from `serp_web/`:
  ```bash
  npm run type-check
  ```
  Expected output: Compilation success (no errors).

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/admin/services/users/usersApi.ts serp_web/src/modules/settings/services/users/usersApi.ts
  git commit -m "feat(serp_web): serialize moduleId query parameter in users APIs"
  ```

---

### Task 3: Filter Users by `moduleId` in CRM Shared Components

**Files:**
- Modify: `serp_web/src/modules/crm/components/shared/CRMUserSelect.tsx`
- Modify: `serp_web/src/modules/crm/components/shared/CRMNotesTab.tsx`

- [ ] **Step 1: Integrate `moduleId` in `CRMUserSelect.tsx`**
  In `serp_web/src/modules/crm/components/shared/CRMUserSelect.tsx`, update imports and queries:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside the `CRMUserSelect` component:
  ```typescript
    const organizationId = useAppSelector(selectOrganizationId);
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;

    const { data: orgUsersResponse, isLoading } = useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
        moduleId: crmModuleId,
      },
      { skip: !organizationId }
    );
  ```

- [ ] **Step 2: Integrate `moduleId` in `CRMNotesTab.tsx`**
  In `serp_web/src/modules/crm/components/shared/CRMNotesTab.tsx`, update imports and queries:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside `CRMNotesTab` component:
  ```typescript
    const organizationId = useAppSelector(selectOrganizationId);
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;

    const { data: notesData, isLoading: isNotesLoading } = useGetNotesQuery({
      entityType,
      entityId,
    });
    const { data: orgUsersResponse } = useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        moduleId: crmModuleId,
      },
      { skip: !organizationId }
    );
  ```

- [ ] **Step 3: Run type check**
  Run from `serp_web/`:
  ```bash
  npm run type-check
  ```
  Expected: Success.

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/shared/CRMUserSelect.tsx serp_web/src/modules/crm/components/shared/CRMNotesTab.tsx
  git commit -m "feat(crm): filter assignee lists in select and notes by crm moduleId"
  ```

---

### Task 4: Filter Users by `moduleId` in CRM Dialogs and Pages

**Files:**
- Modify: `serp_web/src/modules/crm/components/forms/TeamForm.tsx`
- Modify: `serp_web/src/modules/crm/components/meeting-requests/RequestMeetingDialog.tsx`
- Modify: `serp_web/src/modules/crm/pages/teams/TeamDetailPage.tsx`

- [ ] **Step 1: Integrate `moduleId` in `TeamForm.tsx`**
  In `serp_web/src/modules/crm/components/forms/TeamForm.tsx`, update imports and queries:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside `TeamForm` component:
  ```typescript
    const isEditing = !!team;
    const { data: organization } = useGetMyOrganizationQuery();
    const organizationId = organization?.id;
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;

    const { data: usersResponse, isLoading: isLoadingUsers } =
      useGetOrganizationUsersQuery(
        {
          organizationId: organizationId as number,
          page: 0,
          pageSize: 100,
          status: 'ACTIVE',
          moduleId: crmModuleId,
        },
        { skip: !organizationId }
      );
  ```

- [ ] **Step 2: Integrate `moduleId` in `RequestMeetingDialog.tsx`**
  In `serp_web/src/modules/crm/components/meeting-requests/RequestMeetingDialog.tsx`, update imports and queries:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside `RequestMeetingDialog` component:
  ```typescript
    const { data: organization } = useGetMyOrganizationQuery(undefined, {
      skip: !open,
    });
    const organizationId = organization?.id;
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !open || !organizationId,
    });
    const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;

    const { data: orgUsersResponse, isLoading: orgUsersLoading } =
      useGetOrganizationUsersQuery(
        {
          organizationId: organizationId as number,
          page: 0,
          pageSize: 100,
          status: 'ACTIVE',
          moduleId: crmModuleId,
        },
        { skip: !open || !organizationId }
      );
  ```

- [ ] **Step 3: Integrate `moduleId` in `TeamDetailPage.tsx`**
  In `serp_web/src/modules/crm/pages/teams/TeamDetailPage.tsx`, update imports and queries:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside `TeamDetailPage` component:
  ```typescript
    const { data: organization } = useGetMyOrganizationQuery();
    const organizationId = organization?.id;
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const crmModuleId = myModules?.find((m) => m.moduleCode === 'CRM')?.moduleId;

    const { data: teamData, isLoading: isLoadingTeam } = useGetTeamQuery(teamId);
    const { data: membersData, isLoading: isLoadingMembers } =
      useGetTeamMembersQuery({ teamId, page: 1, size: 50 });
    const { data: territoriesData, isLoading: isLoadingTerritories } =
      useGetTeamTerritoriesQuery(teamId);
    const { data: usersData, isLoading: isLoadingUsers } =
      useGetOrganizationUsersQuery(
        {
          organizationId: organizationId as number,
          page: 0,
          pageSize: 100,
          status: 'ACTIVE',
          moduleId: crmModuleId,
        },
        { skip: !organizationId }
      );
  ```

- [ ] **Step 4: Run type check and lint**
  Run from `serp_web/`:
  ```bash
  npm run type-check
  npm run lint
  ```
  Expected: Success.

- [ ] **Step 5: Commit**
  ```bash
  git add serp_web/src/modules/crm/components/forms/TeamForm.tsx serp_web/src/modules/crm/components/meeting-requests/RequestMeetingDialog.tsx serp_web/src/modules/crm/pages/teams/TeamDetailPage.tsx
  git commit -m "feat(crm): filter team and meeting assignees by crm moduleId"
  ```
