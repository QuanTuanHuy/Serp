# CRM Opportunity Display Enrichment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enrich CRM opportunity list, pipeline, and detail views with real relationship names and activity health signals for sales reps and sales managers.

**Architecture:** Add display-only fields to the CRM opportunity response and compute them in `OpportunityUseCase` using batch lookups for accounts, leads, users, and related activities. Then map those fields in `serp_web` and surface them in opportunity cards plus the detail overview without adding persistent schema or new frontend dependencies.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, PostgreSQL/JPA, Next.js, React 19, TypeScript, RTK Query, Tailwind CSS, Shadcn-style UI primitives.

---

## File Structure

- Modify `crm/src/main/java/serp/project/crm/core/domain/dto/response/OpportunityResponse.java`
  - Adds response-only enrichment fields.
- Modify `crm/src/main/java/serp/project/crm/core/port/store/IActivityPort.java`
  - Adds batch activity lookup by opportunity ids.
- Modify `crm/src/main/java/serp/project/crm/infrastructure/store/repository/ActivityRepository.java`
  - Adds Spring Data query method for batch lookup.
- Modify `crm/src/main/java/serp/project/crm/infrastructure/store/adapter/ActivityAdapter.java`
  - Implements the new port method.
- Modify `crm/src/main/java/serp/project/crm/core/usecase/OpportunityUseCase.java`
  - Injects `IActivityPort`, `ILeadPort`, and `IUserProfileClient`.
  - Adds private enrichment helpers used by single, search, and pipeline endpoints.
- Create `crm/src/test/java/serp/project/crm/core/usecase/OpportunityUseCaseTest.java`
  - Covers response enrichment and activity summary semantics.
- Modify `serp_web/src/modules/crm/types/opportunity.ts`
  - Adds optional frontend fields.
- Modify `serp_web/src/modules/crm/api/mappers.ts`
  - Reads backend enrichment fields and converts timestamps to frontend strings.
- Modify `serp_web/src/modules/crm/components/cards/OpportunityCard.tsx`
  - Uses enriched account/owner names and shows activity/close-date badges.
- Modify `serp_web/src/modules/crm/components/opportunities/OpportunityOverviewTab.tsx`
  - Adds account/owner/lead and activity health display.
- Modify `serp_web/src/modules/crm/pages/opportunities/OpportunityDetailPage.tsx`
  - Renders `OpportunityDealMetricsStrip` under the header.

---

### Task 1: Add Backend Contract and Batch Activity Port

**Files:**
- Modify: `crm/src/main/java/serp/project/crm/core/domain/dto/response/OpportunityResponse.java`
- Modify: `crm/src/main/java/serp/project/crm/core/port/store/IActivityPort.java`
- Modify: `crm/src/main/java/serp/project/crm/infrastructure/store/repository/ActivityRepository.java`
- Modify: `crm/src/main/java/serp/project/crm/infrastructure/store/adapter/ActivityAdapter.java`

- [ ] **Step 1: Extend `OpportunityResponse`**

Add these fields after `private Long assignedTo;` and before `private String notes;`:

```java
    private String accountName;
    private String assignedToName;
    private String leadName;
    private Long lastActivityAt;
    private Long nextActivityAt;
    private Integer openActivityCount;
    private Integer overdueActivityCount;
```

- [ ] **Step 2: Add the activity port method**

In `IActivityPort`, add this method after `findByOpportunityId(...)`:

```java
    List<ActivityEntity> findByOpportunityIds(List<Long> opportunityIds, Long tenantId);
```

- [ ] **Step 3: Add the repository method**

In `ActivityRepository`, add this method after `findByTenantIdAndOpportunityId(...)`:

```java
    List<ActivityModel> findByTenantIdAndOpportunityIdIn(Long tenantId, List<Long> opportunityIds);
```

- [ ] **Step 4: Implement the adapter method**

In `ActivityAdapter`, add this method after `findByOpportunityId(...)`:

```java
    @Override
    public List<ActivityEntity> findByOpportunityIds(List<Long> opportunityIds, Long tenantId) {
        if (opportunityIds == null || opportunityIds.isEmpty()) {
            return List.of();
        }
        return activityRepository.findByTenantIdAndOpportunityIdIn(tenantId, opportunityIds)
                .stream()
                .map(activityMapper::toEntity)
                .toList();
    }
```

