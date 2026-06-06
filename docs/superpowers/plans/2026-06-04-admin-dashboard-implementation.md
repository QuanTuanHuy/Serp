# SERP Admin Dashboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the hard-coded SERP admin dashboard with a live account-service operations dashboard backed by `GET /api/v1/admin/dashboard`.

**Architecture:** Add a focused account-service aggregate read path: controller -> use case -> service/port contracts -> repository adapters. The frontend consumes that single endpoint through RTK Query and renders dense operational cards, queues, and lists inside the existing admin layout.

**Tech Stack:** Java 21, Spring Boot 3.5.5, JUnit 5, Mockito, Next.js App Router, React 19, TypeScript, RTK Query, Tailwind CSS, Shadcn-style UI primitives, lucide-react, recharts.

---

## File Structure

Backend files to create:

- `account/src/main/java/serp/project/account/ui/controller/AdminDashboardController.java`: thin REST controller for `/api/v1/admin/dashboard`.
- `account/src/main/java/serp/project/account/core/usecase/AdminDashboardUseCase.java`: aggregate account-service dashboard metrics and response.
- `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardResponse.java`: top-level dashboard DTO.
- `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardMetricsResponse.java`: KPI metrics DTO.
- `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardActionQueueResponse.java`: operational queue DTO.
- `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardRecentOrganizationResponse.java`: recent organization row DTO.
- `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardStatusCountResponse.java`: status breakdown DTO.
- `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardConfigurationCoverageResponse.java`: plans/modules/menu display coverage DTO.
- `account/src/test/java/serp/project/account/core/usecase/AdminDashboardUseCaseTest.java`: focused aggregation regression tests.

Backend files to modify:

- `account/src/main/java/serp/project/account/core/service/IOrganizationService.java`: add count/recent dashboard methods.
- `account/src/main/java/serp/project/account/core/service/IUserService.java`: add global count/status and org-count methods.
- `account/src/main/java/serp/project/account/core/service/ISubscriptionService.java`: add count/status/ending-soon/recent-org subscription methods.
- `account/src/main/java/serp/project/account/core/service/ISubscriptionPlanService.java`: add dashboard count methods.
- `account/src/main/java/serp/project/account/core/service/IModuleService.java`: add dashboard count methods.
- `account/src/main/java/serp/project/account/core/service/IMenuDisplayService.java`: add dashboard count methods.
- `account/src/main/java/serp/project/account/core/port/store/IOrganizationPort.java`: add count/recent port methods.
- `account/src/main/java/serp/project/account/core/port/store/IUserPort.java`: add global count/status and org-count port methods.
- `account/src/main/java/serp/project/account/core/port/store/IOrganizationSubscriptionPort.java`: add count/status/ending-soon/recent-org subscription port methods.
- `account/src/main/java/serp/project/account/core/port/store/ISubscriptionPlanPort.java`: add dashboard count port methods.
- `account/src/main/java/serp/project/account/core/port/store/IModulePort.java`: add dashboard count port methods.
- `account/src/main/java/serp/project/account/core/port/store/IMenuDisplayPort.java`: add dashboard count port methods.
- `account/src/main/java/serp/project/account/core/service/impl/OrganizationService.java`: delegate dashboard methods to port.
- `account/src/main/java/serp/project/account/core/service/impl/UserService.java`: delegate dashboard methods to port.
- `account/src/main/java/serp/project/account/core/service/impl/SubscriptionService.java`: delegate dashboard methods to port.
- `account/src/main/java/serp/project/account/core/service/impl/SubscriptionPlanService.java`: delegate dashboard methods to port.
- `account/src/main/java/serp/project/account/core/service/impl/ModuleService.java`: delegate dashboard methods to port.
- `account/src/main/java/serp/project/account/core/service/impl/MenuDisplayService.java`: delegate dashboard methods to port.
- `account/src/main/java/serp/project/account/infrastructure/store/adapter/OrganizationAdapter.java`: implement count/recent queries.
- `account/src/main/java/serp/project/account/infrastructure/store/adapter/UserAdapter.java`: implement global/user status/org count queries.
- `account/src/main/java/serp/project/account/infrastructure/store/adapter/OrganizationSubscriptionAdapter.java`: implement subscription dashboard queries.
- `account/src/main/java/serp/project/account/infrastructure/store/adapter/SubscriptionPlanAdapter.java`: implement plan count queries.
- `account/src/main/java/serp/project/account/infrastructure/store/adapter/ModuleAdapter.java`: implement module count queries.
- `account/src/main/java/serp/project/account/infrastructure/store/adapter/MenuDisplayAdapter.java`: implement menu display count queries.
- `account/src/main/java/serp/project/account/infrastructure/store/repository/IOrganizationRepository.java`: Spring Data count/recent methods.
- `account/src/main/java/serp/project/account/infrastructure/store/repository/IUserRepository.java`: JPQL count methods.
- `account/src/main/java/serp/project/account/infrastructure/store/repository/IOrganizationSubscriptionRepository.java`: JPQL count/status/recent-org methods.
- `account/src/main/java/serp/project/account/infrastructure/store/repository/ISubscriptionPlanRepository.java`: Spring Data count methods.
- `account/src/main/java/serp/project/account/infrastructure/store/repository/IModuleRepository.java`: Spring Data count methods.
- `account/src/main/java/serp/project/account/infrastructure/store/repository/IMenuDisplayRepository.java`: Spring Data count methods.

