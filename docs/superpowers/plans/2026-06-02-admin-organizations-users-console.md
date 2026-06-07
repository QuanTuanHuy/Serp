# Admin Organizations and Users Console Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a modern admin console for organizations and users with grouped filters, entity drawers, access editing, and organization lifecycle cascade actions.

**Architecture:** Keep backend organization lifecycle logic in the `account` service, with a small command service that updates the organization and cascades user statuses in one transaction. On the frontend, keep the admin module table-first and build a shared filter dialog shell, then layer organization and user drawers/status dialogs on top of the existing RTK Query and store structure.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Next.js 15, React 19, TypeScript, RTK Query, Tailwind CSS, Radix/Shadcn primitives.

---

## File Structure

- Create `account/src/main/java/serp/project/account/core/domain/dto/request/UpdateOrganizationStatusRequest.java`
  - Request body for organization suspend/activate actions.
- Create `account/src/main/java/serp/project/account/core/domain/dto/response/OrganizationStatusUpdateResponse.java`
  - Response wrapper with updated organization and cascade counts.
- Create `account/src/main/java/serp/project/account/core/usecase/organization/command/OrganizationStatusCommandService.java`
  - Transactional orchestration for organization status changes and user cascade.
- Modify `account/src/main/java/serp/project/account/core/service/IOrganizationService.java`
  - Add organization status update contract.
- Modify `account/src/main/java/serp/project/account/core/service/impl/OrganizationService.java`
  - Persist organization status updates.
- Modify `account/src/main/java/serp/project/account/core/usecase/OrganizationUseCase.java`
  - Delegate organization status changes and wrap response payloads.
- Modify `account/src/main/java/serp/project/account/ui/controller/OrganizationController.java`
  - Add admin organization status endpoint, stats wrapper, and organization sort query params.
- Create `account/src/test/java/serp/project/account/core/usecase/organization/command/OrganizationStatusCommandServiceTest.java`
  - Unit coverage for cascade semantics.
- Modify `account/src/test/java/serp/project/account/core/service/impl/OrganizationServiceTest.java`
  - Add status persistence regression coverage.
- Modify `account/src/test/java/serp/project/account/ui/controller/OrganizationControllerTest.java`
  - Verify admin status endpoint, stats wrapper, and sort query params.

- Modify `serp_web/src/modules/admin/types/organization.types.ts`
  - Align organization type/status unions with backend enum names.
  - Add the organization status mutation response type.
- Modify `serp_web/src/modules/admin/types/user.types.ts`
  - Add admin filter fields for user type, role, and department.
  - Add the user detail response and a small department option type.
- Modify `serp_web/src/modules/admin/store/organizations/organizationsSlice.ts`
  - Keep filters and page state aligned with dialog-driven filtering.
- Create `serp_web/src/modules/admin/store/organizations/organizationsUiSlice.ts`
  - Track selected organization, drawer state, and status dialog state.
- Modify `serp_web/src/modules/admin/store/users/usersSlice.ts`
  - Extend user UI state for detail drawer and access/status dialogs.
- Modify `serp_web/src/modules/admin/store/users/usersFiltersSlice.ts`
  - Add department, role, and user type filter state.
- Modify `serp_web/src/modules/admin/store/index.ts`
  - Register any new admin reducers.
- Modify `serp_web/src/modules/admin/services/organizations/organizationsApi.ts`
  - Add organization status mutation.
- Modify `serp_web/src/modules/admin/services/users/usersApi.ts`
  - Add user detail, status, type, and role mutations if missing.
- Create `serp_web/src/modules/admin/services/departments/departmentsApi.ts`
  - Load organization-scoped departments for user filters/access UI.
- Modify `serp_web/src/modules/admin/services/adminApi.ts`
  - Re-export new endpoints/hooks.
- Modify `serp_web/src/modules/admin/hooks/useOrganizations.ts`
  - Wire status mutation, drawer state, and filter dialog state.
- Modify `serp_web/src/modules/admin/hooks/useUsers.ts`
  - Wire detail/access/status mutations and filter dialog state.
- Create `serp_web/src/modules/admin/components/shared/AdminFilterDialog.tsx`
  - Shared two-column grouped filter shell.
