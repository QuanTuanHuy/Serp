# PM Module Access User Filtering in serp_web Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Filter user query parameters in the PM module of `serp_web` to only return users with active access to the Project Management (`PM`) module.

**Architecture:** Inject the `useGetMyModulesQuery` hook in the 5 PM user query points to dynamically extract the module ID of the `'PM'` module and filter the users.

**Tech Stack:** Next.js 15, React 19, TypeScript, Redux Toolkit Query (RTK Query).

---

### Task 1: Filter Users by `moduleId` in PM Components

**Files:**
- Modify: `serp_web/src/modules/pm/components/projects/PMProjectEditForm.tsx`
- Modify: `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarUserCombobox.tsx`

- [ ] **Step 1: Integrate `moduleId` in `PMProjectEditForm.tsx`**
  In `serp_web/src/modules/pm/components/projects/PMProjectEditForm.tsx`, update imports and query:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside the `PMProjectEditForm` component:
  ```typescript
    const organizationId = useAppSelector(selectOrganizationId);
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

    const { data: usersResponse } = useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
        moduleId: pmModuleId,
      },
      {
        skip: !organizationId,
      }
    );
  ```

- [ ] **Step 2: Integrate `moduleId` in `PMResourceCalendarUserCombobox.tsx`**
  In `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarUserCombobox.tsx`, update imports and query:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside the `PMResourceCalendarUserCombobox` component:
  ```typescript
    const organizationId = useAppSelector(selectOrganizationId);
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

    const [search, setSearch] = useState('');
    const deferredSearch = useDeferredValue(search.trim());
    const canSearch = deferredSearch.length > 0;

    const usersQuery = useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        search: deferredSearch || undefined,
        page: 0,
        pageSize: 50,
        status: 'ACTIVE',
        sortBy: 'firstName',
        sortDir: 'ASC',
        moduleId: pmModuleId,
      },
      {
        skip: !organizationId || !canSearch,
      }
    );
  ```

- [ ] **Step 3: Run type check to verify the components compile**
  Run from `serp_web/`:
  ```bash
  npm run type-check
  ```
  Expected output: Compilation success (no errors).

- [ ] **Step 4: Commit**
  ```bash
  git add serp_web/src/modules/pm/components/projects/PMProjectEditForm.tsx serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarUserCombobox.tsx
  git commit -m "feat(pm): filter project edit lead and calendar resources by pm moduleId"
  ```

---

### Task 2: Filter Users by `moduleId` in PM Pages

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectComponentsPage.tsx`
- Modify: `serp_web/src/modules/pm/pages/PMProjectCreatePage.tsx`
- Modify: `serp_web/src/modules/pm/pages/PMProjectPeoplePage.tsx`

- [ ] **Step 1: Integrate `moduleId` in `PMProjectComponentsPage.tsx`**
  In `serp_web/src/modules/pm/pages/PMProjectComponentsPage.tsx`, update imports and query:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside `PMProjectComponentsPage` component:
  ```typescript
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

    const { data: usersResponse, isLoading: isUsersLoading } =
      useGetOrganizationUsersQuery(
        {
          organizationId: organizationId as number,
          page: 0,
          pageSize: 100,
          status: 'ACTIVE',
          sortBy: 'firstName',
          sortDir: 'ASC',
          moduleId: pmModuleId,
        },
        { skip: !organizationId }
      );
  ```

- [ ] **Step 2: Integrate `moduleId` in `PMProjectCreatePage.tsx`**
  In `serp_web/src/modules/pm/pages/PMProjectCreatePage.tsx`, update imports and query:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside `PMProjectCreatePage` component:
  ```typescript
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

    const {
      data: usersResponse,
      isLoading: isUserLoading,
      error: usersError,
    } = useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
        moduleId: pmModuleId,
      },
      { skip: !organizationId }
    );
  ```

- [ ] **Step 3: Integrate `moduleId` in `PMProjectPeoplePage.tsx`**
  In `serp_web/src/modules/pm/pages/PMProjectPeoplePage.tsx`, update imports and query:
  ```typescript
  import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
  ```
  And inside `PMProjectPeoplePage` component:
  ```typescript
    const { data: myModules } = useGetMyModulesQuery(undefined, {
      skip: !organizationId,
    });
    const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

    const peopleQuery = useGetPmProjectPeopleQuery(numericProjectId, {
      skip: !Number.isFinite(numericProjectId),
    });
    const rolesQuery = useGetPmProjectRolesQuery({ page: 0, pageSize: 100 });
    const usersQuery = useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        search: deferredUserSearch || undefined,
        page: 0,
        pageSize: 50,
        status: 'ACTIVE',
        sortBy: 'firstName',
        sortDir: 'ASC',
        moduleId: pmModuleId,
      },
      {
        skip:
          !organizationId ||
          !dialogOpen ||
          dialogMode !== 'add' ||
          !canSearchUsers,
      }
    );
  ```

- [ ] **Step 4: Run type check and lint**
  Run from `serp_web/`:
  ```bash
  npm run type-check
  npm run lint
  ```
  Expected output: Success.

- [ ] **Step 5: Commit**
  ```bash
  git add serp_web/src/modules/pm/pages/PMProjectComponentsPage.tsx serp_web/src/modules/pm/pages/PMProjectCreatePage.tsx serp_web/src/modules/pm/pages/PMProjectPeoplePage.tsx
  git commit -m "feat(pm): filter project components, create, and people leads by pm moduleId"
  ```