Frontend files to create:

- `serp_web/src/modules/admin/types/dashboard.types.ts`: dashboard response types.
- `serp_web/src/modules/admin/services/dashboard/dashboardApi.ts`: RTK Query endpoint for admin dashboard.

Frontend files to modify:

- `serp_web/src/lib/store/api/apiSlice.ts`: add `admin/Dashboard` tag.
- `serp_web/src/modules/admin/types/index.ts`: export dashboard types.
- `serp_web/src/modules/admin/services/adminApi.ts`: export dashboard API hook.
- `serp_web/src/modules/admin/index.ts`: export dashboard type/hook if needed by route.
- `serp_web/src/app/admin/page.tsx`: replace hard-coded dashboard with live data, loading, empty, error, and retry states.

---

### Task 1: Backend Dashboard DTOs And Use Case Test

**Files:**
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardMetricsResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardActionQueueResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardRecentOrganizationResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardStatusCountResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboardConfigurationCoverageResponse.java`
- Create: `account/src/test/java/serp/project/account/core/usecase/AdminDashboardUseCaseTest.java`

- [ ] **Step 1: Create the dashboard DTO files**

Use this structure for every DTO:

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
```

`AdminDashboardResponse.java`:

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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminDashboardResponse {
    private AdminDashboardMetricsResponse metrics;
    private AdminDashboardActionQueueResponse actionQueue;
    private List<AdminDashboardRecentOrganizationResponse> recentOrganizations;
    private List<AdminDashboardStatusCountResponse> organizationStatuses;
    private List<AdminDashboardStatusCountResponse> subscriptionStatuses;
    private AdminDashboardConfigurationCoverageResponse configurationCoverage;
    private Long generatedAt;
}
```

`AdminDashboardMetricsResponse.java`:

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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminDashboardMetricsResponse {
    private Long totalOrganizations;
    private Long activeOrganizations;
    private Long suspendedOrganizations;
    private Long expiredOrganizations;
    private Long totalUsers;
    private Long activeUsers;
    private Long suspendedUsers;
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Long trialSubscriptions;
    private Long pendingSubscriptions;
    private Long expiredSubscriptions;
}
```

`AdminDashboardActionQueueResponse.java`:

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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminDashboardActionQueueResponse {
    private Long pendingSubscriptions;
    private Long subscriptionsEndingSoon;
    private Long trialsEndingSoon;
    private Long suspendedOrganizations;
    private Long expiredOrganizations;
}
```

`AdminDashboardRecentOrganizationResponse.java`:

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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminDashboardRecentOrganizationResponse {
    private Long id;
    private String name;
    private String code;
    private String status;
    private Long userCount;
    private String subscriptionStatus;
    private Long createdAt;
}
```

`AdminDashboardStatusCountResponse.java`:

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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminDashboardStatusCountResponse {
    private String status;
    private Long count;
}
```

`AdminDashboardConfigurationCoverageResponse.java`:

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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminDashboardConfigurationCoverageResponse {
    private Long totalPlans;
    private Long activePlans;
    private Long inactivePlans;
    private Long totalModules;
    private Long availableModules;
    private Long unavailableModules;
    private Long totalMenuDisplays;
    private Long visibleMenuDisplays;
    private Long hiddenMenuDisplays;
}
```

- [ ] **Step 2: Write the failing use case test**

