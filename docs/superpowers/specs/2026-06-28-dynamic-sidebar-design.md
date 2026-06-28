# Design Spec: Dynamic Sidebar UI/UX Improvements

**Author**: Antigravity
**Date**: 2026-06-28
**Status**: Proposal

## 1. Goal & Context

The goal is to improve the UI/UX of the `DynamicSidebar` component in `serp_web`. Currently, the sidebar transitions instantly and hides submenu lists completely when collapsed, creating a disjointed user experience.

This spec details a design to:
1. Enable a smooth, animated sidebar width transition.
2. Animate text labels and footer elements during collapse and expand actions instead of instantly hiding them.
3. Reposition the toggle button to a unified location in the header.
4. Implement hover tooltips for single items in the collapsed state.
5. Integrate Radix `DropdownMenu` to display recursive submenus when hover-activated or clicked in the collapsed state.
6. Animate inline submenu expansions when the sidebar is fully expanded.

---

## 2. Proposed UI/UX Improvements

### 2.1 Sidebar Width and Content Transitions
To achieve smooth layout changes:
*   **Sidebar container**: Animate width using `transition-[width] duration-300 ease-in-out` on the `<aside>` element.
*   **Text labels and footer content**: Wrap text labels in transition classes to fade their opacity and scale their maximum width dynamically.
    *   *Expanded state*: `max-w-[200px] opacity-100 transition-all duration-300 ease-in-out`
    *   *Collapsed state*: `max-w-0 opacity-0 pointer-events-none transition-all duration-300 ease-in-out`
*   **Header Logo & Unified Toggle**: Place a single toggle button in the header.
    *   When collapsed, the logo/module icon is centered, and the toggle button is positioned cleanly.
    *   When expanded, the logo, module name, and toggle button sit inline.
    *   The toggle button icon swaps cleanly between `PanelLeftOpen` and `PanelLeftClose`.
*   **Footer**: The module detail block slides up/down using max-height and opacity transitions.

### 2.2 Collapsed Submenus (Radix Tooltip & DropdownMenu)
When `isCollapsed` is `true`:
*   **Items without submenus**: Wrap the item link in a Radix `<Tooltip>` component. On hover, display a right-aligned tooltip containing `item.name`.
*   **Items with submenus**: Wrap the item link in a Radix `<DropdownMenu>`.
    *   The trigger is the item's icon button.
    *   The content is `<DropdownMenuContent side="right" align="start" sideOffset={12}>`.
    *   It renders a header with the parent item name, a separator, and recursively renders the child items.
    *   Sub-submenus use Radix `<DropdownMenuSub>`, `<DropdownMenuSubTrigger>`, and `<DropdownMenuSubContent>` for recursive flyout dropdown menus.

### 2.3 Expanded Submenu Accordion Slide
When `isCollapsed` is `false`:
*   Submenu containers animate from `max-h-0 opacity-0` to `max-h-96 opacity-100` with `transition-all duration-300 ease-in-out overflow-hidden` when expanded.

---

## 3. Component Architecture & Changes

### 3.1 `SidebarContext.tsx`
No structural changes needed. It will continue providing `isCollapsed`, `setIsCollapsed`, and `toggleSidebar`.

### 3.2 `SidebarMenuItem.tsx`
*   **Modify**: Update imports to include Radix Tooltip and Dropdown Menu components.
*   **Modify**: Restructure the render method. If `isCollapsed` is true, wrap the node in `Tooltip` (if no children) or `DropdownMenu` (if has children).
*   **Modify**: Implement a recursive helper function `renderDropdownItems(items)` to render child items using `<DropdownMenuItem>` or `<DropdownMenuSub>`.
*   **Modify**: Apply max-height transitions for expanded states.

### 3.3 `DynamicSidebar.tsx`
*   **Modify**: Update header structure to place the toggle button cleanly.
*   **Modify**: Refactor layout elements (Module title, description footer, loading states) to use CSS transitions instead of conditional React rendering (`{!isCollapsed && ...}`).

---

## 4. Verification Plan

### 4.1 Manual Verification
1.  **Sidebar Toggle**: Click the toggle button in the header. Verify the sidebar shrinks and grows smoothly (transition duration is 300ms).
2.  **Collapsed Hover Tooltip**: Hover over a menu item with no submenus when the sidebar is collapsed. Verify a tooltip with the menu name appears on the right.
3.  **Collapsed Submenu Dropdown**: Click or hover (if hover triggering is enabled) on a menu item with submenus when collapsed. Verify the dropdown panel appears containing the submenu items.
4.  **Nested Submenus**: Hover over submenus that contain children inside the collapsed dropdown. Verify they open as secondary flyouts.
5.  **Expanded Inline Accordion**: Click on a parent menu item when expanded. Verify submenus slide open smoothly.
6.  **Responsive Layout**: Verify the sidebar behaves correctly across viewport sizes and does not cause layout shifts to the main page content area.
