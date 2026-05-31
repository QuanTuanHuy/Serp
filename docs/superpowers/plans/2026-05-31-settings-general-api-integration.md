# Settings General API Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build data-backed Settings General page with one backend GET endpoint and one backend PUT endpoint for full organization settings.

**Architecture:** Store organization settings as first-class nullable columns on `account.organizations`, then expose them through the existing organization controller/usecase/service layering. Frontend keeps `src/app/settings/page.tsx` thin, owns settings UI/API/types inside `src/modules/settings`, and saves all editable settings through one RTK Query mutation.

**Tech Stack:** Spring Boot 3.5.5, Java 21, Flyway, JPA, JUnit 5, Next.js 15 App Router, React 19, TypeScript, RTK Query, Tailwind/Shadcn UI.

---

## File Structure

Backend files:

- Create `account/src/main/resources/db/migration/V9__add_organization_settings_fields.sql`: adds new organization settings columns.
- Modify `account/src/main/java/serp/project/account/infrastructure/store/model/OrganizationModel.java`: adds JPA fields.
- Modify `account/src/main/java/serp/project/account/core/domain/entity/OrganizationEntity.java`: adds domain fields.
- Modify `account/src/main/java/serp/project/account/infrastructure/store/mapper/OrganizationMapper.java`: maps new fields and supports partial update.
- Create `account/src/main/java/serp/project/account/core/domain/dto/request/UpdateOrganizationSettingsRequest.java`: request DTO for one PUT endpoint.
- Create `account/src/main/java/serp/project/account/core/domain/dto/response/OrganizationSettingsSummaryResponse.java`: nullable stats card data.
- Create `account/src/main/java/serp/project/account/core/domain/dto/response/OrganizationSettingsResponse.java`: GET/PUT response DTO.
- Modify `account/src/main/java/serp/project/account/core/service/IOrganizationService.java`: adds update contract.
- Modify `account/src/main/java/serp/project/account/core/service/impl/OrganizationService.java`: applies settings update in transaction.
- Modify `account/src/main/java/serp/project/account/core/usecase/OrganizationUseCase.java`: adds settings read/update orchestration.
- Modify `account/src/main/java/serp/project/account/ui/controller/OrganizationController.java`: adds GET/PUT endpoints.
- Create `account/src/test/java/serp/project/account/core/service/impl/OrganizationServiceTest.java`: focused service regression tests.

Frontend files:

- Modify `serp_web/src/modules/settings/types/general.types.ts`: align FE types with backend settings DTO.
- Create `serp_web/src/modules/settings/services/general/generalApi.ts`: RTK Query endpoints.
- Modify `serp_web/src/modules/settings/services/settingsApi.ts`: export general settings hooks.
- Create `serp_web/src/modules/settings/components/general/SettingsGeneralPage.tsx`: data-backed settings UI.
- Modify `serp_web/src/modules/settings/components/index.ts`: export page component if local barrel pattern supports it.
- Modify `serp_web/src/app/settings/page.tsx`: thin route wrapper.

## Task 1: Backend Migration And Domain Fields

**Files:**

- Create: `account/src/main/resources/db/migration/V9__add_organization_settings_fields.sql`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/model/OrganizationModel.java`
- Modify: `account/src/main/java/serp/project/account/core/domain/entity/OrganizationEntity.java`
- Modify: `account/src/main/java/serp/project/account/infrastructure/store/mapper/OrganizationMapper.java`

- [ ] **Step 1: Create Flyway migration**

Create `account/src/main/resources/db/migration/V9__add_organization_settings_fields.sql`:

```sql
ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS city VARCHAR(255),
    ADD COLUMN IF NOT EXISTS state VARCHAR(255),
    ADD COLUMN IF NOT EXISTS country VARCHAR(255),
    ADD COLUMN IF NOT EXISTS zip_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS tax_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS secondary_color VARCHAR(20),
    ADD COLUMN IF NOT EXISTS favicon_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS date_format VARCHAR(30),
    ADD COLUMN IF NOT EXISTS time_format VARCHAR(10),
    ADD COLUMN IF NOT EXISTS week_starts_on VARCHAR(20);