Create `account/src/test/java/serp/project/account/core/usecase/AdminDashboardUseCaseTest.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardResponse;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.IMenuDisplayService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.ResponseUtils;

@ExtendWith(MockitoExtension.class)
class AdminDashboardUseCaseTest {
    @Mock
    private IOrganizationService organizationService;
    @Mock
    private IUserService userService;
    @Mock
    private ISubscriptionService subscriptionService;
    @Mock
    private ISubscriptionPlanService subscriptionPlanService;
    @Mock
    private IModuleService moduleService;
    @Mock
    private IMenuDisplayService menuDisplayService;

    private final ResponseUtils responseUtils = new ResponseUtils();

    @InjectMocks
    private AdminDashboardUseCase adminDashboardUseCase;

    @Test
    void getDashboardAggregatesAccountOperationsData() {
        adminDashboardUseCase = new AdminDashboardUseCase(
                organizationService,
                userService,
                subscriptionService,
                subscriptionPlanService,
                moduleService,
                menuDisplayService,
                responseUtils);

        var recentOrganization = OrganizationEntity.builder()
                .id(10L)
                .name("Acme")
                .code("ACME")
                .status(OrganizationStatus.ACTIVE)
                .createdAt(1_700_000_000_000L)
                .build();

        when(organizationService.countOrganizations()).thenReturn(4L);
        when(organizationService.countOrganizationsByStatus(OrganizationStatus.ACTIVE)).thenReturn(3L);
        when(organizationService.countOrganizationsByStatus(OrganizationStatus.SUSPENDED)).thenReturn(1L);
        when(organizationService.countOrganizationsByStatus(OrganizationStatus.EXPIRED)).thenReturn(0L);
        when(organizationService.getRecentOrganizations(5)).thenReturn(List.of(recentOrganization));

        when(userService.countUsers()).thenReturn(12L);
        when(userService.countUsersByStatus(UserStatus.ACTIVE)).thenReturn(10L);
        when(userService.countUsersByStatus(UserStatus.SUSPENDED)).thenReturn(2L);
        when(userService.countUsersByOrganizationIds(List.of(10L))).thenReturn(Map.of(10L, 5L));

        when(subscriptionService.countSubscriptions()).thenReturn(6L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.ACTIVE)).thenReturn(3L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.TRIAL)).thenReturn(1L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING)).thenReturn(1L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING_UPGRADE)).thenReturn(1L);
        when(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.EXPIRED)).thenReturn(0L);
        when(subscriptionService.countSubscriptionsEndingSoon(anyLong(), anyLong())).thenReturn(2L);
        when(subscriptionService.countTrialsEndingSoon(anyLong(), anyLong())).thenReturn(1L);
        when(subscriptionService.getLatestSubscriptionStatusByOrganizationIds(List.of(10L)))
                .thenReturn(Map.of(10L, SubscriptionStatus.ACTIVE.name()));

        when(subscriptionPlanService.countPlans()).thenReturn(3L);
        when(subscriptionPlanService.countActivePlans()).thenReturn(2L);
        when(moduleService.countModules()).thenReturn(5L);
        when(moduleService.countAvailableModules()).thenReturn(4L);
        when(menuDisplayService.countMenuDisplays()).thenReturn(9L);
        when(menuDisplayService.countVisibleMenuDisplays()).thenReturn(7L);

        GeneralResponse<?> response = adminDashboardUseCase.getDashboard();

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isInstanceOf(AdminDashboardResponse.class);
        var dashboard = (AdminDashboardResponse) response.getData();
        assertThat(dashboard.getMetrics().getTotalOrganizations()).isEqualTo(4L);
        assertThat(dashboard.getMetrics().getPendingSubscriptions()).isEqualTo(2L);
        assertThat(dashboard.getActionQueue().getSubscriptionsEndingSoon()).isEqualTo(2L);
        assertThat(dashboard.getRecentOrganizations()).hasSize(1);
        assertThat(dashboard.getRecentOrganizations().getFirst().getUserCount()).isEqualTo(5L);
        assertThat(dashboard.getRecentOrganizations().getFirst().getSubscriptionStatus()).isEqualTo("ACTIVE");
        assertThat(dashboard.getConfigurationCoverage().getInactivePlans()).isEqualTo(1L);
        assertThat(dashboard.getConfigurationCoverage().getUnavailableModules()).isEqualTo(1L);
        assertThat(dashboard.getConfigurationCoverage().getHiddenMenuDisplays()).isEqualTo(2L);
    }
}
```

- [ ] **Step 3: Run the failing test**

Run from `account`:

```bash
mvnw.cmd -Dtest=AdminDashboardUseCaseTest test
```

Expected: compile failure because `AdminDashboardUseCase` and the new service methods do not exist yet.

- [ ] **Step 4: Commit the DTOs and failing test**

```bash
git add account/src/main/java/serp/project/account/core/domain/dto/response/AdminDashboard*.java account/src/test/java/serp/project/account/core/usecase/AdminDashboardUseCaseTest.java
git commit -m "test(account): define admin dashboard aggregation contract"
```

### Task 2: Backend Query Contracts And Adapter Implementations

**Files:**
- Modify: service interfaces, port interfaces, service implementations, adapters, repositories listed in File Structure.

- [ ] **Step 1: Add organization dashboard methods**

Add to `IOrganizationService` and `IOrganizationPort`:

```java
Long countOrganizations();

Long countOrganizationsByStatus(OrganizationStatus status);

List<OrganizationEntity> getRecentOrganizations(int limit);
```

Add to `IOrganizationRepository`:

```java
Long countByStatus(OrganizationStatus status);

List<OrganizationModel> findTop5ByOrderByCreatedAtDesc();
```

Implement in `OrganizationAdapter`:

```java
@Override
public Long countOrganizations() {
    return organizationRepository.count();
}

@Override
public Long countOrganizationsByStatus(OrganizationStatus status) {
    return organizationRepository.countByStatus(status);
}

@Override
public List<OrganizationEntity> getRecentOrganizations(int limit) {
    return organizationMapper.toEntityList(organizationRepository.findTop5ByOrderByCreatedAtDesc());
}
```

Implement in `OrganizationService`:

