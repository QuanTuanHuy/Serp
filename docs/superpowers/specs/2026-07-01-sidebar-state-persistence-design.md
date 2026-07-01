# Design Spec: DynamicSidebar Collapse State Persistence

This specification details the implementation of saving and restoring the expand/collapse state of the global `DynamicSidebar` using browser `localStorage`.

## Objective

Currently, the `DynamicSidebar` component defaults to expanded (`isCollapsed = false`) upon initial rendering. When users collapse the sidebar, their preference is lost upon page refresh or when opening new modules. This design solves that by persisting the user's sidebar preference client-side using `localStorage`.

## Selected Approach

We will use **Option A (localStorage with try-catch & useEffect)** to maintain consistency with the existing sidebar contexts in the codebase (e.g., `ProfileSidebarContext`, `AdminSidebarContext`), while improving robustness by wrapping operations in `try-catch` blocks to prevent crashes in private browsing mode or when storage is restricted.

To prevent SSR (Server-Side Rendering) hydration mismatches, the initial render will default to expanded (`false`), and the state will be restored from storage inside a client-side `useEffect` hook.

## Proposed Changes

### [shared-components]

#### [MODIFY] [SidebarContext.tsx](file:///d:/User2/open_source/serp/serp_web/src/shared/components/DynamicSidebar/SidebarContext.tsx)

We will modify `SidebarProvider` to:
1. Define a persistent storage key: `serp-dynamic-sidebar-collapsed`.
2. Add a `useEffect` hook to read the state from `localStorage` on client-side mount.
3. Wrap `localStorage.getItem` and `localStorage.setItem` in `try-catch` blocks.
4. Update `toggleSidebar` and `setIsCollapsed` to save the new state value in storage.

```tsx
const SIDEBAR_STORAGE_KEY = 'serp-dynamic-sidebar-collapsed';

export const SidebarProvider: React.FC<SidebarProviderProps> = ({
  children,
}) => {
  const [isCollapsed, setIsCollapsed] = useState(false);

  // Restore sidebar state from localStorage on client mount
  useEffect(() => {
    try {
      const stored = localStorage.getItem(SIDEBAR_STORAGE_KEY);
      if (stored !== null) {
        setIsCollapsed(stored === 'true');
      }
    } catch (error) {
      console.warn('Failed to read sidebar state from localStorage:', error);
    }
  }, []);

  const handleSetIsCollapsed = (value: boolean) => {
    setIsCollapsed(value);
    try {
      localStorage.setItem(SIDEBAR_STORAGE_KEY, String(value));
    } catch (error) {
      console.warn('Failed to save sidebar state to localStorage:', error);
    }
  };

  const toggleSidebar = () => {
    setIsCollapsed((prev) => {
      const newState = !prev;
      try {
        localStorage.setItem(SIDEBAR_STORAGE_KEY, String(newState));
      } catch (error) {
        console.warn('Failed to save sidebar state to localStorage:', error);
      }
      return newState;
    });
  };

  return (
    <SidebarContext.Provider
      value={{
        isCollapsed,
        setIsCollapsed: handleSetIsCollapsed,
        toggleSidebar,
      }}
    >
      {children}
    </SidebarContext.Provider>
  );
};
```

## Verification Plan

### Automated Tests
* Run `npm run type-check` from `serp_web/` directory to ensure type safety.
* Run `npm run lint` from `serp_web/` directory to verify clean code formatting.

### Manual Verification
1. Start the dev server using `npm run dev` in `serp_web/`.
2. Go to any module page containing the sidebar.
3. Collapse the sidebar, then refresh the page. Confirm the sidebar remains collapsed.
4. Expand the sidebar, then refresh the page. Confirm the sidebar remains expanded.
5. Open an Incognito/Private window. Confirm that collapsing/expanding the sidebar works without throwing any browser errors or freezing the app.
