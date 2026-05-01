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
import serp.project.logistics2.constant.DeliverySlipStatus;
import serp.project.logistics2.constant.PlanOptimizationStatus;
import serp.project.logistics2.constant.VehicleShipperStatus;
import serp.project.logistics2.dto.message.RoutingRequest;
import serp.project.logistics2.dto.request.DeliveryPlanCreationForm;
import serp.project.logistics2.dto.request.DeliveryPlanUpdateForm;
import serp.project.logistics2.entity.AddressEntity;
import serp.project.logistics2.entity.DeliveryPlanEntity;
import serp.project.logistics2.entity.DeliverySlipEntity;
import serp.project.logistics2.entity.FacilityEntity;
import serp.project.logistics2.entity.VehicleEntity;
import serp.project.logistics2.entity.VehicleShipperEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.AddressRepository;
import serp.project.logistics2.repository.DeliveryPlanRepository;
import serp.project.logistics2.repository.DeliverySlipRepository;
import serp.project.logistics2.repository.FacilityRepository;
import serp.project.logistics2.repository.VehicleRepository;
import serp.project.logistics2.repository.VehicleShipperRepository;
import serp.project.logistics2.util.PaginationUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryPlanService {

    private final RoutingKafkaProducer routingKafkaProducer;

    private final DeliveryPlanRepository deliveryPlanRepository;

    private final DeliverySlipRepository deliverySlipRepository;
    private final VehicleShipperRepository vehicleShipperRepository;

    private final VehicleRepository vehicleRepository;
    private final FacilityRepository facilityRepository;
    private final AddressRepository addressRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createDeliveryPlan(DeliveryPlanCreationForm form, Long userId, Long tenantId) {
        List<DeliverySlipEntity> deliverySlips = deliverySlipRepository.findAllById(form.getDeliverySlipIds());
        if (deliverySlips.size() != form.getDeliverySlipIds().size()) {
            log.info("Some delivery slips have been missing");
            throw new AppException(AppErrorCode.NOT_FOUND);
        } else {
            deliverySlips.forEach(slip -> {
                if (!slip.getStatus().equals(DeliverySlipStatus.PENDING.name())) {
                    log.info("Delivery slip {} is not available in {}", slip.getId(), form.getDeliveryDate());
                    throw new AppException(AppErrorCode.DELIVERY_SLIP_NOT_AVAILABLE);
                }
            });
        }

        List<VehicleShipperEntity> vehicleShippers = vehicleShipperRepository.findAllById(form.getVehicleShipperIds());
        if (vehicleShippers.size() != form.getVehicleShipperIds().size()) {
            log.info("Some vehicle-shipper assignments have been missing");
            throw new AppException(AppErrorCode.NOT_FOUND);
        } else {
            vehicleShippers.forEach(vs -> {
                if (!vs.getStatus().equals(VehicleShipperStatus.ACTIVE.name())
                        || !vs.getWorkingDate().isEqual(form.getDeliveryDate())) {
                    log.info("Vehicle-shipper assignment {} is not available in {}", vs.getId(),
                            form.getDeliveryDate());
                    throw new AppException(AppErrorCode.VEHICLE_SHIPPER_NOT_AVAILABLE);
                }
            });
        }

        DeliveryPlanEntity deliveryPlan = DeliveryPlanEntity.create(
                form.getFacilityId(),
                form.getDeliveryDate(),
                userId,
                tenantId,
                deliverySlips,
                vehicleShippers);

        deliverySlipRepository.updateStatusByIdInAndTenantId(DeliverySlipStatus.ASSIGNED.name(),
                form.getDeliverySlipIds(), tenantId);
        log.info("Delivery slips updated to ASSIGNED status for delivery date: {}", form.getDeliveryDate());

        deliveryPlanRepository.save(deliveryPlan);
        log.info("Delivery plan is created successfully with id: {}", deliveryPlan.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDeliveryPlan(String deliveryPlanId, Long tenantId) {
        DeliveryPlanEntity plan = deliveryPlanRepository.findByIdAndTenantId(deliveryPlanId, tenantId)
                .orElseThrow(() -> {
                    log.info("Delivery plan {} is not found", deliveryPlanId);
                    return new AppException(AppErrorCode.NOT_FOUND);
                });
        if (PlanOptimizationStatus.valueOf(plan.getOptimizationStatus()).ordinal() >= PlanOptimizationStatus.OPTIMIZING
                .ordinal()) {
            log.info("Cannot delete delivery plan {} with optimization status {}", deliveryPlanId,
                    plan.getOptimizationStatus());
            throw new AppException(AppErrorCode.CANNOT_DELETE_DELIVERY_PLAN);
        }

        List<String> slipIds = plan.getSlips().stream().map(DeliverySlipEntity::getId).toList();
        deliverySlipRepository.updateStatusByIdInAndTenantId(DeliverySlipStatus.PENDING.name(), slipIds, tenantId);
        log.info("Delivery slips updated to PENDING status for delivery plan: {}", deliveryPlanId);

        deliveryPlanRepository.delete(plan);
        log.info("Delivery plan {} is deleted successfully", deliveryPlanId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void optimizeDeliveryPlan(String deliveryPlanId, Long tenantId) {
        DeliveryPlanEntity plan = deliveryPlanRepository.findByIdAndTenantId(deliveryPlanId, tenantId)
                .orElseThrow(() -> {
                    log.info("Delivery plan {} is not found", deliveryPlanId);
                    return new AppException(AppErrorCode.NOT_FOUND);
                });
        if (PlanOptimizationStatus.valueOf(plan.getOptimizationStatus()).ordinal() >= PlanOptimizationStatus.OPTIMIZING
                .ordinal()) {
            log.info("Delivery plan {} is already in optimization or optimized", deliveryPlanId);
            throw new AppException(AppErrorCode.PLAN_IN_OPTIMIZATION);
        }

        FacilityEntity facility = facilityRepository.findById(plan.getFacilityId())
                .orElseThrow(() -> {
                    log.info("Facility {} is not found", plan.getFacilityId());
                    return new AppException(AppErrorCode.NOT_FOUND);
                });
        AddressEntity facilityAddress = addressRepository.findById(facility.getCurrentAddressId())
                .orElseThrow(() -> {
                    log.info("Address {} is not found", facility.getCurrentAddressId());
                    return new AppException(AppErrorCode.NOT_FOUND);
                });
        RoutingRequest.Depot depot = new RoutingRequest.Depot(facility.getId(), facilityAddress.getLatitude(),
                facilityAddress.getLongitude());

        List<RoutingRequest.Slip> slips = plan.getSlips().stream().map(slip -> {
            AddressEntity deliveryAddress = addressRepository.findById(slip.getCustomerAddressId())
                    .orElseThrow(() -> {
                        log.info("Address {} is not found", slip.getCustomerAddressId());
                        return new AppException(AppErrorCode.NOT_FOUND);
                    });
            return new RoutingRequest.Slip(slip.getId(), deliveryAddress.getLatitude(), deliveryAddress.getLongitude(),
                    slip.getTotalWeightKg(), slip.getTotalVolumeCbm());
        }).toList();

        List<RoutingRequest.Vehicle> vehicles = plan.getVehicleShippers().stream().map(vs -> {
            VehicleEntity vehicle = vehicleRepository.findById(vs.getVehicleId())
                    .orElseThrow(() -> {
                        log.info("Vehicle {} is not found", vs.getVehicleId());
                        return new AppException(AppErrorCode.NOT_FOUND);
                    });
            return new RoutingRequest.Vehicle(vs.getId(), vehicle.getMaxWeightKg(), vehicle.getMaxVolumeCbm() * 0.8);
        }).toList();

        RoutingRequest routingRequest = new RoutingRequest(plan.getId(), depot, vehicles, slips);
        routingKafkaProducer.sendRoutingRequest(routingRequest);

        plan.setOptimizationStatus(PlanOptimizationStatus.OPTIMIZING.name());
        deliveryPlanRepository.save(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryPlan(String deliveryPlanId, DeliveryPlanUpdateForm form, Long tenantId) {
        DeliveryPlanEntity plan = deliveryPlanRepository.findByIdAndTenantId(deliveryPlanId, tenantId)
                .orElseThrow(() -> {
                    log.info("Delivery plan {} is not found", deliveryPlanId);
                    return new AppException(AppErrorCode.NOT_FOUND);
                });
        if (PlanOptimizationStatus.valueOf(plan.getOptimizationStatus()).ordinal() >= PlanOptimizationStatus.OPTIMIZING
                .ordinal()) {
            log.info("Cannot update delivery plan {} with optimization status {}", deliveryPlanId,
                    plan.getOptimizationStatus());
            throw new AppException(AppErrorCode.PLAN_IN_OPTIMIZATION);
        }

        List<DeliverySlipEntity> additionalDeliverySlips = deliverySlipRepository
                .findAllById(form.getAdditionalDeliverySlipIds());
        if (additionalDeliverySlips.size() != form.getAdditionalDeliverySlipIds().size()) {
            log.info("Some additional delivery slips have been missing");
            throw new AppException(AppErrorCode.NOT_FOUND);
        } else {
            additionalDeliverySlips.forEach(slip -> {
                if (!slip.getStatus().equals(DeliverySlipStatus.PENDING.name())) {
                    log.info("Delivery slip {} is not available in {}", slip.getId(), plan.getDeliveryDate());
                    throw new AppException(AppErrorCode.DELIVERY_SLIP_NOT_AVAILABLE);
                }
            });
        }
        plan.addSlips(additionalDeliverySlips);

        List<VehicleShipperEntity> additionalVehicleShippers = vehicleShipperRepository
                .findAllById(form.getAdditionalVehicleShipperIds());
        if (additionalVehicleShippers.size() != form.getAdditionalVehicleShipperIds().size()) {
            log.info("Some additional vehicle-shipper assignments have been missing");
            throw new AppException(AppErrorCode.NOT_FOUND);
        } else {
            additionalVehicleShippers.forEach(vs -> {
                if (!vs.getStatus().equals(VehicleShipperStatus.ACTIVE.name())
                        || !vs.getWorkingDate().isEqual(plan.getDeliveryDate())) {
                    log.info("Vehicle-shipper assignment {} is not available in {}", vs.getId(),
                            plan.getDeliveryDate());
                    throw new AppException(AppErrorCode.VEHICLE_SHIPPER_NOT_AVAILABLE);
                }
            });
        }
        plan.addVehicleShippers(additionalVehicleShippers);

        deliverySlipRepository.updateStatusByIdInAndTenantId(DeliverySlipStatus.ASSIGNED.name(),
                form.getAdditionalDeliverySlipIds(), tenantId);
        log.info("Additional delivery slips updated to ASSIGNED status for delivery plan: {}", deliveryPlanId);

        deliverySlipRepository.updateStatusByIdInAndTenantId(DeliverySlipStatus.PENDING.name(),
                form.getRemovedDeliverySlipIds(), tenantId);
        log.info("Removed delivery slips updated to PENDING status for delivery plan: {}", deliveryPlanId);

        plan.removeSlips(plan.getSlips().stream()
                .filter(slip -> form.getRemovedDeliverySlipIds().contains(slip.getId())).toList());
        plan.removeVehicleShippers(plan.getVehicleShippers().stream()
                .filter(vs -> form.getRemovedVehicleShipperIds().contains(vs.getId())).toList());
        deliveryPlanRepository.save(plan);
        log.info("Delivery plan {} is updated successfully", deliveryPlanId);
    }

    public DeliveryPlanEntity getDeliveryPlanDetails(String deliveryPlanId, Long tenantId) {
        return deliveryPlanRepository.findByIdAndTenantId(deliveryPlanId, tenantId).orElse(null);
    }

    public Page<DeliveryPlanEntity> searchDeliveryPlans(
            String query,
            String facilityId,
            LocalDate deliveryDate,
            String optimizationStatus,
            Long tenantId,
            int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        var planPage = deliveryPlanRepository.search(query, facilityId, deliveryDate, optimizationStatus, tenantId, pageable);

        List<String> vehicleIds = planPage.getContent().stream()
                .flatMap(plan -> plan.getVehicleShippers().stream())
                .map(VehicleShipperEntity::getVehicleId)
                .distinct()
                .toList();
        List<VehicleEntity> vehicles = vehicleRepository.findAllById(vehicleIds);
        Map<String, VehicleEntity> vehicleMap = vehicles.stream().collect(Collectors.toMap(VehicleEntity::getId, v -> v));

        planPage.getContent().forEach(plan -> {
            plan.getVehicleShippers().forEach(vs -> {
                VehicleEntity vehicle = vehicleMap.get(vs.getVehicleId());
                vs.setVehicle(vehicle);
            });
        });

        return planPage;
    }

}
