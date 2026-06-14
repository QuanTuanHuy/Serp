# Settings Global Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build organization-scoped global search for the settings module across users, departments, and modules.

**Architecture:** The account service exposes a new organization-scoped endpoint at `/api/v1/organizations/{organizationId}/settings/search`. `SettingsSearchUseCase` queries existing user and department services with paginated search, derives subscription-included modules from subscription plan services, and returns grouped result DTOs. The settings frontend adds a typed RTK Query client, a `/settings/search` results page, and wires the existing settings header search form to that page.

**Tech Stack:** Java 21, Spring Boot, JUnit 5, Mockito, Next.js 15 App Router, React 19, TypeScript, RTK Query, Tailwind/Shadcn UI.

---

## File Structure

Backend files:

- Create `account/src/main/java/serp/project/account/core/domain/enums/SettingsGlobalSearchType.java`: enum values `USER`, `DEPARTMENT`, `MODULE`.
- Create `account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchItemResponse.java`: single result item contract.
- Create `account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchGroupResponse.java`: grouped result contract.
- Create `account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchResponse.java`: top-level response contract.
- Create `account/src/main/java/serp/project/account/core/usecase/SettingsSearchUseCase.java`: organization-scoped search orchestration.
- Create `account/src/main/java/serp/project/account/ui/controller/SettingsSearchController.java`: REST endpoint and organization access check.
- Create `account/src/test/java/serp/project/account/core/usecase/SettingsSearchUseCaseTest.java`: use case contract tests.
- Create `account/src/test/java/serp/project/account/ui/controller/SettingsSearchControllerTest.java`: controller delegation and forbidden tests.

Frontend files:

- Create `serp_web/src/modules/settings/types/global-search.types.ts`: settings search TypeScript contract.
- Modify `serp_web/src/modules/settings/types/index.ts`: export search types.
- Create `serp_web/src/modules/settings/services/global-search/globalSearchApi.ts`: RTK Query endpoint.
- Modify `serp_web/src/modules/settings/services/settingsApi.ts`: export search API hook.
- Modify `serp_web/src/modules/settings/index.ts`: ensure service/type exports remain available.
- Create `serp_web/src/app/settings/search/page.tsx`: grouped search results page.
- Modify `serp_web/src/modules/settings/components/layout/SettingsHeader.tsx`: navigate to `/settings/search?q=...` and sync query input on the search route.

---

### Task 1: Backend Search Contract Tests

**Files:**

- Create: `account/src/test/java/serp/project/account/core/usecase/SettingsSearchUseCaseTest.java`

- [ ] **Step 1: Write failing use case tests**

