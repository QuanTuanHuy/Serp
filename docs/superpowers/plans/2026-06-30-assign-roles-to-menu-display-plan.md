# Assign Roles to Menu Display Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide a single synchronize API `PUT /api/v1/menu-displays/{id}/roles` and update the admin menu display role assignment dialog to sync all roles in one call, eliminating network request loops.

**Architecture:** Frontend submits all checked role IDs to the new menu-centric PUT API. The API Gateway forwards the request transparently using `genericProxyController` to the `account` microservice, which performs strict validation and differential updates under a single database transaction.

**Tech Stack:** React 19, Next.js 15, TypeScript, RTK Query, Spring Boot (Java 17), Go, PostgreSQL.

---

### Task 1: Backend DTO and Constants
**Files:**
- Create: `account/src/main/java/serp/project/account/core/domain/dto/request/UpdateMenuDisplayRolesDto.java`
- Modify: `account/src/main/java/serp/project/account/core/domain/constant/Constants.java`

- [ ] **Step 1: Create request DTO**
  Create file `account/src/main/java/serp/project/account/core/domain/dto/request/UpdateMenuDisplayRolesDto.java` with contents:
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

- [ ] **Step 2: Add validation error message**
  Modify `account/src/main/java/serp/project/account/core/domain/constant/Constants.java` inside the static nested class `ErrorMessage` to add the `ROLE_MODULE_MISMATCH` string.
  Line after `ROLE_CANNOT_ASSIGN_MENU_DISPLAYS`:
  ```java
          public static final String ROLE_MODULE_MISMATCH = "Role module does not match menu display module";
  ```

- [ ] **Step 3: Verify Compilation**
  Run: `mvnw.cmd clean compile` inside the `account` directory.
  Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**
  Run: `git add account/src/main/java/serp/project/account/core/domain/dto/request/UpdateMenuDisplayRolesDto.java account/src/main/java/serp/project/account/core/domain/constant/Constants.java` and commit with message `feat(account): add UpdateMenuDisplayRolesDto and new validation constant`

---

