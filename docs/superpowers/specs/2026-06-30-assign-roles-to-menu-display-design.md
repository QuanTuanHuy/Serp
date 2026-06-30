# Design Spec: Assign Roles to Menu Display

Part of Serp Project - Admin Module Navigation Management API and UX refinement.

## Overview
Currently, the Menu Displays configuration in the admin module has role assignment endpoints defined in a role-centric way (`assignMenuDisplaysToRole` / `unassignMenuDisplaysFromRole`). To update the roles associated with a menu display, the frontend has to perform a loop making multiple sequential or parallel API calls.
This design spec introduces a single menu-centric synchronize API endpoint: `PUT /api/v1/menu-displays/{id}/roles` routed through `api_gateway` to the `account` microservice. This endpoint will accept a list of `roleIds` and perform a differential update (delta sync) in a single database transaction.

To minimize boilerplate code, the API Gateway will use the `genericProxyController` to forward requests directly to the `account` service.

## Architectural Changes

```mermaid
graph TD
    subgraph Frontend (serp_web)
        RoleAssignmentDialog[RoleAssignmentDialog.tsx] -->|calls| useMenuDisplays[useMenuDisplays.ts]
        useMenuDisplays -->|triggers mutation| menuDisplaysApi[menuDisplaysApi.ts]
    end

    subgraph Backend (api_gateway Go)
        menuDisplaysApi -->|HTTP PUT /menu-displays/{id}/roles| account_router[account_router.go]
        account_router -->|genericProxyController| account_service_http[account service port]
    end

    subgraph Backend (account service Java)
        account_service_http -->|HTTP PUT /menu-displays/{id}/roles| MenuDisplayController[MenuDisplayController.java]
        MenuDisplayController -->|calls| MenuDisplayUseCase[MenuDisplayUseCase.java]
        MenuDisplayUseCase -->|validates & calls| MenuDisplayService[MenuDisplayService.java]
        MenuDisplayService -->|reads/writes via| IMenuDisplayRolePort[IMenuDisplayRolePort.java]
        IMenuDisplayRolePort -->|batch inserts/deletes| MenuDisplayRoleAdapter[MenuDisplayRoleAdapter.java]
        MenuDisplayRoleAdapter -->|SQL batchUpdate| JdbcTemplate[JdbcTemplate / PostgreSQL]
    end
```

## Detailed Changes

### 1. API Gateway (Go)

#### Router: [account_router.go](file:///d:/User2/open_source/serp/api_gateway/src/ui/router/account_router.go)
Add PUT route in `menuDisplayV1` group proxying to the `account` service:
```go
		menuDisplayV1.Use(middleware.AuthMiddleware()).PUT("/:id/roles", genericProxyController.ProxyHandler("account"))
```
*(No new DTOs, controllers, services, ports, or client adapters are needed in the API Gateway since the request is proxied transparently)*

---

### 2. Backend (Java Spring Boot - `account` service)

#### Request DTO: [UpdateMenuDisplayRolesDto.java](file:///d:/User2/open_source/serp/account/src/main/java/serp/project/account/core/domain/dto/request/UpdateMenuDisplayRolesDto.java) [NEW]
```java
package serp.project.account.core.domain.dto.request;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMenuDisplayRolesDto {
    @NotNull
    private List<@Min(1) Long> roleIds;
}
```

#### New Error Constant in [Constants.java](file:///d:/User2/open_source/serp/account/src/main/java/serp/project/account/core/domain/constant/Constants.java)
```java
public static final String ROLE_MODULE_MISMATCH = "Role module does not match menu display module";
```

#### Controller: [MenuDisplayController.java](file:///d:/User2/open_source/serp/account/src/main/java/serp/project/account/ui/controller/MenuDisplayController.java)
Add new endpoint:
```java
    @PutMapping("/menu-displays/{id}/roles")
    public ResponseEntity<?> updateMenuDisplayRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuDisplayRolesDto request) {
        var response = menuDisplayUseCase.updateMenuDisplayRoles(id, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }
```