```

- [ ] **Step 2: Add fields to `OrganizationModel`**

In `OrganizationModel.java`, add fields after related existing columns:

```java
    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "secondary_color")
    private String secondaryColor;

    @Column(name = "favicon_url")
    private String faviconUrl;

    @Column(name = "date_format")
    private String dateFormat;

    @Column(name = "time_format")
    private String timeFormat;

    @Column(name = "week_starts_on")
    private String weekStartsOn;
```

- [ ] **Step 3: Add fields to `OrganizationEntity`**

In `OrganizationEntity.java`, add matching fields:

```java
    private String city;

    private String state;

    private String country;

    private String zipCode;

    private String taxId;

    private String secondaryColor;

    private String faviconUrl;

    private String dateFormat;

    private String timeFormat;

    private String weekStartsOn;
```

- [ ] **Step 4: Map fields model to entity**

In `OrganizationMapper.toEntity`, add builder mappings near related fields:

```java
                .city(model.getCity())
                .state(model.getState())
                .country(model.getCountry())
                .zipCode(model.getZipCode())
                .taxId(model.getTaxId())
                .secondaryColor(model.getSecondaryColor())
                .faviconUrl(model.getFaviconUrl())
                .dateFormat(model.getDateFormat())
                .timeFormat(model.getTimeFormat())
                .weekStartsOn(model.getWeekStartsOn())
```

- [ ] **Step 5: Map fields entity to model**

In `OrganizationMapper.toModel`, add builder mappings:

```java
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .zipCode(entity.getZipCode())
                .taxId(entity.getTaxId())
                .secondaryColor(entity.getSecondaryColor())
                .faviconUrl(entity.getFaviconUrl())
                .dateFormat(entity.getDateFormat())
                .timeFormat(entity.getTimeFormat())
                .weekStartsOn(entity.getWeekStartsOn())
```

- [ ] **Step 6: Extend partial mapper update support**

In `OrganizationMapper.updateOrganizationMapper`, add setters before `updatedAt` handling:

```java
        if (update.getCity() != null) {
            existing.setCity(update.getCity());
        }

        if (update.getState() != null) {
            existing.setState(update.getState());
        }

        if (update.getCountry() != null) {
            existing.setCountry(update.getCountry());
        }

        if (update.getZipCode() != null) {
            existing.setZipCode(update.getZipCode());
        }

        if (update.getTaxId() != null) {
            existing.setTaxId(update.getTaxId());
        }

        if (update.getSecondaryColor() != null) {
            existing.setSecondaryColor(update.getSecondaryColor());
        }

        if (update.getFaviconUrl() != null) {
            existing.setFaviconUrl(update.getFaviconUrl());
        }

        if (update.getDateFormat() != null) {
            existing.setDateFormat(update.getDateFormat());
        }

        if (update.getTimeFormat() != null) {
            existing.setTimeFormat(update.getTimeFormat());
        }

        if (update.getWeekStartsOn() != null) {
            existing.setWeekStartsOn(update.getWeekStartsOn());
        }