- Create `serp_web/src/modules/admin/components/shared/AdminFilterChips.tsx`
  - Active filter badges and clear actions.
- Create `serp_web/src/modules/admin/components/shared/AdminConfirmStatusDialog.tsx`
  - Reusable confirm dialog for suspend/activate.
- Create `serp_web/src/modules/admin/components/organizations/OrganizationDetailsDrawer.tsx`
  - Organization overview, subscription summary, users preview, and quick actions.
- Create `serp_web/src/modules/admin/components/organizations/OrganizationStatusDialog.tsx`
  - Suspend/activate confirmation flow.
- Create `serp_web/src/modules/admin/components/organizations/OrganizationUsersPreview.tsx`
  - Compact user preview list and navigation shortcut.
- Create `serp_web/src/modules/admin/components/users/UserDetailsDrawer.tsx`
  - User profile, roles, departments, and module access sections.
- Create `serp_web/src/modules/admin/components/users/UserAccessDialog.tsx`
  - User type and role management surface.
- Create `serp_web/src/modules/admin/components/users/UserStatusDialog.tsx`
  - Suspend/activate confirmation flow.
- Modify `serp_web/src/modules/admin/components/users/UserDialog.tsx`
  - Support create/edit mode with organization selection and access sectioning.
- Modify `serp_web/src/modules/admin/components/users/UserForm.tsx`
  - Split identity, credentials, profile, and preference fields cleanly.
- Modify `serp_web/src/app/admin/organizations/page.tsx`
  - Replace plain filters with toolbar + shared dialog + drawer/status wiring.
- Modify `serp_web/src/app/admin/users/page.tsx`
  - Replace plain filters with toolbar + shared dialog + drawer/access/status wiring.
- Modify `serp_web/src/modules/admin/index.ts`
  - Export new hooks/components used by the app routes.

## Task 1: Backend Organization Lifecycle and Admin Stats

**Files:**
- Create: `account/src/main/java/serp/project/account/core/domain/dto/request/UpdateOrganizationStatusRequest.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/OrganizationStatusUpdateResponse.java`
- Create: `account/src/main/java/serp/project/account/core/usecase/organization/command/OrganizationStatusCommandService.java`
- Modify: `account/src/main/java/serp/project/account/core/service/IOrganizationService.java`
- Modify: `account/src/main/java/serp/project/account/core/service/impl/OrganizationService.java`
- Modify: `account/src/main/java/serp/project/account/core/usecase/OrganizationUseCase.java`
- Modify: `account/src/main/java/serp/project/account/ui/controller/OrganizationController.java`
- Test: `account/src/test/java/serp/project/account/core/usecase/organization/command/OrganizationStatusCommandServiceTest.java`
- Test: `account/src/test/java/serp/project/account/core/service/impl/OrganizationServiceTest.java`
- Test: `account/src/test/java/serp/project/account/ui/controller/OrganizationControllerTest.java`

- [ ] **Step 1: Write the failing backend tests**

Add tests that prove the cascade behavior and controller wiring:

```java
@Test
void suspendOrganizationShouldSuspendOrganizationAndAllUsers() {
    when(organizationService.getOrganizationById(10L)).thenReturn(organization);
    when(userService.getUsersByOrganizationId(10L)).thenReturn(List.of(activeUser, invitedUser, suspendedUser));
    when(organizationService.updateOrganizationStatus(10L, OrganizationStatus.SUSPENDED))
            .thenReturn(updatedOrganization);
    when(userService.updateUser(anyLong(), any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(1));

    var response = commandService.updateOrganizationStatus(10L, OrganizationStatus.SUSPENDED, 1L);

    assertEquals(3, response.getAffectedUsers());
    assertEquals(3, response.getSuspendedUsers());
    assertEquals(0, response.getActivatedUsers());
    verify(userService, times(3)).updateUser(anyLong(), any(UserEntity.class));
}
```

```java
@Test
void activateOrganizationShouldActivateOnlySuspendedUsers() {
    when(userService.getUsersByOrganizationId(10L)).thenReturn(List.of(activeUser, suspendedUser));
    when(organizationService.updateOrganizationStatus(10L, OrganizationStatus.ACTIVE))
            .thenReturn(updatedOrganization);

    var response = commandService.updateOrganizationStatus(10L, OrganizationStatus.ACTIVE, 1L);

    assertEquals(1, response.getActivatedUsers());
    assertEquals(0, response.getSuspendedUsers());
}
```

