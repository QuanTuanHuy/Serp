/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.response.BagCapacitySettingsResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;

import java.time.LocalDate;
import java.util.Objects;

import static serp.project.second_mile.kernel.utils.CommonValueUtils.normalizeText;
import static serp.project.second_mile.service.BagCapacityCalculator.canFit;

@Component
@RequiredArgsConstructor
public class BagValidator {
    private final HubRepository hubRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final HubStaffAssignmentRepository hubStaffAssignmentRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;

    public void validateTenantAccess(Bag bag) {
        Long currentTenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        if (bag.getTenantId() == null || !bag.getTenantId().equals(currentTenantId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    public Route validateRouteAndTransport(
            Long tenantId,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode,
            Long routeId,
            Long vehicleId
    ) {
        validateBagLane(tenantId, originHubId, destinationType, destinationHubId, destinationPostOfficeCode);

        if (routeId == null) {
            return null;
        }
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));
        validateRouteMatchesBagTarget(
                tenantId,
                route,
                originHubId,
                destinationType,
                destinationHubId,
                destinationPostOfficeCode,
                vehicleId
        );
        return route;
    }

    public void validateBagLane(
            Long tenantId,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode
    ) {
        if (originHubId == null) {
            throw new AppException(ErrorCode.BAG_HUB_INVALID);
        }

        Hub originHub = hubRepository.findById(originHubId)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_HUB_INVALID));
        if (!tenantId.equals(originHub.getTenantId())) {
            throw new AppException(ErrorCode.BAG_HUB_INVALID);
        }

