/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.HubStaff;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;
import serp.project.second_mile.kafka.event.UserSyncEvent;
import serp.project.second_mile.kernel.utils.HubStaffCodeUtils;
import serp.project.second_mile.repository.HubStaffRepository;
import serp.project.second_mile.service.UserSyncService;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSyncServiceImpl implements UserSyncService {
    private static final String ROLE_TMS_HUB_MANAGER = "TMS_HUB_MANAGER";
    private static final String ROLE_TMS_HUB_EMPLOYEE = "TMS_HUB_EMPLOYEE";
    private static final String ROLE_TMS_HUB_DRIVER = "TMS_HUB_DRIVER";

    private final HubStaffRepository hubStaffRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUser(UserSyncEvent event) {
        List<String> normalizedRoleNames = normalizeRoleNames(event);

        if (!isValidEvent(event, normalizedRoleNames)) {
            log.warn("Skip sync user first-mile: invalid event payload {}", event);
            return;
        }

        HubStaffRole staffRole = mapRole(normalizedRoleNames);
        if (staffRole == null) {
            log.debug("Skip sync user second-mile: unsupported role names {}", normalizedRoleNames);
            return;
        }

        String hubStaffCode = HubStaffCodeUtils.buildHubStaffCode(event.getUserId(), staffRole);
        Long tenantId = resolveTenantId(event);
        HubStaff staff = hubStaffRepository.findByCode(hubStaffCode)
                .orElseGet(() -> HubStaff.builder().code(hubStaffCode).build());

        staff.setFullName(resolveFullName(event));
        staff.setPhoneNumber(normalizeText(event.getPhoneNumber()));
        staff.setEmail(normalizeText(event.getEmail()));
        staff.setRole(staffRole);
        staff.setStatus(HubStaffStatus.ACTIVE);
        staff.setTenantId(tenantId);
        staff.setUserId(event.getUserId());

        if (staff.getHireDate() == null) {
            staff.setHireDate(LocalDate.now());
        }
        if (!hasText(staff.getNote())) {
            staff.setNote("Synced from account service");
        }

        hubStaffRepository.save(staff);
        log.info(
                "Synced hub staff from account: userId={}, tenantId={}, organizationId={}, role={}, sourceRoles={}, code={}",
                event.getUserId(),
                tenantId,
                event.getOrganizationId(),
                staffRole,
                normalizedRoleNames,
                hubStaffCode
        );
    }

    private boolean isValidEvent(UserSyncEvent event, List<String> normalizedRoleNames) {
        return event != null
                && event.getUserId() != null
                && resolveTenantId(event) != null
                && !normalizedRoleNames.isEmpty();
    }

    private Long resolveTenantId(UserSyncEvent event) {
        if (event == null) {
            return null;
        }
        return event.getTenantId() != null ? event.getTenantId() : event.getOrganizationId();
    }

    private HubStaffRole mapRole(List<String> normalizedRoleNames) {
        if (normalizedRoleNames.contains(ROLE_TMS_HUB_MANAGER)) {
            return HubStaffRole.MANAGER;
        }
        if (normalizedRoleNames.contains(ROLE_TMS_HUB_EMPLOYEE)) {
            return HubStaffRole.EMPLOYEE;
        }
        if (normalizedRoleNames.contains(ROLE_TMS_HUB_DRIVER)) {
            return HubStaffRole.DRIVER;
        }
        return null;
    }

    private List<String> normalizeRoleNames(UserSyncEvent event) {
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

    private String resolveFullName(UserSyncEvent event) {
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
