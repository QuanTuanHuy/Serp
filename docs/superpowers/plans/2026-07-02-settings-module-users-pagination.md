# Settings Module User Access List Pagination and Role Mapping Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement server-side search and pagination for the assigned module users list in Organization Settings, display module-specific role badges, and resolve hydration warnings.

**Architecture:** Extend RTK Query endpoint and hooks to accept pagination/search parameters, manage pagination states in the parent dialog component, and render responsive pagination controls and O(1)-filtered role badges inside the list component.

**Tech Stack:** React 19, Next.js 15, TypeScript, RTK Query, Tailwind CSS, shadcn/ui.

---

### Task 1: Update API Query to Support Pagination

**Files:**
- Modify: `serp_web/src/modules/settings/services/modules/modulesApi.ts:82-94`

- [ ] **Step 1: Modify `getModuleUsers` query parameter serialization and payload shape**

Update `getModuleUsers` definition to take page, pageSize, and search query parameters, serializing them using `URLSearchParams`, and switch the return type and transform to `createPaginatedTransform<UserProfile>()`.

Code:
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

- [ ] **Step 2: Commit Task 1**

```bash
git add serp_web/src/modules/settings/services/modules/modulesApi.ts
git commit -m "feat(settings): update getModuleUsers query to support pagination and search"
```

---

### Task 2: Update useModuleUsers Hook to Pass Pagination Parameters

**Files:**
- Modify: `serp_web/src/modules/settings/hooks/useModules.ts:119-128`

- [ ] **Step 1: Update `useModuleUsers` hook signature and arguments**

Modify the hook `useModuleUsers` in `useModules.ts` to accept the optional search, page, and pageSize parameters and pass them to the RTK query.

Code:
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

- [ ] **Step 2: Commit Task 2**

```bash
git add serp_web/src/modules/settings/hooks/useModules.ts
git commit -m "feat(settings): support pagination parameters in useModuleUsers hook"
```

---

### Task 3: Manage Pagination State in ModuleUsersDialog

**Files:**
- Modify: `serp_web/src/modules/settings/components/modules/ModuleUsersDialog.tsx`

- [ ] **Step 1: Declare state and wire query in `ModuleUsersDialog.tsx`**

We will import `useDebounce` hook (already in `useModules.ts` or page), maintain page and search states, pass them into `useModuleUsers`, and construct the `moduleUsers` array and `pagination` object. We will also pass the module `roles` and these pagination handlers into `ModuleUsersList`.

Replace the `useModuleUsers` query invocation around lines 64-69:
```typescript
  const [managePage, setManagePage] = useState(0);
  const [managePageSize] = useState(10);
  const [manageSearch, setManageSearch] = useState('');
  const debouncedManageSearch = useDebounce(manageSearch, 300);

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

- [ ] **Step 2: Update the `ModuleUsersList` invocation props**

Update the props passed to `<ModuleUsersList>` around line 198:
```tsx
                    <ModuleUsersList
                      users={moduleUsers}
                      isLoading={isLoadingModuleUsers}
                      isRevoking={revokeStatus.isLoading}
                      onRevoke={handleRevoke}
                      roles={roles}
                      search={manageSearch}
                      onSearchChange={(search) => {
                        setManageSearch(search);
                        setManagePage(0); // Reset page to 0 when search term changes
                      }}
                      pagination={pagination}
                      onPageChange={setManagePage}
                    />
```

- [ ] **Step 3: Commit Task 3**

```bash
git add serp_web/src/modules/settings/components/modules/ModuleUsersDialog.tsx
git commit -m "feat(settings): wire server-side pagination and search states into ModuleUsersDialog"
```

---

### Task 4: Redesign ModuleUsersList to Display Roles and Pagination Controls

**Files:**
- Modify: `serp_web/src/modules/settings/components/modules/ModuleUsersList.tsx`

- [ ] **Step 1: Update `ModuleUsersListProps` interface**

Update the `ModuleUsersListProps` interface to accept the new search, pagination, and roles props.

Code:
```typescript
export interface ModuleUsersListProps {
  users: UserProfile[];
  isLoading: boolean;
  isRevoking: boolean;
  onRevoke: (userId: number) => void;
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

- [ ] **Step 2: Update local search and filter logic, and implement O(1) matched roles filter**

Inside `ModuleUsersList` component, remove the local state `searchQuery`. Update the list filtering/rendering to use the external `search` prop and search input changes callback. Implement O(1) matching using `useMemo` for the module roles list.

Code:
```typescript
  // Create an O(1) set of module role names
  const moduleRoleNamesSet = useMemo(
    () => new Set(roles.map((r) => r.name)),
    [roles]
  );
```

- [ ] **Step 3: Render Matched Module Roles & Pagination Footer**

Update the JSX to:
1. Render matched roles for each user as purple badges.
2. Render search input that calls `onSearchChange(e.target.value)` on change.
3. Render the pagination controls and stats footer at the bottom of the component.

For each user in the list:
```tsx
                    <div className='flex flex-col min-w-0 flex-1'>
                      <div className='flex items-center gap-2 flex-wrap'>
                        <span className='text-sm font-medium truncate'>
                          {user.firstName} {user.lastName}
                        </span>
                        {/* Render module-specific roles */}
                        {user.roles
                          ?.filter((rName) => moduleRoleNamesSet.has(rName))
                          .map((roleName) => (
                            <Badge
                              key={roleName}
                              variant='secondary'
                              className='bg-purple-50 text-purple-700 border-purple-200 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800 text-[10px] px-1.5 py-0'
                            >
                              {roleName.replace('ROLE_', '').replace('_', ' ')}
                            </Badge>
                          ))}
                      </div>
                      <span className='text-xs text-muted-foreground truncate'>
                        {user.email}
                      </span>
                    </div>
```

Below the ScrollArea:
```tsx
        {/* Pagination Footer */}
        {pagination.totalPages > 1 && (
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
        )}
```

- [ ] **Step 4: Commit Task 4**

```bash
git add serp_web/src/modules/settings/components/modules/ModuleUsersList.tsx
git commit -m "feat(settings): implement pagination controls and module role badges in ModuleUsersList"
```

---

### Task 5: Verification & Quality Check

- [ ] **Step 1: Run compilation check**

Run: `npm run type-check` inside `serp_web/`
Expected: Success with no errors.

- [ ] **Step 2: Run linter**

Run: `npm run lint` inside `serp_web/`
Expected: Success with no style violations.
