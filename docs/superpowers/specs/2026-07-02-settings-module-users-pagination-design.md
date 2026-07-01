# Design Spec: Settings Module User Access List Pagination and Role Mapping

**Author**: Antigravity (AI Coding Assistant)  
**Date**: 2026-07-02  
**Status**: Approved  

---

## 1. Goal & Context

When managing user access to modules under **Organization Settings** > **Module Access** in `serp_web`, opening the "Manage Access" list currently loads a static user list. However:
1. The backend API `/organizations/{organizationId}/modules/{moduleId}/users` retrieves users dynamically using a paginated query builder, but the frontend was fetching without pagination parameters and casting the paginated envelope wrapper as a plain array.
2. In the "Manage Access" list, user roles specific to the active module are not displayed.
3. Radix UI's `DialogDescription` was rendering invalid HTML by nesting block-level components inside a paragraph tag `<p>`, causing hydration issues.

This document describes the design to implement server-side search and pagination for the assigned users list, fix the hydration warnings, and render the appropriate module role badges.

---

## 2. Proposed Changes

### 2.1. API & Hook Updates

#### [MODIFY] [modulesApi.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/services/modules/modulesApi.ts)
We will update `getModuleUsers` query definition to accept pagination and search parameters, and transform it using the standard `createPaginatedTransform`:

```typescript
    getModuleUsers: build.query<
      PaginatedResponse<UserProfile>,
      {
        organizationId: number;
        moduleId: number;
        page?: number;
        pageSize?: number;
        search?: string;
      }
    >({
      query: ({ organizationId, moduleId, page, pageSize, search }) => {
        const params = new URLSearchParams();
        if (page !== undefined) params.append('page', String(page));
        if (pageSize !== undefined) params.append('pageSize', String(pageSize));
        if (search) params.append('search', search);
        return {
          url: `/organizations/${organizationId}/modules/${moduleId}/users?${params.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createPaginatedTransform<UserProfile>(),
      providesTags: (_result, _err, { moduleId }) => [
        { type: 'settings/ModuleUsers', id: moduleId },
      ],
    }),
```

#### [MODIFY] [useModules.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/hooks/useModules.ts)
We will extend `useModuleUsers` helper hook to accept search, page, and pageSize options:

```typescript
  const useModuleUsers = (
    moduleId?: number,
    params?: { page?: number; pageSize?: number; search?: string }
  ) =>
    useGetModuleUsersQuery(
      {
        organizationId: organizationId as number,
        moduleId: moduleId as number,
        page: params?.page,
        pageSize: params?.pageSize,
        search: params?.search,
      },
      {
        skip: !organizationId || !moduleId,
      }
    );
```

---

### 2.2. Component States & Props Updates

#### [MODIFY] [ModuleUsersDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/components/modules/ModuleUsersDialog.tsx)
The parent dialog will maintain state for search queries and page navigation:

* **State Variables**:
  ```typescript
  const [managePage, setManagePage] = useState(0);
  const [managePageSize] = useState(10);
  const [manageSearch, setManageSearch] = useState('');
  const debouncedManageSearch = useDebounce(manageSearch, 300);
  ```
* **Data Fetching**:
  ```typescript
  const {
    data: moduleUsersResponse,
    isLoading: isLoadingModuleUsers,
    refetch: refetchModuleUsers,
  } = useModuleUsers(moduleId, {
    page: managePage,
    pageSize: managePageSize,
    search: debouncedManageSearch,
  });

  const moduleUsers = useMemo(() => moduleUsersResponse?.data?.items || [], [moduleUsersResponse]);
  const pagination = useMemo(() => ({
    currentPage: moduleUsersResponse?.data?.currentPage || 0,
    totalPages: moduleUsersResponse?.data?.totalPages || 0,
    totalItems: moduleUsersResponse?.data?.totalItems || 0,
  }), [moduleUsersResponse]);
  ```

#### [MODIFY] [ModuleUsersList.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/components/modules/ModuleUsersList.tsx)
The props interface will be updated to accept the pagination state, search callbacks, and module roles list:

```typescript
export interface ModuleUsersListProps {
  users: UserProfile[];
  isLoading: boolean;
  isRevoking: boolean;
  onRevoke: (userId: number) => void;
  
  // New Props
  roles: ModuleRole[];
  search: string;
  onSearchChange: (search: string) => void;
  pagination: {
    currentPage: number;
    totalPages: number;
    totalItems: number;
  };
  onPageChange: (page: number) => void;
}
```

---

### 2.3. Visual & Performance Optimizations

#### 1. Vercel Performance Optimization: `js-set-map-lookups`
To prevent $O(N \times M)$ nested matching inside the rendering loop, a lookup `Set` of the module role names will be instantiated at the top level of the component:

```typescript
const moduleRoleNamesSet = useMemo(() => new Set(roles.map((r) => r.name)), [roles]);
```

Each user's matched module roles can then be filtered in $O(1)$ lookup time:
```typescript
const matchedRoles = user.roles?.filter((roleName) => moduleRoleNamesSet.has(roleName)) || [];
```

#### 2. UI Layout for Roles & Pagination Footer
* **Role Badges**: Displayed as inline purple badges (matching Settings theme) next to the user's name:
  ```tsx
  {matchedRoles.map((roleName) => (
    <Badge
      key={roleName}
      variant='secondary'
      className='bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800 text-[10px] px-1.5 py-0'
    >
      {roleName.replace('ROLE_', '').replace('_', ' ')}
    </Badge>
  ))}
  ```
* **Pagination Footer**: Placed at the bottom of the user list container:
  ```tsx
  <div className='flex items-center justify-between border-t pt-4 mt-4'>
    <span className='text-xs text-muted-foreground'>
      Showing Page {pagination.currentPage + 1} of {pagination.totalPages || 1} ({pagination.totalItems} users total)
    </span>
    <div className='flex items-center gap-2'>
      <Button
        variant='outline'
        size='sm'
        disabled={pagination.currentPage === 0 || isLoading}
        onClick={() => onPageChange(pagination.currentPage - 1)}
      >
        Previous
      </Button>
      <Button
        variant='outline'
        size='sm'
        disabled={pagination.currentPage >= pagination.totalPages - 1 || isLoading}
        onClick={() => onPageChange(pagination.currentPage + 1)}
      >
        Next
      </Button>
    </div>
  </div>
  ```

---

## 3. Verification Plan

### 3.1. Automated Verification
* Run `npm run type-check` inside `serp_web/` to ensure no compile-time regressions are introduced.
* Run `npm run lint` inside `serp_web/` to verify consistent code style.

### 3.2. Manual Verification
1. Open the "Module Access" settings page.
2. Select any active module and click **Manage Users**.
3. Go to the **Manage Access** tab. Verify that:
   - Only 10 users are loaded on the first page.
   - User roles belonging to the module are displayed correctly as purple badges.
   - Clicking "Next" loads the next page of users from the server.
   - Searching for a specific user name triggers server-side debounced search.