Create the test file with this shape. The class references DTOs and use case classes that do not exist yet, so compilation should fail before Task 2.

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchResponse;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.SettingsGlobalSearchType;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.IDepartmentService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class SettingsSearchUseCaseTest {
    @Mock
    private IUserService userService;
    @Mock
    private IDepartmentService departmentService;
    @Mock
    private ISubscriptionService subscriptionService;
    @Mock
    private ISubscriptionPlanService subscriptionPlanService;
    @Mock
    private IModuleService moduleService;

    private final ResponseUtils responseUtils = new ResponseUtils();

    @Test
    void searchShouldTrimQueryClampLimitScopeToOrganizationAndReturnThreeGroups() {
        var useCase = new SettingsSearchUseCase(
                userService,
                departmentService,
                subscriptionService,
                subscriptionPlanService,
                moduleService,
                responseUtils);

        when(userService.getUsers(any(GetUserParams.class))).thenReturn(Pair.of(
                1L,
                List.of(UserEntity.builder()
                        .id(10L)
                        .firstName("Mai")
                        .lastName("Nguyen")
                        .email("mai@example.test")
                        .status(UserStatus.ACTIVE)
                        .build())));
        when(departmentService.getDepartments(any(GetDepartmentParams.class))).thenReturn(Pair.of(
                List.of(DepartmentEntity.builder()
                        .id(20L)
                        .organizationId(7L)
                        .name("Marketing")
                        .code("MKT")
                        .isActive(true)
                        .build()),
                1L));
        when(subscriptionService.getActiveOrPendingUpgrade(7L)).thenReturn(
                OrganizationSubscriptionEntity.builder()
                        .id(30L)
                        .organizationId(7L)
                        .subscriptionPlanId(40L)
                        .build());
        when(subscriptionPlanService.getPlanModules(40L)).thenReturn(List.of(
                SubscriptionPlanModuleEntity.builder().moduleId(50L).isIncluded(true).build(),
                SubscriptionPlanModuleEntity.builder().moduleId(51L).isIncluded(false).build()));
        when(moduleService.getModulesByIds(List.of(50L))).thenReturn(List.of(
                ModuleEntity.builder()
                        .id(50L)
                        .moduleName("Marketing Automation")
                        .code("MKT_AUTO")
                        .description("Marketing tools")
                        .status(ModuleStatus.ACTIVE)
                        .build()));

        GeneralResponse<?> response = useCase.search(7L, "  marketing  ", 99);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isInstanceOf(SettingsGlobalSearchResponse.class);
        var data = (SettingsGlobalSearchResponse) response.getData();
        assertThat(data.getQuery()).isEqualTo("marketing");
        assertThat(data.getLimit()).isEqualTo(10);
        assertThat(data.getGroups()).extracting("type").containsExactly(
                SettingsGlobalSearchType.USER,
                SettingsGlobalSearchType.DEPARTMENT,
                SettingsGlobalSearchType.MODULE);
        assertThat(data.getGroups().get(0).getItems().get(0).getUrl())
                .isEqualTo("/settings/users?search=marketing");
        assertThat(data.getGroups().get(1).getItems().get(0).getUrl())
                .isEqualTo("/settings/departments?search=marketing");
        assertThat(data.getGroups().get(2).getItems().get(0).getUrl())
                .isEqualTo("/settings/modules?search=marketing");

        ArgumentCaptor<GetUserParams> userParams = ArgumentCaptor.forClass(GetUserParams.class);
        verify(userService).getUsers(userParams.capture());
        assertThat(userParams.getValue().getOrganizationId()).isEqualTo(7L);
        assertThat(userParams.getValue().getSearch()).isEqualTo("marketing");
        assertThat(userParams.getValue().getPage()).isZero();
        assertThat(userParams.getValue().getPageSize()).isEqualTo(10);

        ArgumentCaptor<GetDepartmentParams> departmentParams = ArgumentCaptor.forClass(GetDepartmentParams.class);
        verify(departmentService).getDepartments(departmentParams.capture());
        assertThat(departmentParams.getValue().getOrganizationId()).isEqualTo(7L);
        assertThat(departmentParams.getValue().getSearch()).isEqualTo("marketing");
        assertThat(departmentParams.getValue().getPage()).isZero();
        assertThat(departmentParams.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void searchShouldReturnBadRequestForBlankQuery() {
        var useCase = new SettingsSearchUseCase(
                userService,
                departmentService,
                subscriptionService,
                subscriptionPlanService,
                moduleService,
                responseUtils);

        GeneralResponse<?> response = useCase.search(7L, "   ", 5);

        assertThat(response.getCode()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo(Constants.ErrorMessage.SEARCH_QUERY_REQUIRED);
    }

    @Test
    void searchShouldUseDefaultLimitWhenLimitIsNullOrTooLow() {
        var useCase = new SettingsSearchUseCase(
                userService,
                departmentService,
                subscriptionService,
                subscriptionPlanService,
                moduleService,
                responseUtils);

        when(userService.getUsers(any(GetUserParams.class))).thenReturn(Pair.of(0L, List.of()));
        when(departmentService.getDepartments(any(GetDepartmentParams.class))).thenReturn(Pair.of(List.of(), 0L));
        when(subscriptionService.getActiveOrPendingUpgrade(7L)).thenReturn(
                OrganizationSubscriptionEntity.builder()
                        .organizationId(7L)
                        .subscriptionPlanId(40L)
                        .build());
        when(subscriptionPlanService.getPlanModules(40L)).thenReturn(List.of());
        when(moduleService.getModulesByIds(List.of())).thenReturn(List.of());

        GeneralResponse<?> response = useCase.search(7L, "sales", 0);

        var data = (SettingsGlobalSearchResponse) response.getData();
        assertThat(data.getLimit()).isEqualTo(5);
        assertThat(data.getGroups()).hasSize(3);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run from `account`:

```powershell
.\mvnw.cmd "-Dtest=SettingsSearchUseCaseTest" test
```

Expected: compilation fails because `SettingsSearchUseCase`, `SettingsGlobalSearchResponse`, and `SettingsGlobalSearchType` do not exist.

- [ ] **Step 3: Commit the failing test**

```powershell
git add account/src/test/java/serp/project/account/core/usecase/SettingsSearchUseCaseTest.java
git commit -m "test(account): define settings global search contract"
```

---

### Task 2: Backend DTOs And Use Case

**Files:**

- Create: `account/src/main/java/serp/project/account/core/domain/enums/SettingsGlobalSearchType.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchItemResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchGroupResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchResponse.java`
- Create: `account/src/main/java/serp/project/account/core/usecase/SettingsSearchUseCase.java`
- Test: `account/src/test/java/serp/project/account/core/usecase/SettingsSearchUseCaseTest.java`

- [ ] **Step 1: Add settings search enum**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.enums;

public enum SettingsGlobalSearchType {
    USER,
    DEPARTMENT,
    MODULE
}
```

- [ ] **Step 2: Add response DTOs**

Use Lombok annotations matching the admin global search DTOs. `SettingsGlobalSearchGroupResponse` should use `SettingsGlobalSearchType`.

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsGlobalSearchItemResponse {
    private String id;
    private String title;
    private String subtitle;
    private String url;
}
```

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsGlobalSearchGroupResponse {
    private SettingsGlobalSearchType type;
    private String title;
    private Long total;
    private List<SettingsGlobalSearchItemResponse> items;
}
```

```java
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsGlobalSearchResponse {
    private String query;
    private Integer limit;
    private List<SettingsGlobalSearchGroupResponse> groups;
}
```

- [ ] **Step 3: Implement `SettingsSearchUseCase`**

Key behavior:

- `search(Long organizationId, String query, Integer limit)`
- trim query
- blank query returns `responseUtils.badRequest(Constants.ErrorMessage.SEARCH_QUERY_REQUIRED)`
- default limit 5, max 10
- users use `GetUserParams` with organization id, page 0, page size limit, sort by email asc
- departments use `GetDepartmentParams` with organization id, page 0, page size limit, sort by name asc
- modules use active/pending subscription, included plan module ids, `moduleService.getModulesByIds(includedIds)`, filter by module name/code/description containing normalized query case-insensitively, sort by module name, limit top N
- URLs use `URLEncoder.encode(normalizedQuery, StandardCharsets.UTF_8)`

Important imports:

```java
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchGroupResponse;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchItemResponse;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchResponse;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.SettingsGlobalSearchType;
import serp.project.account.core.service.IDepartmentService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.ResponseUtils;
```

- [ ] **Step 4: Run use case tests**

Run from `account`:

```powershell
.\mvnw.cmd "-Dtest=SettingsSearchUseCaseTest" test
```

Expected: all `SettingsSearchUseCaseTest` tests pass.

- [ ] **Step 5: Commit backend use case**

```powershell
git add account/src/main/java/serp/project/account/core/domain/enums/SettingsGlobalSearchType.java account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchItemResponse.java account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchGroupResponse.java account/src/main/java/serp/project/account/core/domain/dto/response/SettingsGlobalSearchResponse.java account/src/main/java/serp/project/account/core/usecase/SettingsSearchUseCase.java account/src/test/java/serp/project/account/core/usecase/SettingsSearchUseCaseTest.java
git commit -m "feat(account): add settings global search use case"
```

---

### Task 3: Backend Controller Endpoint

**Files:**

- Create: `account/src/test/java/serp/project/account/ui/controller/SettingsSearchControllerTest.java`
- Create: `account/src/main/java/serp/project/account/ui/controller/SettingsSearchController.java`
- Test: `account/src/test/java/serp/project/account/ui/controller/SettingsSearchControllerTest.java`

- [ ] **Step 1: Write failing controller tests**

Create tests covering delegation and forbidden access.

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

import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.response.SettingsGlobalSearchResponse;
import serp.project.account.core.usecase.SettingsSearchUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class SettingsSearchControllerTest {
    @Mock
    private SettingsSearchUseCase settingsSearchUseCase;
    @Mock
    private AuthUtils authUtils;

    private final ResponseUtils responseUtils = new ResponseUtils();

    @InjectMocks
    private SettingsSearchController controller;

    @Test
    void searchShouldCheckOrganizationAccessDelegateToUseCaseAndUseResponseCode() {
        controller = new SettingsSearchController(settingsSearchUseCase, authUtils, responseUtils);
        GeneralResponse<SettingsGlobalSearchResponse> body = GeneralResponse.<SettingsGlobalSearchResponse>builder()
                .code(200)
                .status("success")
                .message("OK")
                .data(SettingsGlobalSearchResponse.builder()
                        .query("mai")
                        .limit(5)
                        .groups(List.of())
                        .build())
                .build();
        when(authUtils.canAccessOrganization(7L)).thenReturn(true);
        when(settingsSearchUseCase.search(7L, "mai", 5)).thenAnswer(_invocation -> body);

        ResponseEntity<?> response = controller.search(7L, "mai", 5);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(body, response.getBody());
        verify(authUtils).canAccessOrganization(7L);
        verify(settingsSearchUseCase).search(7L, "mai", 5);
    }

    @Test
    void searchShouldReturnForbiddenWhenOrganizationCannotBeAccessed() {
        controller = new SettingsSearchController(settingsSearchUseCase, authUtils, responseUtils);
        when(authUtils.canAccessOrganization(7L)).thenReturn(false);

        ResponseEntity<?> response = controller.search(7L, "mai", 5);

        assertEquals(403, response.getStatusCodeValue());
        verify(authUtils).canAccessOrganization(7L);
    }
}
```

- [ ] **Step 2: Run controller tests to verify they fail**

Run from `account`:

```powershell
.\mvnw.cmd "-Dtest=SettingsSearchControllerTest" test
```

Expected: compilation fails because `SettingsSearchController` does not exist.

- [ ] **Step 3: Implement controller**

Create `SettingsSearchController`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.usecase.SettingsSearchUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/settings/search")
public class SettingsSearchController {
    private final SettingsSearchUseCase settingsSearchUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @GetMapping
    public ResponseEntity<?> search(
            @PathVariable Long organizationId,
            @RequestParam String q,
            @RequestParam(required = false) Integer limit) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        var response = settingsSearchUseCase.search(organizationId, q, limit);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
```

- [ ] **Step 4: Run backend search tests**

Run from `account`:

```powershell
.\mvnw.cmd "-Dtest=SettingsSearchUseCaseTest,SettingsSearchControllerTest" test
```

Expected: both test classes pass.

- [ ] **Step 5: Commit controller endpoint**

```powershell
git add account/src/main/java/serp/project/account/ui/controller/SettingsSearchController.java account/src/test/java/serp/project/account/ui/controller/SettingsSearchControllerTest.java
git commit -m "feat(account): expose settings global search endpoint"
```

---

### Task 4: Frontend RTK Query Contract

**Files:**

- Create: `serp_web/src/modules/settings/types/global-search.types.ts`
- Modify: `serp_web/src/modules/settings/types/index.ts`
- Create: `serp_web/src/modules/settings/services/global-search/globalSearchApi.ts`
- Modify: `serp_web/src/modules/settings/services/settingsApi.ts`
- Modify: `serp_web/src/modules/settings/index.ts`

- [ ] **Step 1: Add settings search types**

Create `global-search.types.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings global search types
 */

export type SettingsGlobalSearchType = 'USER' | 'DEPARTMENT' | 'MODULE';

export interface SettingsGlobalSearchItem {
  id: string;
  title: string;
  subtitle?: string;
  url: string;
}

export interface SettingsGlobalSearchGroup {
  type: SettingsGlobalSearchType;
  title: string;
  total: number;
  items: SettingsGlobalSearchItem[];
}

export interface SettingsGlobalSearchResponse {
  query: string;
  limit: number;
  groups: SettingsGlobalSearchGroup[];
}

export interface SettingsGlobalSearchParams {
  organizationId: number;
  q: string;
  limit?: number;
}
```

Add to `types/index.ts`:

```ts
export * from './global-search.types';
```

- [ ] **Step 2: Add RTK Query endpoint**

Create `services/global-search/globalSearchApi.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings global search API
 */

import { api } from '@/lib/store/api/apiSlice';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  SettingsGlobalSearchParams,
  SettingsGlobalSearchResponse,
} from '@/modules/settings/types';

export const settingsGlobalSearchApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSettingsGlobalSearch: builder.query<
      SettingsGlobalSearchResponse,
      SettingsGlobalSearchParams
    >({
      query: ({ organizationId, q, limit = 5 }) => {
        const params = new URLSearchParams();
        params.set('q', q);
        params.set('limit', String(limit));

        return {
          url: `/organizations/${organizationId}/settings/search?${params.toString()}`,
          method: 'GET',
        };
      },
      transformResponse: createDataTransform<SettingsGlobalSearchResponse>(),
      extraOptions: { service: 'account' },
    }),
  }),
});

export const { useGetSettingsGlobalSearchQuery } = settingsGlobalSearchApi;
```

- [ ] **Step 3: Export the new service**

Add to `services/settingsApi.ts`:

```ts
export {
  settingsGlobalSearchApi,
  useGetSettingsGlobalSearchQuery,
} from './global-search/globalSearchApi';
```

No code change is required in `modules/settings/index.ts` if `export * from './services/settingsApi';` and `export type * from './types';` remain present. Confirm this still compiles.

- [ ] **Step 4: Run frontend type-check**

Run from `serp_web`:

```powershell
npm.cmd run type-check
```

Expected: TypeScript passes, or only reports errors from files changed in earlier unimplemented tasks if this task is run out of order.

- [ ] **Step 5: Commit frontend API contract**

```powershell
git add serp_web/src/modules/settings/types/global-search.types.ts serp_web/src/modules/settings/types/index.ts serp_web/src/modules/settings/services/global-search/globalSearchApi.ts serp_web/src/modules/settings/services/settingsApi.ts serp_web/src/modules/settings/index.ts
git commit -m "feat(settings): add global search api client"
```

---

### Task 5: Frontend Search Results Page

**Files:**

- Create: `serp_web/src/app/settings/search/page.tsx`

- [ ] **Step 1: Implement `/settings/search` page**

Create a client page modeled after `serp_web/src/app/admin/search/page.tsx`, using settings imports and copy. Use `useAuth()` for `user?.organizationId` because `useSettingsDepartments` already uses this source.

Key constants:

```ts
const GROUP_VIEW_ALL_HREF: Record<string, string> = {
  USER: '/settings/users',
  DEPARTMENT: '/settings/departments',
  MODULE: '/settings/modules',
};
```

Key query call:

```ts
const { data, isFetching, isLoading, isError, refetch } =
  useGetSettingsGlobalSearchQuery(
    { organizationId: organizationId as number, q: query, limit: 5 },
    { skip: query.length === 0 || !organizationId }
  );
```

Render states:

- no query: card title `Search settings resources`
- missing organization id: card title `Organization unavailable`
- loading: skeleton layout with 3 sections
- error: retry card
- success: title `Search results`, subtitle `Results for "{data?.query ?? query}" across organization settings.`

Use the same shared UI imports as admin search:

```ts
import { AlertTriangle, ArrowRight, RefreshCw, Search } from 'lucide-react';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';

import { useAuth } from '@/modules/account';
import {
  useGetSettingsGlobalSearchQuery,
  type SettingsGlobalSearchGroup,
} from '@/modules/settings';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';
```

- [ ] **Step 2: Run frontend type-check**

Run from `serp_web`:

```powershell
npm.cmd run type-check
```

Expected: TypeScript passes.

- [ ] **Step 3: Commit search page**

```powershell
git add serp_web/src/app/settings/search/page.tsx
git commit -m "feat(settings): add global search results page"
```

---

### Task 6: Wire Settings Header Search

**Files:**

- Modify: `serp_web/src/modules/settings/components/layout/SettingsHeader.tsx`

- [ ] **Step 1: Update navigation behavior**

Keep the existing navigation import:

```ts
import { useRouter, usePathname } from 'next/navigation';
```

Add an effect before the scroll effect:

```ts
useEffect(() => {
  if (pathname !== '/settings/search') {
    return;
  }

  const params = new URLSearchParams(window.location.search);
  setSearchQuery(params.get('q') || '');
}, [pathname]);
```

Replace `handleSearch`:

```ts
const handleSearch = (e: React.FormEvent) => {
  e.preventDefault();
  const trimmedQuery = searchQuery.trim();
  if (trimmedQuery) {
    router.push(`/settings/search?q=${encodeURIComponent(trimmedQuery)}`);
  }
};
```

- [ ] **Step 2: Run focused frontend checks**

Run from `serp_web`:

```powershell
npm.cmd run type-check
npm.cmd run lint
npx.cmd prettier --check src/app/settings/search/page.tsx src/modules/settings/components/layout/SettingsHeader.tsx src/modules/settings/types/global-search.types.ts src/modules/settings/services/global-search/globalSearchApi.ts src/modules/settings/types/index.ts src/modules/settings/services/settingsApi.ts src/modules/settings/index.ts
```

Expected: type-check, lint, and touched-file prettier check pass.

- [ ] **Step 3: Commit header wiring**

```powershell
git add serp_web/src/modules/settings/components/layout/SettingsHeader.tsx
git commit -m "feat(settings): wire global search header"
```

---

### Task 7: Final Verification

**Files:**

- Verify all touched backend and frontend files.

- [ ] **Step 1: Run backend focused tests**

Run from `account`:

```powershell
.\mvnw.cmd "-Dtest=SettingsSearchUseCaseTest,SettingsSearchControllerTest" test
```

Expected: both test classes pass.

- [ ] **Step 2: Run backend compile**

Run from `account`:

```powershell
.\mvnw.cmd clean compile
```

Expected: compile passes.

- [ ] **Step 3: Run frontend checks**

Run from `serp_web`:

```powershell
npm.cmd run type-check
npm.cmd run lint
npx.cmd prettier --check src/app/settings/search/page.tsx src/modules/settings/components/layout/SettingsHeader.tsx src/modules/settings/types/global-search.types.ts src/modules/settings/services/global-search/globalSearchApi.ts src/modules/settings/types/index.ts src/modules/settings/services/settingsApi.ts src/modules/settings/index.ts
```

Expected: all three frontend commands pass. If `npm.cmd run format:check` is run and fails on unrelated pre-existing files, record that separately and do not change unrelated files.

- [ ] **Step 4: Inspect git history and working tree**

```powershell
git status --short
git log --oneline -n 8
```

Expected: only intended committed changes are present; working tree is clean before final handoff.

---

## Self-Review

- Spec coverage: endpoint, organization access, users/departments/modules scope, grouped response, settings route, header navigation, error states, and verification are covered by Tasks 1-7.
- Placeholder scan: no unresolved implementation blanks or out-of-order undefined public contracts remain in the plan.
- Type consistency: backend uses `SettingsGlobalSearch*` names consistently; frontend uses `SettingsGlobalSearch*` names consistently; endpoint path matches the approved spec.