### Task 2: Backend Port and Adapter
**Files:**
- Modify: `account/src/main/java/serp/project/account/core/port/store/IMenuDisplayRolePort.java`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/adapter/MenuDisplayRoleAdapter.java`

- [ ] **Step 1: Declare deleteByMenuDisplayIdAndRoleIds**
  Modify `account/src/main/java/serp/project/account/core/port/store/IMenuDisplayRolePort.java` to append:
  ```java
      void deleteByMenuDisplayIdAndRoleIds(Long menuDisplayId, List<Long> roleIds);
  ```

- [ ] **Step 2: Implement deleteByMenuDisplayIdAndRoleIds**
  Modify `account/src/main/java/serp/project/account/infrastructure/store/adapter/MenuDisplayRoleAdapter.java` to implement the new method:
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
  *Make sure to import `java.util.stream.Collectors` if not already imported.*

- [ ] **Step 3: Verify Compilation**
  Run: `mvnw.cmd clean compile` inside the `account` directory.
  Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**
  Run: `git add account/src/main/java/serp/project/account/core/port/store/IMenuDisplayRolePort.java account/src/main/java/serp/project/account/infrastructure/store/adapter/MenuDisplayRoleAdapter.java` and commit with message `feat(account): implement deleteByMenuDisplayIdAndRoleIds in port and adapter`

---

### Task 3: Backend Service Layer
**Files:**
- Modify: `account/src/main/java/serp/project/account/core/service/IMenuDisplayService.java`
- Modify: `account/src/main/java/serp/project/account/core/service/impl/MenuDisplayService.java`

- [ ] **Step 1: Declare service method**
  Modify `account/src/main/java/serp/project/account/core/service/IMenuDisplayService.java` to declare:
  ```java
      void updateMenuDisplayRoles(Long menuDisplayId, List<Long> roleIds);
  ```

- [ ] **Step 2: Implement service method**
  Modify `account/src/main/java/serp/project/account/core/service/impl/MenuDisplayService.java` to implement:
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
                                  .createdAt(java.time.Instant.now().toEpochMilli())
                                  .updatedAt(java.time.Instant.now().toEpochMilli())
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

- [ ] **Step 3: Verify Compilation**
  Run: `mvnw.cmd clean compile` inside the `account` directory.
  Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**
  Run: `git add account/src/main/java/serp/project/account/core/service/IMenuDisplayService.java account/src/main/java/serp/project/account/core/service/impl/MenuDisplayService.java` and commit with message `feat(account): implement updateMenuDisplayRoles service logic`

---

### Task 4: Backend UseCase & Controller
**Files:**
- Modify: `account/src/main/java/serp/project/account/core/usecase/MenuDisplayUseCase.java`
- Modify: `account/src/main/java/serp/project/account/ui/controller/MenuDisplayController.java`

- [ ] **Step 1: Implement UseCase logic**
  Modify `account/src/main/java/serp/project/account/core/usecase/MenuDisplayUseCase.java` to add:
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

- [ ] **Step 2: Add import in MenuDisplayController.java**
  Modify `account/src/main/java/serp/project/account/ui/controller/MenuDisplayController.java` to import `UpdateMenuDisplayRolesDto`:
  ```java
  import serp.project.account.core.domain.dto.request.UpdateMenuDisplayRolesDto;
  ```

- [ ] **Step 3: Register controller endpoint**
  Modify `account/src/main/java/serp/project/account/ui/controller/MenuDisplayController.java` to add endpoint:
  ```java
      @PutMapping("/menu-displays/{id}/roles")
      public ResponseEntity<?> updateMenuDisplayRoles(
              @PathVariable Long id,
              @Valid @RequestBody UpdateMenuDisplayRolesDto request) {
          var response = menuDisplayUseCase.updateMenuDisplayRoles(id, request);
          return ResponseEntity.status(response.getCode()).body(response);
      }
  ```

- [ ] **Step 4: Verify Compilation**
  Run: `mvnw.cmd clean compile` inside the `account` directory.
  Expected: BUILD SUCCESS

- [ ] **Step 5: Run tests**
  Run: `mvnw.cmd test` inside the `account` directory to ensure no regressions.
  Expected: BUILD SUCCESS, all tests passing.

- [ ] **Step 6: Commit**
  Run: `git add account/src/main/java/serp/project/account/core/usecase/MenuDisplayUseCase.java account/src/main/java/serp/project/account/ui/controller/MenuDisplayController.java` and commit with message `feat(account): implement use case and controller for menu display roles update`

---

### Task 5: API Gateway Router Registration
**Files:**
- Modify: `api_gateway/src/ui/router/account_router.go`

- [ ] **Step 1: Register path in router**
  Modify `api_gateway/src/ui/router/account_router.go` to add routing for `/api/v1/menu-displays/:id/roles` using `genericProxyController.ProxyHandler("account")`.
  Add it under `menuDisplayV1` group:
  ```go
  		menuDisplayV1.Use(middleware.AuthMiddleware()).PUT("/:id/roles", genericProxyController.ProxyHandler("account"))
  ```

- [ ] **Step 2: Verify Go compilation**
  Run: `go build -o bin/app src/main.go` inside the `api_gateway` directory.
  Expected: Successful compilation, no errors.

- [ ] **Step 3: Commit**
  Run: `git add api_gateway/src/ui/router/account_router.go` and commit with message `feat(api_gateway): route PUT /menu-displays/:id/roles via genericProxyController`

---

### Task 6: Frontend API Service Update
**Files:**
- Modify: `serp_web/src/modules/admin/services/menu-displays/menuDisplaysApi.ts`

- [ ] **Step 1: Update type definitions if necessary**
  Verify if we need to modify `serp_web/src/modules/admin/types/index.ts` to export new DTO. (We do not strictly need type declarations if passing directly as parameters, but let's check).
  Let's add `updateMenuDisplayRoles` endpoint in `menuDisplaysApi.ts` right after `deleteMenuDisplay` mutation:
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
        ],
      }),
  ```
  Also update exports list:
  ```typescript
    useUpdateMenuDisplayRolesMutation,
  ```

- [ ] **Step 2: Commit**
  Run: `git add serp_web/src/modules/admin/services/menu-displays/menuDisplaysApi.ts` and commit with message `feat(web): add updateMenuDisplayRoles mutation hook`

---

### Task 7: Frontend useMenuDisplays Hook Update
**Files:**
- Modify: `serp_web/src/modules/admin/hooks/useMenuDisplays.ts`

- [ ] **Step 1: Import new mutation**
  Modify imports from `../services/adminApi` at the top of `serp_web/src/modules/admin/hooks/useMenuDisplays.ts` to import `useUpdateMenuDisplayRolesMutation`.
  Remove unused `useAssignMenuDisplaysToRoleMutation` and `useUnassignMenuDisplaysFromRoleMutation`.

- [ ] **Step 2: Declare mutation state**
  Replace:
  ```typescript
    const [assignMenuDisplaysToRole, { isLoading: isAssigningRole }] =
      useAssignMenuDisplaysToRoleMutation();
    const [unassignMenuDisplaysFromRole, { isLoading: isUnassigningRole }] =
      useUnassignMenuDisplaysFromRoleMutation();
  ```
  with:
  ```typescript
    const [updateMenuDisplayRoles, { isLoading: isUpdatingRoles }] =
      useUpdateMenuDisplayRolesMutation();
  ```

- [ ] **Step 3: Implement handleUpdateMenuDisplayRoles**
  Replace `handleAssignRole` and `handleUnassignRole` with:
  ```typescript
    const handleUpdateMenuDisplayRoles = useCallback(
      async (menuDisplayId: number, roleIds: number[]) => {
        try {
          const message = await updateMenuDisplayRoles({
            id: menuDisplayId,
            roleIds,
          }).unwrap();
          toast.success(message);
          refetch();
        } catch (error: any) {
          const errorMessage =
            getErrorMessage(error) || 'Failed to update roles';
          toast.error(errorMessage);
          throw error;
        }
      },
      [updateMenuDisplayRoles, refetch]
    );
  ```

