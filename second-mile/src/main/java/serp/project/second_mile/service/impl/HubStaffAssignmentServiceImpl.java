/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubStaff;
import serp.project.second_mile.domain.HubStaffAssignment;
import serp.project.second_mile.dto.response.HubStaffAssignmentResponse;
import serp.project.second_mile.dto.response.HubStaffResponse;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.HubStaffRepository;
import serp.project.second_mile.service.HubStaffAssignmentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HubStaffAssignmentServiceImpl implements HubStaffAssignmentService {
    private final HubRepository hubRepository;
    private final HubStaffRepository hubStaffRepository;
    private final HubStaffAssignmentRepository hubStaffAssignmentRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HubStaffAssignmentResponse assignStaffToHub(Long staffId, Long hubId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Hub hub = getHubOrThrow(hubId, tenantId);
        HubStaff staff = getStaffOrThrow(staffId, tenantId);
        validateStaffActive(staff);

        LocalDate today = LocalDate.now();
        HubStaffAssignment existingAssignment = hubStaffAssignmentRepository
                .findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(staffId, hubId, tenantId, today)
                .orElse(null);
        if (existingAssignment != null) {
            return toResponse(existingAssignment);
        }

        closeOtherActiveAssignments(staffId, hubId, tenantId, today);

        HubStaffAssignment assignment = HubStaffAssignment.builder()
                .hub(hub)
                .staff(staff)
                .assignedFrom(today)
                .isPrimary(Boolean.TRUE)
                .tenantId(tenantId)
                .build();
        HubStaffAssignment savedAssignment = hubStaffAssignmentRepository.save(assignment);
        return toResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubStaffAssignmentResponse> listActiveAssignmentsByHub(Long hubId, HubStaffRole role) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        getHubOrThrow(hubId, tenantId);

        LocalDate today = LocalDate.now();
        List<HubStaffAssignment> assignments = role == null
                ? hubStaffAssignmentRepository.findActiveAssignmentsByHubIdAndTenantIdAndStaffStatus(
                        hubId,
                        tenantId,
                        today,
                        HubStaffStatus.ACTIVE
                )
                : hubStaffAssignmentRepository.findActiveAssignmentsByHubIdAndTenantIdAndStaffRoleAndStatus(
                        hubId,
                        tenantId,
                        today,
                        role,
                        HubStaffStatus.ACTIVE
                );
        return assignments.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HubStaffAssignmentResponse unassignStaffFromHub(Long assignmentId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        HubStaffAssignment assignment = hubStaffAssignmentRepository.findByIdAndTenantId(assignmentId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Hub staff assignment not found."));

        LocalDate today = LocalDate.now();
        if (assignment.getAssignedFrom() != null && assignment.getAssignedFrom().isEqual(today)) {
            hubStaffAssignmentRepository.delete(assignment);
            return toResponse(assignment);
        }

        assignment.setAssignedTo(today.minusDays(1));
        HubStaffAssignment savedAssignment = hubStaffAssignmentRepository.save(assignment);
        return toResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubStaffResponse> getAssignableStaffByRole(HubStaffRole role, String keyword) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        if (role == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        List<HubStaff> staffs = hubStaffRepository.findAssignableByTenantIdAndRoleAndStatusAndKeyword(
                tenantId,
                role,
                HubStaffStatus.ACTIVE,
                buildKeywordLike(keyword),
                LocalDate.now()
        );
        return staffs.stream()
                .map(staff -> new HubStaffResponse(
                        staff.getId(),
                        staff.getCode(),
                        staff.getFullName(),
                        staff.getRole(),
                        staff.getStatus()
                ))
                .toList();
    }

    private Hub getHubOrThrow(Long hubId, Long tenantId) {
        Hub hub = hubRepository.findById(hubId).orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND));
        if (!tenantId.equals(hub.getTenantId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return hub;
    }

    private HubStaff getStaffOrThrow(Long staffId, Long tenantId) {
        return hubStaffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Hub staff not found."));
    }

    private void validateStaffActive(HubStaff staff) {
        if (staff.getStatus() != HubStaffStatus.ACTIVE) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Hub staff must be active.");
        }
    }

    private void closeOtherActiveAssignments(Long staffId, Long targetHubId, Long tenantId, LocalDate today) {
        List<HubStaffAssignment> activeAssignments = hubStaffAssignmentRepository
                .findActiveAssignmentsByStaffIdAndTenantId(staffId, tenantId, today);
        if (activeAssignments.isEmpty()) {
            return;
        }

        List<HubStaffAssignment> assignmentsToUpdate = new ArrayList<>();
        List<HubStaffAssignment> assignmentsToDelete = new ArrayList<>();
        for (HubStaffAssignment activeAssignment : activeAssignments) {
            Long activeHubId = activeAssignment.getHub() == null ? null : activeAssignment.getHub().getId();
            if (targetHubId.equals(activeHubId)) {
                continue;
            }

            if (activeAssignment.getAssignedFrom() != null && activeAssignment.getAssignedFrom().isEqual(today)) {
                assignmentsToDelete.add(activeAssignment);
                continue;
            }

            activeAssignment.setAssignedTo(today.minusDays(1));
            assignmentsToUpdate.add(activeAssignment);
        }

        if (!assignmentsToUpdate.isEmpty()) {
            hubStaffAssignmentRepository.saveAll(assignmentsToUpdate);
        }
        if (!assignmentsToDelete.isEmpty()) {
            hubStaffAssignmentRepository.deleteAll(assignmentsToDelete);
        }
    }

    private HubStaffAssignmentResponse toResponse(HubStaffAssignment assignment) {
        Hub hub = assignment.getHub();
        HubStaff staff = assignment.getStaff();
        return new HubStaffAssignmentResponse(
                assignment.getId(),
                hub == null ? null : hub.getId(),
                hub == null ? null : hub.getCode(),
                hub == null ? null : hub.getName(),
                staff == null ? null : staff.getId(),
                staff == null ? null : staff.getCode(),
                staff == null ? null : staff.getFullName(),
                staff == null ? null : staff.getRole(),
                staff == null ? null : staff.getStatus(),
                assignment.getAssignedFrom(),
                assignment.getAssignedTo(),
                assignment.getIsPrimary(),
                assignment.getNotes(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private String buildKeywordLike(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : "%" + normalized.toLowerCase() + "%";
    }
}