```java
@Test
void adminStatsEndpointShouldBypassTenantScreening() throws Exception {
    mockMvc.perform(get("/api/v1/admin/organizations/10/users/stats"))
            .andExpect(status().isOk());
}
```

- [ ] **Step 2: Run the tests and verify RED**

Run:

```powershell
cd account
.\mvnw.cmd -Dtest=OrganizationStatusCommandServiceTest,OrganizationServiceTest,OrganizationControllerTest test
```

Expected: compile or test failures because the new request/response, service method, and controller endpoints do not exist yet.

- [ ] **Step 3: Add the command DTOs and service contract**

Create the request and response types:

```java
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpdateOrganizationStatusRequest {
    @NotNull
    private OrganizationStatus status;
}
```

```java
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrganizationStatusUpdateResponse {
    private OrganizationEntity organization;
    private int affectedUsers;
    private int activatedUsers;
    private int suspendedUsers;
}
```

Add the service contract:

```java
OrganizationEntity updateOrganizationStatus(Long organizationId, OrganizationStatus status);
```

Implement it in `OrganizationService` as a small transactional update:

```java
@Override
@Transactional(rollbackFor = Exception.class)
public OrganizationEntity updateOrganizationStatus(Long organizationId, OrganizationStatus status) {
    var organization = getOrganizationById(organizationId);
    organization.setStatus(status);
    return organizationPort.save(organization);
}
```

- [ ] **Step 4: Implement the minimal organization lifecycle flow**

Add a transactional command service that:

```java
@Service
@RequiredArgsConstructor
public class OrganizationStatusCommandService {
    private final IOrganizationService organizationService;
    private final IUserService userService;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateOrganizationStatus(Long organizationId, Long updatedBy,
            UpdateOrganizationStatusRequest request) {
        var organization = organizationService.updateOrganizationStatus(organizationId, request.getStatus());
        var users = userService.getUsersByOrganizationId(organizationId);
        int activatedUsers = 0;
        int suspendedUsers = 0;

        for (var user : users) {
            if (request.getStatus() == OrganizationStatus.SUSPENDED) {
                user.suspend();
                suspendedUsers++;
                userService.updateUser(user.getId(), user);
                continue;
            }
            if (user.getStatus() == UserStatus.SUSPENDED) {
                user.activate();
                activatedUsers++;
                userService.updateUser(user.getId(), user);
            }
        }

        return responseUtils.success(OrganizationStatusUpdateResponse.builder()
                .organization(organization)
                .affectedUsers(users.size())
                .activatedUsers(activatedUsers)
                .suspendedUsers(suspendedUsers)
                .build());
    }
}
```

Delegate from `OrganizationUseCase` with the same request and response shape:

```java
public GeneralResponse<?> updateOrganizationStatus(Long organizationId, Long updatedBy,
        UpdateOrganizationStatusRequest request) {
    try {
        return organizationStatusCommandService.updateOrganizationStatus(organizationId, updatedBy, request);
    } catch (Exception e) {
        log.error("Error updating organization status {}: {}", organizationId, e.getMessage());
        return responseUtils.internalServerError(e.getMessage());
    }
}
```

Wire `OrganizationController` with:

```java
@PatchMapping("/admin/organizations/{organizationId}/status")
public ResponseEntity<?> updateOrganizationStatus(
        @PathVariable Long organizationId,
        @Valid @RequestBody UpdateOrganizationStatusRequest request) {
    Long updatedBy = authUtils.getCurrentUserId().orElse(null);
    var response = organizationStatusCommandService.updateOrganizationStatus(organizationId, updatedBy, request);
    return ResponseEntity.status(response.getCode()).body(response);
}
```

Add the admin-safe stats wrapper:

```java
@GetMapping("/admin/organizations/{organizationId}/users/stats")
public ResponseEntity<?> getOrganizationUserStats(@PathVariable Long organizationId) {
    var response = userUseCase.getUserStats(organizationId);
    return ResponseEntity.status(response.getCode()).body(response);
}
```

