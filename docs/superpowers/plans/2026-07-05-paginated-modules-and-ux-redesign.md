# Paginated Modules (V2) & UX/UI Redesign Implementation Plan (Updated)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a paginated backend API version 2 in a dedicated controller `ModuleV2Controller` (`GET /api/v2/modules`) and implement a modernized, URL-driven admin Modules interface in Next.js (`serp_web`) with Grid/List layout toggle and inline controls.

**Architecture:** Use `Pair<List<ModuleEntity>, Long>` in the service layer instead of Spring's `Page` library to retrieve paginated and filtered modules. Format the response using `PaginationUtils`. Integrate the paginated V2 query to the web client via RTK Query, use Next.js `useSearchParams()` for URL state-driven pagination, and implement a redesigned Grid UI using custom Card components.

**Tech Stack:** Java, Spring Boot, Spring Data JPA, React 19, Next.js 15, TypeScript, RTK Query, Lucide Icons.

---

### Task 1: Backend DTO and Repository Paginated Query

**Files:**
- Modify: `account/src/main/java/serp/project/account/core/domain/dto/request/GetModulesParams.java`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/repository/IModuleRepository.java`

- [ ] **Step 1: Update GetModulesParams to support BaseGetParams pagination**

Update `account/src/main/java/serp/project/account/core/domain/dto/request/GetModulesParams.java` to make sure it extends `BaseGetParams` and has search and filters.
```java
package serp.project.account.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.ModuleType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class GetModulesParams extends BaseGetParams {
    private String search;
    private ModuleStatus status;
    private ModuleType moduleType;
}
```

- [ ] **Step 2: Add JPA Repository methods for V2 paginated retrieval**

Modify `account/src/main/java/serp/project/account/infrastructure/store/repository/IModuleRepository.java` to search and count without returning `Page`:
```java
    @Query("""
            SELECT m FROM ModuleModel m
            WHERE (:search IS NULL OR LOWER(m.moduleName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(m.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR m.status = :status)
              AND (:moduleType IS NULL OR m.moduleType = :moduleType)
            """)
    List<ModuleModel> findAllPaginated(
            @Param("search") String search,
            @Param("status") ModuleStatus status,
            @Param("moduleType") ModuleType moduleType,
            Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM ModuleModel m
            WHERE (:search IS NULL OR LOWER(m.moduleName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(m.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR m.status = :status)
              AND (:moduleType IS NULL OR m.moduleType = :moduleType)
            """)
    Long countAllPaginated(
            @Param("search") String search,
            @Param("status") ModuleStatus status,
            @Param("moduleType") ModuleType moduleType);
```

- [ ] **Step 3: Verify backend compiles**

Run: `mvnw.cmd clean compile`
Expected: BUILD SUCCESS

---

### Task 2: Update ModulePort and Adapter Layer to return Pair

**Files:**
- Modify: `account/src/main/java/serp/project/account/core/port/store/IModulePort.java`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/adapter/ModuleAdapter.java`

- [ ] **Step 1: Define getModulesPaginated returning Pair in IModulePort**

Modify `account/src/main/java/serp/project/account/core/port/store/IModulePort.java`:
```java
    Pair<List<ModuleEntity>, Long> getModulesPaginated(
            String search,
            ModuleStatus status,
            ModuleType moduleType,
            org.springframework.data.domain.Pageable pageable);
```

- [ ] **Step 2: Implement getModulesPaginated returning Pair in ModuleAdapter**

Modify `account/src/main/java/serp/project/account/infrastructure/store/adapter/ModuleAdapter.java`:
```java
    @Override
    public Pair<List<ModuleEntity>, Long> getModulesPaginated(
            String search,
            ModuleStatus status,
            ModuleType moduleType,
            org.springframework.data.domain.Pageable pageable) {
        var models = moduleRepository.findAllPaginated(search, status, moduleType, pageable);
        var total = moduleRepository.countAllPaginated(search, status, moduleType);
        return Pair.of(moduleMapper.toEntityList(models), total != null ? total : 0L);
    }
```

- [ ] **Step 3: Verify backend compiles**

Run: `mvnw.cmd clean compile`
Expected: BUILD SUCCESS

---

### Task 3: Update Service and UseCase Layers

**Files:**
- Modify: `account/src/main/java/serp/project/account/core/service/IModuleService.java`
- Modify: `account/src/main/java/serp/project/account/core/service/impl/ModuleService.java`
- Modify: `account/src/main/java/serp/project/account/core/usecase/ModuleUseCase.java`

- [ ] **Step 1: Define getModulesPaginated returning Pair in IModuleService**

Modify `account/src/main/java/serp/project/account/core/service/IModuleService.java`:
```java
    Pair<List<ModuleEntity>, Long> getModulesPaginated(
            String search,
            ModuleStatus status,
            ModuleType moduleType,
            org.springframework.data.domain.Pageable pageable);
```

- [ ] **Step 2: Implement getModulesPaginated in ModuleService**

Modify `account/src/main/java/serp/project/account/core/service/impl/ModuleService.java`:
```java
    @Override
    public Pair<List<ModuleEntity>, Long> getModulesPaginated(
            String search,
            ModuleStatus status,
            ModuleType moduleType,
            org.springframework.data.domain.Pageable pageable) {
        return modulePort.getModulesPaginated(search, status, moduleType, pageable);
    }
```

- [ ] **Step 3: Implement getModulesPaginated in ModuleUseCase**

Modify `account/src/main/java/serp/project/account/core/usecase/ModuleUseCase.java`:
```java
    public GeneralResponse<?> getModulesPaginated(GetModulesParams params) {
        try {
            var pageable = paginationUtils.getPageable(params);
            var result = moduleService.getModulesPaginated(
                    params.getSearch(),
                    params.getStatus(),
                    params.getModuleType(),
                    pageable
            );
            
            var responseData = paginationUtils.getResponse(
                    result.getSecond(),
                    params.getPage(),
                    params.getPageSize(),
                    result.getFirst()
            );
            return responseUtils.success(responseData);
        } catch (Exception e) {
            log.error("Error retrieving paginated modules: {}", e.getMessage());
            throw e;
        }
    }
```

- [ ] **Step 4: Verify backend compiles**

Run: `mvnw.cmd clean compile`
Expected: BUILD SUCCESS

---

### Task 4: Create ModuleV2Controller and Integration Tests

**Files:**
- Modify: `account/src/main/java/serp/project/account/ui/controller/ModuleController.java` (revert getModulesPaginated V2 change from V1 controller)
- Create: `account/src/main/java/serp/project/account/ui/controller/ModuleV2Controller.java`
- Modify: `account/src/test/java/serp/project/account/ui/controller/ModuleControllerTest.java`

- [ ] **Step 1: Revert V1 ModuleController change**

Modify `account/src/main/java/serp/project/account/ui/controller/ModuleController.java` to remove `@GetMapping("/v2")` and `GetModulesParams` import.

- [ ] **Step 2: Create ModuleV2Controller**

Create `account/src/main/java/serp/project/account/ui/controller/ModuleV2Controller.java`:
```java
package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.account.core.domain.dto.request.GetModulesParams;
import serp.project.account.core.usecase.ModuleUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/modules")
public class ModuleV2Controller {
    private final ModuleUseCase moduleUseCase;

    @GetMapping
    public ResponseEntity<?> getModulesPaginated(@Valid GetModulesParams params) {
        var response = moduleUseCase.getModulesPaginated(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
```

- [ ] **Step 3: Update ModuleControllerTest to verify V2 mapping**

Modify `account/src/test/java/serp/project/account/ui/controller/ModuleControllerTest.java` to mock `ModuleV2Controller` instead of `ModuleController`.
```java
package serp.project.account.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetModulesParams;
import serp.project.account.core.usecase.ModuleUseCase;

@ExtendWith(MockitoExtension.class)
class ModuleControllerTest {

    @Mock
    private ModuleUseCase moduleUseCase;

    @InjectMocks
    private ModuleV2Controller controller;

    @Test
    void getModulesPaginatedShouldCallUseCaseWithQueryParams() throws Exception {
        GeneralResponse<?> responseBody = GeneralResponse.builder()
                .code(200)
                .status("SUCCESS")
                .message("OK")
                .build();
        doReturn(responseBody).when(moduleUseCase).getModulesPaginated(any());

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v2/modules")
                .param("page", "1")
                .param("pageSize", "20")
                .param("sortBy", "code")
                .param("sortDirection", "desc")
                .param("search", "crm")
                .param("status", "ACTIVE")
                .param("moduleType", "SYSTEM"))
                .andExpect(status().isOk());

        ArgumentCaptor<GetModulesParams> captor = ArgumentCaptor.forClass(GetModulesParams.class);
        verify(moduleUseCase).getModulesPaginated(captor.capture());
        assertEquals(1, captor.getValue().getPage());
        assertEquals(20, captor.getValue().getPageSize());
        assertEquals("code", captor.getValue().getSortBy());
        assertEquals("desc", captor.getValue().getSortDirection());
        assertEquals("crm", captor.getValue().getSearch());
        assertEquals(serp.project.account.core.domain.enums.ModuleStatus.ACTIVE, captor.getValue().getStatus());
        assertEquals(serp.project.account.core.domain.enums.ModuleType.SYSTEM, captor.getValue().getModuleType());
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvnw.cmd test -Dtest=ModuleControllerTest`
Expected: PASS

---

### Task 5: Web API client configuration for V2

**Files:**
- Modify: `serp_web/src/modules/admin/services/modules/modulesApi.ts`

- [ ] **Step 1: Update API URL to point to /modules/v2 (proxied from /api/v2/modules)**

Modify `serp_web/src/modules/admin/services/modules/modulesApi.ts` to query `/modules/v2`:
```typescript
    getModulesV2: builder.query<PaginatedModulesResponse, ModuleQueryParams>({
      query: (params) => ({
        url: '/modules/v2',
        method: 'GET',
        params,
      }),
      transformResponse: createDataTransform<PaginatedModulesResponse>(),
      providesTags: (result) =>
        result?.items
          ? [
              ...result.items.map(({ id }) => ({
                type: 'admin/Module' as const,
                id,
              })),
              { type: 'admin/Module', id: 'LIST' },
            ]
          : [{ type: 'admin/Module', id: 'LIST' }],
    }),
```

---

### Task 6: Refactor useModules Hook to support URL State synchronization

*Note: Hook already refactored. No changes needed.*

---

### Task 7: Redesign Admin Modules Page UI/UX

*Note: Page UI already redesigned. No changes needed.*