```java
@Override
public Long countOrganizations() {
    return organizationPort.countOrganizations();
}

@Override
public Long countOrganizationsByStatus(OrganizationStatus status) {
    return organizationPort.countOrganizationsByStatus(status);
}

@Override
public List<OrganizationEntity> getRecentOrganizations(int limit) {
    return organizationPort.getRecentOrganizations(limit);
}
```

- [ ] **Step 2: Add user dashboard methods**

Add to `IUserService` and `IUserPort`:

```java
Long countUsers();

Long countUsersByStatus(UserStatus status);

Map<Long, Long> countUsersByOrganizationIds(List<Long> organizationIds);
```

Add to `IUserRepository`:

```java
Long countByStatus(UserStatus status);

@Query("SELECT u.primaryOrganizationId, COUNT(u) FROM UserModel u " +
        "WHERE u.primaryOrganizationId IN :organizationIds " +
        "GROUP BY u.primaryOrganizationId")
List<Object[]> countUsersByOrganizationIds(@Param("organizationIds") List<Long> organizationIds);
```

Implement in `UserAdapter`:

```java
@Override
public Long countUsers() {
    return userRepository.count();
}

@Override
public Long countUsersByStatus(UserStatus status) {
    return userRepository.countByStatus(status);
}

@Override
public Map<Long, Long> countUsersByOrganizationIds(List<Long> organizationIds) {
    if (organizationIds == null || organizationIds.isEmpty()) {
        return Map.of();
    }
    return userRepository.countUsersByOrganizationIds(organizationIds).stream()
            .collect(Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (Long) row[1]));
}
```

Implement matching delegations in `UserService`.

- [ ] **Step 3: Add subscription dashboard methods**

Add to `ISubscriptionService` and `IOrganizationSubscriptionPort`:

```java
Long countSubscriptions();

Long countSubscriptionsByStatus(SubscriptionStatus status);

Long countSubscriptionsEndingSoon(Long fromTimestamp, Long toTimestamp);

Long countTrialsEndingSoon(Long fromTimestamp, Long toTimestamp);

Map<Long, String> getLatestSubscriptionStatusByOrganizationIds(List<Long> organizationIds);
```

Add to `IOrganizationSubscriptionRepository`:

```java
Long countByStatus(SubscriptionStatus status);

@Query("SELECT COUNT(os) FROM OrganizationSubscriptionModel os " +
        "WHERE os.status = 'ACTIVE' " +
        "AND os.endDate IS NOT NULL " +
        "AND os.endDate >= :from " +
        "AND os.endDate <= :to")
Long countActiveEndingSoon(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

@Query("SELECT COUNT(os) FROM OrganizationSubscriptionModel os " +
        "WHERE os.status = 'TRIAL' " +
        "AND os.trialEndsAt IS NOT NULL " +
        "AND os.trialEndsAt >= :from " +
        "AND os.trialEndsAt <= :to")
Long countTrialsEndingSoon(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

@Query("SELECT os FROM OrganizationSubscriptionModel os " +
        "WHERE os.organizationId IN :organizationIds " +
        "AND os.createdAt = (" +
        "  SELECT MAX(innerOs.createdAt) FROM OrganizationSubscriptionModel innerOs " +
        "  WHERE innerOs.organizationId = os.organizationId" +
        ")")
List<OrganizationSubscriptionModel> findLatestByOrganizationIds(@Param("organizationIds") List<Long> organizationIds);
```

Implement in `OrganizationSubscriptionAdapter` by converting epoch millis to `LocalDateTime` with the same mapper utility pattern already used in existing mappers. The method body should return `Map.of()` for empty organization IDs and map status with `subscription.getStatus().name()`.

Implement matching delegations in `SubscriptionService`.

- [ ] **Step 4: Add plan/module/menu display coverage methods**

Add to plan service/port:

```java
Long countPlans();

Long countActivePlans();
```

Add to `ISubscriptionPlanRepository`:

```java
Long countByIsActive(Boolean isActive);
```

Add to module service/port:

```java
Long countModules();

Long countAvailableModules();
```

Add to `IModuleRepository`:

```java
Long countByStatus(ModuleStatus status);
```

Add to menu display service/port:

```java
Long countMenuDisplays();

Long countVisibleMenuDisplays();
```

Add to `IMenuDisplayRepository`:

```java
Long countByIsVisible(Boolean isVisible);
```

Implement adapters with repository `count()` and count-by-field methods. Implement services as direct delegations.

- [ ] **Step 5: Run the focused test again**

Run from `account`:

```bash
mvnw.cmd -Dtest=AdminDashboardUseCaseTest test
```

Expected: compile failure only because `AdminDashboardUseCase` still does not exist.

- [ ] **Step 6: Commit query contracts**

```bash
git add account/src/main/java/serp/project/account/core/service account/src/main/java/serp/project/account/core/port/store account/src/main/java/serp/project/account/core/service/impl account/src/main/java/serp/project/account/infrastructure/store/adapter account/src/main/java/serp/project/account/infrastructure/store/repository
git commit -m "feat(account): add admin dashboard query contracts"
```

