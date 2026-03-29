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
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.service.PostOfficeStaffSyncService;

import java.time.LocalDate;
import java.util.Locale;

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
        if (!isValidEvent(event)) {
            log.warn("Skip sync user first-mile: invalid event payload {}", event);
            return;
        }

        PostOfficeStaffRole staffRole = mapRole(event.getRoleName());
        if (staffRole == null) {
            log.debug("Skip sync user first-mile: unsupported role {}", event.getRoleName());
            return;
        }

        String staffCode = buildStaffCode(event.getUserId(), staffRole);
        Long tenantId = resolveTenantId(event);
        PostOfficeStaff staff = postOfficeStaffRepository.findByCode(staffCode)
                .orElseGet(() -> PostOfficeStaff.builder().code(staffCode).build());

        staff.setFullName(resolveFullName(event));
        staff.setPhoneNumber(normalizeText(event.getPhoneNumber()));
        staff.setEmail(normalizeText(event.getEmail()));
        staff.setRole(staffRole);
        staff.setStatus(PostOfficeStaffStatus.ACTIVE);
        staff.setTenantId(tenantId);

        if (staff.getHireDate() == null) {
            staff.setHireDate(LocalDate.now());
        }
        if (!hasText(staff.getNotes())) {
            staff.setNotes("Synced from account service");
        }

        postOfficeStaffRepository.save(staff);
        log.info(
            "Synced post office staff from account: userId={}, tenantId={}, organizationId={}, role={}, code={}",
                event.getUserId(),
            tenantId,
                event.getOrganizationId(),
                staffRole,
                staffCode
        );
    }

    private boolean isValidEvent(SyncUserFirstMileEvent event) {
        return event != null
                && event.getUserId() != null
                && resolveTenantId(event) != null
                && hasText(event.getRoleName());
    }

    private Long resolveTenantId(SyncUserFirstMileEvent event) {
        if (event == null) {
            return null;
        }
        return event.getTenantId() != null ? event.getTenantId() : event.getOrganizationId();
    }

    private String buildStaffCode(Long userId, PostOfficeStaffRole role) {
        return "USR_" + userId + "_" + role.name();
    }

    private PostOfficeStaffRole mapRole(String roleName) {
        String normalizedRole = roleName.trim().toUpperCase(Locale.ROOT);
        return switch (normalizedRole) {
            case ROLE_TMS_POSTOFFICER_MANAGER -> PostOfficeStaffRole.MANAGER;
            case ROLE_TMS_POSTOFFICER -> PostOfficeStaffRole.COURIER;
            default -> null;
        };
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