Also add `sortBy` and `sortDir` to the existing admin organization list endpoint signature and pass them through to `GetOrganizationParams`.

- [ ] **Step 5: Run the backend tests and verify GREEN**

Run:

```powershell
cd account
.\mvnw.cmd -Dtest=OrganizationStatusCommandServiceTest,OrganizationServiceTest,OrganizationControllerTest test
```

Expected: tests pass.

## Task 2: Shared Admin Filter Shell and Filter Data

**Files:**
- Modify: `serp_web/src/modules/admin/types/organization.types.ts`
- Modify: `serp_web/src/modules/admin/types/user.types.ts`
- Modify: `serp_web/src/modules/admin/store/organizations/organizationsSlice.ts`
- Create: `serp_web/src/modules/admin/store/organizations/organizationsUiSlice.ts`
- Modify: `serp_web/src/modules/admin/store/users/usersSlice.ts`
- Modify: `serp_web/src/modules/admin/store/users/usersFiltersSlice.ts`
- Modify: `serp_web/src/modules/admin/store/index.ts`
- Create: `serp_web/src/modules/admin/components/shared/AdminFilterDialog.tsx`
- Create: `serp_web/src/modules/admin/components/shared/AdminFilterChips.tsx`
- Create: `serp_web/src/modules/admin/services/departments/departmentsApi.ts`
- Modify: `serp_web/src/modules/admin/services/adminApi.ts`
- Modify: `serp_web/src/modules/admin/index.ts`

- [ ] **Step 1: Write the failing type changes**

Update the unions and filter shapes so the current pages stop relying on stale values:

```ts
export type OrganizationStatus =
  | 'ACTIVE'
  | 'TRIAL'
  | 'SUSPENDED'
  | 'EXPIRED'
  | 'CLOSED';

export type OrganizationType =
  | 'ENTERPRISE'
  | 'SMB'
  | 'STARTUP'
  | 'PERSONAL'
  | 'NON_PROFIT'
  | 'GOVERNMENT';
```

```ts
export interface OrganizationStatusUpdateResponse {
  organization: Organization;
  affectedUsers: number;
  activatedUsers: number;
  suspendedUsers: number;
}
```

```ts
export interface UserFilters extends SearchParams {
  status?: UserStatus;
  organizationId?: number;
  userType?: UserType;
  roleId?: number;
  departmentId?: number;
  search?: string;
}
```

```ts
export interface DepartmentOption {
  id: number;
  name: string;
}
```

```ts
export interface UserDetailResponse {
  id: number;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  avatarUrl?: string;
  userType: UserType;
  status: UserStatus;
  lastLoginAt?: number;
  createdAt?: number;
  timezone?: string;
  preferredLanguage?: string;
  organizationId: number;
  organizationName: string;
  roles?: Array<{
    id: number;
    name: string;
    scope?: string;
    description?: string;
    moduleName?: string;
  }>;
  departments?: Array<{
    id: number;
    name: string;
    isPrimary?: boolean;
    jobTitle?: string;
  }>;
  moduleAccesses?: Array<{
    moduleId: number;
    moduleName: string;
    moduleCode: string;
    isActive?: boolean;
    grantedAt?: number;
  }>;
}
```

Add the UI state needed to drive drawers and dialogs instead of scattering modal state inside pages.

- [ ] **Step 2: Run frontend type-check and verify RED**

Run:

```powershell
cd serp_web
npm run type-check
```

Expected: errors in admin pages/hooks until the new filter fields, slices, and dialog shell are wired.

- [ ] **Step 3: Implement the shared filter shell**

Create a reusable dialog scaffold that matches the PM two-column pattern:

```tsx
interface AdminFilterDialogProps {
  open: boolean;
  title: string;
  description: string;
  criteria: Array<{ id: string; label: string; count: number }>;
  selectedCriterion: string;
  onSelectCriterion: (criterion: string) => void;
  onOpenChange: (open: boolean) => void;
  onClear: () => void;
  children: React.ReactNode;
}
```

Create a chip bar that renders active filters and exposes remove/clear actions.

Add an organization-scoped departments API:

```ts
query: (organizationId: number) => ({
  url: `/organizations/${organizationId}/departments?page=0&pageSize=100&sortBy=name&sortDir=ASC`,
  method: 'GET',
})
```

- [ ] **Step 4: Run lint and type-check for the shared layer**

Run:

```powershell
cd serp_web
npm run lint
npm run type-check
```

Expected: shared admin components compile and the new filter state is available to the page layer.

## Task 3: Organizations Page, Drawer, and Cascade Status Actions

**Files:**
- Modify: `serp_web/src/modules/admin/services/organizations/organizationsApi.ts`
- Modify: `serp_web/src/modules/admin/hooks/useOrganizations.ts`
- Create: `serp_web/src/modules/admin/components/organizations/OrganizationDetailsDrawer.tsx`
- Create: `serp_web/src/modules/admin/components/organizations/OrganizationUsersPreview.tsx`
- Create: `serp_web/src/modules/admin/components/organizations/OrganizationStatusDialog.tsx`
- Modify: `serp_web/src/app/admin/organizations/page.tsx`
- Modify: `serp_web/src/modules/admin/index.ts`

- [ ] **Step 1: Write the failing page/action wiring**

Add the organization status mutation and the drawer state the page needs:

```ts
updateOrganizationStatus: builder.mutation<
  OrganizationStatusUpdateResponse,
  { organizationId: number; body: { status: OrganizationStatus } }
>({
  query: ({ organizationId, body }) => ({
    url: `/admin/organizations/${organizationId}/status`,
    method: 'PATCH',
    body,
  }),
  invalidatesTags: (_result, _error, { organizationId }) => [
    { type: 'admin/Organization', id: organizationId },
    { type: 'admin/Organization', id: 'LIST' },
    { type: 'admin/User', id: 'LIST' },
  ],
})
```

Add page-level actions for:

```tsx
<AdminActionMenu
  items={[
    { label: 'View details', onClick: () => openDrawer(row.id) },
    { label: row.status === 'ACTIVE' ? 'Suspend' : 'Activate', onClick: () => openStatusDialog(row.id) },
  ]}
/>
```

- [ ] **Step 2: Run frontend type-check and verify RED**

Run:

```powershell
cd serp_web
npm run type-check
```

Expected: missing drawer/dialog imports and handler wiring errors until the components exist.

- [ ] **Step 3: Build the organization drawer and status dialog**

Implement the drawer sections:

```tsx
<OrganizationDetailsDrawer
  organizationId={selectedOrganizationId}
  onCreateUser={() => openCreateUser(selectedOrganizationId)}
  onViewAllUsers={() => router.push(`/admin/users?organizationId=${selectedOrganizationId}`)}
/>
```

The drawer should fetch organization details and show a compact user preview using the existing users list query filtered by `organizationId`.

The status dialog should confirm cascade behavior with the counts returned by the mutation response:

```tsx
<AdminConfirmStatusDialog
  open={statusDialogOpen}
  status={selectedOrganizationStatus}
  title='Suspend organization'
  description='This will suspend the organization and all users in it.'
  impactText={`${affectedUsers} users will be affected.`}
/>
```

- [ ] **Step 4: Wire the organizations page toolbar and filter dialog**

Replace the plain selects with:

```tsx
<Input value={filters.search || ''} onChange={(e) => handleSearch(e.target.value)} />
<Button variant='outline' onClick={() => setFilterDialogOpen(true)}>
  <SlidersHorizontal className='h-4 w-4' />
</Button>
```

Render active filter chips below the toolbar and move the select controls into the shared dialog.

- [ ] **Step 5: Run the frontend checks**

Run:

```powershell
cd serp_web
npm run lint
npm run type-check
npm run format:check
```

Expected: organizations page compiles and the filter/dialog/drawer flows work.

## Task 4: Users Page, Detail Drawer, Create/Edit, and Access Actions