- [ ] **Step 5: Run the narrow backend compile check**

Run from `crm/`:

```bash
./mvnw -DskipTests clean compile
```

Expected: compile succeeds. If Lombok-generated methods are missing, confirm `OpportunityResponse` still has `@Data` and `@Builder`.

- [ ] **Step 6: Commit backend contract and port changes**

```bash
git add crm/src/main/java/serp/project/crm/core/domain/dto/response/OpportunityResponse.java \
  crm/src/main/java/serp/project/crm/core/port/store/IActivityPort.java \
  crm/src/main/java/serp/project/crm/infrastructure/store/repository/ActivityRepository.java \
  crm/src/main/java/serp/project/crm/infrastructure/store/adapter/ActivityAdapter.java
git commit -m "feat(crm): add opportunity enrichment contract"
```

---

### Task 2: Write Backend Use Case Tests

**Files:**
- Create: `crm/src/test/java/serp/project/crm/core/usecase/OpportunityUseCaseTest.java`

- [ ] **Step 1: Create the test class**

Create `OpportunityUseCaseTest.java` with this content:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.dto.response.OpportunityResponse;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.mapper.OpportunityDtoMapper;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.service.IAccountService;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpportunityUseCaseTest {

    private IOpportunityService opportunityService;
    private IAccountService accountService;
    private ITeamMemberService teamMemberService;
    private IActivityPort activityPort;
    private ILeadPort leadPort;
    private IUserProfileClient userProfileClient;
    private OpportunityUseCase opportunityUseCase;

    @BeforeEach
    void setUp() {
        opportunityService = mock(IOpportunityService.class);
        accountService = mock(IAccountService.class);
        teamMemberService = mock(ITeamMemberService.class);
        activityPort = mock(IActivityPort.class);
        leadPort = mock(ILeadPort.class);
        userProfileClient = mock(IUserProfileClient.class);

        opportunityUseCase = new OpportunityUseCase(
                opportunityService,
                accountService,
                teamMemberService,
                activityPort,
                leadPort,
                userProfileClient,
                new OpportunityDtoMapper(),
                new ResponseUtils());
    }

    @Test
    void getOpportunityByIdEnrichesNamesAndActivitySummary() {
        Long tenantId = 20L;
        Long now = System.currentTimeMillis();
        OpportunityEntity opportunity = OpportunityEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .name("Expansion deal")
                .accountId(10L)
                .leadId(30L)
                .assignedTo(40L)
                .stage(OpportunityStage.PROPOSAL)
                .estimatedValue(BigDecimal.valueOf(120_000_000))
                .probability(50)
                .expectedCloseDate(LocalDate.now().plusDays(5))
                .build();

        ActivityEntity completedPast = ActivityEntity.builder()
                .id(100L)
                .tenantId(tenantId)
                .opportunityId(1L)
                .status(ActivityStatus.COMPLETED)
                .activityDate(now - 86_400_000L)
                .updatedAt(now - 80_000_000L)
                .build();
        ActivityEntity overdueOpen = ActivityEntity.builder()
                .id(101L)
                .tenantId(tenantId)
                .opportunityId(1L)
                .status(ActivityStatus.PLANNED)
                .dueDate(now - 3_600_000L)
                .updatedAt(now - 3_000_000L)
                .build();
        ActivityEntity futureOpen = ActivityEntity.builder()
                .id(102L)
                .tenantId(tenantId)
                .opportunityId(1L)
                .status(ActivityStatus.PLANNED)
                .activityDate(now + 7_200_000L)
                .updatedAt(now)
                .build();
        ActivityEntity cancelledFuture = ActivityEntity.builder()
                .id(103L)
                .tenantId(tenantId)
                .opportunityId(1L)
                .status(ActivityStatus.CANCELLED)
                .activityDate(now + 1_800_000L)
                .updatedAt(now)
                .build();

        when(opportunityService.getOpportunityById(1L, tenantId)).thenReturn(Optional.of(opportunity));
        when(accountService.getAccountsByIds(List.of(10L), tenantId)).thenReturn(List.of(
                AccountEntity.builder().id(10L).name("Acme Manufacturing").build()));
        when(leadPort.findByIds(List.of(30L), tenantId)).thenReturn(List.of(
                LeadEntity.builder().id(30L).name("Jane Lead").build()));
        when(userProfileClient.getUserProfilesByIds(List.of(40L))).thenReturn(List.of(
                serp.project.crm.core.domain.dto.response.user.UserProfileResponse.builder()
                        .id(40L)
                        .firstName("Alex")
                        .lastName("Owner")
                        .build()));
        when(activityPort.findByOpportunityIds(List.of(1L), tenantId)).thenReturn(List.of(
                completedPast, overdueOpen, futureOpen, cancelledFuture));

        GeneralResponse<?> response = opportunityUseCase.getOpportunityById(1L, tenantId);

        OpportunityResponse data = (OpportunityResponse) response.getData();
        assertThat(data.getAccountName()).isEqualTo("Acme Manufacturing");
        assertThat(data.getLeadName()).isEqualTo("Jane Lead");
        assertThat(data.getAssignedToName()).isEqualTo("Alex Owner");
        assertThat(data.getOpenActivityCount()).isEqualTo(2);
        assertThat(data.getOverdueActivityCount()).isEqualTo(1);
        assertThat(data.getNextActivityAt()).isEqualTo(futureOpen.getActivityDate());
        assertThat(data.getLastActivityAt()).isEqualTo(futureOpen.getActivityDate());
    }

    @Test
    void filterOpportunitiesEnrichesEveryReturnedOpportunity() {
        Long tenantId = 20L;
        OpportunityFilterRequest filter = OpportunityFilterRequest.builder().build();
        OpportunityEntity first = OpportunityEntity.builder()
                .id(1L)
                .tenantId(tenantId)
                .name("First")
                .accountId(10L)
                .assignedTo(40L)
                .stage(OpportunityStage.PROSPECTING)
                .estimatedValue(BigDecimal.TEN)
                .build();
        OpportunityEntity second = OpportunityEntity.builder()
                .id(2L)
                .tenantId(tenantId)
                .name("Second")
                .accountId(11L)
                .stage(OpportunityStage.NEGOTIATION)
                .estimatedValue(BigDecimal.ONE)
                .build();

        when(opportunityService.filterOpportunities(eq(filter), eq(tenantId), any()))
                .thenReturn(org.springframework.data.util.Pair.of(List.of(first, second), 2L));
        when(accountService.getAccountsByIds(List.of(10L, 11L), tenantId)).thenReturn(List.of(
                AccountEntity.builder().id(10L).name("Account A").build(),
                AccountEntity.builder().id(11L).name("Account B").build()));
        when(userProfileClient.getUserProfilesByIds(List.of(40L))).thenReturn(List.of(
                serp.project.crm.core.domain.dto.response.user.UserProfileResponse.builder()
                        .id(40L)
                        .firstName("Owner")
                        .lastName("A")
                        .build()));
        when(activityPort.findByOpportunityIds(List.of(1L, 2L), tenantId)).thenReturn(List.of());

        GeneralResponse<?> response = opportunityUseCase.filterOpportunities(filter, tenantId);

        serp.project.crm.core.domain.dto.PageResponse<?> page =
                (serp.project.crm.core.domain.dto.PageResponse<?>) response.getData();
        List<?> items = page.getItems();
        OpportunityResponse firstResponse = (OpportunityResponse) items.get(0);
        OpportunityResponse secondResponse = (OpportunityResponse) items.get(1);
        assertThat(firstResponse.getAccountName()).isEqualTo("Account A");
        assertThat(firstResponse.getAssignedToName()).isEqualTo("Owner A");
        assertThat(firstResponse.getOpenActivityCount()).isZero();
        assertThat(secondResponse.getAccountName()).isEqualTo("Account B");
        assertThat(secondResponse.getAssignedToName()).isNull();
        assertThat(secondResponse.getOpenActivityCount()).isZero();
    }
}
```

- [ ] **Step 2: Run the new test and verify it fails**

Run from `crm/`:

```bash
./mvnw -Dtest=OpportunityUseCaseTest test
```

Expected: compilation fails because `OpportunityUseCase` does not yet have the constructor dependencies and enrichment fields/helpers used by the tests.

- [ ] **Step 3: Commit the failing tests if your workflow allows red commits**

If the branch policy allows TDD red commits:

```bash
git add crm/src/test/java/serp/project/crm/core/usecase/OpportunityUseCaseTest.java
git commit -m "test(crm): cover opportunity response enrichment"
```

If the branch policy requires every commit to pass, skip this commit and commit Task 2 together with Task 3.

---

### Task 3: Implement Backend Opportunity Enrichment

**Files:**
- Modify: `crm/src/main/java/serp/project/crm/core/usecase/OpportunityUseCase.java`
- Modify: `crm/src/test/java/serp/project/crm/core/usecase/OpportunityUseCaseTest.java` if imports need local adjustment

- [ ] **Step 1: Add imports to `OpportunityUseCase`**

Add these imports:

```java
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.port.store.ILeadPort;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
```

- [ ] **Step 2: Inject the new collaborators**

In `OpportunityUseCase`, add these final fields after `teamMemberService`:

```java
    private final IActivityPort activityPort;
    private final ILeadPort leadPort;
    private final IUserProfileClient userProfileClient;
