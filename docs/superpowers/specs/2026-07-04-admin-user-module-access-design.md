# Design Specification: Admin User Module Access Management

**Author**: Antigravity  
**Date**: 2026-07-04  
**Status**: Draft  

---

## 1. Overview & Goal

Currently, the `UserAccessDialog` in the Admin panel allows managing a user's type and organization-wide roles. However, managing detailed module access is not possible from the Admin panel—it is only editable by organization admins in the Settings section. System administrators (SERP admins) need a way to assign/revoke module access and choose module-specific roles for users directly within the Admin panel.

This design introduces a modern, two-tab layout in `UserAccessDialog` to separate general roles and module-specific access. It leverages lazy loading of module roles and parallel execution of API requests to ensure a fast, optimized experience in line with Vercel React Best Practices.

---

## 2. User Experience & UI Design

To prevent the dialog from growing too long, the interface is split into two tabs:

### Tab 1: Organization Roles
- Contains the existing user type selector (`Select` dropdown).
- Contains the list of organization roles (checkboxes within a scrollable area).
- Shows read-only department badges.

### Tab 2: Module Access
- Displays a vertical list of all modules active/subscribed in the user's organization.
- For each module row:
  - **Left**: Module Icon (from the shared `MODULE_ICONS` configuration) and text details (Module Name, Code badge, description).
  - **Center**: A Role selector dropdown (`Select` component) showing roles specific to the module. It is only active when the module is toggled ON.
  - **Right**: A Switch toggle (`Switch` component) to enable or disable access to the module.

---

## 3. Technical Implementation Details

### Component Structure
We will refactor `UserAccessDialog.tsx` to include:
- A new sub-component `ModuleAccessRow` defined outside the parent component to avoid inline definition re-renders (`rerender-no-inline-components`).
- Shared UI components: `Tabs`, `TabsContent`, `TabsList`, `TabsTrigger`, `Switch`, `Select`, `SelectTrigger`, `SelectValue`, `SelectContent`, `SelectItem`.

### State Management
To track local changes before saving, the dialog will manage the following state:
- `selectedModuleIds`: A `Set<number>` tracking modules that are enabled (`O(1)` lookup efficiency).
- `selectedModuleRoles`: A `Map<number, number>` mapping `moduleId` to the chosen `roleId`.

### Data Fetching & Lazy Loading
- Fetch organization active modules:
  ```typescript
  const { data: orgModules } = useGetAccessibleModulesForOrganizationQuery(organizationId);
  ```
- Fetch module-specific roles lazily inside the `ModuleAccessRow` component:
  ```typescript
  const { data: roles, isLoading } = useGetModuleRolesQuery(moduleId, { skip: !isEnabled });
  ```
  This avoids loading roles for all modules upfront, minimizing network traffic.

### Save Handler Logic
When clicking "Save", we calculate the differences between the original user access data and the UI state:
- **Revoke**: Enabled originally, now disabled.
- **Assign**: Disabled originally, now enabled.
- **Role Change**: Enabled in both, but role has changed.

We execute these changes in parallel using `Promise.all` to avoid network waterfalls:
```typescript
const savePromises: Promise<any>[] = [];

// 1. Update User Type if changed
if (selectedUserType !== user.userType) {
  savePromises.push(updateUserType({ organizationId, userId, body: { userType: selectedUserType } }).unwrap());
}

// 2. Update Org Roles if changed
if (rolesChanged) {
  savePromises.push(updateUserRoles({ organizationId, userId, body: { roleIds: selectedRoleIds } }).unwrap());
}

// 3. Revoke module accesses
modulesToRevoke.forEach(moduleId => {
  savePromises.push(revokeUserAccess({ organizationId, moduleId, userId }).unwrap());
});

// 4. Assign module accesses
modulesToAssign.forEach(moduleId => {
  const roleId = selectedModuleRoles.get(moduleId);
  savePromises.push(assignUserAccess({ organizationId, moduleId, userId, roleId }).unwrap());
});

// 5. Replace module roles (Revoke -> Assign sequentially for that module)
modulesWithRoleChanged.forEach(moduleId => {
  const newRoleId = selectedModuleRoles.get(moduleId);
  const changeRolePromise = revokeUserAccess({ organizationId, moduleId, userId }).unwrap()
    .then(() => assignUserAccess({ organizationId, moduleId, userId, roleId: newRoleId }).unwrap());
  savePromises.push(changeRolePromise);
});

await Promise.all(savePromises);
```

---

## 4. Performance Optimizations (Vercel Best Practices)

1. **`async-parallel`**: Parallelizes API requests using `Promise.all` to save network turnaround time.
2. **`js-set-map-lookups`**: Uses native JavaScript `Set` and `Map` for constant-time `O(1)` state checkups.
3. **`rerender-no-inline-components`**: Isolates row-rendering into a separate `ModuleAccessRow` component definition.
4. **Lazy Loading of Module Roles**: Prevents fetching role options for disabled modules.

---

## 5. Verification Plan

### Manual Verification
1. Open the Admin Users page.
2. Open a user drawer and click "Access" to open the dialog.
3. Check the "Module Access" tab. Check that active organization modules are listed.
4. Turn on a module, verify the role dropdown is enabled, and choose a role.
5. Turn off another module, verify its role dropdown is disabled.
6. Click "Save", verify that all corresponding API requests are made and that the drawer/details update correctly.
