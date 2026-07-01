# Module Access Users Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make public and internal account user listing APIs return only users with active access to a requested module when `moduleId` is supplied.

**Architecture:** Add `moduleId` to the shared `GetUserParams` DTO and filter users in `UserQueryBuilder` with an `EXISTS` query on `user_module_access`. Both public `ModuleAccessController` and internal `InternalUserController` route module-user listing through `UserUseCase.getUsers`, preserving pagination, search, sort, and existing response shape.

**Tech Stack:** Java 21, Spring Boot 3.5.5, JUnit 5, Mockito, AssertJ, Spring JDBC `NamedParameterJdbcTemplate` query builder.

---

## File Structure

- Modify `account/src/main/java/serp/project/account/core/domain/dto/request/GetUserParams.java`
  - Add `moduleId` as an optional list filter.
- Modify `account/src/main/java/serp/project/account/infrastructure/store/query/UserQueryBuilder.java`
  - Add a module access SQL filter using `user_module_access`.
- Create `account/src/test/java/serp/project/account/infrastructure/store/query/UserQueryBuilderTest.java`
  - Assert generated SQL and SQL params for module access filtering.
- Modify `account/src/main/java/serp/project/account/core/usecase/UserUseCase.java`
  - Return `400 Bad Request` when `moduleId` is supplied without `organizationId`.
- Create `account/src/test/java/serp/project/account/core/usecase/UserUseCaseGetUsersTest.java`
  - Assert validation short-circuits before querying.
- Modify `account/src/main/java/serp/project/account/ui/controller/ModuleAccessController.java`
  - Make `GET /api/v1/organizations/{organizationId}/modules/{moduleId}/users` build `GetUserParams` and call `UserUseCase.getUsers`.
- Modify `account/src/main/java/serp/project/account/ui/controller/internal/InternalUserController.java`
  - Accept `moduleId`, `userType`, and `departmentId` query params and pass them to `GetUserParams`.
- Create `account/src/test/java/serp/project/account/ui/controller/ModuleAccessControllerTest.java`
  - Assert public controller passes path and query params into `GetUserParams`.
- Create `account/src/test/java/serp/project/account/ui/controller/internal/InternalUserControllerTest.java`
  - Assert internal controller passes `moduleId` and related filters into `GetUserParams`.

---

### Task 1: Add Failing Query Builder Tests

**Files:**
- Create: `account/src/test/java/serp/project/account/infrastructure/store/query/UserQueryBuilderTest.java`

- [ ] **Step 1: Create `UserQueryBuilderTest` with module access SQL assertions**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import serp.project.account.core.domain.dto.request.GetUserParams;

class UserQueryBuilderTest {

    private final UserQueryBuilder userQueryBuilder = new UserQueryBuilder();

    @Test
    void buildGetUsersQueryWithModuleIdAndOrganizationIdShouldFilterActiveModuleAccess() {
        GetUserParams params = GetUserParams.builder()
                .organizationId(10L)
                .moduleId(20L)
                .page(1)
                .pageSize(25)
                .sortBy("email")
                .sortDirection("asc")
                .build();

        SqlQueryResult result = userQueryBuilder.buildGetUsersQuery(params);

        assertThat(result.dataSql())
                .contains("FROM users u")
                .contains("EXISTS (SELECT 1 FROM user_module_access uma")
                .contains("uma.user_id = u.id")
                .contains("uma.organization_id = :organizationId")
                .contains("uma.module_id = :moduleId")
                .contains("uma.is_active = TRUE")
                .contains("ORDER BY u.email ASC")
                .contains("LIMIT :limit OFFSET :offset");
        assertThat(result.countSql())
                .contains("COUNT(*)")
                .contains("EXISTS (SELECT 1 FROM user_module_access uma")
                .contains("uma.module_id = :moduleId");
        assertThat(result.params().getValue("organizationId")).isEqualTo(10L);
        assertThat(result.params().getValue("moduleId")).isEqualTo(20L);
        assertThat(result.params().getValue("limit")).isEqualTo(25);
        assertThat(result.params().getValue("offset")).isEqualTo(25);
    }