### Task 3: Backend Use Case And Controller

**Files:**
- Create: `account/src/main/java/serp/project/account/core/usecase/AdminDashboardUseCase.java`
- Create: `account/src/main/java/serp/project/account/ui/controller/AdminDashboardController.java`
- Modify: `account/src/test/java/serp/project/account/core/usecase/AdminDashboardUseCaseTest.java` if constructor injection details need alignment.

- [ ] **Step 1: Implement `AdminDashboardUseCase`**

Create `AdminDashboardUseCase.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardActionQueueResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardConfigurationCoverageResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardMetricsResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardRecentOrganizationResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardResponse;
import serp.project.account.core.domain.dto.response.AdminDashboardStatusCountResponse;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.IMenuDisplayService;
import serp.project.account.core.service.IModuleService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.ISubscriptionPlanService;
import serp.project.account.core.service.ISubscriptionService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardUseCase {
    private static final int RECENT_ORGANIZATION_LIMIT = 5;
    private static final int ENDING_SOON_DAYS = 7;
    private static final String NO_SUBSCRIPTION = "NO_SUBSCRIPTION";

    private final IOrganizationService organizationService;
    private final IUserService userService;
    private final ISubscriptionService subscriptionService;
    private final ISubscriptionPlanService subscriptionPlanService;
    private final IModuleService moduleService;
    private final IMenuDisplayService menuDisplayService;
    private final ResponseUtils responseUtils;

    @Transactional(readOnly = true)
    public GeneralResponse<?> getDashboard() {
        try {
            long now = Instant.now().toEpochMilli();
            long endingSoon = Instant.now().plus(ENDING_SOON_DAYS, ChronoUnit.DAYS).toEpochMilli();

            long pendingSubscriptions = safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING))
                    + safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING_UPGRADE));

            var metrics = AdminDashboardMetricsResponse.builder()
                    .totalOrganizations(safe(organizationService.countOrganizations()))
                    .activeOrganizations(safe(organizationService.countOrganizationsByStatus(OrganizationStatus.ACTIVE)))
                    .suspendedOrganizations(safe(organizationService.countOrganizationsByStatus(OrganizationStatus.SUSPENDED)))
                    .expiredOrganizations(safe(organizationService.countOrganizationsByStatus(OrganizationStatus.EXPIRED)))
                    .totalUsers(safe(userService.countUsers()))
                    .activeUsers(safe(userService.countUsersByStatus(UserStatus.ACTIVE)))
                    .suspendedUsers(safe(userService.countUsersByStatus(UserStatus.SUSPENDED)))
                    .totalSubscriptions(safe(subscriptionService.countSubscriptions()))
                    .activeSubscriptions(safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.ACTIVE)))
                    .trialSubscriptions(safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.TRIAL)))
                    .pendingSubscriptions(pendingSubscriptions)
                    .expiredSubscriptions(safe(subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.EXPIRED)))
                    .build();

            var actionQueue = AdminDashboardActionQueueResponse.builder()
                    .pendingSubscriptions(pendingSubscriptions)
                    .subscriptionsEndingSoon(safe(subscriptionService.countSubscriptionsEndingSoon(now, endingSoon)))
                    .trialsEndingSoon(safe(subscriptionService.countTrialsEndingSoon(now, endingSoon)))
                    .suspendedOrganizations(metrics.getSuspendedOrganizations())
                    .expiredOrganizations(metrics.getExpiredOrganizations())
                    .build();

            var recentOrganizations = buildRecentOrganizations();
            long totalPlans = safe(subscriptionPlanService.countPlans());
            long activePlans = safe(subscriptionPlanService.countActivePlans());
            long totalModules = safe(moduleService.countModules());
            long availableModules = safe(moduleService.countAvailableModules());
            long totalMenuDisplays = safe(menuDisplayService.countMenuDisplays());
            long visibleMenuDisplays = safe(menuDisplayService.countVisibleMenuDisplays());

            var dashboard = AdminDashboardResponse.builder()
                    .metrics(metrics)
                    .actionQueue(actionQueue)
                    .recentOrganizations(recentOrganizations)
                    .organizationStatuses(buildOrganizationStatuses())
                    .subscriptionStatuses(buildSubscriptionStatuses())
                    .configurationCoverage(AdminDashboardConfigurationCoverageResponse.builder()
                            .totalPlans(totalPlans)
                            .activePlans(activePlans)
                            .inactivePlans(totalPlans - activePlans)
                            .totalModules(totalModules)
                            .availableModules(availableModules)
                            .unavailableModules(totalModules - availableModules)
                            .totalMenuDisplays(totalMenuDisplays)
                            .visibleMenuDisplays(visibleMenuDisplays)
                            .hiddenMenuDisplays(totalMenuDisplays - visibleMenuDisplays)
                            .build())
                    .generatedAt(now)
                    .build();

            return responseUtils.success(dashboard);
        } catch (Exception e) {
            log.error("Error building admin dashboard: {}", e.getMessage(), e);
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    private List<AdminDashboardRecentOrganizationResponse> buildRecentOrganizations() {
        List<OrganizationEntity> organizations = organizationService.getRecentOrganizations(RECENT_ORGANIZATION_LIMIT);
        List<Long> organizationIds = organizations.stream().map(OrganizationEntity::getId).toList();
        Map<Long, Long> userCounts = userService.countUsersByOrganizationIds(organizationIds);
        Map<Long, String> subscriptionStatuses =
                subscriptionService.getLatestSubscriptionStatusByOrganizationIds(organizationIds);

        return organizations.stream()
                .map(organization -> AdminDashboardRecentOrganizationResponse.builder()
                        .id(organization.getId())
                        .name(organization.getName())
                        .code(organization.getCode())
                        .status(organization.getStatus() != null ? organization.getStatus().name() : null)
                        .userCount(userCounts.getOrDefault(organization.getId(), 0L))
                        .subscriptionStatus(subscriptionStatuses.getOrDefault(organization.getId(), NO_SUBSCRIPTION))
                        .createdAt(organization.getCreatedAt())
                        .build())
                .toList();
    }

    private List<AdminDashboardStatusCountResponse> buildOrganizationStatuses() {
        return List.of(
                statusCount(OrganizationStatus.ACTIVE.name(), organizationService.countOrganizationsByStatus(OrganizationStatus.ACTIVE)),
                statusCount(OrganizationStatus.TRIAL.name(), organizationService.countOrganizationsByStatus(OrganizationStatus.TRIAL)),
                statusCount(OrganizationStatus.SUSPENDED.name(), organizationService.countOrganizationsByStatus(OrganizationStatus.SUSPENDED)),
                statusCount(OrganizationStatus.EXPIRED.name(), organizationService.countOrganizationsByStatus(OrganizationStatus.EXPIRED)),
                statusCount(OrganizationStatus.CLOSED.name(), organizationService.countOrganizationsByStatus(OrganizationStatus.CLOSED)));
    }

    private List<AdminDashboardStatusCountResponse> buildSubscriptionStatuses() {
        return List.of(
                statusCount(SubscriptionStatus.ACTIVE.name(), subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.ACTIVE)),
                statusCount(SubscriptionStatus.TRIAL.name(), subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.TRIAL)),
                statusCount(SubscriptionStatus.PENDING.name(), subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING)),
                statusCount(SubscriptionStatus.PENDING_UPGRADE.name(), subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PENDING_UPGRADE)),
                statusCount(SubscriptionStatus.EXPIRED.name(), subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.EXPIRED)),
                statusCount(SubscriptionStatus.CANCELLED.name(), subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.CANCELLED)),
                statusCount(SubscriptionStatus.PAYMENT_FAILED.name(), subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.PAYMENT_FAILED)),
                statusCount(SubscriptionStatus.GRACE_PERIOD.name(), subscriptionService.countSubscriptionsByStatus(SubscriptionStatus.GRACE_PERIOD)));
    }

    private AdminDashboardStatusCountResponse statusCount(String status, Long count) {
        return AdminDashboardStatusCountResponse.builder()
                .status(status)
                .count(safe(count))
                .build();
    }

    private long safe(Long value) {
        return value != null ? value : 0L;
    }
}
```

