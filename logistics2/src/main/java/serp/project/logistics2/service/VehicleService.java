package serp.project.logistics2.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.constant.VehicleStatus;
import serp.project.logistics2.dto.request.VehicleCreationForm;
import serp.project.logistics2.dto.request.VehicleUpdateForm;
import serp.project.logistics2.entity.VehicleEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.VehicleRepository;
import serp.project.logistics2.util.PaginationUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createVehicle(VehicleCreationForm form, Long tenantId) {
        VehicleEntity vehicle = VehicleEntity.create(form.getLicensePlate(), form.getVehicleType(),
                form.getMaxWeightKg(),
                form.getMaxVolumeCbm(), tenantId);
        vehicleRepository.save(vehicle);
        log.info("Created new vehicle with ID: {}", vehicle.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateVehicle(String vehicleId, VehicleUpdateForm form, Long tenantId) {
        VehicleEntity vehicle = vehicleRepository.findByIdAndTenantIdWithLock(vehicleId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        vehicle.update(form.getLicensePlate(), form.getVehicleType(), form.getMaxWeightKg(), form.getMaxVolumeCbm());
        vehicleRepository.save(vehicle);
        log.info("Updated vehicle with ID: {}", vehicleId);
    }

    public VehicleEntity getVehicleById(String vehicleId, Long tenantId) {
        return vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateVehicleStatus(String vehicleId, String status, Long tenantId) {
        VehicleEntity vehicle = vehicleRepository.findByIdAndTenantIdWithLock(vehicleId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        vehicle.updateStatus(status);
        vehicleRepository.save(vehicle);
        log.info("Updated status of vehicle with ID: {} to {}", vehicleId, status);
    }

    public Page<VehicleEntity> searchVehicles(
            String query,
            String vehicleType,
            String vehicleStatus,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return vehicleRepository.search(query, vehicleType, vehicleStatus, tenantId, pageable);
    }

    public Page<VehicleEntity> searchVehiclesForUsage(
            String query,
            String vehicleType,
            LocalDate workingDate,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return vehicleRepository.searchForUsage(query, vehicleType, workingDate, tenantId, pageable);
    }

    public void activateVehicle(String vehicleId, Long tenantId) {
        updateVehicleStatus(vehicleId, VehicleStatus.IN_USE.name(), tenantId);
    }

    public void deactivateVehicle(String vehicleId, Long tenantId) {
        updateVehicleStatus(vehicleId, VehicleStatus.MAINTENANCE.name(), tenantId);
    }

}