    @Test
    void buildGetUsersQueryWithModuleIdShouldKeepExistingFilters() {
        GetUserParams params = GetUserParams.builder()
                .organizationId(10L)
                .moduleId(20L)
                .status("ACTIVE")
                .search("alice")
                .roleId(30L)
                .departmentId(40L)
                .build();

        SqlQueryResult result = userQueryBuilder.buildGetUsersQuery(params);

        assertThat(result.dataSql())
                .contains("u.primary_organization_id = :organizationId")
                .contains("u.status = :status")
                .contains("LOWER(u.email) LIKE :search")
                .contains("user_roles ur")
                .contains("user_departments ud")
                .contains("user_module_access uma");
        assertThat(result.params().getValue("status")).isEqualTo("ACTIVE");
        assertThat(result.params().getValue("search")).isEqualTo("%alice%");
        assertThat(result.params().getValue("roleId")).isEqualTo(30L);
        assertThat(result.params().getValue("departmentId")).isEqualTo(40L);
        assertThat(result.params().getValue("moduleId")).isEqualTo(20L);
    }

    @Test
    void buildGetUsersQueryWithoutModuleIdShouldNotJoinModuleAccess() {
        GetUserParams params = GetUserParams.builder()
                .organizationId(10L)
                .build();

        SqlQueryResult result = userQueryBuilder.buildGetUsersQuery(params);

        assertThat(result.dataSql()).doesNotContain("user_module_access");
        assertThat(result.countSql()).doesNotContain("user_module_access");
    }
}
```

- [ ] **Step 2: Run the focused query builder test and confirm it fails**

Run from `account/`:

```bash
.\mvnw.cmd -Dtest=UserQueryBuilderTest test
```

Expected result: compilation fails because `GetUserParams` does not have `moduleId`, or assertions fail because `UserQueryBuilder` does not include `user_module_access`.

- [ ] **Step 3: Commit the failing test**

```bash
git add account/src/test/java/serp/project/account/infrastructure/store/query/UserQueryBuilderTest.java
git commit -m "test(account): cover user module access query filter"
```

---

### Task 2: Implement `moduleId` in User Query Params and SQL Builder

**Files:**
- Modify: `account/src/main/java/serp/project/account/core/domain/dto/request/GetUserParams.java`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/query/UserQueryBuilder.java`
- Test: `account/src/test/java/serp/project/account/infrastructure/store/query/UserQueryBuilderTest.java`

- [ ] **Step 1: Add `moduleId` to `GetUserParams`**

Update `GetUserParams` to include the new field:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class GetUserParams extends BaseGetParams {
    private Long organizationId;
    private Long moduleId;
    private String search;
    private String status;
    private String userType;
    private Long roleId;
    private Long departmentId;
}
```

- [ ] **Step 2: Add the module access filter to `UserQueryBuilder`**

In `buildGetUsersQuery`, call the new filter after the organization filter:

```java
appendOrganizationFilter(whereClause, sqlParams, params);
appendModuleAccessFilter(whereClause, sqlParams, params);
appendStatusFilter(whereClause, sqlParams, params);
appendSearchFilter(whereClause, sqlParams, params);
appendUserTypeFilter(whereClause, sqlParams, params);
appendRoleFilter(whereClause, sqlParams, params);
appendDepartmentFilter(whereClause, sqlParams, params);
```

Add this private method to `UserQueryBuilder`:

```java
private void appendModuleAccessFilter(StringBuilder whereClause, MapSqlParameterSource sqlParams,
        GetUserParams params) {
    if (params.getModuleId() != null && params.getOrganizationId() != null) {
        whereClause.append(
                " AND EXISTS (SELECT 1 FROM user_module_access uma WHERE uma.user_id = u.id AND uma.organization_id = :organizationId AND uma.module_id = :moduleId AND uma.is_active = TRUE)");
        sqlParams.addValue("moduleId", params.getModuleId());
    }
}
```

- [ ] **Step 3: Run the focused query builder test and confirm it passes**

Run from `account/`:

```bash
.\mvnw.cmd -Dtest=UserQueryBuilderTest test
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 4: Commit the query builder implementation**

```bash
git add account/src/main/java/serp/project/account/core/domain/dto/request/GetUserParams.java account/src/main/java/serp/project/account/infrastructure/store/query/UserQueryBuilder.java
git commit -m "feat(account): filter users by module access"
```

---

### Task 3: Add `moduleId` Tenant-Scoped Validation in `UserUseCase`

**Files:**
- Create: `account/src/test/java/serp/project/account/core/usecase/UserUseCaseGetUsersTest.java`
- Modify: `account/src/main/java/serp/project/account/core/usecase/UserUseCase.java`

