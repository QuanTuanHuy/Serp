# Admin Global Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build phase 1 admin global search with a backend aggregate endpoint and a `/admin/search` frontend results page.

**Architecture:** The `account` service owns the aggregate search contract through `GET /api/v1/admin/search?q=&limit=`. The frontend admin header only navigates to `/admin/search?q=...`; the search page calls one RTK Query endpoint and renders fixed sections for organizations, users, roles, and modules.

**Tech Stack:** Java 21, Spring Boot 3.5.5, JUnit 5, Mockito, Spring Data JPA, Next.js App Router, React 19, TypeScript, RTK Query, Tailwind/shadcn UI.

---

## File Structure

Backend files:

- Create `account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchItemResponse.java`: common result item DTO.
- Create `account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchGroupResponse.java`: section DTO with type, title, total, and items.
- Create `account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchResponse.java`: aggregate response DTO.
- Create `account/src/main/java/serp/project/account/core/domain/enums/AdminGlobalSearchType.java`: four result group types.
- Create `account/src/main/java/serp/project/account/core/usecase/AdminSearchUseCase.java`: query validation, limit clamp, resource search orchestration, common DTO mapping.
- Create `account/src/main/java/serp/project/account/ui/controller/AdminSearchController.java`: thin REST controller.
- Modify `account/src/main/java/serp/project/account/core/domain/constant/Constants.java`: add a stable validation message for empty query.
- Modify `account/src/main/java/serp/project/account/core/port/store/IRolePort.java`: add limited search method.
- Modify `account/src/main/java/serp/project/account/core/port/store/IModulePort.java`: add limited search method.
- Modify `account/src/main/java/serp/project/account/infrastructure/store/adapter/RoleAdapter.java`: implement limited role search.
- Modify `account/src/main/java/serp/project/account/infrastructure/store/adapter/ModuleAdapter.java`: implement limited module search.
- Modify `account/src/main/java/serp/project/account/infrastructure/store/repository/IRoleRepository.java`: add role search/count queries.
- Modify `account/src/main/java/serp/project/account/infrastructure/store/repository/IModuleRepository.java`: add module search/count queries.
- Create `account/src/test/java/serp/project/account/core/usecase/AdminSearchUseCaseTest.java`: focused use case tests.
- Create `account/src/test/java/serp/project/account/ui/controller/AdminSearchControllerTest.java`: controller delegation test.

Frontend files:

- Create `serp_web/src/modules/admin/types/global-search.types.ts`: frontend contract types.
- Create `serp_web/src/modules/admin/services/global-search/globalSearchApi.ts`: RTK Query endpoint.
- Create `serp_web/src/app/admin/search/page.tsx`: search results route.
- Modify `serp_web/src/modules/admin/types/index.ts`: export global search types.
- Modify `serp_web/src/modules/admin/services/adminApi.ts`: export global search API hook.
- Modify `serp_web/src/modules/admin/index.ts`: export the new hook/types from the module barrel.
- Modify `serp_web/src/modules/admin/components/layout/AdminHeader.tsx`: navigate to `/admin/search?q=...`.

---

### Task 1: Backend DTOs And Use Case Tests

**Files:**

- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchItemResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchGroupResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/enums/AdminGlobalSearchType.java`
- Create: `account/src/test/java/serp/project/account/core/usecase/AdminSearchUseCaseTest.java`

- [ ] **Step 1: Add response DTOs and enum**

Create `AdminGlobalSearchType.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum AdminGlobalSearchType {
    ORGANIZATION,
    USER,
    ROLE,
    MODULE
}
```

Create `AdminGlobalSearchItemResponse.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGlobalSearchItemResponse {
    private String id;
    private String title;
    private String subtitle;
    private String url;
}
```

Create `AdminGlobalSearchGroupResponse.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.account.core.domain.enums.AdminGlobalSearchType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGlobalSearchGroupResponse {
    private AdminGlobalSearchType type;
    private String title;
    private Long total;
    private List<AdminGlobalSearchItemResponse> items;
}
```

Create `AdminGlobalSearchResponse.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGlobalSearchResponse {
    private String query;
    private Integer limit;
    private List<AdminGlobalSearchGroupResponse> groups;
}
```

- [ ] **Step 2: Write the failing use case test**

Create `AdminSearchUseCaseTest.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchResponse;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.AdminGlobalSearchType;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.port.store.IModulePort;
import serp.project.account.core.port.store.IOrganizationPort;
import serp.project.account.core.port.store.IRolePort;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class AdminSearchUseCaseTest {
    @Mock
    private IOrganizationPort organizationPort;
    @Mock
    private IUserPort userPort;
    @Mock
    private IRolePort rolePort;
    @Mock
    private IModulePort modulePort;

    private final ResponseUtils responseUtils = new ResponseUtils();

    @Test
    void searchShouldTrimQueryClampLimitAndReturnFourGroups() {
        var useCase = new AdminSearchUseCase(
                organizationPort,
                userPort,
                rolePort,
                modulePort,
                responseUtils);

        when(organizationPort.getOrganizations(any(GetOrganizationParams.class))).thenReturn(Pair.of(
                List.of(OrganizationEntity.builder()
                        .id(1L)
                        .name("Acme Corp")
                        .code("ACME")
                        .email("ops@acme.test")
                        .status(OrganizationStatus.ACTIVE)
                        .build()),
                12L));
        when(userPort.getUsers(any(GetUserParams.class))).thenReturn(Pair.of(
                3L,
                List.of(UserEntity.builder()
                        .id(2L)
                        .firstName("Alice")
                        .lastName("Nguyen")
                        .email("alice@acme.test")
                        .status(UserStatus.ACTIVE)
                        .build())));
        when(rolePort.searchRoles("acme", 10)).thenReturn(Pair.of(
                List.of(RoleEntity.builder()
                        .id(3L)
                        .name("Account Admin")
                        .description("Manages account data")
                        .scope(RoleScope.SYSTEM)
                        .roleType(RoleType.ADMIN)
                        .build()),
                1L));
        when(modulePort.searchModules("acme", 10)).thenReturn(Pair.of(
                List.of(ModuleEntity.builder()
                        .id(4L)
                        .moduleName("Account")
                        .code("ACCOUNT")
                        .description("Account administration")
                        .status(ModuleStatus.ACTIVE)
                        .build()),
                1L));

        GeneralResponse<?> response = useCase.search("  acme  ", 99);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isInstanceOf(AdminGlobalSearchResponse.class);
        var data = (AdminGlobalSearchResponse) response.getData();
        assertThat(data.getQuery()).isEqualTo("acme");
        assertThat(data.getLimit()).isEqualTo(10);
        assertThat(data.getGroups()).extracting("type").containsExactly(
                AdminGlobalSearchType.ORGANIZATION,
                AdminGlobalSearchType.USER,
                AdminGlobalSearchType.ROLE,
                AdminGlobalSearchType.MODULE);
        assertThat(data.getGroups().getFirst().getItems().getFirst().getUrl())
                .isEqualTo("/admin/organizations?search=acme");
        assertThat(data.getGroups().get(1).getItems().getFirst().getTitle())
                .isEqualTo("Alice Nguyen");
        assertThat(data.getGroups().get(2).getItems().getFirst().getSubtitle())
                .isEqualTo("SYSTEM - ADMIN");
        assertThat(data.getGroups().get(3).getItems().getFirst().getSubtitle())
                .isEqualTo("ACCOUNT - ACTIVE");

        ArgumentCaptor<GetOrganizationParams> orgParams = ArgumentCaptor.forClass(GetOrganizationParams.class);
        verify(organizationPort).getOrganizations(orgParams.capture());
        assertThat(orgParams.getValue().getSearch()).isEqualTo("acme");
        assertThat(orgParams.getValue().getPage()).isZero();
        assertThat(orgParams.getValue().getPageSize()).isEqualTo(10);

        ArgumentCaptor<GetUserParams> userParams = ArgumentCaptor.forClass(GetUserParams.class);
        verify(userPort).getUsers(userParams.capture());
        assertThat(userParams.getValue().getSearch()).isEqualTo("acme");
        assertThat(userParams.getValue().getPage()).isZero();
        assertThat(userParams.getValue().getPageSize()).isEqualTo(10);

        verify(rolePort).searchRoles(eq("acme"), eq(10));
        verify(modulePort).searchModules(eq("acme"), eq(10));
    }

    @Test
    void searchShouldReturnBadRequestForBlankQuery() {
        var useCase = new AdminSearchUseCase(
                organizationPort,
                userPort,
                rolePort,
                modulePort,
                responseUtils);

        GeneralResponse<?> response = useCase.search("   ", 5);

        assertThat(response.getCode()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo(Constants.ErrorMessage.SEARCH_QUERY_REQUIRED);
    }

    @Test
    void searchShouldUseDefaultLimitWhenLimitIsNullOrTooLow() {
        var useCase = new AdminSearchUseCase(
                organizationPort,
                userPort,
                rolePort,
                modulePort,
                responseUtils);

        when(organizationPort.getOrganizations(any(GetOrganizationParams.class))).thenReturn(Pair.of(List.of(), 0L));
        when(userPort.getUsers(any(GetUserParams.class))).thenReturn(Pair.of(0L, List.of()));
        when(rolePort.searchRoles("sales", 5)).thenReturn(Pair.of(List.of(), 0L));
        when(modulePort.searchModules("sales", 5)).thenReturn(Pair.of(List.of(), 0L));

        GeneralResponse<?> response = useCase.search("sales", 0);

        var data = (AdminGlobalSearchResponse) response.getData();
        assertThat(data.getLimit()).isEqualTo(5);
        assertThat(data.getGroups()).hasSize(4);
    }
}
```

- [ ] **Step 3: Run the focused test and confirm it fails**

Run from `account/`:

```bash
mvnw.cmd -Dtest=AdminSearchUseCaseTest test
```

Expected: compilation fails because `AdminSearchUseCase`, `SEARCH_QUERY_REQUIRED`, `IRolePort.searchRoles`, and `IModulePort.searchModules` do not exist yet.

- [ ] **Step 4: Commit the failing test and DTO contract**

```bash
git add account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchItemResponse.java account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchGroupResponse.java account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchResponse.java account/src/main/java/serp/project/account/core/domain/enums/AdminGlobalSearchType.java account/src/test/java/serp/project/account/core/usecase/AdminSearchUseCaseTest.java
git commit -m "test(account): define admin global search contract"
```

---

### Task 2: Backend Use Case And Port Contracts

**Files:**

- Create: `account/src/main/java/serp/project/account/core/usecase/AdminSearchUseCase.java`
- Modify: `account/src/main/java/serp/project/account/core/domain/constant/Constants.java`
- Modify: `account/src/main/java/serp/project/account/core/port/store/IRolePort.java`
- Modify: `account/src/main/java/serp/project/account/core/port/store/IModulePort.java`

- [ ] **Step 1: Add the validation message constant**

In `Constants.ErrorMessage`, add:

```java
public static final String SEARCH_QUERY_REQUIRED = "Search query is required";
```

- [ ] **Step 2: Add limited search contracts to role and module ports**

In `IRolePort.java`, add the import and method:

```java
import org.springframework.data.util.Pair;
```

```java
Pair<List<RoleEntity>, Long> searchRoles(String search, int limit);
```

In `IModulePort.java`, add the import and method:

```java
import org.springframework.data.util.Pair;
```

```java
Pair<List<ModuleEntity>, Long> searchModules(String search, int limit);
```

- [ ] **Step 3: Implement the use case**

Create `AdminSearchUseCase.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchGroupResponse;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchItemResponse;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchResponse;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.AdminGlobalSearchType;
import serp.project.account.core.port.store.IModulePort;
import serp.project.account.core.port.store.IOrganizationPort;
import serp.project.account.core.port.store.IRolePort;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
public class AdminSearchUseCase {
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 10;

    private final IOrganizationPort organizationPort;
    private final IUserPort userPort;
    private final IRolePort rolePort;
    private final IModulePort modulePort;
    private final ResponseUtils responseUtils;

    public GeneralResponse<?> search(String query, Integer limit) {
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isEmpty()) {
            return responseUtils.badRequest(Constants.ErrorMessage.SEARCH_QUERY_REQUIRED);
        }

        int normalizedLimit = normalizeLimit(limit);
        String encodedQuery = URLEncoder.encode(normalizedQuery, StandardCharsets.UTF_8);

        Pair<List<OrganizationEntity>, Long> organizations = organizationPort.getOrganizations(
                GetOrganizationParams.builder()
                        .search(normalizedQuery)
                        .page(0)
                        .pageSize(normalizedLimit)
                        .sortBy("name")
                        .sortDirection("asc")
                        .build());
        Pair<Long, List<UserEntity>> users = userPort.getUsers(
                GetUserParams.builder()
                        .search(normalizedQuery)
                        .page(0)
                        .pageSize(normalizedLimit)
                        .sortBy("email")
                        .sortDirection("asc")
                        .build());
        Pair<List<RoleEntity>, Long> roles = rolePort.searchRoles(normalizedQuery, normalizedLimit);
        Pair<List<ModuleEntity>, Long> modules = modulePort.searchModules(normalizedQuery, normalizedLimit);

        AdminGlobalSearchResponse response = AdminGlobalSearchResponse.builder()
                .query(normalizedQuery)
                .limit(normalizedLimit)
                .groups(List.of(
                        organizationGroup(organizations, encodedQuery),
                        userGroup(users, encodedQuery),
                        roleGroup(roles, encodedQuery),
                        moduleGroup(modules, encodedQuery)))
                .build();

        return responseUtils.success(response);
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private AdminGlobalSearchGroupResponse organizationGroup(
            Pair<List<OrganizationEntity>, Long> organizations,
            String encodedQuery) {
        return AdminGlobalSearchGroupResponse.builder()
                .type(AdminGlobalSearchType.ORGANIZATION)
                .title("Organizations")
                .total(organizations.getSecond())
                .items(organizations.getFirst().stream()
                        .map(organization -> AdminGlobalSearchItemResponse.builder()
                                .id(String.valueOf(organization.getId()))
                                .title(valueOrFallback(organization.getName(), "Unnamed organization"))
                                .subtitle(joinNonBlank(" - ", organization.getCode(), enumName(organization.getStatus())))
                                .url("/admin/organizations?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private AdminGlobalSearchGroupResponse userGroup(Pair<Long, List<UserEntity>> users, String encodedQuery) {
        return AdminGlobalSearchGroupResponse.builder()
                .type(AdminGlobalSearchType.USER)
                .title("Users")
                .total(users.getFirst())
                .items(users.getSecond().stream()
                        .map(user -> AdminGlobalSearchItemResponse.builder()
                                .id(String.valueOf(user.getId()))
                                .title(valueOrFallback(user.getFullName(), user.getEmail()))
                                .subtitle(joinNonBlank(" - ", user.getEmail(), enumName(user.getStatus())))
                                .url("/admin/users?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private AdminGlobalSearchGroupResponse roleGroup(Pair<List<RoleEntity>, Long> roles, String encodedQuery) {
        return AdminGlobalSearchGroupResponse.builder()
                .type(AdminGlobalSearchType.ROLE)
                .title("Roles")
                .total(roles.getSecond())
                .items(roles.getFirst().stream()
                        .map(role -> AdminGlobalSearchItemResponse.builder()
                                .id(String.valueOf(role.getId()))
                                .title(valueOrFallback(role.getName(), "Unnamed role"))
                                .subtitle(joinNonBlank(" - ", enumName(role.getScope()), enumName(role.getRoleType())))
                                .url("/admin/roles?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private AdminGlobalSearchGroupResponse moduleGroup(Pair<List<ModuleEntity>, Long> modules, String encodedQuery) {
        return AdminGlobalSearchGroupResponse.builder()
                .type(AdminGlobalSearchType.MODULE)
                .title("Modules")
                .total(modules.getSecond())
                .items(modules.getFirst().stream()
                        .map(module -> AdminGlobalSearchItemResponse.builder()
                                .id(String.valueOf(module.getId()))
                                .title(valueOrFallback(module.getModuleName(), "Unnamed module"))
                                .subtitle(joinNonBlank(" - ", module.getCode(), enumName(module.getStatus())))
                                .url("/admin/modules?search=" + encodedQuery)
                                .build())
                        .toList())
                .build();
    }

    private String enumName(Enum<?> value) {
        return value == null ? "" : value.name();
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String joinNonBlank(String delimiter, String first, String second) {
        boolean hasFirst = first != null && !first.isBlank();
        boolean hasSecond = second != null && !second.isBlank();
        if (hasFirst && hasSecond) {
            return first + delimiter + second;
        }
        if (hasFirst) {
            return first;
        }
        if (hasSecond) {
            return second;
        }
        return "";
    }
}
```

- [ ] **Step 4: Run the focused test and confirm the next failure**

Run from `account/`:

```bash
mvnw.cmd -Dtest=AdminSearchUseCaseTest test
```

Expected: compilation fails in `RoleAdapter` and `ModuleAdapter` because they do not implement the new port methods.

- [ ] **Step 5: Commit the use case and port contracts**

```bash
git add account/src/main/java/serp/project/account/core/usecase/AdminSearchUseCase.java account/src/main/java/serp/project/account/core/domain/constant/Constants.java account/src/main/java/serp/project/account/core/port/store/IRolePort.java account/src/main/java/serp/project/account/core/port/store/IModulePort.java
git commit -m "feat(account): add admin search use case"
```

---

### Task 3: Role And Module Search Store Adapters

**Files:**

- Modify: `account/src/main/java/serp/project/account/infrastructure/store/repository/IRoleRepository.java`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/repository/IModuleRepository.java`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/adapter/RoleAdapter.java`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/adapter/ModuleAdapter.java`

- [ ] **Step 1: Add repository methods**

In `IRoleRepository.java`, add imports:

```java
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
```

Add methods:

```java
@Query("""
        SELECT r FROM RoleModel r
        WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        ORDER BY r.name ASC
        """)
List<RoleModel> searchRoles(@Param("search") String search, Pageable pageable);

@Query("""
        SELECT COUNT(r) FROM RoleModel r
        WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
Long countSearchRoles(@Param("search") String search);
```

In `IModuleRepository.java`, add imports:

```java
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
```

Add methods:

```java
@Query("""
        SELECT m FROM ModuleModel m
        WHERE LOWER(m.moduleName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(m.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        ORDER BY m.moduleName ASC
        """)
List<ModuleModel> searchModules(@Param("search") String search, Pageable pageable);

@Query("""
        SELECT COUNT(m) FROM ModuleModel m
        WHERE LOWER(m.moduleName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(m.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        """)
Long countSearchModules(@Param("search") String search);
```

- [ ] **Step 2: Implement adapter methods**

In `RoleAdapter.java`, add imports:

```java
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
```

Add method:

```java
@Override
public Pair<List<RoleEntity>, Long> searchRoles(String search, int limit) {
    var pageable = PageRequest.of(0, limit);
    var roles = roleMapper.toEntityList(roleRepository.searchRoles(search, pageable));
    Long total = roleRepository.countSearchRoles(search);
    return Pair.of(roles, total != null ? total : 0L);
}
```

In `ModuleAdapter.java`, add imports:

```java
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
```

Add method:

```java
@Override
public Pair<List<ModuleEntity>, Long> searchModules(String search, int limit) {
    var pageable = PageRequest.of(0, limit);
    var modules = moduleMapper.toEntityList(moduleRepository.searchModules(search, pageable));
    Long total = moduleRepository.countSearchModules(search);
    return Pair.of(modules, total != null ? total : 0L);
}
```

- [ ] **Step 3: Run the use case test and confirm it passes**

Run from `account/`:

```bash
mvnw.cmd -Dtest=AdminSearchUseCaseTest test
```

Expected: PASS, 3 tests.

- [ ] **Step 4: Run compile for repository query validation**

Run from `account/`:

```bash
mvnw.cmd clean compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit store support**

```bash
git add account/src/main/java/serp/project/account/infrastructure/store/repository/IRoleRepository.java account/src/main/java/serp/project/account/infrastructure/store/repository/IModuleRepository.java account/src/main/java/serp/project/account/infrastructure/store/adapter/RoleAdapter.java account/src/main/java/serp/project/account/infrastructure/store/adapter/ModuleAdapter.java
git commit -m "feat(account): add limited role and module search"
```

---

### Task 4: Backend Admin Search Controller

**Files:**

- Create: `account/src/main/java/serp/project/account/ui/controller/AdminSearchController.java`
- Create: `account/src/test/java/serp/project/account/ui/controller/AdminSearchControllerTest.java`

- [ ] **Step 1: Write the controller test**

Create `AdminSearchControllerTest.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.response.AdminGlobalSearchResponse;
import serp.project.account.core.usecase.AdminSearchUseCase;

@ExtendWith(MockitoExtension.class)
class AdminSearchControllerTest {
    @Mock
    private AdminSearchUseCase adminSearchUseCase;

    @InjectMocks
    private AdminSearchController controller;

    @Test
    void searchShouldDelegateToUseCaseAndUseResponseCode() {
        var body = GeneralResponse.builder()
                .code(200)
                .status("success")
                .message("OK")
                .data(AdminGlobalSearchResponse.builder()
                        .query("acme")
                        .limit(5)
                        .groups(List.of())
                        .build())
                .build();
        when(adminSearchUseCase.search("acme", 5)).thenReturn(body);

        ResponseEntity<?> response = controller.search("acme", 5);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(body, response.getBody());
        verify(adminSearchUseCase).search("acme", 5);
    }
}
```

- [ ] **Step 2: Run the controller test and confirm it fails**

Run from `account/`:

```bash
mvnw.cmd -Dtest=AdminSearchControllerTest test
```

Expected: compilation fails because `AdminSearchController` does not exist.

- [ ] **Step 3: Implement the controller**

Create `AdminSearchController.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.usecase.AdminSearchUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/search")
public class AdminSearchController {
    private final AdminSearchUseCase adminSearchUseCase;

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String q,
            @RequestParam(required = false) Integer limit) {
        var response = adminSearchUseCase.search(q, limit);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
```

- [ ] **Step 4: Run backend focused tests**

Run from `account/`:

```bash
mvnw.cmd -Dtest=AdminSearchUseCaseTest,AdminSearchControllerTest test
```

Expected: PASS, all tests in both classes.

- [ ] **Step 5: Commit controller**

```bash
git add account/src/main/java/serp/project/account/ui/controller/AdminSearchController.java account/src/test/java/serp/project/account/ui/controller/AdminSearchControllerTest.java
git commit -m "feat(account): expose admin global search endpoint"
```

---

### Task 5: Frontend Types And RTK Query Endpoint

**Files:**

- Create: `serp_web/src/modules/admin/types/global-search.types.ts`
- Create: `serp_web/src/modules/admin/services/global-search/globalSearchApi.ts`
- Modify: `serp_web/src/modules/admin/types/index.ts`
- Modify: `serp_web/src/modules/admin/services/adminApi.ts`
- Modify: `serp_web/src/modules/admin/index.ts`

- [ ] **Step 1: Add global search types**

Create `global-search.types.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin global search types
 */

export type AdminGlobalSearchType =
  | 'ORGANIZATION'
  | 'USER'
  | 'ROLE'
  | 'MODULE';

export interface AdminGlobalSearchItem {
  id: string;
  title: string;
  subtitle?: string;
  url: string;
}

export interface AdminGlobalSearchGroup {
  type: AdminGlobalSearchType;
  title: string;
  total: number;
  items: AdminGlobalSearchItem[];
}

export interface AdminGlobalSearchResponse {
  query: string;
  limit: number;
  groups: AdminGlobalSearchGroup[];
}

export interface AdminGlobalSearchParams {
  q: string;
  limit?: number;
}
```

- [ ] **Step 2: Add RTK Query endpoint**

Create `globalSearchApi.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin global search API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';

import type {
  AdminGlobalSearchParams,
  AdminGlobalSearchResponse,
} from '../../types';

export const globalSearchApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAdminGlobalSearch: builder.query<
      AdminGlobalSearchResponse,
      AdminGlobalSearchParams
    >({
      query: ({ q, limit = 5 }) => {
        const params = new URLSearchParams();
        params.append('q', q);
        params.append('limit', String(limit));

        return {
          url: `/admin/search?${params.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createDataTransform<AdminGlobalSearchResponse>(),
      extraOptions: { service: 'account' },
    }),
  }),
  overrideExisting: false,
});

export const { useGetAdminGlobalSearchQuery } = globalSearchApi;
```

- [ ] **Step 3: Export types and hook from barrels**

Add to `serp_web/src/modules/admin/types/index.ts`:

```ts
// Global search types
export type {
  AdminGlobalSearchType,
  AdminGlobalSearchItem,
  AdminGlobalSearchGroup,
  AdminGlobalSearchResponse,
  AdminGlobalSearchParams,
} from './global-search.types';
```

Add to `serp_web/src/modules/admin/services/adminApi.ts`:

```ts
// Global Search API
export {
  globalSearchApi,
  useGetAdminGlobalSearchQuery,
} from './global-search/globalSearchApi';
```

Add these type names to the existing `export type { ... } from './types';` block in `serp_web/src/modules/admin/index.ts`:

```ts
AdminGlobalSearchGroup,
AdminGlobalSearchItem,
AdminGlobalSearchParams,
AdminGlobalSearchResponse,
AdminGlobalSearchType,
```

Add these API names to the existing `export { ... } from './services/adminApi';` block in `serp_web/src/modules/admin/index.ts`:

```ts
// Global Search
globalSearchApi,
useGetAdminGlobalSearchQuery,
```

- [ ] **Step 4: Run frontend type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: PASS. If it fails because `extraOptions` is not allowed at endpoint level in this local RTK Query typing, remove that line because the shared API defaults account-service requests to account behavior.

- [ ] **Step 5: Commit frontend API contract**

```bash
git add serp_web/src/modules/admin/types/global-search.types.ts serp_web/src/modules/admin/services/global-search/globalSearchApi.ts serp_web/src/modules/admin/types/index.ts serp_web/src/modules/admin/services/adminApi.ts serp_web/src/modules/admin/index.ts
git commit -m "feat(admin): add global search api client"
```

---

### Task 6: Frontend Search Results Page

**Files:**

- Create: `serp_web/src/app/admin/search/page.tsx`

- [ ] **Step 1: Implement the route**

Create `page.tsx`:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin global search results page
 */

'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { AlertTriangle, ArrowRight, RefreshCw, Search } from 'lucide-react';

import { useGetAdminGlobalSearchQuery } from '@/modules/admin';
import type { AdminGlobalSearchGroup } from '@/modules/admin';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';

const GROUP_VIEW_ALL_HREF: Record<string, string> = {
  ORGANIZATION: '/admin/organizations',
  USER: '/admin/users',
  ROLE: '/admin/roles',
  MODULE: '/admin/modules',
};

const buildSearchHref = (baseHref: string, query: string) =>
  `${baseHref}?search=${encodeURIComponent(query)}`;

function SearchSection({
  group,
  query,
}: {
  group: AdminGlobalSearchGroup;
  query: string;
}) {
  const viewAllHref = buildSearchHref(GROUP_VIEW_ALL_HREF[group.type], query);

  return (
    <Card>
      <CardHeader className='flex flex-row items-center justify-between gap-3 pb-3'>
        <div>
          <CardTitle className='text-base'>{group.title}</CardTitle>
          <p className='text-sm text-muted-foreground'>
            {group.total} matching result{group.total === 1 ? '' : 's'}
          </p>
        </div>
        <Button asChild variant='ghost' size='sm' className='shrink-0 gap-2'>
          <Link href={viewAllHref}>
            View all
            <ArrowRight className='h-4 w-4' />
          </Link>
        </Button>
      </CardHeader>
      <CardContent>
        {group.items.length > 0 ? (
          <div className='space-y-2'>
            {group.items.map((item) => (
              <Link
                key={`${group.type}-${item.id}`}
                href={item.url}
                className='block rounded-md border px-3 py-2 transition-colors hover:bg-muted'
              >
                <p className='truncate text-sm font-medium'>{item.title}</p>
                {item.subtitle && (
                  <p className='mt-1 truncate text-xs text-muted-foreground'>
                    {item.subtitle}
                  </p>
                )}
              </Link>
            ))}
          </div>
        ) : (
          <p className='rounded-md border border-dashed px-3 py-6 text-center text-sm text-muted-foreground'>
            No {group.title.toLowerCase()} matched this search.
          </p>
        )}
      </CardContent>
    </Card>
  );
}

function LoadingState() {
  return (
    <div className='space-y-6'>
      <div className='space-y-2'>
        <Skeleton className='h-8 w-72' />
        <Skeleton className='h-4 w-96 max-w-full' />
      </div>
      <div className='grid gap-4 xl:grid-cols-2'>
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} className='h-48 rounded-lg' />
        ))}
      </div>
    </div>
  );
}

export default function AdminSearchPage() {
  const searchParams = useSearchParams();
  const query = (searchParams.get('q') || '').trim();

  const { data, isFetching, isLoading, isError, refetch } =
    useGetAdminGlobalSearchQuery(
      { q: query, limit: 5 },
      { skip: query.length === 0 }
    );

  if (!query) {
    return (
      <Card className='max-w-xl'>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-base'>
            <Search className='h-5 w-5 text-muted-foreground' />
            Search admin resources
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className='text-sm text-muted-foreground'>
            Enter a query in the admin header to search organizations, users,
            roles, and modules.
          </p>
        </CardContent>
      </Card>
    );
  }

  if (isLoading || isFetching) {
    return <LoadingState />;
  }

  if (isError) {
    return (
      <Card className='max-w-xl'>
        <CardHeader>
          <CardTitle className='flex items-center gap-2 text-base'>
            <AlertTriangle className='h-5 w-5 text-destructive' />
            Search unavailable
          </CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <p className='text-sm text-muted-foreground'>
            The account service did not return search results for "{query}".
          </p>
          <Button onClick={() => refetch()} className='gap-2'>
            <RefreshCw className='h-4 w-4' />
            Retry
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-2xl font-semibold tracking-tight'>
          Search results
        </h1>
        <p className='text-sm text-muted-foreground'>
          Results for "{data?.query ?? query}" across admin account resources.
        </p>
      </div>

      <div className='grid gap-4 xl:grid-cols-2'>
        {(data?.groups ?? []).map((group) => (
          <SearchSection key={group.type} group={group} query={query} />
        ))}
      </div>
    </div>
  );
}
```

- [ ] **Step 2: Run frontend type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: PASS.

- [ ] **Step 3: Run lint on the new page and API file**

Run from `serp_web/`:

```bash
npx eslint src/app/admin/search/page.tsx src/modules/admin/services/global-search/globalSearchApi.ts
```

Expected: no lint errors.

- [ ] **Step 4: Commit search page**

```bash
git add serp_web/src/app/admin/search/page.tsx
git commit -m "feat(admin): add global search results page"
```

---

### Task 7: Admin Header Navigation And Final Verification

**Files:**

- Modify: `serp_web/src/modules/admin/components/layout/AdminHeader.tsx`

- [ ] **Step 1: Wire header submit to the search route**

In `AdminHeader.tsx`, replace `handleSearch` with:

```tsx
const handleSearch = (e: React.FormEvent) => {
  e.preventDefault();
  const trimmedQuery = searchQuery.trim();

  if (trimmedQuery) {
    router.push(`/admin/search?q=${encodeURIComponent(trimmedQuery)}`);
  }
};
```

Add this effect after `const pathname = usePathname();` so the header keeps the query while on `/admin/search`:

```tsx
useEffect(() => {
  if (pathname !== '/admin/search') {
    return;
  }

  const params = new URLSearchParams(window.location.search);
  setSearchQuery(params.get('q') || '');
}, [pathname]);
```

- [ ] **Step 2: Run frontend checks**

Run from `serp_web/`:

```bash
npm run type-check
npm run lint
npm run format:check
```

Expected: all pass. If `format:check` reports formatting differences in touched files, run `npx prettier --write src/app/admin/search/page.tsx src/modules/admin/components/layout/AdminHeader.tsx src/modules/admin/types/global-search.types.ts src/modules/admin/services/global-search/globalSearchApi.ts`, then rerun `npm run format:check`.

- [ ] **Step 3: Run backend checks**

Run from `account/`:

```bash
mvnw.cmd -Dtest=AdminSearchUseCaseTest,AdminSearchControllerTest test
mvnw.cmd clean compile
```

Expected: both commands end with BUILD SUCCESS.

- [ ] **Step 4: Run full targeted diff review**

Run from repo root:

```bash
git diff --check
git diff --stat
```

Expected: `git diff --check` prints no whitespace errors. `git diff --stat` only includes the files listed in this plan plus formatting changes produced by Prettier on those files.

- [ ] **Step 5: Commit header wiring and verification fixes**

```bash
git add serp_web/src/modules/admin/components/layout/AdminHeader.tsx serp_web/src/app/admin/search/page.tsx serp_web/src/modules/admin/types/global-search.types.ts serp_web/src/modules/admin/services/global-search/globalSearchApi.ts serp_web/src/modules/admin/types/index.ts serp_web/src/modules/admin/services/adminApi.ts serp_web/src/modules/admin/index.ts account/src/main/java/serp/project/account/core/domain/constant/Constants.java account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchItemResponse.java account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchGroupResponse.java account/src/main/java/serp/project/account/core/domain/dto/response/AdminGlobalSearchResponse.java account/src/main/java/serp/project/account/core/domain/enums/AdminGlobalSearchType.java account/src/main/java/serp/project/account/core/usecase/AdminSearchUseCase.java account/src/main/java/serp/project/account/core/port/store/IRolePort.java account/src/main/java/serp/project/account/core/port/store/IModulePort.java account/src/main/java/serp/project/account/infrastructure/store/repository/IRoleRepository.java account/src/main/java/serp/project/account/infrastructure/store/repository/IModuleRepository.java account/src/main/java/serp/project/account/infrastructure/store/adapter/RoleAdapter.java account/src/main/java/serp/project/account/infrastructure/store/adapter/ModuleAdapter.java account/src/main/java/serp/project/account/ui/controller/AdminSearchController.java account/src/test/java/serp/project/account/core/usecase/AdminSearchUseCaseTest.java account/src/test/java/serp/project/account/ui/controller/AdminSearchControllerTest.java
git commit -m "feat(admin): wire global search"
```

---

## Final Acceptance Criteria

- `GET /api/v1/admin/search?q=acme&limit=5` returns a successful `GeneralResponse<?>` with four groups in order: organizations, users, roles, modules.
- Empty or whitespace-only `q` returns a bad request response with `Search query is required`.
- Each group contains at most `limit` items and includes a `total`.
- Header search submit navigates to `/admin/search?q=<encoded query>`.
- `/admin/search?q=<query>` shows loading, error, empty-query, empty-group, and populated states.
- Result items and "View all" links navigate to the matching admin list page with `search=<encoded query>`.
- Backend checks pass from `account/`: `mvnw.cmd -Dtest=AdminSearchUseCaseTest,AdminSearchControllerTest test` and `mvnw.cmd clean compile`.
- Frontend checks pass from `serp_web/`: `npm run type-check`, `npm run lint`, and `npm run format:check`.
