# PM Resource Calendar Settings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build workspace-level PM resource calendar settings with reusable profiles, user assignments, exceptions, and materialized capacity slots for optimization.

**Architecture:** Add a new `domain.resourcecalendar` capability in `pm_core` and keep `resource_calendar_slots` as the optimization read model. Backend command handlers own transactions and trigger materialization; infrastructure store adapters persist settings and hard-replace generated slots. Frontend adds a PM Settings section backed by a dedicated RTK Query API file.

**Tech Stack:** Java 21, Spring Boot 3.5, JPA, Flyway, JUnit 5, Mockito, Next.js 15, React 19, TypeScript, RTK Query, Tailwind/Shadcn UI.

---

## File Structure

Backend files to create:

- `pm_core/src/main/resources/db/migration/V30__create_resource_calendar_settings.sql`
  - Tables and indexes for profiles, blocks, assignments, and exceptions.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/enums/ResourceCalendarExceptionType.java`
  - `UNAVAILABLE`, `CAPACITY_OVERRIDE`.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/enums/ResourceCalendarSlotSource.java`
  - `PROFILE`, `EXCEPTION`, `EXTERNAL`.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/entity/*.java`
  - Domain entities for profile, block, assignment, exception.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/model/GeneratedResourceCalendarSlot.java`
  - Domain output model for materialized slots.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/IResourceCalendarMaterializationService.java`
  - Interface used by application commands.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/impl/ResourceCalendarMaterializationService.java`
  - Slot generation and exception merge rules.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/IResourceCalendarSettingsService.java`
  - Interface for validation and CRUD-oriented domain operations.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/impl/ResourceCalendarSettingsService.java`
  - Domain validation for profiles, blocks, assignments, exceptions.
- `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/port/*.java`
  - Ports for profile, block, assignment, exception, materialized slot writes.
- `pm_core/src/main/java/serp/project/pmcore/application/resourcecalendar/**`
  - Commands, query handlers, views.
- `pm_core/src/main/java/serp/project/pmcore/ui/rest/resourcecalendar/**`
  - Controllers and request DTOs.
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarProfileModel.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarProfileBlockModel.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarAssignmentModel.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarExceptionModel.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarProfileRepository.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarProfileBlockRepository.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarAssignmentRepository.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarExceptionRepository.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/ResourceCalendarSettingsAdapter.java`
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/ResourceCalendarSlotWriteAdapter.java`
- `pm_core/src/main/java/serp/project/pmcore/kernel/scheduler/ResourceCalendarMaterializationScheduler.java`

Backend files to modify:

- `pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java`
  - Add resource calendar paths.
- `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarSlotRepository.java`
  - Add hard-delete query for generated slots.

Frontend files to create:

- `serp_web/src/modules/pm/types/resource-calendar-api.types.ts`
- `serp_web/src/modules/pm/api/resourceCalendarApi.ts`
- `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarSettingsSection.tsx`
- `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarProfileTable.tsx`
- `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarProfileDialog.tsx`
- `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarAssignmentPanel.tsx`
- `serp_web/src/modules/pm/components/settings/resource-calendar/PMResourceCalendarExceptionPanel.tsx`
- `serp_web/src/modules/pm/components/settings/resource-calendar/index.ts`

Frontend files to modify:

- `serp_web/src/modules/pm/api/index.ts`
  - Export resource calendar API hooks and types.
- `serp_web/src/modules/pm/types/api.ts`
  - Export resource calendar types.
- `serp_web/src/modules/pm/components/settings/settings-page.types.ts`
  - Add `resource-calendars` settings item.
- `serp_web/src/modules/pm/pages/PMSettingsPage.tsx`
  - Render the new section and use the new overview query.

---

### Task 1: Schema And Persistence Models

**Files:**
- Create: `pm_core/src/main/resources/db/migration/V30__create_resource_calendar_settings.sql`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarProfileModel.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarProfileBlockModel.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarAssignmentModel.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarExceptionModel.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarSettingsModelMappingTest.java`

- [ ] **Step 1: Write the model mapping test**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceCalendarSettingsModelMappingTest {

    @Test
    void modelsShouldKeepCalendarSettingsFields() {
        ResourceCalendarProfileModel profile = ResourceCalendarProfileModel.builder()
                .tenantId(10L)
                .name("VN Full-time")
                .description("Default Vietnam office calendar")
                .timezone("Asia/Ho_Chi_Minh")
                .isDefault(true)
                .build();

        ResourceCalendarProfileBlockModel block = ResourceCalendarProfileBlockModel.builder()
                .profileId(1L)
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .capacityFactor(new BigDecimal("0.75"))
                .build();

        ResourceCalendarAssignmentModel assignment = ResourceCalendarAssignmentModel.builder()
                .tenantId(10L)
                .userId(20L)
                .profileId(1L)
                .effectiveFrom(LocalDate.of(2026, 6, 7))
                .effectiveTo(null)
                .build();

        ResourceCalendarExceptionModel exception = ResourceCalendarExceptionModel.builder()
                .tenantId(10L)
                .userId(20L)
                .exceptionType("CAPACITY_OVERRIDE")
                .startAt(LocalDateTime.of(2026, 6, 8, 9, 0))
                .endAt(LocalDateTime.of(2026, 6, 8, 12, 0))
                .capacityFactor(new BigDecimal("0.50"))
                .reason("Training")
                .build();

        assertThat(profile.getTimezone()).isEqualTo("Asia/Ho_Chi_Minh");
        assertThat(block.getCapacityFactor()).isEqualByComparingTo("0.75");
        assertThat(assignment.getEffectiveTo()).isNull();
        assertThat(exception.getExceptionType()).isEqualTo("CAPACITY_OVERRIDE");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run from `pm_core`:

```bash
mvn "-Dtest=ResourceCalendarSettingsModelMappingTest" test
```

Expected: FAIL because the new model classes do not exist.

- [ ] **Step 3: Add Flyway migration**

Create `V30__create_resource_calendar_settings.sql`:

```sql
CREATE TABLE resource_calendar_profiles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    timezone VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_resource_calendar_profiles_tenant
    ON resource_calendar_profiles (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE TABLE resource_calendar_profile_blocks (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    capacity_factor NUMERIC(5, 2) NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_resource_calendar_profile_blocks_profile
        FOREIGN KEY (profile_id) REFERENCES resource_calendar_profiles (id),
    CONSTRAINT chk_resource_calendar_profile_blocks_day
        CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_resource_calendar_profile_blocks_range
        CHECK (start_time < end_time),
    CONSTRAINT chk_resource_calendar_profile_blocks_capacity
        CHECK (capacity_factor > 0 AND capacity_factor <= 1)
);

CREATE INDEX idx_resource_calendar_profile_blocks_profile
    ON resource_calendar_profile_blocks (profile_id, day_of_week, start_time)
    WHERE deleted_at IS NULL;

CREATE TABLE resource_calendar_assignments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_resource_calendar_assignments_profile
        FOREIGN KEY (profile_id) REFERENCES resource_calendar_profiles (id),
    CONSTRAINT chk_resource_calendar_assignments_range
        CHECK (effective_to IS NULL OR effective_from <= effective_to)
);

CREATE INDEX idx_resource_calendar_assignments_user
    ON resource_calendar_assignments (tenant_id, user_id, effective_from, effective_to)
    WHERE deleted_at IS NULL;

CREATE TABLE resource_calendar_exceptions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    exception_type VARCHAR(50) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    capacity_factor NUMERIC(5, 2),
    reason VARCHAR(500),
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_resource_calendar_exceptions_range
        CHECK (start_at < end_at),
    CONSTRAINT chk_resource_calendar_exceptions_type
        CHECK (exception_type IN ('UNAVAILABLE', 'CAPACITY_OVERRIDE')),
    CONSTRAINT chk_resource_calendar_exceptions_capacity
        CHECK (capacity_factor IS NULL OR (capacity_factor >= 0 AND capacity_factor <= 2))
);

CREATE INDEX idx_resource_calendar_exceptions_user_range
    ON resource_calendar_exceptions (tenant_id, user_id, start_at, end_at)
    WHERE deleted_at IS NULL;
```

- [ ] **Step 4: Add model classes**

Each model extends `BaseModel`, uses `@Entity`, `@Table`, `@SQLRestriction("deleted_at IS NULL")`, Lombok `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@SuperBuilder`.

Example for `ResourceCalendarProfileBlockModel.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "resource_calendar_profile_blocks")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ResourceCalendarProfileBlockModel extends BaseModel {
    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "capacity_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal capacityFactor;
}
```

Create the remaining models with these exact fields:

- `ResourceCalendarProfileModel`: `Long tenantId`, `String name`, `String description`, `String timezone`, `Boolean isDefault`.
- `ResourceCalendarAssignmentModel`: `Long tenantId`, `Long userId`, `Long profileId`, `LocalDate effectiveFrom`, `LocalDate effectiveTo`.
- `ResourceCalendarExceptionModel`: `Long tenantId`, `Long userId`, `String exceptionType`, `LocalDateTime startAt`, `LocalDateTime endAt`, `BigDecimal capacityFactor`, `String reason`.

- [ ] **Step 5: Run test to verify it passes**

Run from `pm_core`:

```bash
mvn "-Dtest=ResourceCalendarSettingsModelMappingTest" test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add pm_core/src/main/resources/db/migration/V30__create_resource_calendar_settings.sql pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarProfileModel.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarProfileBlockModel.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarAssignmentModel.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarExceptionModel.java pm_core/src/test/java/serp/project/pmcore/infrastructure/store/model/ResourceCalendarSettingsModelMappingTest.java
git commit -m "feat: add resource calendar settings schema"
```

### Task 2: Domain Entities, Enums, Ports, And Validation Service

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/enums/ResourceCalendarExceptionType.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/enums/ResourceCalendarSlotSource.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/entity/ResourceCalendarProfileEntity.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/entity/ResourceCalendarProfileBlockEntity.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/entity/ResourceCalendarAssignmentEntity.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/entity/ResourceCalendarExceptionEntity.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/IResourceCalendarSettingsService.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/impl/ResourceCalendarSettingsService.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/resourcecalendar/service/ResourceCalendarSettingsServiceTest.java`

- [ ] **Step 1: Write validation tests**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.resourcecalendar.service.impl.ResourceCalendarSettingsService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceCalendarSettingsServiceTest {

    private final ResourceCalendarSettingsService service = new ResourceCalendarSettingsService();

    @Test
    void validateBlocksShouldRejectInvalidRangeAndCapacity() {
        ResourceCalendarProfileBlockEntity block = ResourceCalendarProfileBlockEntity.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(9, 0))
                .capacityFactor(BigDecimal.ONE)
                .build();

        assertThatThrownBy(() -> service.validateBlocks(List.of(block)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void validateExceptionShouldAllowUnavailableWithoutCapacityFactor() {
        ResourceCalendarExceptionEntity exception = ResourceCalendarExceptionEntity.builder()
                .exceptionType(ResourceCalendarExceptionType.UNAVAILABLE)
                .startAt(LocalDateTime.of(2026, 6, 8, 9, 0))
                .endAt(LocalDateTime.of(2026, 6, 8, 17, 0))
                .capacityFactor(null)
                .build();

        assertThatCode(() -> service.validateException(exception)).doesNotThrowAnyException();
    }

    @Test
    void validateAssignmentsShouldRejectOverlapForSameUser() {
        List<ResourceCalendarAssignmentEntity> assignments = List.of(
                ResourceCalendarAssignmentEntity.builder()
                        .userId(20L)
                        .profileId(1L)
                        .effectiveFrom(LocalDate.of(2026, 6, 1))
                        .effectiveTo(LocalDate.of(2026, 6, 30))
                        .build(),
                ResourceCalendarAssignmentEntity.builder()
                        .userId(20L)
                        .profileId(2L)
                        .effectiveFrom(LocalDate.of(2026, 6, 15))
                        .effectiveTo(null)
                        .build()
        );

        assertThatThrownBy(() -> service.validateAssignments(assignments))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn "-Dtest=ResourceCalendarSettingsServiceTest" test
```

Expected: FAIL because domain classes do not exist.

- [ ] **Step 3: Add enums**

```java
package serp.project.pmcore.domain.resourcecalendar.enums;

public enum ResourceCalendarExceptionType {
    UNAVAILABLE,
    CAPACITY_OVERRIDE
}
```

```java
package serp.project.pmcore.domain.resourcecalendar.enums;

public enum ResourceCalendarSlotSource {
    PROFILE,
    EXCEPTION,
    EXTERNAL
}
```

- [ ] **Step 4: Add entity classes**

Use Lombok `@Builder`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`. Entity fields mirror model fields but use enum types for exception type.

Example `ResourceCalendarExceptionEntity.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCalendarExceptionEntity {
    private Long id;
    private Long tenantId;
    private Long userId;
    private ResourceCalendarExceptionType exceptionType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal capacityFactor;
    private String reason;
}
```

- [ ] **Step 5: Add validation service**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.resourcecalendar.service.IResourceCalendarSettingsService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ResourceCalendarSettingsService implements IResourceCalendarSettingsService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TWO = new BigDecimal("2.00");

    @Override
    public void validateBlocks(List<ResourceCalendarProfileBlockEntity> blocks) {
        for (ResourceCalendarProfileBlockEntity block : blocks == null ? List.<ResourceCalendarProfileBlockEntity>of() : blocks) {
            if (block.getDayOfWeek() == null || block.getDayOfWeek() < 1 || block.getDayOfWeek() > 7) {
                throw violation("Calendar block day must be between 1 and 7");
            }
            if (block.getStartTime() == null || block.getEndTime() == null || !block.getStartTime().isBefore(block.getEndTime())) {
                throw violation("Calendar block start time must be before end time");
            }
            BigDecimal factor = block.getCapacityFactor();
            if (factor == null || factor.compareTo(ZERO) <= 0 || factor.compareTo(ONE) > 0) {
                throw violation("Calendar block capacity factor must be greater than 0 and no more than 1");
            }
        }
    }

    @Override
    public void validateException(ResourceCalendarExceptionEntity exception) {
        if (exception.getStartAt() == null || exception.getEndAt() == null || !exception.getStartAt().isBefore(exception.getEndAt())) {
            throw violation("Calendar exception start must be before end");
        }
        if (exception.getExceptionType() == ResourceCalendarExceptionType.CAPACITY_OVERRIDE) {
            BigDecimal factor = exception.getCapacityFactor();
            if (factor == null || factor.compareTo(ZERO) < 0 || factor.compareTo(TWO) > 0) {
                throw violation("Capacity override factor must be from 0 through 2");
            }
        }
    }

    @Override
    public void validateAssignments(List<ResourceCalendarAssignmentEntity> assignments) {
        var byUserId = (assignments == null ? List.<ResourceCalendarAssignmentEntity>of() : assignments)
                .stream()
                .filter(assignment -> assignment.getUserId() != null)
                .collect(Collectors.groupingBy(ResourceCalendarAssignmentEntity::getUserId));
        for (List<ResourceCalendarAssignmentEntity> userAssignments : byUserId.values()) {
            List<ResourceCalendarAssignmentEntity> ordered = userAssignments.stream()
                    .sorted(Comparator.comparing(ResourceCalendarAssignmentEntity::getEffectiveFrom))
                    .toList();
            for (int index = 1; index < ordered.size(); index++) {
                ResourceCalendarAssignmentEntity previous = ordered.get(index - 1);
                ResourceCalendarAssignmentEntity current = ordered.get(index);
                if (previous.getEffectiveTo() == null || !previous.getEffectiveTo().isBefore(current.getEffectiveFrom())) {
                    throw violation("A user cannot have overlapping active calendar assignments");
                }
            }
        }
    }

    private BusinessRuleViolationException violation(String message) {
        return new BusinessRuleViolationException(DomainErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
```

- [ ] **Step 6: Run test to verify it passes**

```bash
mvn "-Dtest=ResourceCalendarSettingsServiceTest" test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar pm_core/src/test/java/serp/project/pmcore/domain/resourcecalendar/service/ResourceCalendarSettingsServiceTest.java
git commit -m "feat: add resource calendar domain validation"
```

### Task 3: Materialization Domain Service

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/model/GeneratedResourceCalendarSlot.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/model/ResourceCalendarMaterializationInput.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/IResourceCalendarMaterializationService.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/impl/ResourceCalendarMaterializationService.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/domain/resourcecalendar/service/ResourceCalendarMaterializationServiceTest.java`

- [ ] **Step 1: Write materialization tests**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.resourcecalendar.model.ResourceCalendarMaterializationInput;
import serp.project.pmcore.domain.resourcecalendar.service.impl.ResourceCalendarMaterializationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceCalendarMaterializationServiceTest {

    private final ResourceCalendarMaterializationService service = new ResourceCalendarMaterializationService();

    @Test
    void materializeShouldGenerateWeeklyBlocksWithCapacityFactor() {
        ResourceCalendarMaterializationInput input = new ResourceCalendarMaterializationInput(
                10L,
                List.of(20L),
                "Asia/Ho_Chi_Minh",
                LocalDate.of(2026, 6, 8),
                LocalDate.of(2026, 6, 8),
                List.of(block(1, 9, 17, "0.50")),
                List.of()
        );

        var slots = service.materialize(input);

        assertThat(slots).hasSize(1);
        assertThat(slots.getFirst().capacityMillis()).isEqualTo(4L * 60 * 60 * 1000);
    }

    @Test
    void materializeShouldRemoveUnavailableOverlap() {
        ResourceCalendarMaterializationInput input = new ResourceCalendarMaterializationInput(
                10L,
                List.of(20L),
                "Asia/Ho_Chi_Minh",
                LocalDate.of(2026, 6, 8),
                LocalDate.of(2026, 6, 8),
                List.of(block(1, 9, 17, "1.00")),
                List.of(ResourceCalendarExceptionEntity.builder()
                        .tenantId(10L)
                        .userId(20L)
                        .exceptionType(ResourceCalendarExceptionType.UNAVAILABLE)
                        .startAt(LocalDateTime.of(2026, 6, 8, 12, 0))
                        .endAt(LocalDateTime.of(2026, 6, 8, 13, 0))
                        .build())
        );

        var slots = service.materialize(input);

        assertThat(slots).hasSize(2);
        assertThat(slots).extracting("capacityMillis")
                .containsExactly(3L * 60 * 60 * 1000, 4L * 60 * 60 * 1000);
    }

    private ResourceCalendarProfileBlockEntity block(int day, int startHour, int endHour, String factor) {
        return ResourceCalendarProfileBlockEntity.builder()
                .dayOfWeek(day)
                .startTime(LocalTime.of(startHour, 0))
                .endTime(LocalTime.of(endHour, 0))
                .capacityFactor(new BigDecimal(factor))
                .build();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn "-Dtest=ResourceCalendarMaterializationServiceTest" test
```

Expected: FAIL because the materialization service does not exist.

- [ ] **Step 3: Add materialization records and interface**

```java
package serp.project.pmcore.domain.resourcecalendar.model;

import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarSlotSource;

public record GeneratedResourceCalendarSlot(
        Long tenantId,
        Long userId,
        Long slotStart,
        Long slotEnd,
        Long capacityMillis,
        ResourceCalendarSlotSource source,
        String externalRef
) {
}
```

```java
package serp.project.pmcore.domain.resourcecalendar.model;

import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;

import java.time.LocalDate;
import java.util.List;

public record ResourceCalendarMaterializationInput(
        Long tenantId,
        List<Long> userIds,
        String timezone,
        LocalDate windowStart,
        LocalDate windowEnd,
        List<ResourceCalendarProfileBlockEntity> blocks,
        List<ResourceCalendarExceptionEntity> exceptions
) {
}
```

```java
package serp.project.pmcore.domain.resourcecalendar.service;

import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;
import serp.project.pmcore.domain.resourcecalendar.model.ResourceCalendarMaterializationInput;

import java.util.List;

public interface IResourceCalendarMaterializationService {
    List<GeneratedResourceCalendarSlot> materialize(ResourceCalendarMaterializationInput input);
}
```

- [ ] **Step 4: Write service with base generation and unavailable split**

Implement base weekly slots and split by unavailable exceptions. Use `ZoneId.of(input.timezone())`, convert slot start/end to epoch millis, and capacity as `durationMillis * factor`.

The service must ignore blocks for days outside the window and must sort by `userId`, then `slotStart`.

- [ ] **Step 5: Run test to verify it passes**

```bash
mvn "-Dtest=ResourceCalendarMaterializationServiceTest" test
```

Expected: PASS.

- [ ] **Step 6: Add override capacity test and implementation**

Add test:

```java
@Test
void materializeShouldApplyCapacityOverrideOverlap() {
    ResourceCalendarMaterializationInput input = new ResourceCalendarMaterializationInput(
            10L,
            List.of(20L),
            "Asia/Ho_Chi_Minh",
            LocalDate.of(2026, 6, 8),
            LocalDate.of(2026, 6, 8),
            List.of(block(1, 9, 17, "1.00")),
            List.of(ResourceCalendarExceptionEntity.builder()
                    .tenantId(10L)
                    .userId(20L)
                    .exceptionType(ResourceCalendarExceptionType.CAPACITY_OVERRIDE)
                    .startAt(LocalDateTime.of(2026, 6, 8, 13, 0))
                    .endAt(LocalDateTime.of(2026, 6, 8, 17, 0))
                    .capacityFactor(new BigDecimal("0.50"))
                    .build())
    );

    var slots = service.materialize(input);

    assertThat(slots).hasSize(2);
    assertThat(slots).extracting("capacityMillis")
            .containsExactly(4L * 60 * 60 * 1000, 2L * 60 * 60 * 1000);
}
```

Run the focused test, implement override split, then rerun.

- [ ] **Step 7: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/model pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/IResourceCalendarMaterializationService.java pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/service/impl/ResourceCalendarMaterializationService.java pm_core/src/test/java/serp/project/pmcore/domain/resourcecalendar/service/ResourceCalendarMaterializationServiceTest.java
git commit -m "feat: add resource calendar materialization"
```

### Task 4: Repositories, Mappers, And Store Adapters

**Files:**
- Create repository and adapter files listed in File Structure.
- Modify: `pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarSlotRepository.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/infrastructure/store/adapter/ResourceCalendarSlotWriteAdapterTest.java`

- [ ] **Step 1: Write hard delete adapter test**

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarSlotSource;
import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarSlotRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResourceCalendarSlotWriteAdapterTest {
    @Mock
    private IResourceCalendarSlotRepository repository;

    @InjectMocks
    private ResourceCalendarSlotWriteAdapter adapter;

    @Test
    void replaceGeneratedSlotsShouldHardDeleteProfileAndExceptionSourcesBeforeInsert() {
        List<GeneratedResourceCalendarSlot> slots = List.of(new GeneratedResourceCalendarSlot(
                10L, 20L, 1791411600000L, 1791440400000L, 28800000L,
                ResourceCalendarSlotSource.PROFILE, null
        ));

        adapter.replaceGeneratedSlots(10L, List.of(20L), 1791411600000L, 1791498000000L, slots);

        verify(repository).hardDeleteGeneratedSlots(eq(10L), eq(List.of(20L)), any(), any(), eq(List.of("PROFILE", "EXCEPTION")));
        verify(repository).saveAll(any());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn "-Dtest=ResourceCalendarSlotWriteAdapterTest" test
```

Expected: FAIL because write adapter and repository method do not exist.

- [ ] **Step 3: Add slot write port and adapter**

Create `IResourceCalendarSlotWritePort`:

```java
package serp.project.pmcore.domain.resourcecalendar.port;

import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;

import java.util.List;

public interface IResourceCalendarSlotWritePort {
    void replaceGeneratedSlots(Long tenantId, List<Long> userIds, Long windowStart, Long windowEnd, List<GeneratedResourceCalendarSlot> slots);
}
```

Create `ResourceCalendarSlotWriteAdapter` that converts epoch millis to UTC `LocalDateTime`, calls hard delete, and inserts `ResourceCalendarSlotModel`.

- [ ] **Step 4: Add repository hard delete**

Add to `IResourceCalendarSlotRepository`:

```java
@Modifying
@Query("""
        DELETE FROM ResourceCalendarSlotModel s
        WHERE s.tenantId = :tenantId
          AND s.userId IN :userIds
          AND s.slotStart < :windowEnd
          AND s.slotEnd > :windowStart
          AND s.source IN :sources
        """)
void hardDeleteGeneratedSlots(@Param("tenantId") Long tenantId,
                              @Param("userIds") List<Long> userIds,
                              @Param("windowStart") LocalDateTime windowStart,
                              @Param("windowEnd") LocalDateTime windowEnd,
                              @Param("sources") List<String> sources);
```

Add imports for `@Modifying`.

- [ ] **Step 5: Add settings repositories and adapter**

Define repository interfaces extending `JpaRepository` with these methods:

```java
List<ResourceCalendarProfileModel> findByTenantIdOrderByNameAsc(Long tenantId);
List<ResourceCalendarProfileBlockModel> findByProfileIdOrderByDayOfWeekAscStartTimeAsc(Long profileId);
List<ResourceCalendarAssignmentModel> findByTenantIdAndProfileIdOrderByUserIdAsc(Long tenantId, Long profileId);
List<ResourceCalendarAssignmentModel> findByTenantIdOrderByUserIdAscEffectiveFromAsc(Long tenantId);
List<ResourceCalendarExceptionModel> findByTenantIdAndUserIdInAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
        Long tenantId,
        List<Long> userIds,
        LocalDateTime windowEnd,
        LocalDateTime windowStart
);
```

Create `ResourceCalendarSettingsAdapter` implementing ports:

- `IResourceCalendarProfilePort`
- `IResourceCalendarProfileBlockPort`
- `IResourceCalendarAssignmentPort`
- `IResourceCalendarExceptionPort`

The settings adapter must expose these port methods:

```java
List<ResourceCalendarProfileEntity> listProfiles(Long tenantId);
ResourceCalendarProfileEntity saveProfile(ResourceCalendarProfileEntity profile);
void deleteProfile(Long tenantId, Long profileId);
List<ResourceCalendarProfileBlockEntity> replaceBlocks(Long profileId, List<ResourceCalendarProfileBlockEntity> blocks);
List<ResourceCalendarAssignmentEntity> replaceProfileAssignments(Long tenantId, Long profileId, List<ResourceCalendarAssignmentEntity> assignments);
List<ResourceCalendarAssignmentEntity> listActiveAssignments(Long tenantId);
List<ResourceCalendarExceptionEntity> listExceptions(Long tenantId, List<Long> userIds, Long windowStart, Long windowEnd);
ResourceCalendarExceptionEntity saveException(ResourceCalendarExceptionEntity exception);
void deleteException(Long tenantId, Long exceptionId);
```

- [ ] **Step 6: Run adapter tests**

```bash
mvn "-Dtest=ResourceCalendarSlotWriteAdapterTest" test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/domain/resourcecalendar/port pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarSlotRepository.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarProfileRepository.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarProfileBlockRepository.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarAssignmentRepository.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/repository/IResourceCalendarExceptionRepository.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/ResourceCalendarSettingsAdapter.java pm_core/src/main/java/serp/project/pmcore/infrastructure/store/adapter/ResourceCalendarSlotWriteAdapter.java pm_core/src/test/java/serp/project/pmcore/infrastructure/store/adapter/ResourceCalendarSlotWriteAdapterTest.java
git commit -m "feat: add resource calendar store adapters"
```

### Task 5: Application Commands, Overview Query, And Materialization Trigger

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/application/resourcecalendar/settings/*.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/resourcecalendar/command/profile/*.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/resourcecalendar/command/block/*.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/resourcecalendar/command/assignment/*.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/resourcecalendar/command/exception/*.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/resourcecalendar/command/assignment/ReplaceResourceCalendarAssignmentsCommandHandlerTest.java`

- [ ] **Step 1: Write materialization trigger test**

```java
@ExtendWith(MockitoExtension.class)
class ReplaceResourceCalendarAssignmentsCommandHandlerTest {
    @Mock private IResourceCalendarAssignmentPort assignmentPort;
    @Mock private IResourceCalendarProfileBlockPort blockPort;
    @Mock private IResourceCalendarExceptionPort exceptionPort;
    @Mock private IResourceCalendarMaterializationService materializationService;
    @Mock private IResourceCalendarSlotWritePort slotWritePort;
    @InjectMocks private ReplaceResourceCalendarAssignmentsCommandHandler handler;

    @Test
    void handleShouldReplaceAssignmentsAndMaterializeAffectedUsers() {
        ReplaceResourceCalendarAssignmentsCommand command = new ReplaceResourceCalendarAssignmentsCommand(
                10L,
                1L,
                List.of(new ReplaceResourceCalendarAssignmentsCommand.Assignment(20L, LocalDate.of(2026, 6, 7), null))
        );
        when(assignmentPort.replaceProfileAssignments(eq(10L), eq(1L), any()))
                .thenReturn(List.of(ResourceCalendarAssignmentEntity.builder()
                        .tenantId(10L)
                        .userId(20L)
                        .profileId(1L)
                        .effectiveFrom(LocalDate.of(2026, 6, 7))
                        .build()));
        when(blockPort.listByProfileId(1L)).thenReturn(List.of());
        when(exceptionPort.listExceptions(eq(10L), eq(List.of(20L)), any(), any())).thenReturn(List.of());
        when(materializationService.materialize(any())).thenReturn(List.of());

        handler.handle(command);

        verify(slotWritePort).replaceGeneratedSlots(eq(10L), eq(List.of(20L)), any(), any(), eq(List.of()));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn "-Dtest=ReplaceResourceCalendarAssignmentsCommandHandlerTest" test
```

Expected: FAIL because command handler does not exist.

- [ ] **Step 3: Add command records and handlers**

Create these command records and handlers:

- `CreateResourceCalendarProfileCommand` and `CreateResourceCalendarProfileCommandHandler`
- `UpdateResourceCalendarProfileCommand` and `UpdateResourceCalendarProfileCommandHandler`
- `DeleteResourceCalendarProfileCommand` and `DeleteResourceCalendarProfileCommandHandler`
- `ReplaceResourceCalendarBlocksCommand` and `ReplaceResourceCalendarBlocksCommandHandler`
- `ReplaceResourceCalendarAssignmentsCommand` and `ReplaceResourceCalendarAssignmentsCommandHandler`
- `CreateResourceCalendarExceptionCommand` and `CreateResourceCalendarExceptionCommandHandler`
- `UpdateResourceCalendarExceptionCommand` and `UpdateResourceCalendarExceptionCommandHandler`
- `DeleteResourceCalendarExceptionCommand` and `DeleteResourceCalendarExceptionCommandHandler`

Each mutating handler must be `@Service`, `@RequiredArgsConstructor`, and `@Transactional(rollbackFor = Exception.class)`.

For handlers that affect users, call a shared private method or helper application service:

```java
private void rematerialize(Long tenantId, Long profileId, List<Long> userIds) {
    LocalDate windowStart = LocalDate.now(ZoneOffset.UTC);
    LocalDate windowEnd = windowStart.plusDays(90);
    Long windowStartMillis = windowStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    Long windowEndMillis = windowEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    List<ResourceCalendarProfileBlockEntity> blocks = blockPort.listByProfileId(profileId);
    List<ResourceCalendarExceptionEntity> exceptions = exceptionPort.listExceptions(tenantId, userIds, windowStartMillis, windowEndMillis);
    List<GeneratedResourceCalendarSlot> slots = materializationService.materialize(new ResourceCalendarMaterializationInput(
            tenantId,
            userIds,
            "Asia/Ho_Chi_Minh",
            windowStart,
            windowEnd,
            blocks,
            exceptions
    ));
    slotWritePort.replaceGeneratedSlots(tenantId, userIds, windowStartMillis, windowEndMillis, slots);
}
```

Use profile timezone instead of hard-coded `"Asia/Ho_Chi_Minh"` once profile port exposes `getById`.

- [ ] **Step 4: Add overview query and views**

Create `GetResourceCalendarSettingsOverviewQuery`, `GetResourceCalendarSettingsOverviewQueryHandler`, `ResourceCalendarSettingsOverviewView`.

The view must include:

```java
public record ResourceCalendarSettingsOverviewView(
        List<ProfileView> profiles,
        List<AssignmentView> assignments,
        List<ExceptionView> upcomingExceptions,
        List<Long> unassignedUserIds,
        Long materializedWindowStart,
        Long materializedWindowEnd,
        Long fetchedAt
) {
}
```

For MVP, set `unassignedUserIds` to `List.of()` because `pm_core` does not currently expose a tenant-wide user listing port. The UI still renders the field and can show assigned/unassigned coverage once a tenant user source is added.

- [ ] **Step 5: Run application tests**

```bash
mvn "-Dtest=ReplaceResourceCalendarAssignmentsCommandHandlerTest" test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/resourcecalendar pm_core/src/test/java/serp/project/pmcore/application/resourcecalendar/command/assignment/ReplaceResourceCalendarAssignmentsCommandHandlerTest.java
git commit -m "feat: add resource calendar application handlers"
```

### Task 6: REST API And Path Constants

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/resourcecalendar/ResourceCalendarSettingsController.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/resourcecalendar/ResourceCalendarProfileController.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/ui/rest/resourcecalendar/ResourceCalendarExceptionController.java`
- Create: request DTOs under `pm_core/src/main/java/serp/project/pmcore/ui/rest/resourcecalendar/dto/request/`
- Test: `pm_core/src/test/java/serp/project/pmcore/ui/rest/resourcecalendar/ResourceCalendarSettingsControllerTest.java`

- [ ] **Step 1: Write controller unit test**

```java
@ExtendWith(MockitoExtension.class)
class ResourceCalendarSettingsControllerTest {
    @Mock private AuthUtils authUtils;
    @Mock private ResponseUtils responseUtils;
    @Mock private GetResourceCalendarSettingsOverviewQueryHandler handler;
    @InjectMocks private ResourceCalendarSettingsController controller;

    @Test
    void getOverviewShouldResolveTenantAndReturnResponse() {
        ResourceCalendarSettingsOverviewView view = new ResourceCalendarSettingsOverviewView(
                List.of(), List.of(), List.of(), List.of(), 1L, 2L, 3L
        );
        GeneralResponse<ResourceCalendarSettingsOverviewView> envelope = new GeneralResponse<>();
        when(authUtils.getCurrentTenantId()).thenReturn(Optional.of(10L));
        when(handler.handle(new GetResourceCalendarSettingsOverviewQuery(10L))).thenReturn(view);
        when(responseUtils.success(view)).thenReturn(envelope);

        var response = controller.getOverview();

        assertThat(response.getBody()).isSameAs(envelope);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn "-Dtest=ResourceCalendarSettingsControllerTest" test
```

Expected: FAIL because controller does not exist.

- [ ] **Step 3: Add path constants**

Add to `PathConstants.java`:

```java
public static final String RESOURCE_CALENDAR_SETTINGS = API_BASE_PATH + "/resource-calendar-settings";
public static final String RESOURCE_CALENDAR_PROFILES = API_BASE_PATH + "/resource-calendar-profiles";
public static final String RESOURCE_CALENDAR_EXCEPTIONS = API_BASE_PATH + "/resource-calendar-exceptions";
```

- [ ] **Step 4: Implement controllers**

Controllers must:

- use `@RestController`
- use `@RequestMapping(PathConstants.X)`
- resolve `tenantId` through `AuthUtils`
- return `ResponseUtils.success(...)`
- delegate to one handler per action

Request DTOs:

- `CreateResourceCalendarProfileRequest`
- `UpdateResourceCalendarProfileRequest`
- `ReplaceResourceCalendarBlocksRequest`
- `ReplaceResourceCalendarAssignmentsRequest`
- `CreateResourceCalendarExceptionRequest`
- `UpdateResourceCalendarExceptionRequest`

Use `@NotBlank`, `@NotNull`, `@Valid`, `@DecimalMin`, `@DecimalMax` where relevant.

Controller route mapping:

- `ResourceCalendarSettingsController`: `GET /api/v1/resource-calendar-settings/overview`
- `ResourceCalendarProfileController`: `POST /api/v1/resource-calendar-profiles`, `PUT /api/v1/resource-calendar-profiles/{id}`, `DELETE /api/v1/resource-calendar-profiles/{id}`, `PUT /api/v1/resource-calendar-profiles/{id}/blocks`, `PUT /api/v1/resource-calendar-profiles/{id}/assignments`
- `ResourceCalendarExceptionController`: `POST /api/v1/resource-calendar-exceptions`, `PUT /api/v1/resource-calendar-exceptions/{id}`, `DELETE /api/v1/resource-calendar-exceptions/{id}`

- [ ] **Step 5: Run controller test**

```bash
mvn "-Dtest=ResourceCalendarSettingsControllerTest" test
```

Expected: PASS.

- [ ] **Step 6: Run backend compile**

```bash
mvn clean compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/ui/rest/shared/constant/PathConstants.java pm_core/src/main/java/serp/project/pmcore/ui/rest/resourcecalendar pm_core/src/test/java/serp/project/pmcore/ui/rest/resourcecalendar/ResourceCalendarSettingsControllerTest.java
git commit -m "feat: expose resource calendar settings api"
```

### Task 7: Scheduled Refresh

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/kernel/scheduler/ResourceCalendarMaterializationScheduler.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/kernel/scheduler/ResourceCalendarMaterializationSchedulerTest.java`

- [ ] **Step 1: Write scheduler test**

```java
@ExtendWith(MockitoExtension.class)
class ResourceCalendarMaterializationSchedulerTest {
    @Mock private IResourceCalendarAssignmentPort assignmentPort;
    @Mock private ReplaceResourceCalendarAssignmentsCommandHandler assignmentHandler;
    @InjectMocks private ResourceCalendarMaterializationScheduler scheduler;

    @Test
    void refreshShouldNoopWhenNoActiveAssignments() {
        when(assignmentPort.listActiveAssignments(any())).thenReturn(List.of());

        scheduler.refreshMaterializedSlots();

        verifyNoInteractions(assignmentHandler);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn "-Dtest=ResourceCalendarMaterializationSchedulerTest" test
```

Expected: FAIL because scheduler does not exist.

- [ ] **Step 3: Add scheduler**

Use `@Component` and `@Scheduled(cron = "0 15 2 * * *")`.

Because `pm_core` does not expose a tenant enumeration port, implement scheduler as a thin component with a public method and this code comment:

```java
// Tenant enumeration is not available in pm_core yet; command-triggered materialization
// is the active MVP path, and this component is ready for wiring when tenant listing exists.
```

Do not invent a cross-service tenant lookup.

- [ ] **Step 4: Run scheduler test**

```bash
mvn "-Dtest=ResourceCalendarMaterializationSchedulerTest" test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/kernel/scheduler/ResourceCalendarMaterializationScheduler.java pm_core/src/test/java/serp/project/pmcore/kernel/scheduler/ResourceCalendarMaterializationSchedulerTest.java
git commit -m "feat: add resource calendar refresh scheduler"
```

### Task 8: Frontend Types And RTK Query API

**Files:**
- Create: `serp_web/src/modules/pm/types/resource-calendar-api.types.ts`
- Create: `serp_web/src/modules/pm/api/resourceCalendarApi.ts`
- Modify: `serp_web/src/modules/pm/api/index.ts`
- Modify: `serp_web/src/modules/pm/types/api.ts`

- [ ] **Step 1: Create TypeScript type definitions**

Create:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM resource calendar API types
 */