- [ ] **Step 1: Create a failing use case validation test**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.usecase.user.command.UserAccountCommandService;
import serp.project.account.core.usecase.user.command.UserProvisioningCoordinator;
import serp.project.account.core.usecase.user.command.UserRoleCoordinator;
import serp.project.account.core.usecase.user.export.UserExportService;
import serp.project.account.core.usecase.user.password.UserPasswordResetCoordinator;
import serp.project.account.core.usecase.user.query.UserQueryService;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class UserUseCaseGetUsersTest {

    private static final String ORGANIZATION_REQUIRED_FOR_MODULE_FILTER =
            "organizationId is required when moduleId is provided";

    @Mock
    private IOrganizationService organizationService;

    @Mock
    private UserProvisioningCoordinator userProvisioningCoordinator;

    @Mock
    private UserRoleCoordinator userRoleCoordinator;

    @Mock
    private UserAccountCommandService userAccountCommandService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserPasswordResetCoordinator userPasswordResetCoordinator;

    @Mock
    private UserExportService userExportService;

    @Mock
    private ResponseUtils responseUtils;

    @InjectMocks
    private UserUseCase userUseCase;

    @Test
    void getUsersWithModuleIdWithoutOrganizationIdShouldReturnBadRequest() {
        GeneralResponse<?> badRequest = GeneralResponse.builder()
                .status("ERROR")
                .code(400)
                .message(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER)
                .build();
        when(responseUtils.badRequest(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER)).thenReturn(badRequest);

        GeneralResponse<?> response = userUseCase.getUsers(GetUserParams.builder()
                .moduleId(20L)
                .build());

        assertEquals(400, response.getCode());
        assertEquals(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER, response.getMessage());
        verify(responseUtils).badRequest(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER);
        verifyNoInteractions(userQueryService);
    }
}
```

- [ ] **Step 2: Run the validation test and confirm it fails**

Run from `account/`:

```bash
.\mvnw.cmd -Dtest=UserUseCaseGetUsersTest test
```

Expected result: test fails because `UserUseCase.getUsers` still delegates to `userQueryService`.

- [ ] **Step 3: Implement validation in `UserUseCase`**

Add this constant near the fields:

```java
private static final String ORGANIZATION_REQUIRED_FOR_MODULE_FILTER =
        "organizationId is required when moduleId is provided";
