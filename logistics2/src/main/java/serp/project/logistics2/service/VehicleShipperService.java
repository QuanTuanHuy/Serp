package serp.project.logistics2.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.constant.VehicleShipperStatus;
import serp.project.logistics2.dto.request.VehicleShipperAssignmentForm;
import serp.project.logistics2.entity.VehicleEntity;
import serp.project.logistics2.entity.VehicleShipperEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.VehicleRepository;
import serp.project.logistics2.repository.VehicleShipperRepository;
import serp.project.logistics2.util.PaginationUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleShipperService {

    private final VehicleShipperRepository vehicleShipperRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional(rollbackFor = Exception.class)
    public void assignVehicleToShipper(VehicleShipperAssignmentForm form, Long shipperId, Long tenantId) {
        if(vehicleShipperRepository.existsByVehicleIdAndWorkingDateAndStatusAndTenantId(
                form.getVehicleId(), form.getWorkingDate(), VehicleShipperStatus.ACTIVE.name(), tenantId)) {
            log.info("Vehicle {} is already assigned for date {} in tenant {}", form.getVehicleId(),
                    form.getWorkingDate(), tenantId);
            throw new AppException(AppErrorCode.VEHICLE_ALREADY_ASSIGNED);
        }
        VehicleShipperEntity vehicleShipper = VehicleShipperEntity.create(
                form.getVehicleId(),
                shipperId,
                form.getWorkingDate(),
                tenantId);
        vehicleShipperRepository.save(vehicleShipper);
        log.info("Assigned vehicle {} to shipper {} for date {}", form.getVehicleId(), shipperId,
                form.getWorkingDate());
    }

    @Transactional(rollbackFor = Exception.class)
    public void requestCancelVehicleAssignment(String vehicleShipperId, Long userId, Long tenantId) {
        VehicleShipperEntity vehicleShipper = vehicleShipperRepository.findByIdAndTenantId(vehicleShipperId, tenantId)
                .orElse(null);
        if (vehicleShipper == null || !vehicleShipper.getShipperId().equals(userId)) {
            log.info("VehicleShipper with ID {} not found or not owned by user {} for tenant {}", vehicleShipperId,
                    userId, tenantId);
            throw new AppException(AppErrorCode.VEHICLE_SHIPPER_NOT_FOUND);
        }
        vehicleShipper.setStatus(VehicleShipperStatus.INACTIVATE_REQUESTED.name());
        vehicleShipperRepository.save(vehicleShipper);
        log.info("Requested cancellation of vehicle assignment with ID {} for tenant {}", vehicleShipperId, tenantId);
    }

    public void cancelVehicleAssignment(String vehicleShipperId, Long tenantId) {
        VehicleShipperEntity vehicleShipper = vehicleShipperRepository.findByIdAndTenantId(vehicleShipperId, tenantId)
                .orElse(null);
        if (vehicleShipper == null) {
            log.warn("VehicleShipper with ID {} not found for tenant {}", vehicleShipperId, tenantId);
            return;
        }
        vehicleShipper.setStatus(VehicleShipperStatus.CANCELED.name());
        vehicleShipperRepository.save(vehicleShipper);
        log.info("Cancelled vehicle assignment with ID {} for tenant {}", vehicleShipperId, tenantId);
    }

    public VehicleShipperEntity getDetailedVehicleShipper(String vehicleShipperId, Long tenantId) {
        VehicleShipperEntity vehicleShipper = vehicleShipperRepository.findByIdAndTenantId(vehicleShipperId, tenantId)
                .orElse(null);
        if (vehicleShipper == null) {
            log.info("VehicleShipper with ID {} not found for tenant {}", vehicleShipperId, tenantId);
            return null;
        }
        vehicleShipper.setVehicle(vehicleRepository.findByIdAndTenantId(vehicleShipper.getVehicleId(), tenantId)
                .orElse(null));
        return vehicleShipper;
    }

    public Page<VehicleShipperEntity> searchVehicleShippers(
            Long shipperId,
            String vehicleId,
            LocalDate workingDate,
            Long tenantId,
            int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        Page<VehicleShipperEntity> vehicleShippers = vehicleShipperRepository.search(
                shipperId, vehicleId, workingDate, tenantId, pageable);
        List<String> vehicleIds = vehicleShippers.stream()
                .map(VehicleShipperEntity::getVehicleId)
                .distinct()
                .collect(Collectors.toList());
        List<VehicleEntity> vehicles = vehicleRepository.findByIdInAndTenantId(vehicleIds, tenantId);
        Map<String, VehicleEntity> vehicleMap = vehicles.stream()
                .collect(Collectors.toMap(VehicleEntity::getId, v -> v));
        vehicleShippers.forEach(vs -> vs.setVehicle(vehicleMap.get(vs.getVehicleId())));
        return vehicleShippers;
    }
}