```

- [ ] **Step 3: Enrich single opportunity responses**

Replace this code in `getOpportunityById`:

```java
            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);
            return responseUtils.success(response);
```

with:

```java
            OpportunityResponse response = opportunityDtoMapper.toResponse(opportunity);
            enrichOpportunityResponses(List.of(response), tenantId);
            return responseUtils.success(response);
```

- [ ] **Step 4: Enrich search/list responses**

In `filterOpportunities`, replace:

```java
            List<OpportunityResponse> opportunityResponses = result.getFirst().stream()
                    .map(opportunityDtoMapper::toResponse)
                    .toList();
```

with:

```java
            List<OpportunityResponse> opportunityResponses = result.getFirst().stream()
                    .map(opportunityDtoMapper::toResponse)
                    .toList();
            enrichOpportunityResponses(opportunityResponses, tenantId);
```

Also apply the same replacement in `getAllOpportunities` if that method still returns opportunity lists.

- [ ] **Step 5: Enrich pipeline stage opportunities**

In `getPipeline`, before the `for (OpportunityStage stage : OpportunityStage.values())` loop, create response lists once:

```java
        Map<Long, OpportunityResponse> enrichedById = opportunities.stream()
                .map(opportunityDtoMapper::toResponse)
                .collect(Collectors.toMap(OpportunityResponse::getId, Function.identity()));
        enrichOpportunityResponses(new ArrayList<>(enrichedById.values()), tenantId);