- [ ] **Step 2: Implement `AdminDashboardController`**

Create `AdminDashboardController.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.usecase.AdminDashboardUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Slf4j
public class AdminDashboardController {
    private final AdminDashboardUseCase adminDashboardUseCase;

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        log.info("GET /api/v1/admin/dashboard - Getting admin dashboard");
        var response = adminDashboardUseCase.getDashboard();
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
```

- [ ] **Step 3: Run the focused backend test**

Run from `account`:

```bash
mvnw.cmd -Dtest=AdminDashboardUseCaseTest test
```

Expected: PASS.

- [ ] **Step 4: Run account compile**

Run from `account`:

```bash
mvnw.cmd clean compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit backend vertical slice**

```bash
git add account/src/main/java/serp/project/account/core/usecase/AdminDashboardUseCase.java account/src/main/java/serp/project/account/ui/controller/AdminDashboardController.java account/src/test/java/serp/project/account/core/usecase/AdminDashboardUseCaseTest.java
git commit -m "feat(account): add admin dashboard endpoint"
```

### Task 4: Frontend Dashboard API Contract

**Files:**
- Create: `serp_web/src/modules/admin/types/dashboard.types.ts`
- Create: `serp_web/src/modules/admin/services/dashboard/dashboardApi.ts`
- Modify: `serp_web/src/lib/store/api/apiSlice.ts`
- Modify: `serp_web/src/modules/admin/types/index.ts`
- Modify: `serp_web/src/modules/admin/services/adminApi.ts`
- Modify: `serp_web/src/modules/admin/index.ts`

- [ ] **Step 1: Add dashboard frontend types**

Create `dashboard.types.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin dashboard types
 */