```

- [ ] **Step 7: Run backend compile check**

Run from `account`:

```bash
mvnw.cmd clean compile
```

Expected: compile succeeds. If Lombok builder methods are missing, one field name is inconsistent between model/entity/mapper.

## Task 2: Backend Settings DTOs And Service Logic

**Files:**

- Create: `account/src/main/java/serp/project/account/core/domain/dto/request/UpdateOrganizationSettingsRequest.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/OrganizationSettingsSummaryResponse.java`
- Create: `account/src/main/java/serp/project/account/core/domain/dto/response/OrganizationSettingsResponse.java`
- Modify: `account/src/main/java/serp/project/account/core/service/IOrganizationService.java`
- Modify: `account/src/main/java/serp/project/account/core/service/impl/OrganizationService.java`

- [ ] **Step 1: Create update request DTO**

Create `UpdateOrganizationSettingsRequest.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UpdateOrganizationSettingsRequest {
    @Size(max = 255)
    private String name;

    @Email
    private String email;

    @Size(max = 50)
    private String phoneNumber;

    @Size(max = 500)
    private String website;

    @Size(max = 500)
    private String address;

    @Size(max = 255)
    private String city;

    @Size(max = 255)
    private String state;

    @Size(max = 255)
    private String country;

    @Size(max = 50)
    private String zipCode;

    @Size(max = 100)
    private String taxId;

    @Size(max = 255)
    private String industry;

    @Min(0)
    private Integer employeeCount;

    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 500)
    private String faviconUrl;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "primaryColor must be a hex color")
    private String primaryColor;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "secondaryColor must be a hex color")
    private String secondaryColor;

    @Size(max = 100)
    private String timezone;

    @Pattern(regexp = "^(MM/DD/YYYY|DD/MM/YYYY|YYYY-MM-DD)$", message = "dateFormat is invalid")
    private String dateFormat;

    @Pattern(regexp = "^(12h|24h)$", message = "timeFormat is invalid")
    private String timeFormat;

    @Pattern(regexp = "^(sunday|monday)$", message = "weekStartsOn is invalid")
    private String weekStartsOn;

    @Size(max = 10)
    private String currency;

    @Size(max = 10)
    private String language;
}
```

- [ ] **Step 2: Create summary response DTO**

Create `OrganizationSettingsSummaryResponse.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

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
public class OrganizationSettingsSummaryResponse {
    private Long totalUsers;
    private Long totalDepartments;
    private String subscriptionPlan;
}
```

- [ ] **Step 3: Create settings response DTO**

Create `OrganizationSettingsResponse.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

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
public class OrganizationSettingsResponse {
    private Long id;
    private String name;
    private String code;
    private String email;
    private String phoneNumber;
    private String website;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String taxId;
    private String industry;
    private Integer employeeCount;
    private String description;
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
    private String secondaryColor;
    private String timezone;
    private String dateFormat;
    private String timeFormat;
    private String weekStartsOn;
    private String currency;
    private String language;
    private Long createdAt;
    private Long updatedAt;
    private OrganizationSettingsSummaryResponse summary;
}
```

- [ ] **Step 4: Add service interface method**

Modify `IOrganizationService.java` imports and methods:

```java
import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
```

```java
    OrganizationEntity updateOrganizationSettings(Long organizationId, UpdateOrganizationSettingsRequest request);
```

- [ ] **Step 5: Implement service update method**

Modify `OrganizationService.java` imports:

```java
import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
```

Add method:

```java
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationEntity updateOrganizationSettings(Long organizationId, UpdateOrganizationSettingsRequest request) {
        var organization = getOrganizationById(organizationId);

        applySettingsUpdate(organization, request);
        return organizationPort.save(organization);
    }
