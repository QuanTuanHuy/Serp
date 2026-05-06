/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.CreateVehicleRequest;
import serp.project.second_mile.dto.request.UpdateVehicleRequest;
import serp.project.second_mile.dto.request.VehicleFilterRequest;
import serp.project.second_mile.dto.response.VehicleResponse;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.mapper.VehicleMapper;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.specification.VehicleSpecification;
import serp.project.second_mile.service.FileStorageService;
import serp.project.second_mile.service.VehicleService;
import serp.project.second_mile.service.dto.request.FileUploadRequest;
import serp.project.second_mile.service.dto.response.FileUploadResponse;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private static final String STORAGE_SERVICE_NAME = "second-mile";
    private static final String VEHICLE_IMAGE_FOLDER = "vehicle-image";

    private final VehicleRepository vehicleRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getVehicles(int page, int size, VehicleFilterRequest filterRequest) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        VehicleFilterRequest normalizedFilterRequest = normalizeFilterRequest(filterRequest);

        Page<Vehicle> vehiclePage = vehicleRepository.findAll(
                VehicleSpecification.byFilter(tenantId, normalizedFilterRequest),
                pageable
        );

        Page<VehicleResponse> mappedPage = vehiclePage.map(VehicleMapper::toResponse);

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

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = getVehicleOrThrow(id);
        validateTenantAccess(vehicle);
        return VehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        String normalizedLicensePlate = normalizeText(request.getLicensePlate());

        if (normalizedLicensePlate == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (vehicleRepository.existsByTenantIdAndLicensePlateIgnoreCase(tenantId, normalizedLicensePlate)) {
            throw new AppException(ErrorCode.VEHICLE_LICENSE_PLATE_EXISTED);
        }

        Vehicle vehicle = VehicleMapper.toEntity(request);
        vehicle.setLicensePlate(normalizedLicensePlate);
        vehicle.setTenantId(tenantId);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(savedVehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        Vehicle vehicle = getVehicleOrThrow(id);
        validateTenantAccess(vehicle);

        String normalizedLicensePlate = normalizeText(request.getLicensePlate());
        if (normalizedLicensePlate == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!vehicle.getLicensePlate().equalsIgnoreCase(normalizedLicensePlate)
                && vehicleRepository.existsByTenantIdAndLicensePlateIgnoreCase(tenantId, normalizedLicensePlate)) {
            throw new AppException(ErrorCode.VEHICLE_LICENSE_PLATE_EXISTED);
        }

        VehicleMapper.mapForUpdate(request, vehicle);
        vehicle.setLicensePlate(normalizedLicensePlate);
        vehicle.setTenantId(tenantId);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(updatedVehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse uploadImage(Long id, MultipartFile file) {
        Vehicle vehicle = getVehicleOrThrow(id);
        validateTenantAccess(vehicle);

        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }

        String contentType = ImageContentTypeUtils.normalizeImageContentType(file.getContentType());

        try {
            FileUploadResponse uploadResponse = fileStorageService.upload(FileUploadRequest.builder()
                    .content(file.getBytes())
                    .originalFileName(file.getOriginalFilename())
                    .contentType(contentType)
                    .serviceName(STORAGE_SERVICE_NAME)
                    .folder(VEHICLE_IMAGE_FOLDER)
                    .tenantId(vehicle.getTenantId())
                    .uploaderId(secondMileAccessUtils.getCurrentUserIdOrNull())
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
        Vehicle vehicle = getVehicleOrThrow(id);
        validateTenantAccess(vehicle);
        vehicleRepository.delete(vehicle);
    }

    private Vehicle getVehicleOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    private boolean isCurrentTenant(Vehicle vehicle) {
        Long currentTenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        return vehicle.getTenantId() != null && vehicle.getTenantId().equals(currentTenantId);
    }

    private void validateTenantAccess(Vehicle vehicle) {
        if (!isCurrentTenant(vehicle)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private VehicleFilterRequest normalizeFilterRequest(VehicleFilterRequest filterRequest) {
        if (filterRequest == null) {
            return VehicleFilterRequest.builder().build();
        }

        return VehicleFilterRequest.builder()
                .keyword(normalizeText(filterRequest.getKeyword()))
                .licensePlate(normalizeText(filterRequest.getLicensePlate()))
                .vehicleType(filterRequest.getVehicleType())
                .hubId(filterRequest.getHubId())
                .assignedStaffId(filterRequest.getAssignedStaffId())
                .status(filterRequest.getStatus())
                .build();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}