#### UseCase: [MenuDisplayUseCase.java](file:///d:/User2/open_source/serp/account/src/main/java/serp/project/account/core/usecase/MenuDisplayUseCase.java)
Validates that the menu display exists, all roles exist, can be assigned to menu displays, and system roles or module matches. If validation passes, calls the service.
```java
    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateMenuDisplayRoles(Long id, UpdateMenuDisplayRolesDto request) {
        try {
            var menuDisplay = menuDisplayService.getMenuDisplayById(id);
            if (menuDisplay == null) {
                throw new AppException(Constants.ErrorMessage.MENU_DISPLAY_NOT_FOUND);
            }
            List<Long> roleIds = request.getRoleIds().stream().distinct().toList();
            if (!roleIds.isEmpty()) {
                var roles = roleService.getAllRoles().stream()
                        .filter(r -> roleIds.contains(r.getId()))
                        .toList();
                if (roles.size() != roleIds.size()) {
                    throw new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND);
                }
                for (var role : roles) {
                    if (!role.canAssignToMenuDisplays()) {
                        throw new AppException(Constants.ErrorMessage.ROLE_CANNOT_ASSIGN_MENU_DISPLAYS);
                    }
                    if (!role.isSystemRole() && !Objects.equals(role.getModuleId(), menuDisplay.getModuleId())) {
                        throw new AppException(Constants.ErrorMessage.ROLE_MODULE_MISMATCH);
                    }
                }
            }

            menuDisplayService.updateMenuDisplayRoles(id, roleIds);
            return responseUtils.success("Updated menu display roles successfully");
        } catch (AppException e) {
            log.error("Error updating menu display roles: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected Error updating menu display roles: {}", e.getMessage());
            throw e;
        }
    }
```

#### Service Layer: [IMenuDisplayService.java](file:///d:/User2/open_source/serp/account/src/main/java/serp/project/account/core/service/IMenuDisplayService.java) & [MenuDisplayService.java](file:///d:/User2/open_source/serp/account/src/main/java/serp/project/account/core/service/impl/MenuDisplayService.java)
Calculates the delta and commits inserts/deletes.
```java
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenuDisplayRoles(Long menuDisplayId, List<Long> roleIds) {
        try {
            var assignedRoles = menuDisplayRolePort.getByMenuDisplayIds(List.of(menuDisplayId));
            var assignedRoleIds = assignedRoles.stream()
                    .map(MenuDisplayRoleEntity::getRoleId)
                    .toList();

            var roleIdsToUnassign = assignedRoleIds.stream()
                    .filter(id -> !roleIds.contains(id))
                    .toList();

            var roleIdsToAssign = roleIds.stream()
                    .filter(id -> !assignedRoleIds.contains(id))
                    .toList();

            if (!roleIdsToUnassign.isEmpty()) {
                menuDisplayRolePort.deleteByMenuDisplayIdAndRoleIds(menuDisplayId, roleIdsToUnassign);
                log.info("Unassigned roles successfully from menu: {}, roles: {}", menuDisplayId, roleIdsToUnassign);
            }

            if (!roleIdsToAssign.isEmpty()) {
                List<MenuDisplayRoleEntity> newRelations = roleIdsToAssign.stream()
                        .map(roleId -> MenuDisplayRoleEntity.builder()
                                .roleId(roleId)
                                .menuDisplayId(menuDisplayId)
                                .createdAt(Instant.now().toEpochMilli())
                                .updatedAt(Instant.now().toEpochMilli())
                                .build())
                        .toList();
                menuDisplayRolePort.save(newRelations);
                log.info("Assigned roles successfully to menu: {}, roles: {}", menuDisplayId, roleIdsToAssign);
            }
        } catch (Exception e) {
            log.error("Error updating menu display roles: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.UPDATE_MENU_DISPLAY_FAILED);
        }
    }
```

#### Port & Adapter Layer: [IMenuDisplayRolePort.java](file:///d:/User2/open_source/serp/account/src/main/java/serp/project/account/core/port/store/IMenuDisplayRolePort.java) & [MenuDisplayRoleAdapter.java](file:///d:/User2/open_source/serp/account/src/main/java/serp/project/account/infrastructure/store/adapter/MenuDisplayRoleAdapter.java)
Adds `deleteByMenuDisplayIdAndRoleIds` to batch-delete mappings.
```java
    @Override
    public void deleteByMenuDisplayIdAndRoleIds(Long menuDisplayId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        String placeholders = roleIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "DELETE FROM menu_display_roles WHERE menu_display_id = ? AND role_id IN (" + placeholders + ")";
        Object[] params = new Object[1 + roleIds.size()];
        params[0] = menuDisplayId;
        for (int i = 0; i < roleIds.size(); i++) {
            params[i + 1] = roleIds.get(i);
        }
        jdbcTemplate.update(sql, params);
    }
```