export interface AdminDashboardMetrics {
  totalOrganizations: number;
  activeOrganizations: number;
  suspendedOrganizations: number;
  expiredOrganizations: number;
  totalUsers: number;
  activeUsers: number;
  suspendedUsers: number;
  totalSubscriptions: number;
  activeSubscriptions: number;
  trialSubscriptions: number;
  pendingSubscriptions: number;
  expiredSubscriptions: number;
}

export interface AdminDashboardActionQueue {
  pendingSubscriptions: number;
  subscriptionsEndingSoon: number;
  trialsEndingSoon: number;
  suspendedOrganizations: number;
  expiredOrganizations: number;
}

export interface AdminDashboardRecentOrganization {
  id: number;
  name: string;
  code: string;
  status: string | null;
  userCount: number;
  subscriptionStatus: string;
  createdAt: number | null;
}

export interface AdminDashboardStatusCount {
  status: string;
  count: number;
}

export interface AdminDashboardConfigurationCoverage {
  totalPlans: number;
  activePlans: number;
  inactivePlans: number;
  totalModules: number;
  availableModules: number;
  unavailableModules: number;
  totalMenuDisplays: number;
  visibleMenuDisplays: number;
  hiddenMenuDisplays: number;
}

export interface AdminDashboard {
  metrics: AdminDashboardMetrics;
  actionQueue: AdminDashboardActionQueue;
  recentOrganizations: AdminDashboardRecentOrganization[];
  organizationStatuses: AdminDashboardStatusCount[];
  subscriptionStatuses: AdminDashboardStatusCount[];
  configurationCoverage: AdminDashboardConfigurationCoverage;
  generatedAt: number;
}
```

- [ ] **Step 2: Add RTK Query dashboard API**

Create `dashboardApi.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin dashboard API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';

import type { AdminDashboard } from '../../types';

export const dashboardApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAdminDashboard: builder.query<AdminDashboard, void>({
      query: () => ({
        url: '/admin/dashboard',
        method: 'GET',
      }),
      transformResponse: createDataTransform<AdminDashboard>(),
      providesTags: [{ type: 'admin/Dashboard', id: 'OVERVIEW' }],
    }),
  }),
  overrideExisting: false,
});

export const { useGetAdminDashboardQuery } = dashboardApi;
```

- [ ] **Step 3: Add `admin/Dashboard` tag**

Modify `serp_web/src/lib/store/api/apiSlice.ts` by adding this string in the admin tag section:

```ts
'admin/Dashboard',
```

- [ ] **Step 4: Export dashboard types and hook**

Modify `serp_web/src/modules/admin/types/index.ts`:

```ts
export type {
  AdminDashboard,
  AdminDashboardActionQueue,
  AdminDashboardConfigurationCoverage,
  AdminDashboardMetrics,
  AdminDashboardRecentOrganization,
  AdminDashboardStatusCount,
} from './dashboard.types';
```

Modify `serp_web/src/modules/admin/services/adminApi.ts`:

```ts
export {
  dashboardApi,
  useGetAdminDashboardQuery,
} from './dashboard/dashboardApi';
```

Modify `serp_web/src/modules/admin/index.ts` by adding API exports:

```ts
dashboardApi,
useGetAdminDashboardQuery,
```

and type exports:

```ts
AdminDashboard,
AdminDashboardActionQueue,
AdminDashboardConfigurationCoverage,
AdminDashboardMetrics,
AdminDashboardRecentOrganization,
AdminDashboardStatusCount,
```

- [ ] **Step 5: Run frontend type check**

Run from `serp_web`:

```bash
npm run type-check
```

Expected: no TypeScript errors for the new API contract.

- [ ] **Step 6: Commit frontend API contract**

```bash
git add serp_web/src/modules/admin/types/dashboard.types.ts serp_web/src/modules/admin/services/dashboard/dashboardApi.ts serp_web/src/lib/store/api/apiSlice.ts serp_web/src/modules/admin/types/index.ts serp_web/src/modules/admin/services/adminApi.ts serp_web/src/modules/admin/index.ts
git commit -m "feat(web): add admin dashboard api contract"
```

### Task 5: Frontend Dashboard Page

**Files:**
- Modify: `serp_web/src/app/admin/page.tsx`

- [ ] **Step 1: Replace hard-coded dashboard with live component**

Replace `serp_web/src/app/admin/page.tsx` with a client page that:

- calls `useGetAdminDashboardQuery()`;
- shows skeletons when loading;
- shows an error card with retry when the query fails;
- shows a compact empty state if `data` is missing after a successful request;
- renders KPI cards, action queue, recent organizations, subscription statuses, and configuration coverage.

Use imports from existing shared UI primitives and lucide icons:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Admin dashboard page
 */

'use client';

import Link from 'next/link';
import React from 'react';
import {
  Activity,
  AlertTriangle,
  Building2,
  CheckCircle2,
  Clock,
  CreditCard,
  Package,
  Puzzle,
  RefreshCw,
  ShieldAlert,
  Users,
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import {
  AdminStatsCard,
  AdminStatusBadge,
  useGetAdminDashboardQuery,
} from '@/modules/admin';
import type { AdminDashboardStatusCount } from '@/modules/admin';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Skeleton } from '@/shared/components/ui/skeleton';

const formatNumber = (value: number | undefined) =>
  new Intl.NumberFormat('en-US').format(value ?? 0);

const formatGeneratedAt = (value: number | undefined) => {
  if (!value) return '';
  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
};

const filterVisibleStatuses = (items: AdminDashboardStatusCount[]) =>
  items.filter((item) => item.count > 0);
```

