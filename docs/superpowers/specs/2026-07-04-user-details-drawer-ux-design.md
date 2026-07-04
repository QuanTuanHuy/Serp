# User Details Drawer UX/UI Improvements Design Spec

*   **Date:** 2026-07-04
*   **Author:** QuanTuanHuy & Antigravity (AI Coding Assistant)
*   **Status:** Draft / Pending Review

## 1. Goal

Enhance the User Experience (UX) and User Interface (UI) of the [UserDetailsDrawer.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/admin/components/users/UserDetailsDrawer.tsx) component. 
The main issues addressed are:
- The "Module access" list doesn't display fully when it is long due to a nested scroll container with a fixed height.
- Action buttons are vertically stacked on the top right, causing clutter.
- Info tiles have heavy borders and icons, making the layout feel visually busy.

---

## 2. Proposed Design

### Layout Restructuring
- Convert the entire drawer content (`SheetContent`) from a simple vertically scrolling container into a **fixed-height flex column** (`flex flex-col h-full`).
- **Header:** Sticky top header using `SheetHeader`.
- **Content Area:** Scrollable main area (`flex-1 overflow-y-auto px-6 py-5`) containing:
  - **User Hero Section:** Avatar, Name, Email, Status badge, and User Type badge. This section remains at the top of the scrollable content.
  - **Tabs Navigation:** Using `Tabs`, `TabsList`, `TabsTrigger`, and `TabsContent`.
- **Footer:** Sticky bottom footer (`border-t px-6 py-4 flex justify-end gap-2 bg-background`) containing all action buttons (Edit, Access, Suspend/Activate). This keeps the actions always within reach.

### Tab 1: Overview
- **Info Grid:** Render key-value information (Email, Organization, Phone, Timezone, Last Login, Created) in a clean 2-column grid. Remove the heavy `InfoTile` borders and background. Use subtle icon styling.
- **Roles & Departments:** Rendered as clean wrapping badge lists under their respective headings.

### Tab 2: Module Access
- **Search Filter:** Add a client-side search input (`Search modules...`) at the top of this tab to allow admins to quickly filter modules when the list is long.
- **Natural Scrolling:** Remove the inner `ScrollArea` with `max-h-56`. Let the module list render fully and scroll naturally inside the drawer's main content area.
- **Row Styling:** Modernize rows to look like clean list items with:
  - Module Name & Code on the left.
  - Active/Disabled status on the right using light-colored status badges (e.g., light green for enabled, light gray for disabled) matching current design conventions.

---

## 3. Component Architecture & State

### New State Variables
- `activeTab`: string state (`'overview' | 'access'`) to control the selected tab (defaults to `'overview'`).
- `searchTerm`: string state in the Module Access tab to filter `user.moduleAccesses` dynamically.

### Component Imports
- Import `Tabs`, `TabsContent`, `TabsList`, `TabsTrigger` from `@/shared/components/ui`.
- Import `Input` from `@/shared/components/ui/input` (for search).

---

## 4. Verification Plan

### Automated Checks
- Verify that `serp_web` builds and typescript check passes:
  ```bash
  npm run type-check
  npm run lint
  ```

### Manual Verification
- Open the user list in the admin panel.
- Click on a user with a large number of module accesses to open the drawer.
- Verify:
  1. The layout is clean and does not jump.
  2. Tab navigation works perfectly.
  3. Action buttons are sticky in the footer.
  4. Search filter dynamically filters module accesses in real-time.
  5. The nested scroll is gone and scrolling is smooth.
