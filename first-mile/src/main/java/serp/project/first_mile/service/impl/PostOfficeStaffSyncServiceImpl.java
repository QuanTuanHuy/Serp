/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.dto.message.SyncUserFirstMileEvent;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.kernel.utils.PostOfficeStaffCodeUtils;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.service.PostOfficeStaffSyncService;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostOfficeStaffSyncServiceImpl implements PostOfficeStaffSyncService {
    private static final String ROLE_TMS_POSTOFFICER_MANAGER = "TMS_POSTOFFICER_MANAGER";
    private static final String ROLE_TMS_POSTOFFICER = "TMS_POSTOFFICER";

    private final PostOfficeStaffRepository postOfficeStaffRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUser(SyncUserFirstMileEvent event) {
        List<String> normalizedRoleNames = normalizeRoleNames(event);

        if (!isValidEvent(event, normalizedRoleNames)) {
            log.warn("Skip sync user first-mile: invalid event payload {}", event);
            return;
        }

        PostOfficeStaffRole staffRole = mapRole(normalizedRoleNames);
        if (staffRole == null) {
            log.debug("Skip sync user first-mile: unsupported role names {}", normalizedRoleNames);
            return;
        }

        String staffCode = PostOfficeStaffCodeUtils.buildStaffCode(event.getUserId(), staffRole);
        Long tenantId = resolveTenantId(event);
        PostOfficeStaff staff = postOfficeStaffRepository.findByCode(staffCode)
                .orElseGet(() -> PostOfficeStaff.builder().code(staffCode).build());

        staff.setFullName(resolveFullName(event));
        staff.setPhoneNumber(normalizeText(event.getPhoneNumber()));
        staff.setEmail(normalizeText(event.getEmail()));
        staff.setRole(staffRole);
        staff.setStatus(PostOfficeStaffStatus.ACTIVE);
        staff.setTenantId(tenantId);
        staff.setUserId(event.getUserId());

        if (staff.getHireDate() == null) {
            staff.setHireDate(LocalDate.now());
        }
        if (!hasText(staff.getNotes())) {
            staff.setNotes("Synced from account service");
        }

        postOfficeStaffRepository.save(staff);
        log.info(
            "Synced post office staff from account: userId={}, tenantId={}, organizationId={}, role={}, sourceRoles={}, code={}",
                event.getUserId(),
                tenantId,
                event.getOrganizationId(),
                staffRole,
                normalizedRoleNames,
                staffCode
        );
    }

    private boolean isValidEvent(SyncUserFirstMileEvent event, List<String> normalizedRoleNames) {
        return event != null
                && event.getUserId() != null
                && resolveTenantId(event) != null
                && !normalizedRoleNames.isEmpty();
    }

    private Long resolveTenantId(SyncUserFirstMileEvent event) {
        if (event == null) {
            return null;
        }
        return event.getTenantId() != null ? event.getTenantId() : event.getOrganizationId();
    }

    private PostOfficeStaffRole mapRole(List<String> normalizedRoleNames) {
        if (normalizedRoleNames.contains(ROLE_TMS_POSTOFFICER_MANAGER)) {
            return PostOfficeStaffRole.MANAGER;
        }
        if (normalizedRoleNames.contains(ROLE_TMS_POSTOFFICER)) {
            return PostOfficeStaffRole.COURIER;
        }
        return null;
    }

    private List<String> normalizeRoleNames(SyncUserFirstMileEvent event) {
        if (event == null || event.getRoleNames() == null) {
            return List.of();
        }

        return event.getRoleNames().stream()
                .filter(Objects::nonNull)
                .map(roleName -> roleName.trim().toUpperCase(Locale.ROOT))
                .filter(roleName -> !roleName.isEmpty())
                .distinct()
                .toList();
    }

    private String resolveFullName(SyncUserFirstMileEvent event) {
        if (hasText(event.getFullName())) {
            return event.getFullName().trim();
        }

        String firstName = normalizeText(event.getFirstName());
        String lastName = normalizeText(event.getLastName());
        String combined = (firstName + " " + lastName).trim();
        if (hasText(combined)) {
            return combined;
        }

        if (hasText(event.getEmail())) {
            return event.getEmail().trim();
        }

        return "USER-" + event.getUserId();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
