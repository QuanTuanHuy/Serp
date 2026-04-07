/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;
import serp.project.first_mile.dto.request.UpdatePostOfficeStaffRequest;
import serp.project.first_mile.dto.response.PostOfficeStaffAssignmentResponse;
import serp.project.first_mile.dto.response.PostOfficeStaffResponse;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.mapper.PostOfficeStaffMapper;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.service.PostOfficeStaffService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostOfficeStaffServiceImpl implements PostOfficeStaffService {
    private static final String ROLE_TMS_ADMIN = "TMS_ADMIN";
    private static final String ROLE_TMS_POSTOFFICER_MANAGER = "TMS_POSTOFFICER_MANAGER";

    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final AuthUtils authUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeStaffResponse updatePostOfficeStaff(Long id, UpdatePostOfficeStaffRequest request) {
        Long tenantId = getCurrentTenantIdOrThrow();
        PostOfficeStaff staff = getPostOfficeStaffByIdAndTenantOrThrow(id, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessStaff(staff.getId(), tenantId, managedPostOfficeIds);
        }

        staff.setMaxDailyStops(normalizeNonNegative(request.getMaxDailyStops()));
        staff.setMaxDailyParcels(normalizeNonNegative(request.getMaxDailyParcels()));
        staff.setPhoneNumber(normalizeText(request.getPhoneNumber()));
        staff.setNotes(normalizeText(request.getNotes()));

        PostOfficeStaff updatedStaff = postOfficeStaffRepository.save(staff);
        return PostOfficeStaffMapper.toResponse(updatedStaff);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeStaffAssignmentResponse assignCourierToPostOffice(Long id, Long postOfficeId) {
        Long tenantId = getCurrentTenantIdOrThrow();

        PostOfficeStaff courier = getPostOfficeStaffByIdAndTenantOrThrow(id, tenantId);
        validateStaffRole(courier, PostOfficeStaffRole.COURIER);
        validateStaffActive(courier);

        PostOffice postOffice = getPostOfficeByIdAndTenantOrThrow(postOfficeId, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessPostOffice(postOffice.getId(), managedPostOfficeIds);
        }

        LocalDate today = LocalDate.now();
        PostOfficeStaffAssignment existingAssignment = postOfficeStaffAssignmentRepository
                .findFirstActiveAssignmentByStaffIdAndPostOfficeIdAndTenantId(courier.getId(), postOffice.getId(), tenantId, today)
                .orElse(null);
        if (existingAssignment != null) {
            return PostOfficeStaffMapper.toAssignmentResponse(existingAssignment);
        }

        PostOfficeStaffAssignment assignment = buildDefaultAssignment(postOffice, courier, tenantId, today);
        PostOfficeStaffAssignment savedAssignment = postOfficeStaffAssignmentRepository.save(assignment);
        return PostOfficeStaffMapper.toAssignmentResponse(savedAssignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeStaffAssignmentResponse assignManagerToPostOffice(Long id, Long postOfficeId) {
        Long tenantId = getCurrentTenantIdOrThrow();

        PostOfficeStaff manager = getPostOfficeStaffByIdAndTenantOrThrow(id, tenantId);
        validateStaffRole(manager, PostOfficeStaffRole.MANAGER);
        validateStaffActive(manager);

        PostOffice postOffice = getPostOfficeByIdAndTenantOrThrow(postOfficeId, tenantId);
        LocalDate today = LocalDate.now();

        List<PostOfficeStaffAssignment> activeManagerAssignments = postOfficeStaffAssignmentRepository
                .findActiveAssignmentsByPostOfficeIdAndTenantIdAndStaffRole(
                        postOffice.getId(),
                        tenantId,
                        today,
                        PostOfficeStaffRole.MANAGER
                );

        for (PostOfficeStaffAssignment activeAssignment : activeManagerAssignments) {
            if (activeAssignment.getStaff() != null && manager.getId().equals(activeAssignment.getStaff().getId())) {
                return PostOfficeStaffMapper.toAssignmentResponse(activeAssignment);
            }
        }

        List<PostOfficeStaffAssignment> assignmentsToUpdate = new ArrayList<>();
        List<PostOfficeStaffAssignment> assignmentsToDelete = new ArrayList<>();
        for (PostOfficeStaffAssignment activeAssignment : activeManagerAssignments) {
            if (activeAssignment.getAssignedFrom() != null && activeAssignment.getAssignedFrom().isEqual(today)) {
                assignmentsToDelete.add(activeAssignment);
                continue;
            }

            activeAssignment.setAssignedTo(today.minusDays(1));
            assignmentsToUpdate.add(activeAssignment);
        }

        if (!assignmentsToUpdate.isEmpty()) {
            postOfficeStaffAssignmentRepository.saveAll(assignmentsToUpdate);
        }
        if (!assignmentsToDelete.isEmpty()) {
            postOfficeStaffAssignmentRepository.deleteAll(assignmentsToDelete);
        }

        PostOfficeStaffAssignment assignment = buildDefaultAssignment(postOffice, manager, tenantId, today);
        PostOfficeStaffAssignment savedAssignment = postOfficeStaffAssignmentRepository.save(assignment);
        return PostOfficeStaffMapper.toAssignmentResponse(savedAssignment);
    }

    private PostOfficeStaffAssignment buildDefaultAssignment(
            PostOffice postOffice,
            PostOfficeStaff staff,
            Long tenantId,
            LocalDate assignedFrom
    ) {
        return PostOfficeStaffAssignment.builder()
                .postOffice(postOffice)
                .staff(staff)
                .assignedFrom(assignedFrom)
                .isPrimary(Boolean.TRUE)
                .tenantId(tenantId)
                .build();
    }

    private PostOfficeStaff getPostOfficeStaffByIdAndTenantOrThrow(Long id, Long tenantId) {
        return postOfficeStaffRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_STAFF_NOT_FOUND));
    }

    private PostOffice getPostOfficeByIdAndTenantOrThrow(Long id, Long tenantId) {
        return postOfficeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
    }

    private void validateStaffRole(PostOfficeStaff staff, PostOfficeStaffRole expectedRole) {
        if (!expectedRole.equals(staff.getRole())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateStaffActive(PostOfficeStaff staff) {
        if (!PostOfficeStaffStatus.ACTIVE.equals(staff.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateManagerCanAccessPostOffice(Long postOfficeId, Set<Long> managedPostOfficeIds) {
        if (postOfficeId == null || managedPostOfficeIds == null || !managedPostOfficeIds.contains(postOfficeId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateManagerCanAccessStaff(Long postOfficeStaffId, Long tenantId, Set<Long> managedPostOfficeIds) {
        if (postOfficeStaffId == null
                || managedPostOfficeIds == null
                || managedPostOfficeIds.isEmpty()
                || !isStaffAssignedToAnyManagedPostOffice(postOfficeStaffId, tenantId, managedPostOfficeIds)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private boolean isStaffAssignedToAnyManagedPostOffice(
            Long postOfficeStaffId,
            Long tenantId,
            Set<Long> managedPostOfficeIds
    ) {
        if (managedPostOfficeIds == null || managedPostOfficeIds.isEmpty()) {
            return false;
        }

        return postOfficeStaffAssignmentRepository.existsActiveAssignmentByStaffIdAndPostOfficeIdsAndTenantId(
                postOfficeStaffId,
                managedPostOfficeIds,
                tenantId,
                LocalDate.now()
        );
    }

    private Set<Long> getManagedPostOfficeIdsOrThrow(Long tenantId) {
        Long currentUserId = authUtils.getCurrentUserId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        String managerCode = buildStaffCode(currentUserId, PostOfficeStaffRole.MANAGER);

        PostOfficeStaff managerStaff = postOfficeStaffRepository.findByCodeAndTenantId(managerCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        return postOfficeStaffAssignmentRepository.findActivePostOfficeIdsByStaffIdAndTenantId(
                managerStaff.getId(),
                tenantId,
                LocalDate.now()
        );
    }

    private String buildStaffCode(Long userId, PostOfficeStaffRole role) {
        return "USR_" + userId + "_" + role.name();
    }

    private Long getCurrentTenantIdOrThrow() {
        return authUtils.getCurrentTenantId().orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    private boolean isManagerScopedAccess() {
        return isPostOfficerManager() && !isAdmin();
    }

    private boolean isAdmin() {
        return authUtils.hasAnyRole(ROLE_TMS_ADMIN);
    }

    private boolean isPostOfficerManager() {
        return authUtils.hasAnyRole(ROLE_TMS_POSTOFFICER_MANAGER);
    }

    private Integer normalizeNonNegative(Integer value) {
        if (value == null) {
            return null;
        }
        if (value < 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return value;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}