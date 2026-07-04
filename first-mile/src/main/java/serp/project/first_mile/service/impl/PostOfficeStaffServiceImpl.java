/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeStaffAssignmentRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeStaffRequest;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.dto.response.PostOfficeStaffAssignmentResponse;
import serp.project.first_mile.dto.response.PostOfficeStaffResponse;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.first_mile.mapper.PostOfficeStaffMapper;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.PostOfficeStaffService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostOfficeStaffServiceImpl implements PostOfficeStaffService {
    private static final String STORAGE_SERVICE_NAME = "first-mile";

    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final FileStorageService fileStorageService;
    private final FirstMileAccessUtils firstMileAccessUtils;

    @Override
    @Transactional(readOnly = true)
    public PostOfficeStaffResponse getPostOfficeStaffById(Long id) {
        Long tenantId = getCurrentTenantIdOrThrow();
        PostOfficeStaff staff = getPostOfficeStaffByIdAndTenantOrThrow(id, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessStaff(staff.getId(), tenantId, managedPostOfficeIds);
        }

        return PostOfficeStaffMapper.toResponse(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostOfficeStaffResponse> getAssignableStaffByRole(PostOfficeStaffRole role, String keyword) {
        Long tenantId = getCurrentTenantIdOrThrow();
        if (role == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String keywordLike = buildKeywordLike(keyword);
        List<PostOfficeStaff> staffs = postOfficeStaffRepository.findAssignableByTenantIdAndRoleAndStatusAndKeyword(
                tenantId,
                role,
                PostOfficeStaffStatus.ACTIVE,
                keywordLike,
                LocalDate.now()
        );
        return staffs.stream().map(PostOfficeStaffMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostOfficeStaffResponse> getActiveCouriersByPostOffice(Long postOfficeId) {
        Long tenantId = getCurrentTenantIdOrThrow();
        getPostOfficeByIdAndTenantOrThrow(postOfficeId, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessPostOffice(postOfficeId, managedPostOfficeIds);
        }

        LocalDate today = LocalDate.now();
        List<PostOfficeStaffAssignment> assignments =
                postOfficeStaffAssignmentRepository.findActiveAssignmentsByPostOfficeIdAndTenantIdAndStaffRoleAndStaffStatus(
                        postOfficeId,
                        tenantId,
                        today,
                        PostOfficeStaffRole.COURIER,
                        PostOfficeStaffStatus.ACTIVE
                );

        Map<Long, PostOfficeStaffResponse> uniqueCouriers = new LinkedHashMap<>();
        for (PostOfficeStaffAssignment assignment : assignments) {
            PostOfficeStaff staff = assignment.getStaff();
            if (staff == null || staff.getId() == null) {
                continue;
            }

            uniqueCouriers.putIfAbsent(staff.getId(), PostOfficeStaffMapper.toResponse(staff));
        }

        return uniqueCouriers.values().stream()
                .sorted(Comparator.comparing(
                        PostOfficeStaffResponse::fullName,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .toList();
    }

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

        closeOtherActiveAssignmentsForStaff(courier.getId(), postOffice.getId(), tenantId, today);
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

        closeOtherActiveAssignmentsForStaff(manager.getId(), postOffice.getId(), tenantId, today);
        PostOfficeStaffAssignment assignment = buildDefaultAssignment(postOffice, manager, tenantId, today);
        PostOfficeStaffAssignment savedAssignment = postOfficeStaffAssignmentRepository.save(assignment);
        return PostOfficeStaffMapper.toAssignmentResponse(savedAssignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeStaffAssignmentResponse updateCourierAssignmentDetails(
            Long assignmentId,
            UpdatePostOfficeStaffAssignmentRequest request
    ) {
        Long tenantId = getCurrentTenantIdOrThrow();
        PostOfficeStaffAssignment assignment = getAssignmentByIdAndTenantOrThrow(assignmentId, tenantId);
        validateAssignmentBelongsToCourier(assignment);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            Long postOfficeId = assignment.getPostOffice() != null ? assignment.getPostOffice().getId() : null;
            validateManagerCanAccessPostOffice(postOfficeId, managedPostOfficeIds);
        }

        applyAssignmentDetailUpdate(assignment, request);

        PostOfficeStaffAssignment updatedAssignment = postOfficeStaffAssignmentRepository.save(assignment);
        return PostOfficeStaffMapper.toAssignmentResponse(updatedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostOfficeStaffAssignmentResponse> getActiveAssignmentsByPostOffice(
            Long postOfficeId,
            PostOfficeStaffRole role
    ) {
        Long tenantId = getCurrentTenantIdOrThrow();
        getPostOfficeByIdAndTenantOrThrow(postOfficeId, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessPostOffice(postOfficeId, managedPostOfficeIds);
        }

        LocalDate today = LocalDate.now();
        List<PostOfficeStaffAssignment> assignments = role == null
                ? postOfficeStaffAssignmentRepository.findActiveAssignmentsByPostOfficeIdAndTenantId(
                        postOfficeId,
                        tenantId,
                        today
                )
                : postOfficeStaffAssignmentRepository.findActiveAssignmentsByPostOfficeIdAndTenantIdAndStaffRole(
                        postOfficeId,
                        tenantId,
                        today,
                        role
                );
        return assignments.stream().map(PostOfficeStaffMapper::toAssignmentResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeStaffAssignmentResponse unassignStaffFromPostOffice(Long assignmentId) {
        Long tenantId = getCurrentTenantIdOrThrow();
        PostOfficeStaffAssignment assignment = getAssignmentByIdAndTenantOrThrow(assignmentId, tenantId);

        Long postOfficeId = assignment.getPostOffice() == null ? null : assignment.getPostOffice().getId();
        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessPostOffice(postOfficeId, managedPostOfficeIds);
        }

        LocalDate today = LocalDate.now();
        if (assignment.getAssignedFrom() != null && assignment.getAssignedFrom().isEqual(today)) {
            postOfficeStaffAssignmentRepository.delete(assignment);
            return PostOfficeStaffMapper.toAssignmentResponse(assignment);
        }

        assignment.setAssignedTo(today.minusDays(1));
        PostOfficeStaffAssignment updatedAssignment = postOfficeStaffAssignmentRepository.save(assignment);
        return PostOfficeStaffMapper.toAssignmentResponse(updatedAssignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeStaffResponse uploadAvatar(Long id, MultipartFile file) {
        Long tenantId = getCurrentTenantIdOrThrow();
        PostOfficeStaff staff = getPostOfficeStaffByIdAndTenantOrThrow(id, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessStaff(staff.getId(), tenantId, managedPostOfficeIds);
        }

        validateStaffRoleForAvatar(staff);

        String folder = staff.getRole() == PostOfficeStaffRole.MANAGER
                ? "manager-avatar"
                : "courier-avatar";

        FileUploadResponse uploadResponse = uploadImageFile(file, tenantId, folder);
        staff.setAvatarUrl(uploadResponse.getUrl());

        PostOfficeStaff updatedStaff = postOfficeStaffRepository.save(staff);
        return PostOfficeStaffMapper.toResponse(updatedStaff);
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

    private void closeOtherActiveAssignmentsForStaff(
            Long staffId,
            Long targetPostOfficeId,
            Long tenantId,
            LocalDate today
    ) {
        List<PostOfficeStaffAssignment> activeAssignments = postOfficeStaffAssignmentRepository
                .findActiveAssignmentsByStaffIdAndTenantId(staffId, tenantId, today);
        if (activeAssignments.isEmpty()) {
            return;
        }

        List<PostOfficeStaffAssignment> assignmentsToUpdate = new ArrayList<>();
        List<PostOfficeStaffAssignment> assignmentsToDelete = new ArrayList<>();

        for (PostOfficeStaffAssignment activeAssignment : activeAssignments) {
            Long activePostOfficeId = activeAssignment.getPostOffice() == null
                    ? null
                    : activeAssignment.getPostOffice().getId();
            if (targetPostOfficeId != null && targetPostOfficeId.equals(activePostOfficeId)) {
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
            postOfficeStaffAssignmentRepository.saveAll(assignmentsToUpdate);
        }
        if (!assignmentsToDelete.isEmpty()) {
            postOfficeStaffAssignmentRepository.deleteAll(assignmentsToDelete);
        }
    }

    private PostOfficeStaff getPostOfficeStaffByIdAndTenantOrThrow(Long id, Long tenantId) {
        return postOfficeStaffRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_STAFF_NOT_FOUND));
    }

    private PostOffice getPostOfficeByIdAndTenantOrThrow(Long id, Long tenantId) {
        return postOfficeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
    }

    private PostOfficeStaffAssignment getAssignmentByIdAndTenantOrThrow(Long assignmentId, Long tenantId) {
        return postOfficeStaffAssignmentRepository.findByIdAndTenantId(assignmentId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_STAFF_ASSIGNMENT_NOT_FOUND));
    }

    private void validateAssignmentBelongsToCourier(PostOfficeStaffAssignment assignment) {
        if (assignment == null
                || assignment.getStaff() == null
                || !PostOfficeStaffRole.COURIER.equals(assignment.getStaff().getRole())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void applyAssignmentDetailUpdate(
            PostOfficeStaffAssignment assignment,
            UpdatePostOfficeStaffAssignmentRequest request
    ) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (request.getAssignedFrom() != null) {
            assignment.setAssignedFrom(request.getAssignedFrom());
        }

        if (request.getAssignedTo() != null) {
            assignment.setAssignedTo(request.getAssignedTo());
        }

        LocalDate effectiveAssignedFrom = assignment.getAssignedFrom();
        LocalDate effectiveAssignedTo = assignment.getAssignedTo();
        if (effectiveAssignedFrom == null
                || (effectiveAssignedTo != null && effectiveAssignedTo.isBefore(effectiveAssignedFrom))) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (request.getIsPrimary() != null) {
            assignment.setIsPrimary(request.getIsPrimary());
        }

        if (request.getNotes() != null) {
            assignment.setNotes(normalizeText(request.getNotes()));
        }
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
        return firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
    }

    private void validateStaffRoleForAvatar(PostOfficeStaff staff) {
        if (staff.getRole() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!PostOfficeStaffRole.COURIER.equals(staff.getRole())
                && !PostOfficeStaffRole.MANAGER.equals(staff.getRole())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private FileUploadResponse uploadImageFile(MultipartFile file, Long tenantId, String folder) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }

        String contentType = ImageContentTypeUtils.normalizeImageContentType(file.getContentType());

        try {
            return fileStorageService.upload(FileUploadRequest.builder()
                    .content(file.getBytes())
                    .originalFileName(file.getOriginalFilename())
                    .contentType(contentType)
                    .serviceName(STORAGE_SERVICE_NAME)
                    .folder(folder)
                    .tenantId(tenantId)
                    .uploaderId(firstMileAccessUtils.getCurrentUserIdOrNull())
                    .publicFile(true)
                    .build());
        } catch (IOException exception) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private Long getCurrentTenantIdOrThrow() {
        return firstMileAccessUtils.getCurrentTenantIdOrThrow();
    }

    private boolean isManagerScopedAccess() {
        return firstMileAccessUtils.isManagerScopedAccess();
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

    private String buildKeywordLike(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : "%" + normalized.toLowerCase() + "%";
    }
}