export type PMResourceCalendarExceptionType =
  | 'UNAVAILABLE'
  | 'CAPACITY_OVERRIDE';

export interface PMResourceCalendarBlockApi {
  id: number;
  profileId: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  capacityFactor: number;
}

export interface PMResourceCalendarProfileApi {
  id: number;
  tenantId: number;
  name: string;
  description: string | null;
  timezone: string;
  isDefault: boolean;
  assignmentCount: number;
  blocks: PMResourceCalendarBlockApi[];
}

export interface PMResourceCalendarAssignmentApi {
  id: number;
  userId: number;
  profileId: number;
  effectiveFrom: string;
  effectiveTo: string | null;
}

export interface PMResourceCalendarExceptionApi {
  id: number;
  userId: number;
  exceptionType: PMResourceCalendarExceptionType;
  startAt: number;
  endAt: number;
  capacityFactor: number | null;
  reason: string | null;
}

export interface PMResourceCalendarSettingsOverviewApi {
  profiles: PMResourceCalendarProfileApi[];
  assignments: PMResourceCalendarAssignmentApi[];
  upcomingExceptions: PMResourceCalendarExceptionApi[];
  unassignedUserIds: number[];
  materializedWindowStart: number | null;
  materializedWindowEnd: number | null;
  fetchedAt: number;
}