```

Add helpers in `OrganizationService`:

```java
    private void applySettingsUpdate(OrganizationEntity organization, UpdateOrganizationSettingsRequest request) {
        if (request.getName() != null) {
            organization.setName(normalizeRequired(request.getName()));
        }
        organization.setEmail(normalizeOptional(request.getEmail()));
        organization.setPhoneNumber(normalizeOptional(request.getPhoneNumber()));
        organization.setWebsite(normalizeOptional(request.getWebsite()));
        organization.setAddress(normalizeOptional(request.getAddress()));
        organization.setCity(normalizeOptional(request.getCity()));
        organization.setState(normalizeOptional(request.getState()));
        organization.setCountry(normalizeOptional(request.getCountry()));
        organization.setZipCode(normalizeOptional(request.getZipCode()));
        organization.setTaxId(normalizeOptional(request.getTaxId()));
        organization.setIndustry(normalizeOptional(request.getIndustry()));
        organization.setEmployeeCount(request.getEmployeeCount());
        organization.setDescription(normalizeOptional(request.getDescription()));
        organization.setLogoUrl(normalizeOptional(request.getLogoUrl()));
        organization.setFaviconUrl(normalizeOptional(request.getFaviconUrl()));
        organization.setPrimaryColor(normalizeOptional(request.getPrimaryColor()));
        organization.setSecondaryColor(normalizeOptional(request.getSecondaryColor()));
        organization.setTimezone(normalizeOptional(request.getTimezone()));
        organization.setDateFormat(normalizeOptional(request.getDateFormat()));
        organization.setTimeFormat(normalizeOptional(request.getTimeFormat()));
        organization.setWeekStartsOn(normalizeOptional(request.getWeekStartsOn()));
        organization.setCurrency(normalizeOptional(request.getCurrency()));
        organization.setLanguage(normalizeOptional(request.getLanguage()));
    }

    private String normalizeRequired(String value) {
        var normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new AppException(Constants.ErrorMessage.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
```

Note: this PUT design treats supplied nullable/blank values as clearing fields. `employeeCount` clears when omitted by current code; if preserving omitted scalar values is required later, switch request to JSON merge patch or field presence tracking.

- [ ] **Step 6: Run backend compile check**

Run from `account`:

```bash
mvnw.cmd clean compile
```

Expected: compile succeeds.

## Task 3: Backend Use Case And Controller Endpoints

**Files:**

- Modify: `account/src/main/java/serp/project/account/core/usecase/OrganizationUseCase.java`
- Modify: `account/src/main/java/serp/project/account/ui/controller/OrganizationController.java`

- [ ] **Step 1: Add usecase imports**

Modify `OrganizationUseCase.java` imports:

```java
import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
import serp.project.account.core.domain.dto.response.OrganizationSettingsResponse;
import serp.project.account.core.domain.dto.response.OrganizationSettingsSummaryResponse;
import serp.project.account.core.domain.entity.OrganizationEntity;
```

- [ ] **Step 2: Add settings read/update methods**

Add methods to `OrganizationUseCase`:

```java
    public GeneralResponse<?> getOrganizationSettings(Long organizationId) {
        try {
            var organization = organizationService.getOrganizationById(organizationId);
            return responseUtils.success(toSettingsResponse(organization));
        } catch (Exception e) {
            log.error("Error getting organization settings: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> updateOrganizationSettings(Long organizationId, UpdateOrganizationSettingsRequest request) {
        try {
            var organization = organizationService.updateOrganizationSettings(organizationId, request);
            return responseUtils.success(toSettingsResponse(organization));
        } catch (Exception e) {
            log.error("Error updating organization settings: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
```

- [ ] **Step 3: Add response mapper helper**

Add private helper to `OrganizationUseCase`:

```java
    private OrganizationSettingsResponse toSettingsResponse(OrganizationEntity organization) {
        return OrganizationSettingsResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .code(organization.getCode())
                .email(organization.getEmail())
                .phoneNumber(organization.getPhoneNumber())
                .website(organization.getWebsite())
                .address(organization.getAddress())
                .city(organization.getCity())
                .state(organization.getState())
                .country(organization.getCountry())
                .zipCode(organization.getZipCode())
                .taxId(organization.getTaxId())
                .industry(organization.getIndustry())
                .employeeCount(organization.getEmployeeCount())
                .description(organization.getDescription())
                .logoUrl(organization.getLogoUrl())
                .faviconUrl(organization.getFaviconUrl())
                .primaryColor(organization.getPrimaryColor())
                .secondaryColor(organization.getSecondaryColor())
                .timezone(organization.getTimezone())
                .dateFormat(organization.getDateFormat())
                .timeFormat(organization.getTimeFormat())
                .weekStartsOn(organization.getWeekStartsOn())
                .currency(organization.getCurrency())
                .language(organization.getLanguage())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .summary(OrganizationSettingsSummaryResponse.builder().build())
                .build();
    }
```

- [ ] **Step 4: Add controller imports**

Modify `OrganizationController.java` imports:

```java
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
```

- [ ] **Step 5: Add settings endpoints**

Add endpoints to `OrganizationController`:

```java
    @GetMapping("/organizations/me/settings")
    public ResponseEntity<?> getMyOrganizationSettings() {
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);
        var response = organizationUseCase.getOrganizationSettings(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/organizations/me/settings")
    public ResponseEntity<?> updateMyOrganizationSettings(
            @Valid @RequestBody UpdateOrganizationSettingsRequest request) {
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);
        var response = organizationUseCase.updateOrganizationSettings(organizationId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }
```

- [ ] **Step 6: Run backend compile check**

Run from `account`:

```bash
mvnw.cmd clean compile
```

Expected: compile succeeds.

## Task 4: Backend Focused Tests

**Files:**

- Create: `account/src/test/java/serp/project/account/core/service/impl/OrganizationServiceTest.java`

- [ ] **Step 1: Add service test**

Create `OrganizationServiceTest.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.port.store.IOrganizationPort;
import serp.project.account.core.port.store.IUserOrganizationPort;
import serp.project.account.infrastructure.store.mapper.OrganizationMapper;
import serp.project.account.infrastructure.store.mapper.UserOrganizationMapper;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @Mock
    private IOrganizationPort organizationPort;

    @Mock
    private IUserOrganizationPort userOrganizationPort;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private UserOrganizationMapper userOrganizationMapper;

    @InjectMocks
    private OrganizationService organizationService;

    private OrganizationEntity organization;

    @BeforeEach
    void setUp() {
        organization = OrganizationEntity.builder()
                .id(1L)
                .name("Old Name")
                .email("old@example.com")
                .city("Old City")
                .primaryColor("#111111")
                .build();
    }

    @Test
    void updateOrganizationSettingsShouldTrimValuesAndClearBlankOptionalFields() {
        var request = UpdateOrganizationSettingsRequest.builder()
                .name(" New Name ")
                .email(" ")
                .city(" Hanoi ")
                .primaryColor("#7c3aed")
                .weekStartsOn("monday")
                .build();

        when(organizationPort.getById(1L)).thenReturn(organization);
        when(organizationPort.save(any(OrganizationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = organizationService.updateOrganizationSettings(1L, request);

        assertEquals("New Name", updated.getName());
        assertNull(updated.getEmail());
        assertEquals("Hanoi", updated.getCity());
        assertEquals("#7c3aed", updated.getPrimaryColor());
        assertEquals("monday", updated.getWeekStartsOn());
        verify(organizationPort).save(organization);
    }
}
```

- [ ] **Step 2: Run focused test**

Run from `account`:

```bash
mvnw.cmd -Dtest=OrganizationServiceTest test
```

Expected: test passes.

- [ ] **Step 3: Run backend compile**

Run from `account`:

```bash
mvnw.cmd clean compile
```

Expected: compile succeeds.

## Task 5: Frontend Types And RTK Query API

**Files:**

- Modify: `serp_web/src/modules/settings/types/general.types.ts`
- Create: `serp_web/src/modules/settings/services/general/generalApi.ts`
- Modify: `serp_web/src/modules/settings/services/settingsApi.ts`

- [ ] **Step 1: Replace general settings types**

Update `general.types.ts` to backend-aligned types:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - General settings types
 */

export type DateFormat = 'MM/DD/YYYY' | 'DD/MM/YYYY' | 'YYYY-MM-DD';
export type TimeFormat = '12h' | '24h';
export type WeekDay = 'sunday' | 'monday';

export interface OrganizationSettingsSummary {
  totalUsers?: number | null;
  totalDepartments?: number | null;
  subscriptionPlan?: string | null;
}

export interface OrganizationSettings {
  id: number;
  name: string;
  code: string;
  email?: string | null;
  phoneNumber?: string | null;
  website?: string | null;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  zipCode?: string | null;
  taxId?: string | null;
  industry?: string | null;
  employeeCount?: number | null;
  description?: string | null;
  logoUrl?: string | null;
  faviconUrl?: string | null;
  primaryColor?: string | null;
  secondaryColor?: string | null;
  timezone?: string | null;
  dateFormat?: DateFormat | null;
  timeFormat?: TimeFormat | null;
  weekStartsOn?: WeekDay | null;
  currency?: string | null;
  language?: string | null;
  createdAt?: number | null;
  updatedAt?: number | null;
  summary?: OrganizationSettingsSummary | null;
}

export type UpdateOrganizationSettingsRequest = Partial<
  Omit<
    OrganizationSettings,
    'id' | 'code' | 'createdAt' | 'updatedAt' | 'summary'
  >
>;
```

- [ ] **Step 2: Add general RTK Query API**

Create `serp_web/src/modules/settings/services/general/generalApi.ts`:

```ts
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - General settings API endpoints
 */

import { api } from '@/lib/store/api';
import { createDataTransform } from '@/lib/store/api/utils';
import type {
  OrganizationSettings,
  UpdateOrganizationSettingsRequest,
} from '@/modules/settings/types/general.types';

export const settingsGeneralApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getOrganizationSettings: builder.query<OrganizationSettings, void>({
      query: () => ({ url: '/organizations/me/settings', method: 'GET' }),
      transformResponse: createDataTransform<OrganizationSettings>(),
      providesTags: [{ type: 'settings/Organization', id: 'SETTINGS' }],
      extraOptions: { service: 'account' },
    }),
    updateOrganizationSettings: builder.mutation<
      OrganizationSettings,
      UpdateOrganizationSettingsRequest
    >({
      query: (body) => ({
        url: '/organizations/me/settings',
        method: 'PUT',
        body,
      }),
      transformResponse: createDataTransform<OrganizationSettings>(),
      invalidatesTags: [{ type: 'settings/Organization', id: 'SETTINGS' }],
      extraOptions: { service: 'account' },
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetOrganizationSettingsQuery,
  useUpdateOrganizationSettingsMutation,
} = settingsGeneralApi;
```

- [ ] **Step 3: Export general API hooks**

Modify `settingsApi.ts` uncommenting/replacing the general section:

```ts
export {
  settingsGeneralApi,
  useGetOrganizationSettingsQuery,
  useUpdateOrganizationSettingsMutation,
} from './general/generalApi';
```

- [ ] **Step 4: Run frontend type check**

Run from `serp_web`:

```bash
npm run type-check
```

Expected: TypeScript passes, or only reports unrelated existing errors. Fix errors caused by these files before proceeding.

## Task 6: Frontend Settings General Page Refactor

**Files:**

- Create: `serp_web/src/modules/settings/components/general/SettingsGeneralPage.tsx`
- Modify: `serp_web/src/modules/settings/components/index.ts`
- Modify: `serp_web/src/app/settings/page.tsx`

- [ ] **Step 1: Create data-backed module page component**

Create `SettingsGeneralPage.tsx`. Use existing page markup as base, but replace hardcoded object with RTK Query state. Required imports:

```ts
'use client';

import React, { useEffect, useState } from 'react';
import { toast } from 'sonner';
import {
  Building2,
  Calendar,
  Clock,
  Crown,
  DollarSign,
  Globe,
  Languages,
  Mail,
  MapPin,
  Palette,
  Phone,
  Save,
  Settings as SettingsIcon,
  Users,
} from 'lucide-react';

import { getErrorMessage } from '@/lib/store/api';
import { SettingsStatsCard } from '@/modules/settings/components/shared/SettingsStatsCard';
import {
  useGetOrganizationSettingsQuery,
  useUpdateOrganizationSettingsMutation,
} from '@/modules/settings/services/settingsApi';
import type {
  DateFormat,
  TimeFormat,
  UpdateOrganizationSettingsRequest,
  WeekDay,
} from '@/modules/settings/types/general.types';
import { Alert, AlertDescription } from '@/shared/components/ui/alert';
import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Input } from '@/shared/components/ui/input';
import { Label } from '@/shared/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Separator } from '@/shared/components/ui/separator';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui/tabs';
import { Textarea } from '@/shared/components/ui/textarea';
```

- [ ] **Step 2: Add form defaults and helpers**

Inside file before component:

```ts
const DEFAULT_FORM: UpdateOrganizationSettingsRequest = {
  name: '',
  email: '',
  phoneNumber: '',
  website: '',
  address: '',
  city: '',
  state: '',
  country: '',
  zipCode: '',
  taxId: '',
  industry: '',
  employeeCount: null,
  description: '',
  logoUrl: '',
  faviconUrl: '',
  primaryColor: '#7c3aed',
  secondaryColor: '#a78bfa',
  timezone: 'Asia/Ho_Chi_Minh',
  dateFormat: 'DD/MM/YYYY',
  timeFormat: '24h',
  weekStartsOn: 'monday',
  currency: 'VND',
  language: 'vi',
};

const toInputValue = (value: string | number | null | undefined) =>
  value == null ? '' : String(value);
```

- [ ] **Step 3: Add component state and load data**

Inside component:

```ts
  const [activeTab, setActiveTab] = useState('profile');
  const [form, setForm] = useState<UpdateOrganizationSettingsRequest>(DEFAULT_FORM);
  const { data, error, isLoading, isFetching } = useGetOrganizationSettingsQuery();
  const [updateSettings, { isLoading: isSaving }] =
    useUpdateOrganizationSettingsMutation();

  useEffect(() => {
    if (!data) {
      return;
    }

    setForm({
      name: data.name ?? '',
      email: data.email ?? '',
      phoneNumber: data.phoneNumber ?? '',
      website: data.website ?? '',
      address: data.address ?? '',
      city: data.city ?? '',
      state: data.state ?? '',
      country: data.country ?? '',
      zipCode: data.zipCode ?? '',
      taxId: data.taxId ?? '',
      industry: data.industry ?? '',
      employeeCount: data.employeeCount ?? null,
      description: data.description ?? '',
      logoUrl: data.logoUrl ?? '',
      faviconUrl: data.faviconUrl ?? '',
      primaryColor: data.primaryColor ?? '#7c3aed',
      secondaryColor: data.secondaryColor ?? '#a78bfa',
      timezone: data.timezone ?? 'Asia/Ho_Chi_Minh',
      dateFormat: data.dateFormat ?? 'DD/MM/YYYY',
      timeFormat: data.timeFormat ?? '24h',
      weekStartsOn: data.weekStartsOn ?? 'monday',
      currency: data.currency ?? 'VND',
      language: data.language ?? 'vi',
    });
  }, [data]);
```

- [ ] **Step 4: Add update and save handlers**

Inside component:

```ts
  const updateField = <K extends keyof UpdateOrganizationSettingsRequest>(
    key: K,
    value: UpdateOrganizationSettingsRequest[K]
  ) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const handleSave = async () => {
    try {
      await updateSettings(form).unwrap();
      toast.success('Organization settings saved.');
    } catch (saveError) {
      toast.error('Failed to save organization settings', {
        description: getErrorMessage(saveError),
      });
    }
  };
```

- [ ] **Step 5: Render loading and error states**

At top of JSX:

```tsx
  if (isLoading) {
    return <div className='text-muted-foreground'>Loading settings...</div>;
  }

  if (error) {
    return (
      <Alert variant='destructive'>
        <AlertDescription>{getErrorMessage(error)}</AlertDescription>
      </Alert>
    );
  }
```

- [ ] **Step 6: Replace hardcoded values in markup**

Use these representative patterns throughout copied markup:

```tsx
<Input
  id='name'
  value={toInputValue(form.name)}
  onChange={(event) => updateField('name', event.target.value)}
  placeholder='Enter organization name'
/>
```

```tsx
<Input id='code' value={data?.code ?? ''} disabled />
```

```tsx
<Input
  id='employeeCount'
  type='number'
  min={0}
  value={toInputValue(form.employeeCount)}
  onChange={(event) =>
    updateField(
      'employeeCount',
      event.target.value ? Number(event.target.value) : null
    )
  }
/>
```

```tsx
<Select
  value={(form.dateFormat as DateFormat) ?? 'DD/MM/YYYY'}
  onValueChange={(value: DateFormat) => updateField('dateFormat', value)}
>
```

```tsx
<Button onClick={handleSave} disabled={isSaving || isFetching}>
  <Save className='h-4 w-4 mr-2' />
  {isSaving ? 'Saving...' : 'Save Changes'}
</Button>
```

- [ ] **Step 7: Export module page component**

Modify `components/index.ts`:

```ts
export { default as SettingsGeneralPage } from './general/SettingsGeneralPage';
```

- [ ] **Step 8: Make app route thin**

Replace `serp_web/src/app/settings/page.tsx` contents:

```tsx
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings general page route
 */

import { SettingsGeneralPage } from '@/modules/settings/components';

export default function SettingsPage() {
  return <SettingsGeneralPage />;
}
```

- [ ] **Step 9: Run frontend checks**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all pass, or only unrelated existing failures are documented. Fix any failures from touched files.

## Task 7: Final Verification

**Files:**

- Read-only verification across touched backend/frontend files.

- [ ] **Step 1: Run backend focused test**

Run from `account`:

```bash
mvnw.cmd -Dtest=OrganizationServiceTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run backend compile**

Run from `account`:

```bash
mvnw.cmd clean compile
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Run frontend verification**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all commands exit 0.

- [ ] **Step 4: Inspect git diff**

Run from repo root:

```bash
git diff -- account serp_web docs/superpowers/specs/2026-05-31-settings-general-design.md docs/superpowers/plans/2026-05-31-settings-general-api-integration.md
```

Expected: diff only contains settings design, migration, backend settings API, frontend settings integration, and focused test changes.

## Self-Review Results

- Spec coverage: covered migration, one GET endpoint, one PUT endpoint, backend DTOs/service/usecase/controller, FE RTK Query, FE page refactor, loading/error/saving states, and verification.
- Red-flag scan: no incomplete markers or open-ended implementation steps remain.
- Type consistency: backend uses `phoneNumber`, `zipCode`, `taxId`, `faviconUrl`, `secondaryColor`, `dateFormat`, `timeFormat`, `weekStartsOn`; frontend uses same property names.