Then implement the component body using the same file, with stable grid layouts and no hard-coded business values.

- [ ] **Step 2: Add loading state**

Use a small skeleton grid:

```tsx
if (isLoading) {
  return (
    <div className='space-y-6'>
      <div className='space-y-2'>
        <Skeleton className='h-8 w-64' />
        <Skeleton className='h-4 w-96 max-w-full' />
      </div>
      <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} className='h-32 rounded-lg' />
        ))}
      </div>
      <div className='grid gap-4 xl:grid-cols-[1.1fr_0.9fr]'>
        <Skeleton className='h-80 rounded-lg' />
        <Skeleton className='h-80 rounded-lg' />
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Add error and empty states**

Use this shape:

```tsx
if (isError) {
  return (
    <Card className='max-w-xl'>
      <CardHeader>
        <CardTitle className='flex items-center gap-2 text-base'>
          <AlertTriangle className='h-5 w-5 text-destructive' />
          Dashboard unavailable
        </CardTitle>
      </CardHeader>
      <CardContent className='space-y-4'>
        <p className='text-sm text-muted-foreground'>
          The account service did not return dashboard data.
        </p>
        <Button onClick={() => refetch()} className='gap-2'>
          <RefreshCw className='h-4 w-4' />
          Retry
        </Button>
      </CardContent>
    </Card>
  );
}

if (!dashboard) {
  return (
    <Card className='max-w-xl'>
      <CardHeader>
        <CardTitle className='text-base'>No dashboard data</CardTitle>
      </CardHeader>
      <CardContent>
        <p className='text-sm text-muted-foreground'>
          No account-service operations data is available yet.
        </p>
      </CardContent>
    </Card>
  );
}
```

- [ ] **Step 4: Render live dashboard sections**

The final JSX should include:

- KPI grid with `AdminStatsCard`.
- Action queue card with counts and icons.
- Recent organizations card with links to `/admin/organizations`.
- Subscription status chart using filtered status counts.
- Configuration coverage cards.

Keep the page inside `space-y-6`; use `xl:grid-cols-[1.1fr_0.9fr]` for the main body; use `AdminStatusBadge` for status labels.

- [ ] **Step 5: Run frontend checks**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all checks pass. If format fails only because new code needs formatting, run `npm run format` from `serp_web`, then rerun `npm run format:check`.

- [ ] **Step 6: Commit frontend dashboard page**

```bash
git add serp_web/src/app/admin/page.tsx
git commit -m "feat(web): render admin operations dashboard"
```

### Task 6: End-To-End Verification

**Files:**
- No planned source edits unless verification finds defects.

- [ ] **Step 1: Run focused backend test**

Run from `account`:

```bash
mvnw.cmd -Dtest=AdminDashboardUseCaseTest test
```

Expected: BUILD SUCCESS and `AdminDashboardUseCaseTest` passes.

- [ ] **Step 2: Run backend compile**

Run from `account`:

```bash
mvnw.cmd clean compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Run frontend checks**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all commands exit successfully.

- [ ] **Step 4: Optional route build check**

Run from `serp_web` if route or bundling errors are suspected after type check:

```bash
npm run build
```

Expected: Next.js build completes successfully.

- [ ] **Step 5: Final status check**

Run from repo root:

```bash
git status --short
```

Expected: no unstaged implementation files except deliberate uncommitted verification artifacts. Do not revert unrelated user changes.

---

## Self-Review

Spec coverage:

- Backend aggregate endpoint is covered by Tasks 1-3.
- Account-service-only metrics are covered by Tasks 2-3.
- Frontend RTK Query contract is covered by Task 4.
- `/admin` hard-coded page replacement is covered by Task 5.
- Loading, empty, error, retry, and verification states are covered by Task 5.
- Backend and frontend verification commands are covered by Task 6.

Placeholder scan:

- The plan intentionally avoids gateway health, revenue, cross-service metrics, migrations, and frontend test framework setup because they are non-goals in the approved spec.
- No step requires unspecified behavior from a missing external system.

Type consistency:

- Java DTO field names match TypeScript dashboard type field names.
- RTK Query endpoint path `/admin/dashboard` maps to backend `/api/v1/admin/dashboard` through the shared account base query.
- `pendingSubscriptions` is consistently defined as `PENDING + PENDING_UPGRADE` for workload metrics while status breakdown keeps individual statuses.