```

Then replace the pipeline stage response mapping:

```java
                    .opportunities(stageOpportunities.stream().map(opportunityDtoMapper::toResponse).toList())
```

with:

```java
                    .opportunities(stageOpportunities.stream()
                            .map(opportunity -> enrichedById.get(opportunity.getId()))
                            .filter(Objects::nonNull)
                            .toList())
```

- [ ] **Step 6: Add private enrichment helpers**

Add these private methods before `estimatedValueOrZero(...)`:

```java
    private void enrichOpportunityResponses(List<OpportunityResponse> responses, Long tenantId) {
        if (responses == null || responses.isEmpty() || tenantId == null) {
            return;
        }

        List<Long> accountIds = responses.stream()
                .map(OpportunityResponse::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> accountNames = accountIds.isEmpty()
                ? Collections.emptyMap()
                : accountService.getAccountsByIds(accountIds, tenantId).stream()
                        .collect(Collectors.toMap(
                                AccountEntity::getId,
                                AccountEntity::getName,
                                (first, second) -> first));

        List<Long> leadIds = responses.stream()
                .map(OpportunityResponse::getLeadId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> leadNames = leadIds.isEmpty()
                ? Collections.emptyMap()
                : leadPort.findByIds(leadIds, tenantId).stream()
                        .collect(Collectors.toMap(
                                LeadEntity::getId,
                                LeadEntity::getName,
                                (first, second) -> first));

        List<Long> userIds = responses.stream()
                .map(OpportunityResponse::getAssignedTo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> userNames = userIds.isEmpty()
                ? Collections.emptyMap()
                : userProfileClient.getUserProfilesByIds(userIds).stream()
                        .collect(Collectors.toMap(
                                profile -> profile.getId(),
                                profile -> profile.getFullName(),
                                (first, second) -> first));

        List<Long> opportunityIds = responses.stream()
                .map(OpportunityResponse::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, List<ActivityEntity>> activitiesByOpportunityId = opportunityIds.isEmpty()
                ? Collections.emptyMap()
                : activityPort.findByOpportunityIds(opportunityIds, tenantId).stream()
                        .filter(activity -> activity.getOpportunityId() != null)
                        .collect(Collectors.groupingBy(ActivityEntity::getOpportunityId));

        for (OpportunityResponse response : responses) {
            response.setAccountName(accountNames.get(response.getAccountId()));
            response.setLeadName(leadNames.get(response.getLeadId()));
            response.setAssignedToName(userNames.get(response.getAssignedTo()));
            applyActivitySummary(response, activitiesByOpportunityId.getOrDefault(response.getId(), List.of()));
        }
    }

    private void applyActivitySummary(OpportunityResponse response, List<ActivityEntity> activities) {
        long now = System.currentTimeMillis();
        int openCount = 0;
        int overdueCount = 0;
        Long lastActivityAt = null;
        Long nextActivityAt = null;

        for (ActivityEntity activity : activities) {
            Long activityTimestamp = preferredActivityTimestamp(activity);
            if (activityTimestamp != null) {
                lastActivityAt = maxTimestamp(lastActivityAt, activityTimestamp);
            }

            boolean open = !ActivityStatus.COMPLETED.equals(activity.getStatus())
                    && !ActivityStatus.CANCELLED.equals(activity.getStatus());
            if (!open) {
                continue;
            }

            openCount++;
            Long dueOrActivityAt = preferredDueTimestamp(activity);
            if (dueOrActivityAt != null && dueOrActivityAt < now) {
                overdueCount++;
            }
            if (dueOrActivityAt != null && dueOrActivityAt >= now) {
                nextActivityAt = minTimestamp(nextActivityAt, dueOrActivityAt);
            }
        }

        response.setLastActivityAt(lastActivityAt);
        response.setNextActivityAt(nextActivityAt);
        response.setOpenActivityCount(openCount);
        response.setOverdueActivityCount(overdueCount);
    }

    private static Long preferredActivityTimestamp(ActivityEntity activity) {
        if (activity.getActivityDate() != null) {
            return activity.getActivityDate();
        }
        if (activity.getDueDate() != null) {
            return activity.getDueDate();
        }
        return activity.getUpdatedAt();
    }

    private static Long preferredDueTimestamp(ActivityEntity activity) {
        if (activity.getActivityDate() != null) {
            return activity.getActivityDate();
        }
        return activity.getDueDate();
    }

    private static Long maxTimestamp(Long current, Long candidate) {
        return current == null || candidate > current ? candidate : current;
    }

    private static Long minTimestamp(Long current, Long candidate) {
        return current == null || candidate < current ? candidate : current;
    }
```

- [ ] **Step 7: Run backend tests**

Run from `crm/`:

```bash
./mvnw -Dtest=OpportunityUseCaseTest test
```

Expected: tests pass.

- [ ] **Step 8: Run broader CRM backend verification**

Run from `crm/`:

```bash
./mvnw test
```

Expected: all CRM tests pass.

- [ ] **Step 9: Commit backend enrichment implementation**

```bash
git add crm/src/main/java/serp/project/crm/core/usecase/OpportunityUseCase.java \
  crm/src/test/java/serp/project/crm/core/usecase/OpportunityUseCaseTest.java
git commit -m "feat(crm): enrich opportunity responses"
```

---

### Task 4: Map Enriched Opportunity Fields in Frontend

**Files:**
- Modify: `serp_web/src/modules/crm/types/opportunity.ts`
- Modify: `serp_web/src/modules/crm/api/mappers.ts`

- [ ] **Step 1: Extend the frontend `Opportunity` type**

In `Opportunity`, add these fields after `assignedToName?: string;`:

```ts
  accountName?: string;
  leadName?: string;
  lastActivityAt?: string;
  nextActivityAt?: string;
  openActivityCount?: number;
  overdueActivityCount?: number;
```

- [ ] **Step 2: Extend the `BackendOpportunity` type**

In `BackendOpportunity`, add these fields after `assignedTo?: number | string | null;`:

```ts
  accountName?: string | null;
  assignedToName?: string | null;
  leadName?: string | null;
  lastActivityAt?: number | string | null;
  nextActivityAt?: number | string | null;
  openActivityCount?: number | null;
  overdueActivityCount?: number | null;
```

- [ ] **Step 3: Update `mapBackendOpportunityAssignedToName`**

Replace the function with:

```ts
const mapBackendOpportunityAssignedToName = (
  assignedTo?: number | string | null,
  assignedToName?: string | null
) => {
  const trimmedName = assignedToName?.trim();
  if (trimmedName) {
    return trimmedName;
  }

  if (assignedTo === null || assignedTo === undefined || assignedTo === '') {
    return 'Unassigned';
  }

  return `User #${assignedTo}`;
};
```

- [ ] **Step 4: Map enriched fields**

In `mapBackendOpportunityToOpportunity`, update account and assignee fields:

```ts
  const accountName = opportunity.accountName?.trim() || undefined;
```

Then use these returned properties:

```ts
    customerName: accountName || (accountId ? `Account #${accountId}` : undefined),
    accountName,
    leadName: opportunity.leadName?.trim() || undefined,
    assignedToName: mapBackendOpportunityAssignedToName(
      opportunity.assignedTo,
      opportunity.assignedToName
    ),
    lastActivityAt:
      opportunity.lastActivityAt === null ||
      opportunity.lastActivityAt === undefined
        ? undefined
        : toIsoString(opportunity.lastActivityAt),
    nextActivityAt:
      opportunity.nextActivityAt === null ||
      opportunity.nextActivityAt === undefined
        ? undefined
        : toIsoString(opportunity.nextActivityAt),
    openActivityCount: opportunity.openActivityCount ?? 0,
    overdueActivityCount: opportunity.overdueActivityCount ?? 0,
```

Keep existing `customerId`, `estimatedValue`, `value`, `actualValue`, and `customFields` behavior.

- [ ] **Step 5: Run frontend type-check**

Run from `serp_web/`:

```bash
npm run type-check
```

Expected: TypeScript passes. If the mapper object has duplicate properties, keep the enriched version and remove the older duplicate assignment.

- [ ] **Step 6: Commit frontend mapper changes**

```bash
git add serp_web/src/modules/crm/types/opportunity.ts \
  serp_web/src/modules/crm/api/mappers.ts
git commit -m "feat(crm-web): map opportunity enrichment fields"
```

---

### Task 5: Add Opportunity Card Action Signals

**Files:**
- Modify: `serp_web/src/modules/crm/components/cards/OpportunityCard.tsx`

- [ ] **Step 1: Import `AlertCircle` and `Clock3` icons**

Extend the existing lucide import:

```ts
  AlertCircle,
  Clock3,
```

- [ ] **Step 2: Add date and badge helpers**

Add these helpers after `getDaysUntil(...)`:

```ts
const isClosedOpportunity = (opportunity: Opportunity): boolean =>
  opportunity.stage === 'CLOSED_WON' || opportunity.stage === 'CLOSED_LOST';

const hasNoNextAction = (opportunity: Opportunity): boolean =>
  !isClosedOpportunity(opportunity) && !opportunity.nextActivityAt;

const isClosingSoon = (opportunity: Opportunity): boolean => {
  if (isClosedOpportunity(opportunity)) return false;
  const { days, isOverdue } = getDaysUntil(opportunity.expectedCloseDate);
  return !isOverdue && days <= 7;
};

const formatShortDateTime = (dateString?: string): string => {
  if (!dateString) return 'Not scheduled';
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
  });
};
```

- [ ] **Step 3: Prefer enriched account names**

Replace `getDisplayAccountName` with:

```ts
const getDisplayAccountName = (opportunity: Opportunity): string => {
  return (
    opportunity.accountName?.trim() ||
    opportunity.customerName?.trim() ||
    (opportunity.accountId
      ? `Account #${opportunity.accountId}`
      : 'Unlinked Account')
  );
};
```

- [ ] **Step 4: Add pipeline card action line**

In the `variant === 'pipeline'` branch, after the probability bar and before the footer, insert:

```tsx
        <div className='mt-3 flex flex-wrap gap-1'>
          {(opportunity.overdueActivityCount ?? 0) > 0 && (
            <Badge variant='destructive' className='px-1.5 py-0 text-[10px]'>
              {opportunity.overdueActivityCount} overdue
            </Badge>
          )}
          {hasNoNextAction(opportunity) && (
            <Badge variant='secondary' className='px-1.5 py-0 text-[10px]'>
              No next action
            </Badge>
          )}
          {!hasNoNextAction(opportunity) && opportunity.nextActivityAt && (
            <Badge variant='outline' className='px-1.5 py-0 text-[10px]'>
              Next {formatShortDateTime(opportunity.nextActivityAt)}
            </Badge>
          )}
        </div>
```

- [ ] **Step 5: Add compact card activity chips**

In the `variant === 'compact'` branch, after the value block and before `</Card>`, insert:

```tsx
        <div className='hidden min-w-[120px] text-right text-xs text-muted-foreground sm:block'>
          {(opportunity.overdueActivityCount ?? 0) > 0 ? (
            <span className='font-medium text-destructive'>
              {opportunity.overdueActivityCount} overdue
            </span>
          ) : opportunity.nextActivityAt ? (
            <span>Next {formatShortDateTime(opportunity.nextActivityAt)}</span>
          ) : hasNoNextAction(opportunity) ? (
            <span className='font-medium text-amber-600'>No next action</span>
          ) : (
            <span>{opportunity.openActivityCount ?? 0} open</span>
          )}
        </div>
```

- [ ] **Step 6: Add full card badge row**

In the default full card branch, after the Stage & Type Badges block, insert:

```tsx
        <div className='mb-4 flex flex-wrap gap-2'>
          {(opportunity.overdueActivityCount ?? 0) > 0 && (
            <Badge variant='destructive' className='gap-1'>
              <AlertCircle className='h-3 w-3' />
              {opportunity.overdueActivityCount} overdue activities
            </Badge>
          )}
          {hasNoNextAction(opportunity) && (
            <Badge variant='secondary' className='gap-1 text-amber-700'>
              <Clock3 className='h-3 w-3' />
              No next action
            </Badge>
          )}
          {!hasNoNextAction(opportunity) && opportunity.nextActivityAt && (
            <Badge variant='outline' className='gap-1'>
              <Clock3 className='h-3 w-3' />
              Next {formatShortDateTime(opportunity.nextActivityAt)}
            </Badge>
          )}
          {closeDateInfo.isOverdue && !isClosedOpportunity(opportunity) && (
            <Badge variant='destructive'>Close overdue</Badge>
          )}
          {isClosingSoon(opportunity) && (
            <Badge variant='outline'>Closing soon</Badge>
          )}
        </div>
```

- [ ] **Step 7: Run focused frontend checks**

Run from `serp_web/`:

```bash
npx eslint src/modules/crm/components/cards/OpportunityCard.tsx
npm run type-check
```

Expected: ESLint and TypeScript pass.

- [ ] **Step 8: Commit card UI changes**

```bash
git add serp_web/src/modules/crm/components/cards/OpportunityCard.tsx
git commit -m "feat(crm-web): show opportunity action signals"
```

---

### Task 6: Enrich Opportunity Detail Overview

**Files:**
- Modify: `serp_web/src/modules/crm/components/opportunities/OpportunityOverviewTab.tsx`
- Modify: `serp_web/src/modules/crm/pages/opportunities/OpportunityDetailPage.tsx`

- [ ] **Step 1: Add metrics strip render in detail page**

In `OpportunityDetailPage.tsx`, after `RequestMeetingDialog` and before the pipeline progress block, insert:

```tsx
      <div className='rounded-lg border bg-card p-4 md:p-5'>
        <OpportunityDealMetricsStrip
          estimatedValue={estimatedValue}
          weightedValue={weightedValue}
          probability={probability}
          daysUntilClose={daysUntilClose}
          formatCurrency={formatCurrency}
        />
      </div>
```

- [ ] **Step 2: Add activity health display to overview**

In `OpportunityOverviewTab.tsx`, inside the right column before `System Metadata`, insert this card:

```tsx
        <Card>
          <CardHeader>
            <h3 className='font-semibold'>Activity Health</h3>
          </CardHeader>
          <CardContent className='space-y-3'>
            <div>
              <span className='text-sm text-muted-foreground'>
                Last Activity
              </span>
              <p className='text-sm font-medium'>
                {formatDateTime(opportunity.lastActivityAt)}
              </p>
            </div>
            <div>
              <span className='text-sm text-muted-foreground'>
                Next Activity
              </span>
              <p className='text-sm font-medium'>
                {formatDateTime(opportunity.nextActivityAt)}
              </p>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-sm text-muted-foreground'>
                Open Activities
              </span>
              <span className='font-medium'>
                {opportunity.openActivityCount ?? 0}
              </span>
            </div>
            <div className='flex items-center justify-between'>
              <span className='text-sm text-muted-foreground'>
                Overdue Activities
              </span>
              <span
                className={cn(
                  'font-medium',
                  (opportunity.overdueActivityCount ?? 0) > 0 &&
                    'text-destructive'
                )}
              >
                {opportunity.overdueActivityCount ?? 0}
              </span>
            </div>
          </CardContent>
        </Card>
```

- [ ] **Step 3: Prefer enriched names in overview**

In the Account display, replace:

```tsx
                  {opportunity.customerName ||
                    `Account #${opportunity.accountId || ''}`}
```

with:

```tsx
                  {opportunity.accountName ||
                    opportunity.customerName ||
                    `Account #${opportunity.accountId || ''}`}
```

After the existing `Lead ID` block, add:

```tsx
              <div>
                <label className='text-sm font-medium text-muted-foreground'>
                  Linked Lead
                </label>
                <p className='font-medium'>
                  {opportunity.leadName ||
                    (opportunity.leadId
                      ? `Lead #${opportunity.leadId}`
                      : 'No linked lead')}
                </p>
              </div>
```

- [ ] **Step 4: Run focused frontend checks**

Run from `serp_web/`:

```bash
npx eslint src/modules/crm/components/opportunities/OpportunityOverviewTab.tsx src/modules/crm/pages/opportunities/OpportunityDetailPage.tsx
npm run type-check
```

Expected: ESLint and TypeScript pass.

- [ ] **Step 5: Commit detail UI changes**

```bash
git add serp_web/src/modules/crm/components/opportunities/OpportunityOverviewTab.tsx \
  serp_web/src/modules/crm/pages/opportunities/OpportunityDetailPage.tsx
git commit -m "feat(crm-web): enrich opportunity detail overview"
```

---

### Task 7: Final Verification

**Files:**
- Verify all files touched in Tasks 1-6.

- [ ] **Step 1: Run backend verification**

Run from `crm/`:

```bash
./mvnw test
```

Expected: all backend tests pass.

- [ ] **Step 2: Run frontend verification**

Run from `serp_web/`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: lint, TypeScript, and Prettier checks pass.

- [ ] **Step 3: Inspect final diff**

Run from repo root:

```bash
git status --short
git diff --stat HEAD
```

Expected: either clean working tree after commits, or only intended files from the implementation if commits were deferred.

- [ ] **Step 4: Manual data-contract smoke check**

With CRM backend and frontend running, inspect one opportunity in list, pipeline, and detail. Confirm these visible states:

- Real account name appears when backend returns `accountName`.
- Real owner name appears when backend returns `assignedToName`.
- Active opportunity with no `nextActivityAt` shows `No next action`.
- Opportunity with overdue open activities shows an overdue activity badge.
- Opportunity detail shows the metrics strip and Activity Health card.

---

## Self-Review Notes

- Spec coverage: backend contract, single/search/pipeline enrichment, frontend mapper, list/pipeline card display, detail metrics strip, activity health, fallbacks, and verification are all covered.
- Scope check: the plan does not add database migrations, products, competitors, tags, forecast categories, or a timeline/history API.
- Type consistency: backend uses `Long` timestamps and frontend maps them to optional ISO strings. Field names match the approved spec: `accountName`, `assignedToName`, `leadName`, `lastActivityAt`, `nextActivityAt`, `openActivityCount`, `overdueActivityCount`.