```

Update `getUsers`:

```java
public GeneralResponse<?> getUsers(GetUserParams params) {
    try {
        if (params.getModuleId() != null && params.getOrganizationId() == null) {
            return responseUtils.badRequest(ORGANIZATION_REQUIRED_FOR_MODULE_FILTER);
        }
        return responseUtils.success(userQueryService.getUsers(params));
    } catch (AppException e) {
        log.error("Get users failed: {}", e.getMessage());
        return responseUtils.error(e.getCode(), e.getMessage());
    } catch (Exception e) {
        log.error("Unexpected error getting users", e);
        return responseUtils.internalServerError(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
    }
}
```

- [ ] **Step 4: Run the validation test and confirm it passes**

Run from `account/`:

```bash
.\mvnw.cmd -Dtest=UserUseCaseGetUsersTest test
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the validation change**

```bash
git add account/src/test/java/serp/project/account/core/usecase/UserUseCaseGetUsersTest.java account/src/main/java/serp/project/account/core/usecase/UserUseCase.java
git commit -m "fix(account): require organization for module user listing"
```

---

### Task 4: Route Public and Internal Controllers Through Shared User Listing

**Files:**
- Create: `account/src/test/java/serp/project/account/ui/controller/ModuleAccessControllerTest.java`
- Create: `account/src/test/java/serp/project/account/ui/controller/internal/InternalUserControllerTest.java`
- Modify: `account/src/main/java/serp/project/account/ui/controller/ModuleAccessController.java`
- Modify: `account/src/main/java/serp/project/account/ui/controller/internal/InternalUserController.java`

- [ ] **Step 1: Create a failing public controller test**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.usecase.ModuleAccessUseCase;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class ModuleAccessControllerTest {

    @Mock
    private ModuleAccessUseCase moduleAccessUseCase;

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private ResponseUtils responseUtils;

    @InjectMocks
    private ModuleAccessController controller;

    @Test
    void getUsersWithAccessToModuleShouldPassModuleFilterToUserUseCase() {
        when(authUtils.canAccessOrganization(10L)).thenReturn(true);
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("items", List.of()))
                .build();
        when(userUseCase.getUsers(any(GetUserParams.class))).thenReturn(responseBody);

        ResponseEntity<?> response = controller.getUsersWithAccessToModule(
                10L,
                20L,
                2,
                25,
                "email",
                "asc",
                "alice",
                "ACTIVE",
                "MEMBER",
                30L,
                40L);

        assertEquals(200, response.getStatusCodeValue());
        ArgumentCaptor<GetUserParams> captor = ArgumentCaptor.forClass(GetUserParams.class);
        verify(userUseCase).getUsers(captor.capture());
        assertEquals(10L, captor.getValue().getOrganizationId());
        assertEquals(20L, captor.getValue().getModuleId());
        assertEquals(2, captor.getValue().getPage());
        assertEquals(25, captor.getValue().getPageSize());
        assertEquals("email", captor.getValue().getSortBy());
        assertEquals("asc", captor.getValue().getSortDirection());
        assertEquals("alice", captor.getValue().getSearch());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals("MEMBER", captor.getValue().getUserType());
        assertEquals(30L, captor.getValue().getRoleId());
        assertEquals(40L, captor.getValue().getDepartmentId());
    }
}
```

- [ ] **Step 2: Create a failing internal controller test**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.usecase.UserUseCase;

@ExtendWith(MockitoExtension.class)
class InternalUserControllerTest {

    @Mock
    private UserUseCase userUseCase;

    @InjectMocks
    private InternalUserController controller;

    @Test
    void getUsersShouldPassModuleIdAndFiltersToUserUseCase() {
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .data(Map.of("items", List.of()))
                .build();
        when(userUseCase.getUsers(any(GetUserParams.class))).thenReturn(responseBody);

        ResponseEntity<?> response = controller.getUsers(
                1,
                10,
                "email",
                "desc",
                "alice",
                "ACTIVE",
                "MEMBER",
                30L,
                40L,
                20L,
                10L);

        assertEquals(200, response.getStatusCodeValue());
        ArgumentCaptor<GetUserParams> captor = ArgumentCaptor.forClass(GetUserParams.class);
        verify(userUseCase).getUsers(captor.capture());
        assertEquals(1, captor.getValue().getPage());
        assertEquals(10, captor.getValue().getPageSize());
        assertEquals("email", captor.getValue().getSortBy());
        assertEquals("desc", captor.getValue().getSortDirection());
        assertEquals("alice", captor.getValue().getSearch());
        assertEquals("ACTIVE", captor.getValue().getStatus());
        assertEquals("MEMBER", captor.getValue().getUserType());
        assertEquals(30L, captor.getValue().getRoleId());
        assertEquals(40L, captor.getValue().getDepartmentId());
        assertEquals(20L, captor.getValue().getModuleId());
        assertEquals(10L, captor.getValue().getOrganizationId());
    }
}
```

- [ ] **Step 3: Run controller tests and confirm they fail**

Run from `account/`:

```bash
.\mvnw.cmd -Dtest=ModuleAccessControllerTest,InternalUserControllerTest test
```

Expected result: compilation fails because controller method signatures and `ModuleAccessController` dependencies do not yet match the tests.

- [ ] **Step 4: Modify `ModuleAccessController` imports and dependency**

Add imports:

```java
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.usecase.UserUseCase;
```

Add the field:

```java
private final UserUseCase userUseCase;
```

- [ ] **Step 5: Replace `getUsersWithAccessToModule` in `ModuleAccessController`**

```java
@GetMapping("/modules/{moduleId}/users")
public ResponseEntity<?> getUsersWithAccessToModule(
        @PathVariable Long organizationId,
        @PathVariable Long moduleId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDir,
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "ACTIVE") String status,
        @RequestParam(required = false) String userType,
        @RequestParam(required = false) Long roleId,
        @RequestParam(required = false) Long departmentId) {
    if (!authUtils.canAccessOrganization(organizationId)) {
        var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    GetUserParams params = GetUserParams.builder()
            .page(page)
            .pageSize(pageSize)
            .sortBy(sortBy)
            .sortDirection(sortDir)
            .search(search)
            .status(status)
            .userType(userType)
            .roleId(roleId)
            .departmentId(departmentId)
            .organizationId(organizationId)
            .moduleId(moduleId)
            .build();

    log.info("GET /api/v1/organizations/{}/modules/{}/users - Fetching users with active module access",
            organizationId, moduleId);
    var response = userUseCase.getUsers(params);
    return ResponseEntity.status(response.getCode()).body(response);
}
```

