# Module Bulk Access Frontend Design Specification

**Goal:** Redesign the Module Access Management Dialog in `serp_web` to support bulk assignment, bulk revocation, and improve UI/UX using a unified table layout.

## 1. Context & Scope

Currently, the `ModuleUsersDialog` has a two-tab layout ("Assign Users" and "Manage Access") which only allows managing access one user at a time. This design is cramped, requires multiple clicks, and does not leverage the backend bulk assign (`POST .../users/bulk`) and bulk revoke (`POST .../users/bulk-revoke`) capabilities.

We are replacing this tabbed dialog with a **Unified Table View** that:
- Shows all organization users.
- Displays each user's current module access status and matched module roles.
- Supports multi-selection of users across search/pagination filters.
- Implements a floating/sticky **Bulk Action Bar** to perform bulk assignment (with role selection) and bulk revocation.

---

## 2. API & Caching Integration

We will inject two new endpoints into `settingsModulesApi` in `serp_web/src/modules/settings/services/modules/modulesApi.ts`:

### 2.1. `bulkAssignUsersToModule`
- **Method:** `POST`
- **Path:** `/organizations/{organizationId}/modules/{moduleId}/users/bulk`
- **Request Body:**
  ```typescript
  {
    userIds: number[];
    roleId?: number;
  }
  ```
- **InvalidatesTags:**
  - `{ type: 'settings/Module', id: moduleId }` (to refresh user counts)
  - `{ type: 'settings/ModuleUsers', id: moduleId }` (to refresh lists)
  - `{ type: 'settings/Module', id: 'LIST' }` (to refresh the settings page stats)

### 2.2. `bulkRevokeUsersFromModule`
- **Method:** `POST`
- **Path:** `/organizations/{organizationId}/modules/{moduleId}/users/bulk-revoke`
- **Request Body:**
  ```typescript
  {
    userIds: number[];
  }
  ```
- **InvalidatesTags:**
  - `{ type: 'settings/Module', id: moduleId }`
  - `{ type: 'settings/ModuleUsers', id: moduleId }`

---

## 3. Hook Updates (`useSettingsModules`)

In `serp_web/src/modules/settings/hooks/useModules.ts`, we expose the following wrappers:
- `bulkAssign(moduleId: number, userIds: number[], roleId?: number)`: Calls the `bulkAssignUsersToModule` mutation, handles success notifications (e.g., "Successfully granted access to X users, skipped Y users"), and catches errors.
- `bulkRevoke(moduleId: number, userIds: number[])`: Calls the `bulkRevokeUsersFromModule` mutation, handles success notifications (e.g., "Successfully revoked access for Z users"), and catches errors.
- `bulkAssignStatus` & `bulkRevokeStatus`: Expose mutation loading states to show spinners during execution.

---

## 4. State Management & Dialog Logic

Inside `ModuleUsersDialog.tsx`, we will manage:

1. **`selectedUserIds` (Set of numbers):**
   - Stores selected user IDs for bulk actions.
   - Preserves selection when switching pages or typing search queries.
   - Clears upon successful bulk operations or dialog close.
2. **Access Lookup Set (`hasAccessSet`):**
   - Loaded by querying `/organizations/{orgId}/modules/{moduleId}/users` with `pageSize: 1000` to retrieve all users who already have active access.
   - Constructed as a `new Set(moduleUsers.map(u => u.id))` for O(1) checks.
3. **Tab Filtering State:**
   - Filters the table between:
     - `ALL`: Fetches organization users using `/organizations/{orgId}/users` (without module filter).
     - `GRANTED`: Fetches only users who have active access by passing the `moduleId` parameter to the users endpoint.
4. **Pagination & Search State:**
   - Separated search input (`searchQuery`) and debounced search to query organization users.
   - Current page (`page`) and page size (`pageSize`).

---

## 5. UI/UX Layout Redesign

### 5.1. Dialog Shell
- **Max Width:** Upgrade to `max-w-6xl` (1152px) for a spacious table presentation.
- **Height:** Set to `h-[90vh]` to maximize list visibility.
- **Header:** Shows Module Name, Code, Description, and an info banner displaying the current usage stats (e.g., "12 active users / 50 seats total").

### 5.2. Search and Filter Bar
- A search input with a magnifying glass icon.
- A Segmented Tabs control to toggle between:
  - **All Members**
  - **Members with Access**

### 5.3. The Unified Table
A robust table containing columns:
- **Checkbox:** Header checkbox to toggle all rows on the current page; row-level checkboxes for individual selection.
- **Member:** Avatar displaying user initials, followed by Full Name and Email.
- **Access Status & Role:**
  - If user is in `hasAccessSet`, displays a green badge `Active` and a purple badge showing their module role (matching their assigned `roles` against the module's roles).
  - If user is not in `hasAccessSet`, displays a grey badge `No Access`.
- **Account Status:** Displays the user's organization status (e.g., `ACTIVE` in green, `INACTIVE` in gray).
- **Quick Action:**
  - For non-accessed users: An **"Assign"** button that opens a role selector popover to grant immediate single access.
  - For accessed users: A **"Revoke"** button (danger variant) to revoke single access with confirmation.

### 5.4. Floating Bulk Action Bar
A glassmorphic (blurred background + border + shadow) banner that slides up at the bottom of the table when `selectedUserIds.size > 0`:
- **Text:** "Selected **{selectedCount}** users"
- **Actions:**
  1. **Role Select:** A dropdown to choose which role to assign.
  2. **Bulk Assign Button:** Executes `bulkAssign` with the selected role.
  3. **Bulk Revoke Button:** Executes `bulkRevoke` (triggers an `AlertDialog` confirmation first).
  4. **Cancel Button:** Deselects all.

---

## 6. Verification Plan

### 6.1. Automated Verifications
- Run TypeScript compilation checks inside `serp_web`:
  ```bash
  npm run type-check
  ```
- Run ESLint checks inside `serp_web` to ensure no lint violations:
  ```bash
  npm run lint
  ```

### 6.2. Manual Verifications
1. **Single Actions:**
   - Verify that clicking "Assign" on a single user assigns them and updates the UI immediately.
   - Verify that clicking "Revoke" on a single user triggers confirmation, revokes access, and updates the UI.
2. **Bulk Selection & Persistence:**
   - Select 2 users on Page 1.
   - Search for a name, select another user.
   - Clear search, check that all 3 users remain selected.
3. **Bulk Assign:**
   - Select 3 users without access.
   - Click "Assign Role" in the floating bar, select a role, and confirm.
   - Verify all 3 users are now shown with "Active" status and the correct role.
4. **Bulk Revoke:**
   - Select 2 users with access.
   - Click "Revoke Access" in the floating bar, confirm the popup.
   - Verify both users have their access status changed to "No Access".
5. **Deselection:**
   - Select a user, verify the Floating Bar appears.
   - Click "Cancel", verify the bar hides and all checkboxes are unchecked.