export interface PMCreateResourceCalendarProfileRequest {
  name: string;
  description: string | null;
  timezone: string;
  isDefault: boolean;
}

export type PMUpdateResourceCalendarProfileRequest =
  Partial<PMCreateResourceCalendarProfileRequest>;

export interface PMReplaceResourceCalendarBlocksRequest {
  blocks: Array<{
    dayOfWeek: number;
    startTime: string;
    endTime: string;
    capacityFactor: number;
  }>;
}

export interface PMReplaceResourceCalendarAssignmentsRequest {
  assignments: Array<{
    userId: number;
    effectiveFrom: string;
    effectiveTo: string | null;
  }>;
}

export interface PMCreateResourceCalendarExceptionRequest {
  userId: number;
  exceptionType: PMResourceCalendarExceptionType;
  startAt: number;
  endAt: number;
  capacityFactor: number | null;
  reason: string | null;
}

export type PMUpdateResourceCalendarExceptionRequest =
  Partial<PMCreateResourceCalendarExceptionRequest>;
```

- [ ] **Step 2: Create RTK Query API**

Create `resourceCalendarApi.ts` with this endpoint map and `extraOptions: { service: 'pm' }` on every endpoint:

```ts
getPmResourceCalendarSettingsOverview: GET /resource-calendar-settings/overview
createPmResourceCalendarProfile: POST /resource-calendar-profiles
updatePmResourceCalendarProfile: PUT /resource-calendar-profiles/:id
deletePmResourceCalendarProfile: DELETE /resource-calendar-profiles/:id
replacePmResourceCalendarBlocks: PUT /resource-calendar-profiles/:id/blocks
replacePmResourceCalendarAssignments: PUT /resource-calendar-profiles/:id/assignments
createPmResourceCalendarException: POST /resource-calendar-exceptions
updatePmResourceCalendarException: PUT /resource-calendar-exceptions/:id
deletePmResourceCalendarException: DELETE /resource-calendar-exceptions/:id
```

Export hooks:

```ts
useGetPmResourceCalendarSettingsOverviewQuery
useCreatePmResourceCalendarProfileMutation
useUpdatePmResourceCalendarProfileMutation
useDeletePmResourceCalendarProfileMutation
useReplacePmResourceCalendarBlocksMutation
useReplacePmResourceCalendarAssignmentsMutation
useCreatePmResourceCalendarExceptionMutation
useUpdatePmResourceCalendarExceptionMutation
useDeletePmResourceCalendarExceptionMutation
```

Use tag type strings:

```ts
{ type: 'pm/ResourceCalendarSettings' as const, id: 'OVERVIEW' }
{ type: 'pm/ResourceCalendarProfile' as const, id }
{ type: 'pm/ResourceCalendarException' as const, id }
```

- [ ] **Step 3: Export API and types**

Update `api/index.ts` and `types/api.ts` to re-export the new API hooks and types.

- [ ] **Step 4: Run frontend type check**

Run from `serp_web`:

```bash
npm run type-check
```

Expected: no TypeScript errors from the new files.

- [ ] **Step 5: Commit**

```bash
git add serp_web/src/modules/pm/types/resource-calendar-api.types.ts serp_web/src/modules/pm/api/resourceCalendarApi.ts serp_web/src/modules/pm/api/index.ts serp_web/src/modules/pm/types/api.ts
git commit -m "feat: add pm resource calendar api client"
```

### Task 9: Frontend Settings Section

**Files:**
- Modify: `serp_web/src/modules/pm/components/settings/settings-page.types.ts`
- Modify: `serp_web/src/modules/pm/pages/PMSettingsPage.tsx`
- Create component files under `serp_web/src/modules/pm/components/settings/resource-calendar/`

- [ ] **Step 1: Add settings section key**

Update `PMSettingsSection` union:

```ts
| 'resource-calendars'
```

Add item:

```ts
{
  key: 'resource-calendars',
  title: 'Resource calendars',
  description: 'Configure working calendars and capacity for optimization.',
  group: 'Optimization',
}
```

- [ ] **Step 2: Create section shell component**

Create `PMResourceCalendarSettingsSection.tsx`.

Required props:

```ts
interface PMResourceCalendarSettingsSectionProps {
  overview: PMResourceCalendarSettingsOverviewApi | undefined;
  isLoading: boolean;
  errorMessage?: string;
}
```

Render:

- loading skeleton rows
- error text
- mini stats for profiles, assignments, exceptions, unassigned users
- profile table
- assignment panel
- exception panel

- [ ] **Step 3: Add profile table and dialog**

`PMResourceCalendarProfileTable` renders columns:

- name
- timezone
- assignment count
- block count
- default badge
- edit/delete actions

`PMResourceCalendarProfileDialog` handles create/edit profile and weekly blocks. Use controlled local state with inputs for:

- name
- description
- timezone
- isDefault
- blocks array with day of week, start time, end time, capacity factor

- [ ] **Step 4: Add assignment and exception panels**

Assignment panel:

- select profile
- list assigned users by user ID
- add/remove rows for user ID and effective date

Exception panel:

- list upcoming exceptions
- create/edit dialog fields: user ID, exception type, start/end datetime, factor, reason

Keep MVP user display as user ID unless an existing PM user option API is available.

- [ ] **Step 5: Wire page**

In `PMSettingsPage.tsx`:

- import resource calendar query and mutations
- add section descriptions/search placeholders/add labels for `resource-calendars`
- render `<PMResourceCalendarSettingsSection />` when active section is `resource-calendars`
- avoid including resource calendars in the existing generic Work Types/Priority/Workflow table branches

- [ ] **Step 6: Run frontend checks**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all pass. If `format:check` fails only for created files, run:

```bash
npx prettier --write src/modules/pm/api/resourceCalendarApi.ts src/modules/pm/types/resource-calendar-api.types.ts src/modules/pm/components/settings/resource-calendar/*.tsx src/modules/pm/components/settings/resource-calendar/index.ts src/modules/pm/pages/PMSettingsPage.tsx src/modules/pm/components/settings/settings-page.types.ts src/modules/pm/api/index.ts src/modules/pm/types/api.ts
```

Then rerun the three checks.

- [ ] **Step 7: Commit**

```bash
git add serp_web/src/modules/pm/components/settings/settings-page.types.ts serp_web/src/modules/pm/pages/PMSettingsPage.tsx serp_web/src/modules/pm/components/settings/resource-calendar
git commit -m "feat: add pm resource calendar settings ui"
```

### Task 10: End-To-End Verification

**Files:**
- Modify only if verification exposes defects.

- [ ] **Step 1: Run focused backend tests**

Run from `pm_core`:

```bash
mvn "-Dtest=ResourceCalendarSettingsModelMappingTest,ResourceCalendarSettingsServiceTest,ResourceCalendarMaterializationServiceTest,ResourceCalendarSlotWriteAdapterTest,ReplaceResourceCalendarAssignmentsCommandHandlerTest,ResourceCalendarSettingsControllerTest,ResourceCalendarMaterializationSchedulerTest,ResourceCalendarServiceTest,ResourceCapacityServiceTest,OptimizationProjectModelBuilderTest" test
```

Expected: PASS.

- [ ] **Step 2: Run backend compile**

Run from `pm_core`:

```bash
mvn clean compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Run frontend checks**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all pass.

- [ ] **Step 4: Run full backend tests if time allows**

Run from `pm_core`:

```bash
mvn test
```

Expected: PASS, except note any pre-existing unrelated failures with exact class and assertion.

- [ ] **Step 5: Final status**

Run from repo root:

```bash
git status --short
git log --oneline -5
```

Expected: only intentional changes remain, and recent commits match the task commits above.