- [ ] **Step 6: Replace `getUsers` signature and builder in `InternalUserController`**

```java
@GetMapping
public ResponseEntity<?> getUsers(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortDir,
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "ACTIVE") String status,
        @RequestParam(required = false) String userType,
        @RequestParam(required = false) Long roleId,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(required = false) Long moduleId,
        @RequestParam(required = false) Long organizationId) {
    GetUserParams params = GetUserParams.builder()
            .page(page).pageSize(pageSize).sortBy(sortBy).sortDirection(sortDir)
            .search(search)
            .status(status)
            .userType(userType)
            .roleId(roleId)
            .departmentId(departmentId)
            .moduleId(moduleId)
            .organizationId(organizationId)
            .build();
    log.info("Received request to get users with params: {}", params);
    var response = userUseCase.getUsers(params);
    return ResponseEntity.status(response.getCode()).body(response);
}
```

- [ ] **Step 7: Run controller tests and confirm they pass**

Run from `account/`:

```bash
.\mvnw.cmd -Dtest=ModuleAccessControllerTest,InternalUserControllerTest test
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 8: Commit controller routing**

```bash
git add account/src/main/java/serp/project/account/ui/controller/ModuleAccessController.java account/src/main/java/serp/project/account/ui/controller/internal/InternalUserController.java account/src/test/java/serp/project/account/ui/controller/ModuleAccessControllerTest.java account/src/test/java/serp/project/account/ui/controller/internal/InternalUserControllerTest.java
git commit -m "feat(account): route module user listing through shared query"
```

---

### Task 5: Verification and Cleanup

**Files:**
- Review all modified files from Tasks 1-4.

- [ ] **Step 1: Run the focused regression suite**

Run from `account/`:

```bash
.\mvnw.cmd -Dtest=UserQueryBuilderTest,UserUseCaseGetUsersTest,ModuleAccessControllerTest,InternalUserControllerTest test
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 2: Run account compile**

Run from `account/`:

```bash
.\mvnw.cmd clean compile
```

Expected result: `BUILD SUCCESS`.

- [ ] **Step 3: Inspect git diff for accidental unrelated changes**

Run from repository root:

```bash
git status --short
git diff -- account/src/main/java/serp/project/account/core/domain/dto/request/GetUserParams.java account/src/main/java/serp/project/account/infrastructure/store/query/UserQueryBuilder.java account/src/main/java/serp/project/account/core/usecase/UserUseCase.java account/src/main/java/serp/project/account/ui/controller/ModuleAccessController.java account/src/main/java/serp/project/account/ui/controller/internal/InternalUserController.java account/src/test/java/serp/project/account/infrastructure/store/query/UserQueryBuilderTest.java account/src/test/java/serp/project/account/core/usecase/UserUseCaseGetUsersTest.java account/src/test/java/serp/project/account/ui/controller/ModuleAccessControllerTest.java account/src/test/java/serp/project/account/ui/controller/internal/InternalUserControllerTest.java
```

Expected result: only the planned files are modified, and no generated or environment files are present.

- [ ] **Step 4: Commit final verification notes if any code changed after Task 4**

If Step 3 shows no changes after the Task 4 commit, skip this step.

If Step 3 shows formatting or small compile fixes, commit those exact files:

```bash
git add account/src/main/java/serp/project/account/core/domain/dto/request/GetUserParams.java account/src/main/java/serp/project/account/infrastructure/store/query/UserQueryBuilder.java account/src/main/java/serp/project/account/core/usecase/UserUseCase.java account/src/main/java/serp/project/account/ui/controller/ModuleAccessController.java account/src/main/java/serp/project/account/ui/controller/internal/InternalUserController.java account/src/test/java/serp/project/account/infrastructure/store/query/UserQueryBuilderTest.java account/src/test/java/serp/project/account/core/usecase/UserUseCaseGetUsersTest.java account/src/test/java/serp/project/account/ui/controller/ModuleAccessControllerTest.java account/src/test/java/serp/project/account/ui/controller/internal/InternalUserControllerTest.java
git commit -m "chore(account): verify module access user listing"
```
