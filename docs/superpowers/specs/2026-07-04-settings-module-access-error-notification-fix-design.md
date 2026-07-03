# Design Spec: Fix Duplicate Error Notifications in Settings Module Access

**Date**: 2026-07-04  
**Author**: Antigravity  
**Status**: Pending

## Root Cause Analysis
The get modules API (`/api/v1/organizations/{id}/modules`) is called once but triggers the error toast 3 times due to the following factors:

1. **Multiple Hook Instantiations**: The `useSettingsModules()` hook is instantiated in two components on the screen:
   - `SettingsModulesPage` (the main page wrapper)
   - `ModuleUsersDialog` (a child dialog component)
   Both instances subscribe to the RTK Query cache. Under the hood, RTK Query de-duplicates the query request (resulting in 1 network call), but both hook instances receive the error state and run their respective `useEffect` blocks.
2. **Unstable Dependency**: The `useEffect` inside `useSettingsModules()` depends on `showError` returned by `useNotification()`. `useNotification()` returns a new object and new functions on every single render, causing `useEffect` to execute again on subsequent re-renders even if the query's `error` reference has not changed.

---

## Proposed Changes

### 1. Hook Options: Add `skipQuery` in `useSettingsModules`
We will add a `skipQuery` option to the hook to allow components that only need mutations or sub-queries (like `ModuleUsersDialog`) to skip subscribing to the main modules query.

**File**: [useModules.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/hooks/useModules.ts)
```typescript
export function useSettingsModules(options?: { skipQuery?: boolean }) {
  // ...
  const {
    data: modules,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useGetAccessibleModulesForOrganizationQuery(organizationId as number, {
    skip: !organizationId || options?.skipQuery,
  });
  // ...
}
```

### 2. Error Notification Refactoring: Use Ref to Ensure One-Time Trigger
We will track the last shown error using a `useRef` within `useSettingsModules()`. This ensures that even if the hook re-renders or `showError` reference changes, the error notification toast is only shown once per actual error change.

**File**: [useModules.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/hooks/useModules.ts)
```typescript
  const lastErrorRef = useRef<any>(null);

  useEffect(() => {
    if (error && error !== lastErrorRef.current) {
      showError(getErrorMessage(error));
      lastErrorRef.current = error;
    } else if (!error) {
      lastErrorRef.current = null;
    }
  }, [error, showError]);
```

### 3. Component Updates: Use `skipQuery: true` in `ModuleUsersDialog`
We will configure `ModuleUsersDialog` to skip subscribing to the modules list query since it only needs management operations.

**File**: [ModuleUsersDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/components/modules/ModuleUsersDialog.tsx)
```typescript
  const {
    assign,
    revoke,
    assignStatus,
    revokeStatus,
    useModuleRoles,
    useModuleUsers,
  } = useSettingsModules({ skipQuery: true });
```

---

## Verification Plan

### Automated Verification
Run:
```bash
npm run type-check
```

### Manual Verification
- Simulate a network failure on the get modules API.
- Verify that only one error toast is shown to the user.
