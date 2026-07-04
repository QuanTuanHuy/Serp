# Design Spec: Settings Module Access Success Response Message Parsing

**Date**: 2026-07-04  
**Author**: Antigravity  
**Status**: Approved

## Context
In the `serp_web` frontend, the "Module Access" screen of the Settings module handles assigning and revoking access for users to various organization modules. Currently, upon success, the notifications show hardcoded success messages: `'User assigned to module'` and `'User access revoked'`. 

We want to parse the success message returned from the backend response instead, falling back to a default message only if the backend does not return one.

## Proposed Changes

### 1. API Utilities: Add `getResponseMessage`
We will introduce a helper function `getResponseMessage` in `src/lib/store/api/utils.ts` to extract the message field from standard API response objects (both raw and transformed responses).

**File**: [utils.ts](file:///d:/User2/open_source/serp/serp_web/src/lib/store/api/utils.ts)
```typescript
/**
 * Extract success/response message from API response
 */
export const getResponseMessage = (response: any, fallback = 'Success'): string => {
  if (response?.message) {
    return response.message;
  }
  if (response?.data?.message) {
    return response.data.message;
  }
  return fallback;
};
```

---

### 2. API Endpoint Transformation Updates
We will update `assignUserToModule` and `revokeUserAccessToModule` mutations in `modulesApi.ts` to return the `ApiResponse<any>` type and use `createApiResponseTransform<any>()` to parse/format the response data. This ensures the full response body containing `message` is accessible to components when using `.unwrap()`.

**File**: [modulesApi.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/services/modules/modulesApi.ts)
- Import `ApiResponse` from `@/lib/store/api/types`.
- Import `createApiResponseTransform` from `@/lib/store/api/utils`.
- Change `assignUserToModule` signature to return `ApiResponse<any>` and specify `transformResponse: createApiResponseTransform<any>()`.
- Change `revokeUserAccessToModule` signature to return `ApiResponse<any>` and specify `transformResponse: createApiResponseTransform<any>()`.

---

### 3. Hook Integration
We will update `useModules.ts` to import `getResponseMessage` and pass the mutation result to it to dynamically show the response message in success notification toast.

**File**: [useModules.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/settings/hooks/useModules.ts)
- Import `getResponseMessage` from `@/lib/store/api/utils`.
- Update `assign` function:
  ```typescript
  const result = await assignUser({ organizationId, moduleId, userId, roleId }).unwrap();
  success(getResponseMessage(result, 'User assigned to module'));
  ```
- Update `revoke` function:
  ```typescript
  const result = await revokeUser({ organizationId, moduleId, userId }).unwrap();
  success(getResponseMessage(result, 'User access revoked'));
  ```
- Update the `useEffect` error handler for modules list query:
  ```typescript
  useEffect(() => {
    if (error) {
      showError(getErrorMessage(error));
    }
  }, [error, showError]);
  ```

## Verification Plan

### Automated Verification
Run build and type check to verify compilation correctness:
```bash
npm run type-check
```

### Manual Verification
- Trigger an error for the modules query (e.g. by passing an invalid organization ID or mocking a server error) and check that the notification shows the custom error message parsed from the API response instead of "Failed to load modules".
- Assign a user to a module and verify that the custom success message returned by the backend (e.g. `"User access granted to module"`) is displayed in the notification.
- Revoke access and verify the corresponding success message is displayed.