**Files:**
- Modify: `serp_web/src/modules/admin/services/users/usersApi.ts`
- Modify: `serp_web/src/modules/admin/hooks/useUsers.ts`
- Create: `serp_web/src/modules/admin/components/users/UserDetailsDrawer.tsx`
- Create: `serp_web/src/modules/admin/components/users/UserAccessDialog.tsx`
- Create: `serp_web/src/modules/admin/components/users/UserStatusDialog.tsx`
- Modify: `serp_web/src/modules/admin/components/users/UserDialog.tsx`
- Modify: `serp_web/src/modules/admin/components/users/UserForm.tsx`
- Modify: `serp_web/src/app/admin/users/page.tsx`
- Modify: `serp_web/src/modules/admin/index.ts`

- [ ] **Step 1: Write the failing user mutation and drawer flow**

Add the missing user detail and access mutations:

```ts
getUserDetail: builder.query<UserDetailResponse, { organizationId: number; userId: number }>({
  query: ({ organizationId, userId }) => ({
    url: `/organizations/${organizationId}/users/${userId}/detail`,
    method: 'GET',
  }),
})
```

```ts
updateUserStatus: builder.mutation<
  UserResponse,
  { organizationId: number; userId: number; status: 'ACTIVE' | 'SUSPENDED' | 'INACTIVE' }
>({
  query: ({ organizationId, userId, status }) => ({
    url: `/organizations/${organizationId}/users/${userId}/status`,
    method: 'PATCH',
    body: { status },
  }),
})
```

The page should open:

```tsx
<UserDetailsDrawer userId={selectedUserId} organizationId={selectedOrganizationId} />
<UserAccessDialog userId={selectedUserId} organizationId={selectedOrganizationId} />
<UserStatusDialog userId={selectedUserId} organizationId={selectedOrganizationId} />
```

- [ ] **Step 2: Run frontend type-check and verify RED**

Run:

```powershell
cd serp_web
npm run type-check
```

Expected: compile errors until the user drawer, create/edit form, and access flow are wired.

- [ ] **Step 3: Improve the create/edit user form**

Refactor `UserForm` into clear sections:

```tsx
<section>
  <h3>Organization</h3>
  <Combobox value={organizationId} onChange={setOrganizationId} />
</section>

<section>
  <h3>Basic information</h3>
  <Input value={firstName} />
  <Input value={lastName} />
  <Input type='email' value={email} />
</section>

<section>
  <h3>Credentials</h3>
  <Input type='password' value={password} />
  <Input type='password' value={confirmPassword} />
</section>

<section>
  <h3>Access</h3>
  <Select value={userType} />
  <ScrollArea className='h-40 rounded-md border p-2'>
    {roles.map((role) => (
      <label key={role.id} className='flex items-center gap-2 px-2 py-1.5'>
        <Checkbox checked={roleIds.includes(role.id)} />
        <span>{role.name}</span>
      </label>
    ))}
  </ScrollArea>
</section>
```

Keep edit mode focused on profile and preference fields, and move role/type/status changes into the access dialogs.

- [ ] **Step 4: Wire the users page toolbar, drawer, and filters**

Move the status/organization/user-type/role/department controls into the shared filter dialog and keep search in the toolbar.

Use the drawer for details and preview sections, and use separate dialogs for access and status changes so the edit form stays narrow.

- [ ] **Step 5: Run the frontend checks**

Run:

```powershell
cd serp_web
npm run lint
npm run type-check
npm run format:check
```

Expected: users page compiles, creates/edits still work, and the new drawer/access/status flows are wired.

## Task 5: Final Verification and Cleanup

**Files:**
- All backend and frontend files touched above.

- [ ] **Step 1: Run the focused backend regression pass**

Run:

```powershell
cd account
.\mvnw.cmd -Dtest=OrganizationStatusCommandServiceTest,OrganizationServiceTest,OrganizationControllerTest test
```

Expected: backend tests pass.

- [ ] **Step 2: Run the frontend quality gate**

Run:

```powershell
cd serp_web
npm run lint
npm run type-check
npm run format:check
```

Expected: frontend checks pass.

- [ ] **Step 3: Check the diff for accidental churn**

Run:

```powershell
git diff --stat
git diff --check
```

Expected: only admin console files change, with no whitespace errors.

- [ ] **Step 4: Report what shipped and what was validated**

Summarize:

- organization status cascade implementation;
- grouped admin filter dialog;
- organization drawer and user preview;
- user drawer, create/edit, and access/status flows;
- backend and frontend verification commands that passed.
