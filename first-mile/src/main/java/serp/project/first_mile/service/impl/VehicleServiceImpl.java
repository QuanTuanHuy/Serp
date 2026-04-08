/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.Vehicle;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreateVehicleRequest;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.request.UpdateVehicleRequest;
import serp.project.first_mile.dto.request.VehicleImportDTO;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.dto.response.VehicleResponse;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.mapper.VehicleMapper;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.VehicleRepository;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.VehicleImportExcelService;
import serp.project.first_mile.service.VehicleService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private static final String ROLE_TMS_ADMIN = "TMS_ADMIN";
    private static final String ROLE_TMS_POSTOFFICER_MANAGER = "TMS_POSTOFFICER_MANAGER";
    private static final String STORAGE_SERVICE_NAME = "first-mile";
    private static final String VEHICLE_IMAGE_FOLDER = "vehicle-image";

    private final VehicleRepository vehicleRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;
    private final FileStorageService fileStorageService;
    private final VehicleImportExcelService vehicleImportExcelService;
    private final AuthUtils authUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getVehicles(int page, int size, String keyword) {
        Long tenantId = getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String normalizedKeyword = normalizeKeyword(keyword);

        Page<VehicleResponse> mappedPage;
        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            if (managedPostOfficeIds.isEmpty()) {
                return emptyPageResponse(page, size);
            }

            mappedPage = vehicleRepository.searchByTenantIdAndManagedPostOfficeIds(
                            tenantId,
                            normalizedKeyword,
                            managedPostOfficeIds,
                            LocalDate.now(),
                            pageable
                    )
                    .map(VehicleMapper::toResponse);
        } else {
            mappedPage = vehicleRepository.searchByTenantId(tenantId, normalizedKeyword, pageable)
                    .map(VehicleMapper::toResponse);
        }

        return toPageResponse(mappedPage);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Long tenantId = getCurrentTenantIdOrThrow();
        Vehicle vehicle = getVehicleByIdAndTenantOrThrow(id, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessVehicle(vehicle, tenantId, managedPostOfficeIds);
        }

        return VehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTemplate() {
        return vehicleImportExcelService.exportTemplate();
    }

    @Override
    public ValidateImportFileDTO<VehicleImportDTO> validateImportFile(MultipartFile file, Long tenantId) {
        return vehicleImportExcelService.validateImportFile(file, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportHistoryResponse importVehiclesAsync(MultipartFile file, Long tenantId) {
        return vehicleImportExcelService.importVehiclesAsync(file, tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        Long tenantId = getCurrentTenantIdOrThrow();
        String normalizedLicensePlate = normalizeLicensePlate(request.getLicensePlate());

        if (vehicleRepository.existsByLicensePlateIgnoreCaseAndTenantId(normalizedLicensePlate, tenantId)) {
            throw new AppException(ErrorCode.VEHICLE_LICENSE_PLATE_EXISTED);
        }

        PostOffice postOffice = resolvePostOfficeOrNull(request.getPostOfficeId(), tenantId);
        Long postOfficeStaffId = resolveCourierIdOrNull(request.getPostOfficeStaffId(), tenantId);

        if (postOffice != null && postOfficeStaffId != null) {
            validateCourierAssignedToPostOffice(postOfficeStaffId, postOffice.getId(), tenantId);
        }

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessRequestedOwnership(postOffice, postOfficeStaffId, tenantId, managedPostOfficeIds);
        }

        Vehicle vehicle = VehicleMapper.toEntity(request, postOffice, postOfficeStaffId);
        vehicle.setLicensePlate(normalizedLicensePlate);
        vehicle.setTenantId(tenantId);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(savedVehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        Long tenantId = getCurrentTenantIdOrThrow();
        Vehicle vehicle = getVehicleByIdAndTenantOrThrow(id, tenantId);

        Set<Long> managedPostOfficeIds = null;
        if (isManagerScopedAccess()) {
            managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessVehicle(vehicle, tenantId, managedPostOfficeIds);
        }

        String normalizedLicensePlate = normalizeLicensePlate(request.getLicensePlate());

        if (vehicleRepository.existsByLicensePlateIgnoreCaseAndTenantIdAndIdNot(normalizedLicensePlate, tenantId, id)) {
            throw new AppException(ErrorCode.VEHICLE_LICENSE_PLATE_EXISTED);
        }

        PostOffice postOffice = resolvePostOfficeOrNull(request.getPostOfficeId(), tenantId);
        Long postOfficeStaffId = resolveCourierIdOrNull(request.getPostOfficeStaffId(), tenantId);

        if (postOffice != null && postOfficeStaffId != null) {
            validateCourierAssignedToPostOffice(postOfficeStaffId, postOffice.getId(), tenantId);
        }

        if (managedPostOfficeIds != null) {
            validateManagerCanAccessRequestedOwnership(postOffice, postOfficeStaffId, tenantId, managedPostOfficeIds);
        }

        VehicleMapper.mapForUpdate(request, vehicle, postOffice, postOfficeStaffId);
        vehicle.setLicensePlate(normalizedLicensePlate);
        vehicle.setTenantId(tenantId);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(updatedVehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse uploadImage(Long id, MultipartFile file) {
        Long tenantId = getCurrentTenantIdOrThrow();
        Vehicle vehicle = getVehicleByIdAndTenantOrThrow(id, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessVehicle(vehicle, tenantId, managedPostOfficeIds);
        }

        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }

        String contentType = normalizeImageContentType(file.getContentType());

        try {
            FileUploadResponse uploadResponse = fileStorageService.upload(FileUploadRequest.builder()
                    .content(file.getBytes())
                    .originalFileName(file.getOriginalFilename())
                    .contentType(contentType)
                    .serviceName(STORAGE_SERVICE_NAME)
                    .folder(VEHICLE_IMAGE_FOLDER)
                    .tenantId(vehicle.getTenantId())
                    .uploaderId(authUtils.getCurrentUserId().orElse(null))
                    .publicFile(true)
                    .build());

            vehicle.setImageUrl(uploadResponse.getUrl());
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return VehicleMapper.toResponse(updatedVehicle);
        } catch (IOException exception) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVehicle(Long id) {
        Long tenantId = getCurrentTenantIdOrThrow();
        Vehicle vehicle = getVehicleByIdAndTenantOrThrow(id, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessVehicle(vehicle, tenantId, managedPostOfficeIds);
        }

        vehicleRepository.delete(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse assignVehicleToPostOffice(Long id, Long postOfficeId) {
        Long tenantId = getCurrentTenantIdOrThrow();
        Vehicle vehicle = getVehicleByIdAndTenantOrThrow(id, tenantId);

        if (isManagerScopedAccess()) {
            Set<Long> managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessVehicle(vehicle, tenantId, managedPostOfficeIds);
            validateManagerCanAccessPostOffice(postOfficeId, managedPostOfficeIds);
        }

        PostOffice postOffice = getPostOfficeByIdAndTenantOrThrow(postOfficeId, tenantId);

        if (vehicle.getPostOfficeStaffId() != null) {
            validateCourierAssignedToPostOffice(vehicle.getPostOfficeStaffId(), postOffice.getId(), tenantId);
        }

        vehicle.setPostOffice(postOffice);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(updatedVehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse assignVehicleToCourier(Long id, Long postOfficeStaffId) {
        Long tenantId = getCurrentTenantIdOrThrow();
        Vehicle vehicle = getVehicleByIdAndTenantOrThrow(id, tenantId);

        Set<Long> managedPostOfficeIds = null;
        if (isManagerScopedAccess()) {
            managedPostOfficeIds = getManagedPostOfficeIdsOrThrow(tenantId);
            validateManagerCanAccessVehicle(vehicle, tenantId, managedPostOfficeIds);
        }

        Long courierId = resolveCourierIdOrNull(postOfficeStaffId, tenantId);

        if (managedPostOfficeIds != null) {
            validateManagerCanAccessCourier(courierId, tenantId, managedPostOfficeIds);
        }

        if (vehicle.getPostOffice() != null) {
            validateCourierAssignedToPostOffice(courierId, vehicle.getPostOffice().getId(), tenantId);
        }

        vehicle.setPostOfficeStaffId(courierId);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(updatedVehicle);
    }

    private Vehicle getVehicleByIdAndTenantOrThrow(Long id, Long tenantId) {
        return vehicleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    private PostOffice getPostOfficeByIdAndTenantOrThrow(Long postOfficeId, Long tenantId) {
        return postOfficeRepository.findByIdAndTenantId(postOfficeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
    }

    private PostOfficeStaff getPostOfficeStaffByIdAndTenantOrThrow(Long postOfficeStaffId, Long tenantId) {
        return postOfficeStaffRepository.findByIdAndTenantId(postOfficeStaffId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_STAFF_NOT_FOUND));
    }

    private PageResponse<VehicleResponse> toPageResponse(Page<VehicleResponse> mappedPage) {
        return PageResponse.<VehicleResponse>builder()
                .items(mappedPage.getContent())
                .page(mappedPage.getNumber())
                .size(mappedPage.getSize())
                .totalElements(mappedPage.getTotalElements())
                .totalPages(mappedPage.getTotalPages())
                .hasNext(mappedPage.hasNext())
                .hasPrevious(mappedPage.hasPrevious())
                .build();
    }

    private PageResponse<VehicleResponse> emptyPageResponse(int page, int size) {
        return PageResponse.<VehicleResponse>builder()
                .items(List.of())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    private void validateManagerCanAccessVehicle(Vehicle vehicle, Long tenantId, Set<Long> managedPostOfficeIds) {
        if (managedPostOfficeIds == null || managedPostOfficeIds.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (vehicle.getPostOffice() != null && managedPostOfficeIds.contains(vehicle.getPostOffice().getId())) {
            return;
        }

        if (vehicle.getPostOfficeStaffId() != null
                && isStaffAssignedToAnyManagedPostOffice(vehicle.getPostOfficeStaffId(), tenantId, managedPostOfficeIds)) {
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateManagerCanAccessRequestedOwnership(
            PostOffice postOffice,
            Long postOfficeStaffId,
            Long tenantId,
            Set<Long> managedPostOfficeIds
    ) {
        if (postOffice == null && postOfficeStaffId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (postOffice != null) {
            validateManagerCanAccessPostOffice(postOffice.getId(), managedPostOfficeIds);
        }

        if (postOfficeStaffId != null) {
            validateManagerCanAccessCourier(postOfficeStaffId, tenantId, managedPostOfficeIds);
        }
    }

    private void validateManagerCanAccessPostOffice(Long postOfficeId, Set<Long> managedPostOfficeIds) {
        if (postOfficeId == null || managedPostOfficeIds == null || !managedPostOfficeIds.contains(postOfficeId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateManagerCanAccessCourier(Long postOfficeStaffId, Long tenantId, Set<Long> managedPostOfficeIds) {
        if (postOfficeStaffId == null
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

    private void validateCourierAssignedToPostOffice(Long courierId, Long postOfficeId, Long tenantId) {
        boolean isAssigned = postOfficeStaffAssignmentRepository.existsActiveAssignmentByStaffIdAndPostOfficeIdAndTenantId(
                courierId,
                postOfficeId,
                tenantId,
                LocalDate.now()
        );
        if (!isAssigned) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
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

    private PostOffice resolvePostOfficeOrNull(Long postOfficeId, Long tenantId) {
        if (postOfficeId == null) {
            return null;
        }
        return getPostOfficeByIdAndTenantOrThrow(postOfficeId, tenantId);
    }

    private Long resolveCourierIdOrNull(Long postOfficeStaffId, Long tenantId) {
        if (postOfficeStaffId == null) {
            return null;
        }

        PostOfficeStaff staff = getPostOfficeStaffByIdAndTenantOrThrow(postOfficeStaffId, tenantId);
        if (!PostOfficeStaffRole.COURIER.equals(staff.getRole())) {
            throw new AppException(ErrorCode.VEHICLE_OWNER_MUST_BE_COURIER);
        }
        if (!PostOfficeStaffStatus.ACTIVE.equals(staff.getStatus())) {
            throw new AppException(ErrorCode.VEHICLE_OWNER_STAFF_NOT_ACTIVE);
        }

        return staff.getId();
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

    private String normalizeImageContentType(String contentType) {
        String normalized = contentType == null
                ? ""
                : contentType.trim().toLowerCase(Locale.ROOT);

        if (!normalized.startsWith("image/")) {
            throw new AppException(ErrorCode.FILE_IMAGE_TYPE_INVALID);
        }
        return normalized;
    }

    private String normalizeLicensePlate(String licensePlate) {
        if (licensePlate == null) {
            return null;
        }
        return licensePlate.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }
}