        if (destinationType == BagDestinationType.HUB) {
            validateDestinationHub(tenantId, originHubId, destinationHubId);
        } else if (destinationType == BagDestinationType.POST_OFFICE) {
            validateDestinationPostOffice(tenantId, destinationPostOfficeCode);
        } else {
            throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
        }
    }

    public void validateBagEditable(Bag bag) {
        if (bag.getStatus() != BagStatus.CREATED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }
    }

    public void validateOrderForBagAssignment(
            Long tenantId,
            Bag bag,
            TmsOrderOperationView order,
            double extraWeight,
            double extraVolume,
            int extraOrders,
            BagCapacitySettingsResponse capacitySettings
    ) {
        if (!OrderStatus.isReadyForBagging(order.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Order is not ready for bagging.");
        }

        validateOrderOriginMatchesHub(tenantId, order, bag.getOriginHubId());
        validateOrderDestinationMatchesTarget(
                tenantId,
                bag.getOriginHubId(),
                bag.getDestinationType(),
                bag.getDestinationHubId(),
                bag.getDestinationPostOfficeCode(),
                order
        );
        if (!canFit(bag, order, extraWeight, extraVolume, extraOrders, capacitySettings)) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID, "Bag capacity exceeded.");
        }
    }

    public void validateOrderOriginMatchesHub(Long tenantId, TmsOrderOperationView order, Long originHubId) {
        Long resolvedOriginHubId = resolveOriginHubIdByOrder(tenantId, order);
        if (!Objects.equals(resolvedOriginHubId, originHubId)) {
            throw new AppException(ErrorCode.BAG_HUB_INVALID);
        }
    }

    public void validateOrderDestinationMatchesTarget(
            Long tenantId,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode,
            TmsOrderOperationView order
    ) {
        if (destinationType == BagDestinationType.POST_OFFICE) {
            String orderDestinationPo = normalizeText(order.getDestinationPostOfficeCode());
            if (orderDestinationPo == null
                    || !orderDestinationPo.equalsIgnoreCase(normalizeText(destinationPostOfficeCode))) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            return;
        }

        if (destinationType == BagDestinationType.HUB) {
            String orderDestinationPo = normalizeText(order.getDestinationPostOfficeCode());
            if (orderDestinationPo == null) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            HubPostOfficeMapping destinationMapping = hubPostOfficeMappingRepository
                    .findByTenantIdAndPostOfficeCode(tenantId, orderDestinationPo)
                    .orElseThrow(() -> new AppException(ErrorCode.BAG_POST_OFFICE_INVALID));
            Long resolvedDestinationHubId = destinationMapping.getHub() == null ? null : destinationMapping.getHub().getId();
            if (Objects.equals(originHubId, resolvedDestinationHubId)) {
                throw new AppException(
                        ErrorCode.BAG_DESTINATION_INVALID,
                        "Same-hub destination orders must be bagged to a destination post office."
                );
            }
            if (!Objects.equals(resolvedDestinationHubId, destinationHubId)) {
                throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
            }
            return;
        }
        throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
    }

    public Long resolveOriginHubIdByOrder(Long tenantId, TmsOrderOperationView order) {
        String originPostOfficeCode = normalizeText(order.getOriginPostOfficeCode());
        if (originPostOfficeCode == null) {
            throw new AppException(ErrorCode.BAG_HUB_INVALID);
        }
        HubPostOfficeMapping mapping = hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(tenantId, originPostOfficeCode)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_HUB_INVALID));
        return mapping.getHub() == null ? null : mapping.getHub().getId();
    }

    public void validateTmsOrderTenant(Long tenantId, TmsOrderOperationView order) {
        if (order == null || order.getId() == null) {
            throw new AppException(ErrorCode.BAG_ORDER_NOT_FOUND);
        }
        if (order.getTenantId() != null && !Objects.equals(order.getTenantId(), tenantId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateDestinationHub(Long tenantId, Long originHubId, Long destinationHubId) {
        if (destinationHubId == null) {
            throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
        }
        Hub destinationHub = hubRepository.findById(destinationHubId)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_DESTINATION_INVALID));
        if (!tenantId.equals(destinationHub.getTenantId())) {
            throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
        }
        if (Objects.equals(originHubId, destinationHubId)) {
            throw new AppException(
                    ErrorCode.BAG_DESTINATION_INVALID,
                    "Same-hub orders must use a destination post office bag."
            );
        }
    }

    private void validateDestinationPostOffice(Long tenantId, String destinationPostOfficeCode) {
        String normalizedPostOfficeCode = normalizeText(destinationPostOfficeCode);
        if (normalizedPostOfficeCode == null) {
            throw new AppException(ErrorCode.BAG_DESTINATION_INVALID);
        }
        hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(tenantId, normalizedPostOfficeCode)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_POST_OFFICE_INVALID));
    }

    private void validateRouteMatchesBagTarget(
            Long tenantId,
            Route route,
            Long originHubId,
            BagDestinationType destinationType,
            Long destinationHubId,
            String destinationPostOfficeCode,
            Long requestedVehicleId
    ) {
        if (!tenantId.equals(route.getTenantId()) || route.getStatus() != RouteStatus.ACTIVE) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
        }
        if (route.getOriginType() != RouteEndpointType.HUB || !Objects.equals(route.getOriginHubId(), originHubId)) {
            throw new AppException(ErrorCode.ROUTE_HUB_INVALID, "Route origin hub must match bag origin hub.");
        }
        RouteDestinationType expectedRouteDestinationType = RouteDestinationType.valueOf(destinationType.name());
        if (route.getDestinationType() != expectedRouteDestinationType) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID, "Route destination type does not match.");
        }
        if (destinationType == BagDestinationType.HUB
                && !Objects.equals(route.getDestinationHubId(), destinationHubId)) {
            throw new AppException(ErrorCode.ROUTE_HUB_INVALID);
        }
        if (destinationType == BagDestinationType.POST_OFFICE
                && !Objects.equals(
                        normalizeText(route.getDestinationPostOfficeCode()),
                        normalizeText(destinationPostOfficeCode)
                )) {
            throw new AppException(ErrorCode.ROUTE_POST_OFFICE_INVALID);
        }
        if (route.getVehicleId() == null) {
            throw new AppException(ErrorCode.ROUTE_VEHICLE_INVALID, "Route must have an assigned vehicle.");
        }
        if (requestedVehicleId != null && !Objects.equals(route.getVehicleId(), requestedVehicleId)) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Selected vehicle must match the vehicle assigned to the route."
            );
        }
        Vehicle vehicle = validateVehicle(tenantId, originHubId, route.getVehicleId());
        validateVehicleHasAssignedDriver(tenantId, vehicle);
        validateDriverAssignedToHub(tenantId, vehicle.getAssignedStaffId(), originHubId);
    }

    private Vehicle validateVehicle(Long tenantId, Long originHubId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.BAG_VEHICLE_INVALID));
        if (!tenantId.equals(vehicle.getTenantId()) || vehicle.getStatus() != VehicleStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAG_VEHICLE_INVALID);
        }
        if (!Objects.equals(vehicle.getHubId(), originHubId)) {
            throw new AppException(
                    ErrorCode.BAG_VEHICLE_INVALID,
                    "Bag route vehicle must belong to the origin hub."
            );
        }
        return vehicle;
    }

    private void validateVehicleHasAssignedDriver(Long tenantId, Vehicle vehicle) {
        if (vehicle == null) {
            throw new AppException(ErrorCode.BAG_VEHICLE_INVALID);
        }
        secondMileAccessUtils.ensureActiveDriverStaffOrThrow(tenantId, vehicle.getAssignedStaffId());
    }

    private void validateDriverAssignedToHub(Long tenantId, Long driverStaffId, Long hubId) {
        if (driverStaffId == null || hubId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Assigned vehicle driver and hub are required.");
        }
        boolean assignedToHub = hubStaffAssignmentRepository
                .findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
                        driverStaffId,
                        hubId,
                        tenantId,
                        LocalDate.now()
                )
                .isPresent();
        if (!assignedToHub) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Assigned vehicle driver must be active at the origin hub."
            );
        }
    }
}