---

### 3. Frontend (React/Next.js in `serp_web`)

#### API Mutation: [menuDisplaysApi.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/admin/services/menu-displays/menuDisplaysApi.ts)
Add `updateMenuDisplayRoles` endpoint:
```typescript
    updateMenuDisplayRoles: builder.mutation<
      string,
      { id: number; roleIds: number[] }
    >({
      query: ({ id, roleIds }) => ({
        url: `/menu-displays/${id}/roles`,
        method: 'PUT',
        body: { roleIds },
      }),
      transformResponse: createDataTransform<string>(),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'admin/MenuDisplay', id },
        { type: 'admin/MenuDisplay', id: 'LIST' },
        { type: 'admin/MenuDisplay', id: 'ROLE_LIST' },
        { type: 'admin/Role', id: 'LIST' },
      ],
    }),
```

#### Hooks: [useMenuDisplays.ts](file:///d:/User2/open_source/serp/serp_web/src/modules/admin/hooks/useMenuDisplays.ts)
Replace `useAssignMenuDisplaysToRoleMutation` and `useUnassignMenuDisplaysFromRoleMutation` with `useUpdateMenuDisplayRolesMutation`. Expose `handleUpdateMenuDisplayRoles` and `isUpdatingRoles`.

#### UI Component: [RoleAssignmentDialog.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/admin/components/menu-displays/RoleAssignmentDialog.tsx)
- Refactor props to accept `onUpdateRoles: (menuDisplayId: number, roleIds: number[]) => Promise<void>`.
- Initialize `selectedRoleIds` state with the pre-assigned role IDs.
- Display "Will Remove" (destructive badge) for roles currently assigned but unchecked.
- Enable the apply button only if the role assignments have changed (`hasChanged`).
- Submit all selected roles at once.

---

## Frontend Performance Validation (Vercel React Best Practices)

Applying the **Vercel React Best Practices** guidelines, we verified the following design aspects of the frontend:

1. **Eliminating Waterfalls (async-parallel, async-api-routes):**
   * **Issue:** Previously, updating role assignments executed sequential `await onAssign(...)` and `await onUnassign(...)` in a `for` loop inside `RoleAssignmentDialog.tsx`. This created an \(O(N)\) sequential network waterfall.
   * **Fix:** The new design collapses this into a single `PUT /menu-displays/{id}/roles` HTTP call. This completely eliminates the request waterfall, optimizing network bandwidth and resolving the highest-priority performance bottleneck.

2. **Re-render Optimization (rerender-derived-state-no-effect):**
   * **Pattern:** We compute `hasChanged` directly during render via `useMemo` based on `assignedRoleIds` and `selectedRoleIds`.
   * **Benefit:** We avoid tracking `hasChanged` as a separate React state set in a `useEffect`. Doing so would trigger an unnecessary secondary component re-render when selections change.

3. **JavaScript O(1) Lookup Performance (js-set-map-lookups):**
   * **Pattern:** Both `assignedRoleIds` and `selectedRoleIds` are managed as Javascript `Set` objects rather than lists.
   * **Benefit:** In `filteredRoles.map(...)`, lookups to check checkbox states (`selectedRoleIds.has(role.id)`) and badges run in \(O(1)\) time instead of \(O(N)\) array lookups, ensuring 60fps scrolling performance even when hundreds of roles are loaded.

4. **Callback Stability (rerender-functional-setstate):**
   * **Pattern:** We use the functional setter update pattern `setSelectedRoleIds((prev) => { ... })` in `handleToggleRole`.
   * **Benefit:** This prevents closure stale-state bugs and keeps handlers independent of local state changes, avoiding unnecessary recreation of event callback functions.

---

## Verification Plan

### Backend verification
- Add test or run existing maven tests in `account` to ensure build succeeds.
- Call the endpoint `PUT /api/v1/menu-displays/{id}/roles` with postman or curl to verify:
  - Assigns new roles correctly.
  - Removes unselected roles correctly.
  - Returns appropriate error if a role does not exist or module mismatches.

### Frontend verification
- Ensure `npm run lint` and `npm run type-check` pass.
- Open Role Assignment Dialog on a menu display, add/remove roles, and verify the badges show "Will Assign", "Will Remove" correctly.
- Save and verify only 1 API call is triggered and roles update instantly in the tree view.