- [ ] **Step 4: Update return values**
  Replace returned variables:
  `isAssigningRole`, `isUnassigningRole`, `handleAssignRole`, `handleUnassignRole`
  with:
  `isUpdatingRoles`, `handleUpdateMenuDisplayRoles`.

- [ ] **Step 5: Commit**
  Run: `git add serp_web/src/modules/admin/hooks/useMenuDisplays.ts` and commit with message `feat(web): update useMenuDisplays hook to use updateMenuDisplayRoles`

---

### Task 8: Frontend UI Dialog Update
**Files:**
- Modify: `serp_web/src/modules/admin/components/menu-displays/RoleAssignmentDialog.tsx`
- Modify: `serp_web/src/app/admin/menu-displays/page.tsx`

- [ ] **Step 1: Update RoleAssignmentDialog props**
  Modify `serp_web/src/modules/admin/components/menu-displays/RoleAssignmentDialog.tsx` props type:
  ```typescript
  interface RoleAssignmentDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    menuDisplay: MenuDisplayDetail | null;
    onUpdateRoles: (menuDisplayId: number, roleIds: number[]) => Promise<void>;
    isLoading?: boolean;
  }
  ```

- [ ] **Step 2: Update selectedRoleIds initialization**
  Change the `useEffect` inside `RoleAssignmentDialog` that resets state:
  ```typescript
    React.useEffect(() => {
      if (open) {
        if (menuDisplay?.assignedRoles) {
          setSelectedRoleIds(new Set(menuDisplay.assignedRoles.map((r) => r.roleId)));
        } else {
          setSelectedRoleIds(new Set());
        }
        setSearchInput('');
      }
    }, [open, menuDisplay]);
  ```

- [ ] **Step 3: Simplify and enhance status badges**
  Change `getRoleStatus` helper:
  ```typescript
    const getRoleStatus = (roleId: number): 'assigned' | 'unassigned' | 'new' | 'removed' => {
      const wasAssigned = assignedRoleIds.has(roleId);
      const isSelected = selectedRoleIds.has(roleId);

      if (wasAssigned && isSelected) return 'assigned';
      if (!wasAssigned && isSelected) return 'new';
      if (wasAssigned && !isSelected) return 'removed';
      return 'unassigned';
    };
  ```
  And update render badges in JSX (around line 215-231):
  ```typescript
                            {status === 'new' && (
                              <Badge variant='default' className='text-xs bg-green-600 hover:bg-green-600 text-white'>
                                Will Assign
                              </Badge>
                            )}
                            {status === 'removed' && (
                              <Badge variant='destructive' className='text-xs'>
                                Will Remove
                              </Badge>
                            )}
  ```

- [ ] **Step 4: Update handleApply and change detection**
  Define `hasChanged` using `useMemo`:
  ```typescript
    const hasChanged = useMemo(() => {
      if (assignedRoleIds.size !== selectedRoleIds.size) return true;
      for (const id of selectedRoleIds) {
        if (!assignedRoleIds.has(id)) return true;
      }
      return false;
    }, [assignedRoleIds, selectedRoleIds]);
  ```
  Update `handleApply`:
  ```typescript
    const handleApply = async () => {
      if (!menuDisplay?.id) return;

      setProcessing(true);
      try {
        await onUpdateRoles(menuDisplay.id, Array.from(selectedRoleIds));
        onOpenChange(false);
      } catch (error) {
        console.error('Role assignment error:', error);
      } finally {
        setProcessing(false);
      }
    };
  ```
  Update "Apply Changes" button disabled condition:
  ```typescript
              disabled={
                !hasChanged ||
                processing ||
                isLoading ||
                !menuDisplay
              }
  ```

- [ ] **Step 5: Connect callback in page.tsx**
  Modify `serp_web/src/app/admin/menu-displays/page.tsx` rendering of `RoleAssignmentDialog`:
  ```tsx
        <RoleAssignmentDialog
          open={roleDialogOpen}
          onOpenChange={(open) => (open ? null : closeRoleDialog())}
          menuDisplay={selectedMenuForRole}
          onUpdateRoles={handleUpdateMenuDisplayRoles}
          isLoading={isUpdatingRoles}
        />
  ```

- [ ] **Step 6: Verify Frontend Compilation**
  Run: `npm run type-check` inside `serp_web` directory.
  Expected: Successful compilation, no type errors.
  Run: `npm run lint` inside `serp_web` directory.
  Expected: Successful lint check.

- [ ] **Step 7: Commit**
  Run: `git add serp_web/src/modules/admin/components/menu-displays/RoleAssignmentDialog.tsx serp_web/src/app/admin/menu-displays/page.tsx` and commit with message `feat(web): integrate new updateMenuDisplayRoles API in Dialog`
