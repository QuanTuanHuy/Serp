# Design Specification: Resource Calendar UX/UI Improvements

Author: Antigravity AI
Date: 2026-07-01
Description: Design specification for implementing UX/UI improvements on the Resource Calendar settings page under the PM module.

---

## 1. Goal & Context
The current Resource Calendar settings page in `serp_web` (under `src/modules/pm/components/settings/resource-calendar/`) contains multiple disconnected forms and listings that display raw numeric `userId` values, require repetitive configuration of weekly blocks day-by-day, and lack a visual overview. 

This design implements a modern, cohesive **Integrated Tabbed Dashboard** to improve readability, efficiency of bulk operations, and visual timeline monitoring of working capacity and exceptions.

---

## 2. Component Architecture & File Layout
The existing files under [resource-calendar](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/) will be restructured or refactored:

* [PMResourceCalendarSettingsSection.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarSettingsSection.tsx) (Refactor)
  - Acts as the main shell and coordinates the parent state.
  - Contains overview stats mini-cards.
  - Renders a `<Tabs defaultValue="profiles">` wrapper.
* [PMResourceCalendarProfileTable.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarProfileTable.tsx) (Refactor/Keep)
  - Renders inside Tab 1 (`profiles`).
* [PMResourceCalendarProfileDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarProfileDialog.tsx) (Refactor)
  - Refactors the block editor inside the profile creation/edit dialog to include a weekly grid summary and a quick-copy Monday-to-weekday button.
* [PMResourceCalendarAssignmentPanel.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarAssignmentPanel.tsx) (Refactor)
  - Renders inside Tab 2 (`assignments`).
  - Redesigned into a comprehensive Resource Directory Table with searching/filtering, individual rows with user avatar/name, and a floating bulk-action bar.
* [PMResourceCalendarExceptionPanel.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarExceptionPanel.tsx) (Refactor)
  - Renders inside Tab 3 (`exceptions`).
  - Implements a monthly Calendar View along with a toggleable List View. Resolves user IDs to real user names.
* [PMResourceCalendarUserCombobox.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarUserCombobox.tsx) (Keep)
  - Reused for single-user selects.

---

## 3. Detailed UI Design & Improvements

### 3.1. Tabbed Layout & Overview Stats
- **Header & Stats**: Renders at the top. Stats include:
  - *Profiles count*
  - *Assigned users count*
  - *Active upcoming exceptions count*
  - *Unassigned users count*
- **Tabs Component**:
  - `Profiles` (Tab 1)
  - `Assignments` (Tab 2)
  - `Exceptions` (Tab 3)

### 3.2. Tab 1: Profiles & Weekly Blocks Editor
- **Table**: Lists existing profiles.
- **Dialog Editor (`PMResourceCalendarProfileDialog`)**:
  - **Mon-Sun Summary**: A visual 7-column grid displaying the configured working hours for each day of the week. Days without blocks show an "Off-day" badge.
  - **Quick Copy**: A button labeled "Copy Monday to Weekdays (Tue-Fri)". Clicking this automatically populates Tuesday, Wednesday, Thursday, and Friday with duplicate block inputs matching Monday.
  - **Chronological Form**: Blocks are grouped and sorted by Day of Week.
  - **Inline Validation**: If `startTime >= endTime` or if overlapping blocks are detected on the same day, validation warnings are shown immediately.

### 3.3. Tab 2: Assignments & Bulk Actions
- **User Directory Table**:
  - Shows all organization users under the PM module.
  - Resolves name, email, and avatar (using `useGetOrganizationUsersQuery`).
  - Columns: Checkbox, User (Avatar + Name + Email), Assigned Calendar Profile (badge or dropdown), Effective Range, Actions (Edit/Remove).
- **Search & Filter**:
  - Search box: Filters the directory by name or email.
  - Dropdown Filter: Filter by Assigned Profile (e.g., only show "Unassigned", or "VN Full-time").
- **Bulk Action Bar**:
  - Appears floating at the top of the table when $\ge 1$ checkboxes are selected.
  - Displays `[N] users selected`.
  - Dropdown selector to choose a profile, a date-picker for start/end, and a button to "Assign Profile".
  - A secondary button to "Remove Calendar" in bulk.

### 3.4. Tab 3: Exceptions & Calendar Grid
- **Toggle View**:
  - `Calendar View` / `List View` switch.
- **Calendar View**:
  - Month calendar grid containing event tags for each exception.
  - **Color-Coding**:
    - `UNAVAILABLE` (Red)
    - `CAPACITY_OVERRIDE` (Orange/Yellow)
  - **Hover Tooltip**: Displays detailed reason, capacity factor, user name, and specific hours.
  - **Double-click creation**: Click or double-click a date cell to open `ExceptionDialog` with dates pre-filled.
- **List View**:
  - Table of exceptions showing columns: User (resolved name/email), Type, Window, Factor, Reason, Actions.
- **Form Enhancements (`ExceptionDialog`)**:
  - If type is `UNAVAILABLE`, the `capacityFactor` input is hidden or disabled.
  - If type is `CAPACITY_OVERRIDE`, the `capacityFactor` input is required and validated to be between 0.01 and 2.00.

---

## 4. Client State & API Integration
To resolve the numeric `userId` values returned by the Resource Calendar Overview API into user names, email, and avatar:
1. `PMResourceCalendarSettingsSection` will invoke `useGetOrganizationUsersQuery` with `pageSize: 100` (or greater) to fetch the organization's user profiles.
2. Build a local dictionary lookup maps:
   ```typescript
   const userMap = useMemo(() => {
     const map = new Map<number, UserProfile>();
     (usersQuery.data?.data.items ?? []).forEach(user => {
       map.set(user.id, user);
     });
     return map;
   }, [usersQuery.data]);
   ```
3. Components like `PMResourceCalendarExceptionPanel` and `PMResourceCalendarAssignmentPanel` will query this map to display proper user labels (e.g., `Huy Nguyen (huy.nguyen@example.com)`) instead of raw IDs.

---

## 5. Verification Plan
- **Manual Verification**:
  1. Verify navigation across all three tabs behaves properly.
  2. Create a new calendar profile, add a block to Monday, click "Copy Monday to Weekdays", and check that Tuesday-Friday are correctly populated.
  3. Search for a specific user in Tab 2, check their checkbox, select a profile and date range, and click "Assign Profile". Verify they are successfully assigned.
  4. Create an exception for a user. Verify it shows up in red or orange on the Tab 3 Month Calendar and displays a tooltip on hover.
