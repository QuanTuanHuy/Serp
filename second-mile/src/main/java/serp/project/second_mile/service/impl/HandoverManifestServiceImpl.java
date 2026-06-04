/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.caller.TmsOrderClient;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.second_mile.domain.HandoverManifest;
import serp.project.second_mile.domain.HandoverManifestOrder;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.ConfirmHandoverInboundRequest;
import serp.project.second_mile.dto.request.CreateHandoverManifestRequest;
import serp.project.second_mile.dto.request.DriverHandoverCheckinRequest;
import serp.project.second_mile.dto.request.HandoverManifestFilterRequest;
import serp.project.second_mile.dto.response.HandoverManifestOrderResponse;
import serp.project.second_mile.dto.response.HandoverManifestResponse;
import serp.project.second_mile.enums.HandoverManifestStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kafka.HandoverManifestSyncEventPublisher;
import serp.project.second_mile.kafka.event.HandoverManifestSyncEvent;
import serp.project.second_mile.kafka.event.HandoverManifestSyncEventType;
import serp.project.second_mile.kafka.event.HandoverManifestSyncOrigin;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.kernel.utils.TransactionAfterCommit;
import serp.project.second_mile.repository.HandoverManifestOrderRepository;
import serp.project.second_mile.repository.HandoverManifestRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.specification.HandoverManifestSpecification;
import serp.project.second_mile.service.HandoverManifestService;
import serp.project.second_mile.service.TmsOrderTransitionOutboxService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HandoverManifestServiceImpl implements HandoverManifestService {
    private static final String TRANSITION_SOURCE = "SECOND_MILE";
    private static final DateTimeFormatter MANIFEST_CODE_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final double CHECKIN_RADIUS_METERS = 100.0;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;
    private static final List<HandoverManifestStatus> ACTIVE_MANIFEST_STATUSES = List.of(
            HandoverManifestStatus.CREATED,
            HandoverManifestStatus.OUTBOUND_CONFIRMED
    );

    private record OutboundSyncValidation(
            HandoverManifest existingManifest,
            Vehicle vehicle,
            Route route,
            List<TmsOrderOperationView> orders
    ) {
    }

    private final HandoverManifestRepository handoverManifestRepository;
    private final HandoverManifestOrderRepository handoverManifestOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final HubRepository hubRepository;
    private final HubStaffAssignmentRepository hubStaffAssignmentRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final HandoverManifestSyncEventPublisher handoverManifestSyncEventPublisher;
    private final TmsOrderClient tmsOrderClient;
    private final TmsOrderTransitionOutboxService tmsOrderTransitionOutboxService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HandoverManifestResponse createManifest(CreateHandoverManifestRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        String normalizedOriginPostOfficeCode = normalizeText(request.getOriginPostOfficeCode());
        if (normalizedOriginPostOfficeCode == null
                || request.getTargetHubId() == null
                || request.getVehicleId() == null
                || request.getRouteId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        validatePlannedWindow(request.getPlannedDepartureAt(), request.getPlannedArrivalAt());
        validateGeoCoordinatePair(
                request.getOriginPostOfficeLatitude(),
                request.getOriginPostOfficeLongitude(),
                "Origin post office location is required for driver check-in."
        );

        validatePostOfficeMappedToHub(tenantId, normalizedOriginPostOfficeCode, request.getTargetHubId());
        Vehicle vehicle = validateVehicleForManifest(tenantId, request.getTargetHubId(), request.getVehicleId());
        validateVehicleHasAssignedDriver(tenantId, vehicle);
        validateDriverAssignedToHub(tenantId, vehicle.getAssignedStaffId(), request.getTargetHubId());
        Route route = validateRouteForManifest(
                tenantId,
                request.getTargetHubId(),
                normalizedOriginPostOfficeCode,
                request.getRouteId(),
                request.getVehicleId()
        );

        List<String> normalizedOrderCodes = normalizeOrderCodes(request.getOrderCodes());
        if (normalizedOrderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<TmsOrderOperationView> orders = lookupOrdersByCodes(tenantId, normalizedOrderCodes);
        for (TmsOrderOperationView order : orders) {
            validateOrderOriginAndOutboundStatus(order, normalizedOriginPostOfficeCode);
        }
        validateVehicleCapacity(vehicle, orders);
        validateVehicleScheduleAvailability(
                tenantId,
                vehicle.getId(),
                vehicle.getAssignedStaffId(),
                request.getPlannedDepartureAt(),
                request.getPlannedArrivalAt(),
                null
        );

        String manifestCode = generateManifestCode(tenantId, request.getTargetHubId());
        HandoverManifest manifest = HandoverManifest.builder()
                .manifestCode(manifestCode)
                .originPostOfficeCode(normalizedOriginPostOfficeCode)
                .targetHubId(request.getTargetHubId())
                .vehicleId(vehicle.getId())
                .assignedDriverId(vehicle.getAssignedStaffId())
                .routeId(route.getId())
                .plannedDepartureAt(request.getPlannedDepartureAt())
                .plannedArrivalAt(request.getPlannedArrivalAt())
                .originPostOfficeLatitude(request.getOriginPostOfficeLatitude())
                .originPostOfficeLongitude(request.getOriginPostOfficeLongitude())
                .status(HandoverManifestStatus.CREATED)
                .tenantId(tenantId)
                .build();
        HandoverManifest savedManifest = handoverManifestRepository.save(manifest);

        List<HandoverManifestOrder> manifestOrders = orders.stream()
                .map(order -> toManifestOrder(savedManifest, order, tenantId))
                .toList();
        handoverManifestOrderRepository.saveAll(manifestOrders);

        enqueueManifestTransition(
                tenantId,
                idempotencyKey("manifest", savedManifest.getId(), "outbound-ready"),
                orders.stream()
                        .map(order -> toTransitionItem(
                                order,
                                OrderStatus.OUTBOUND_READY_FROM_PO,
                                List.of(OrderStatus.AT_ORIGIN_POST_OFFICE, OrderStatus.OUTBOUND_READY_FROM_PO),
                                "Second-mile handover manifest created.",
                                buildManifestContext(savedManifest, vehicle, route)
                        ))
                        .toList()
        );

        return toResponse(savedManifest, manifestOrders, vehicle, route);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HandoverManifestResponse> listManifests(
            int page,
            int size,
            HandoverManifestFilterRequest filterRequest
    ) {
        secondMileAccessUtils.ensureHubOperationOrDriverRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffOrDriverRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long driverStaffId = resolveDriverScopedStaffId();
        Specification<HandoverManifest> specification = HandoverManifestSpecification.byFilter(tenantId, filterRequest);
        if (driverStaffId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("assignedDriverId"), driverStaffId)
            );
        }

        Page<HandoverManifest> manifestPage = handoverManifestRepository.findAll(
                specification,
                pageable
        );

        List<HandoverManifestResponse> items = manifestPage.getContent().stream()
                .map(manifest -> {
                    List<HandoverManifestOrder> manifestOrders = findManifestOrders(manifest.getId(), tenantId);
                    Vehicle vehicle = loadVehicle(manifest.getVehicleId());
                    Route route = loadRoute(manifest.getRouteId());
                    return toResponse(manifest, manifestOrders, vehicle, route);
                })
                .toList();

        return PageResponse.<HandoverManifestResponse>builder()
                .items(items)
                .page(manifestPage.getNumber())
                .size(manifestPage.getSize())
                .totalElements(manifestPage.getTotalElements())
                .totalPages(manifestPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HandoverManifestResponse confirmOutbound(Long manifestId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        HandoverManifest manifest = getManifestOrThrow(manifestId);
        return processOutboundCheckin(manifest, false, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HandoverManifestResponse confirmInbound(Long manifestId, ConfirmHandoverInboundRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        HandoverManifest manifest = getManifestOrThrow(manifestId);
        return processInboundCheckin(manifest, request, false, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HandoverManifestResponse driverCheckinStart(Long manifestId, DriverHandoverCheckinRequest request) {
        secondMileAccessUtils.ensureHubOperationOrDriverRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffOrDriverRoleOrThrow();

        HandoverManifest manifest = getManifestOrThrow(manifestId);
        return processOutboundCheckin(manifest, true, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HandoverManifestResponse driverCheckinEnd(Long manifestId, DriverHandoverCheckinRequest request) {
        secondMileAccessUtils.ensureHubOperationOrDriverRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffOrDriverRoleOrThrow();

        HandoverManifest manifest = getManifestOrThrow(manifestId);
        return processInboundCheckin(manifest, null, true, request);
    }

    @Override
    @Transactional(readOnly = true)
    public HandoverManifestResponse getManifest(Long manifestId) {
        secondMileAccessUtils.ensureHubOperationOrDriverRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffOrDriverRoleOrThrow();

        HandoverManifest manifest = getManifestOrThrow(manifestId);
        Long driverStaffId = resolveDriverScopedStaffId();
        if (driverStaffId != null && !Objects.equals(manifest.getAssignedDriverId(), driverStaffId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        List<HandoverManifestOrder> manifestOrders = findManifestOrders(manifest.getId(), manifest.getTenantId());
        return toResponse(
                manifest,
                manifestOrders,
                loadVehicle(manifest.getVehicleId()),
                loadRoute(manifest.getRouteId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void validateOutboundSync(HandoverManifestSyncEvent event) {
        resolveOutboundSyncValidation(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyOutboundSync(HandoverManifestSyncEvent event) {
        if (event == null || !HandoverManifestSyncOrigin.FIRST_MILE.equals(event.getOrigin())) {
            return;
        }
        Long tenantId = event.getTenantId();
        String manifestCode = normalizeText(event.getManifestCode());
        String originPostOfficeCode = normalizeText(event.getOriginPostOfficeCode());
        if (tenantId == null || manifestCode == null || originPostOfficeCode == null || event.getTargetHubId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid handover manifest sync payload.");
        }

        HandoverManifest existingManifest = handoverManifestRepository
                .findByTenantIdAndManifestCodeIgnoreCase(tenantId, manifestCode)
                .orElse(null);
        if (HandoverManifestSyncEventType.CANCELLED.equals(event.getEventType())) {
            handleOutboundSyncCancellation(existingManifest, tenantId);
            return;
        }

        if (!HandoverManifestSyncEventType.OUTBOUND_CONFIRMED.equals(event.getEventType())) {
            return;
        }
        OutboundSyncValidation validation = validateOutboundSyncEvent(
                event,
                tenantId,
                originPostOfficeCode,
                existingManifest
        );
        Vehicle vehicle = validation.vehicle();
        Route route = validation.route();
        List<TmsOrderOperationView> orders = validation.orders();

        HandoverManifest manifest = existingManifest == null
                ? HandoverManifest.builder()
                .manifestCode(manifestCode)
                .originPostOfficeCode(originPostOfficeCode)
                .targetHubId(event.getTargetHubId())
                .vehicleId(vehicle.getId())
                .assignedDriverId(vehicle.getAssignedStaffId())
                .routeId(route.getId())
                .plannedDepartureAt(event.getPlannedDepartureAt())
                .plannedArrivalAt(event.getPlannedArrivalAt())
                .originPostOfficeLatitude(event.getOriginPostOfficeLatitude())
                .originPostOfficeLongitude(event.getOriginPostOfficeLongitude())
                .status(HandoverManifestStatus.OUTBOUND_CONFIRMED)
                .tenantId(tenantId)
                .build()
                : existingManifest;
        if (manifest.getStatus() == HandoverManifestStatus.INBOUND_CONFIRMED) {
            return;
        }
        manifest.setOriginPostOfficeCode(originPostOfficeCode);
        manifest.setTargetHubId(event.getTargetHubId());
        manifest.setVehicleId(vehicle.getId());
        manifest.setAssignedDriverId(vehicle.getAssignedStaffId());
        manifest.setRouteId(route.getId());
        manifest.setPlannedDepartureAt(event.getPlannedDepartureAt());
        manifest.setPlannedArrivalAt(event.getPlannedArrivalAt());
        manifest.setOriginPostOfficeLatitude(event.getOriginPostOfficeLatitude());
        manifest.setOriginPostOfficeLongitude(event.getOriginPostOfficeLongitude());
        manifest.setStatus(HandoverManifestStatus.OUTBOUND_CONFIRMED);
        HandoverManifest savedManifest = handoverManifestRepository.save(manifest);

        LocalDateTime scanOutTime = event.getDispatchedAt() == null ? LocalDateTime.now() : event.getDispatchedAt();
        List<HandoverManifestOrder> manifestOrders = new ArrayList<>();
        List<TmsOrderStatusTransitionRequest.Item> transitionItems = new ArrayList<>();
        TmsOrderStatusTransitionRequest.Context context = buildManifestContext(savedManifest, vehicle, route);
        for (TmsOrderOperationView order : orders) {
            if (!Objects.equals(originPostOfficeCode, normalizeText(order.getOriginPostOfficeCode()))) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "All synced handover orders must match the origin post office."
                );
            }
            if (shouldAdvanceToOutboundReady(order.getStatus())) {
                transitionItems.add(toTransitionItem(
                        order,
                        OrderStatus.OUTBOUND_READY_FROM_PO,
                        List.of(OrderStatus.AT_ORIGIN_POST_OFFICE, OrderStatus.OUTBOUND_READY_FROM_PO),
                        "First-mile outbound handover confirmed.",
                        context
                ));
            }
            HandoverManifestOrder manifestOrder = handoverManifestOrderRepository
                    .findByManifest_IdAndTmsOrderIdAndTenantId(savedManifest.getId(), order.getId(), tenantId)
                    .orElseGet(() -> HandoverManifestOrder.builder()
                            .manifest(savedManifest)
                            .tmsOrderId(order.getId())
                            .tenantId(tenantId)
                            .build());
            updateManifestOrderSnapshot(manifestOrder, order);
            if (manifestOrder.getScanOutTime() == null) {
                manifestOrder.setScanOutTime(scanOutTime);
            }
            manifestOrders.add(manifestOrder);
        }
        handoverManifestOrderRepository.saveAll(manifestOrders);
        enqueueManifestTransition(
                tenantId,
                idempotencyKey("manifest", savedManifest.getId(), "outbound-sync"),
                transitionItems
        );
    }

    private OutboundSyncValidation resolveOutboundSyncValidation(HandoverManifestSyncEvent event) {
        if (event == null
                || !HandoverManifestSyncOrigin.FIRST_MILE.equals(event.getOrigin())
                || !HandoverManifestSyncEventType.OUTBOUND_CONFIRMED.equals(event.getEventType())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid handover manifest sync payload.");
        }

        Long tenantId = event.getTenantId();
        String manifestCode = normalizeText(event.getManifestCode());
        String originPostOfficeCode = normalizeText(event.getOriginPostOfficeCode());
        if (tenantId == null || manifestCode == null || originPostOfficeCode == null || event.getTargetHubId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid handover manifest sync payload.");
        }

        HandoverManifest existingManifest = handoverManifestRepository
                .findByTenantIdAndManifestCodeIgnoreCase(tenantId, manifestCode)
                .orElse(null);
        return validateOutboundSyncEvent(event, tenantId, originPostOfficeCode, existingManifest);
    }

    private OutboundSyncValidation validateOutboundSyncEvent(
            HandoverManifestSyncEvent event,
            Long tenantId,
            String originPostOfficeCode,
            HandoverManifest existingManifest
    ) {
        if (event.getVehicleId() == null
                || event.getRouteId() == null
                || event.getPlannedDepartureAt() == null
                || event.getPlannedArrivalAt() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Handover manifest sync must include vehicle_id, route_id, planned_departure_at, and planned_arrival_at."
            );
        }
        validatePlannedWindow(event.getPlannedDepartureAt(), event.getPlannedArrivalAt());
        validateGeoCoordinatePair(
                event.getOriginPostOfficeLatitude(),
                event.getOriginPostOfficeLongitude(),
                "Origin post office location is required for driver check-in."
        );

        validatePostOfficeMappedToHub(tenantId, originPostOfficeCode, event.getTargetHubId());
        Vehicle vehicle = validateVehicleForManifest(tenantId, event.getTargetHubId(), event.getVehicleId());
        validateVehicleHasAssignedDriver(tenantId, vehicle);
        validateDriverAssignedToHub(tenantId, vehicle.getAssignedStaffId(), event.getTargetHubId());
        Route route = validateRouteForManifest(
                tenantId,
                event.getTargetHubId(),
                originPostOfficeCode,
                event.getRouteId(),
                event.getVehicleId()
        );
        List<String> normalizedOrderCodes = normalizeOrderCodes(event.getOrderCodes());
        if (normalizedOrderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Handover manifest sync must include order_codes.");
        }

        List<TmsOrderOperationView> orders = lookupOrdersByCodes(tenantId, normalizedOrderCodes);
        validateVehicleCapacity(vehicle, orders);
        validateVehicleScheduleAvailability(
                tenantId,
                vehicle.getId(),
                vehicle.getAssignedStaffId(),
                event.getPlannedDepartureAt(),
                event.getPlannedArrivalAt(),
                existingManifest == null ? null : existingManifest.getId()
        );
        return new OutboundSyncValidation(existingManifest, vehicle, route, orders);
    }

    private void handleOutboundSyncCancellation(HandoverManifest existingManifest, Long tenantId) {
        if (existingManifest == null || existingManifest.getStatus() == HandoverManifestStatus.INBOUND_CONFIRMED) {
            return;
        }

        List<HandoverManifestOrder> manifestOrders = findManifestOrders(existingManifest.getId(), tenantId);
        List<HandoverManifestOrder> rollbackTargets = manifestOrders.stream()
                .filter(item -> item.getScanInTime() == null)
                .toList();
        for (HandoverManifestOrder target : rollbackTargets) {
            target.setLastKnownStatus(OrderStatus.AT_ORIGIN_POST_OFFICE.name());
        }
        handoverManifestOrderRepository.saveAll(rollbackTargets);
        enqueueManifestTransition(
                tenantId,
                idempotencyKey("manifest", existingManifest.getId(), "cancel"),
                rollbackTargets.stream()
                        .map(item -> toTransitionItem(
                                item,
                                OrderStatus.AT_ORIGIN_POST_OFFICE,
                                List.of(OrderStatus.OUTBOUND_READY_FROM_PO),
                                "First-mile handover manifest cancelled.",
                                buildManifestContext(existingManifest, loadVehicle(existingManifest.getVehicleId()), loadRoute(existingManifest.getRouteId()))
                        ))
                        .toList()
        );

        existingManifest.setStatus(HandoverManifestStatus.CANCELLED);
        handoverManifestRepository.save(existingManifest);
    }

    private HandoverManifest getManifestOrThrow(Long manifestId) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        return handoverManifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Handover manifest not found."));
    }

    private List<HandoverManifestOrder> findManifestOrders(Long manifestId, Long tenantId) {
        return handoverManifestOrderRepository.findByManifest_IdAndTenantId(manifestId, tenantId);
    }

    private void validatePostOfficeMappedToHub(Long tenantId, String originPostOfficeCode, Long targetHubId) {
        HubPostOfficeMapping mapping = hubPostOfficeMappingRepository
                .findByTenantIdAndPostOfficeCode(tenantId, originPostOfficeCode)
                .orElseThrow(() -> new AppException(
                        ErrorCode.HUB_POST_OFFICE_CODE_INVALID,
                        "Origin post office is not assigned to any hub."
                ));

        if (mapping.getHub() == null || !Objects.equals(mapping.getHub().getId(), targetHubId)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Target hub does not match the hub assigned to the origin post office."
            );
        }
    }

    private Vehicle validateVehicleForManifest(Long tenantId, Long targetHubId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        if (!tenantId.equals(vehicle.getTenantId()) || vehicle.getStatus() != VehicleStatus.ACTIVE) {
            throw new AppException(ErrorCode.ROUTE_VEHICLE_INVALID, "Vehicle is not active for transport.");
        }
        if (!Objects.equals(vehicle.getHubId(), targetHubId)) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Vehicle must belong to the target hub."
            );
        }
        return vehicle;
    }

    private void validateVehicleHasAssignedDriver(Long tenantId, Vehicle vehicle) {
        if (vehicle == null) {
            throw new AppException(ErrorCode.VEHICLE_NOT_FOUND);
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
                    "Assigned vehicle driver must be active at the target hub."
            );
        }
    }

    private void validateVehicleCapacity(Vehicle vehicle, List<TmsOrderOperationView> orders) {
        if (vehicle == null || orders == null || orders.isEmpty()) {
            return;
        }
        double totalWeight = orders.stream()
                .map(TmsOrderOperationView::getTotalWeight)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
        double totalVolume = orders.stream()
                .map(TmsOrderOperationView::getTotalVolume)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
        if (vehicle.getMaxWeight() > 0 && totalWeight > vehicle.getMaxWeight()) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    String.format(
                            Locale.ROOT,
                            "Vehicle weight capacity exceeded: %.2f > %.2f",
                            totalWeight,
                            vehicle.getMaxWeight()
                    )
            );
        }
        if (vehicle.getMaxVolume() > 0 && totalVolume > vehicle.getMaxVolume()) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    String.format(
                            Locale.ROOT,
                            "Vehicle volume capacity exceeded: %.2f > %.2f",
                            totalVolume,
                            vehicle.getMaxVolume()
                    )
            );
        }
    }

    private void validateVehicleScheduleAvailability(
            Long tenantId,
            Long vehicleId,
            Long assignedDriverId,
            LocalDateTime plannedDepartureAt,
            LocalDateTime plannedArrivalAt,
            Long excludeManifestId
    ) {
        if (vehicleId == null || plannedDepartureAt == null || plannedArrivalAt == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        boolean hasActiveManifest = handoverManifestRepository.existsOverlappingActiveAssignment(
                tenantId,
                vehicleId,
                assignedDriverId,
                plannedDepartureAt,
                plannedArrivalAt,
                ACTIVE_MANIFEST_STATUSES,
                excludeManifestId
        );
        if (hasActiveManifest) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Vehicle or driver is already assigned to another active handover manifest in this time window."
            );
        }
    }

    private Route validateRouteForManifest(
            Long tenantId,
            Long targetHubId,
            String originPostOfficeCode,
            Long routeId,
            Long vehicleId
    ) {
        if (routeId == null) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID, "Route is required for handover manifest.");
        }

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));

        if (!tenantId.equals(route.getTenantId()) || route.getStatus() != RouteStatus.ACTIVE) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
        }
        if (route.getOriginType() != RouteEndpointType.POST_OFFICE) {
            throw new AppException(
                    ErrorCode.ROUTE_DEFINITION_INVALID,
                    "Route must start from the origin post office for post office to hub runs."
            );
        }
        if (!Objects.equals(normalizeText(route.getOriginPostOfficeCode()), originPostOfficeCode)) {
            throw new AppException(
                    ErrorCode.ROUTE_POST_OFFICE_INVALID,
                    "Route origin post office must match manifest origin post office."
            );
        }
        if (route.getDestinationType() != RouteDestinationType.HUB) {
            throw new AppException(
                    ErrorCode.ROUTE_DEFINITION_INVALID,
                    "Route must target a hub for post office to hub runs."
            );
        }
        if (!Objects.equals(route.getDestinationHubId(), targetHubId)) {
            throw new AppException(ErrorCode.ROUTE_HUB_INVALID);
        }
        if (route.getVehicleId() == null) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Route must have a dedicated assigned vehicle."
            );
        }
        if (!Objects.equals(route.getVehicleId(), vehicleId)) {
            throw new AppException(
                    ErrorCode.ROUTE_VEHICLE_INVALID,
                    "Selected vehicle must match the vehicle assigned to the route."
            );
        }
        return route;
    }

    private Vehicle loadVehicle(Long vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        return vehicleRepository.findById(vehicleId).orElse(null);
    }

    private Route loadRoute(Long routeId) {
        if (routeId == null) {
            return null;
        }
        return routeRepository.findById(routeId).orElse(null);
    }

    private String generateManifestCode(Long tenantId, Long hubId) {
        String suffix = LocalDateTime.now().format(MANIFEST_CODE_SUFFIX_FORMATTER);
        String candidate = String.format("HM-%d-%d-%s", tenantId, hubId, suffix);
        if (!handoverManifestRepository.existsByTenantIdAndManifestCodeIgnoreCase(tenantId, candidate)) {
            return candidate;
        }
        return candidate + "-" + System.currentTimeMillis() % 1000;
    }

    private void validatePlannedWindow(LocalDateTime plannedDepartureAt, LocalDateTime plannedArrivalAt) {
        if (plannedDepartureAt == null || plannedArrivalAt == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "planned_departure_at and planned_arrival_at are required."
            );
        }
        if (!plannedArrivalAt.isAfter(plannedDepartureAt)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "planned_arrival_at must be after planned_departure_at."
            );
        }
    }

    private void validateGeoCoordinatePair(Double latitude, Double longitude, String message) {
        if (latitude == null || longitude == null
                || latitude < -90.0 || latitude > 90.0
                || longitude < -180.0 || longitude > 180.0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, message);
        }
    }

    private void recordDriverStartCheckin(
            HandoverManifest manifest,
            DriverHandoverCheckinRequest request
    ) {
        if (manifest.getDriverStartCheckinAt() != null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver has already checked in at the post office.");
        }
        validateCheckinRequest(request);
        validateGeoCoordinatePair(
                manifest.getOriginPostOfficeLatitude(),
                manifest.getOriginPostOfficeLongitude(),
                "Origin post office location is missing."
        );
        double distanceMeters = calculateDistanceMeters(
                request.getLatitude(),
                request.getLongitude(),
                manifest.getOriginPostOfficeLatitude(),
                manifest.getOriginPostOfficeLongitude()
        );
        validateCheckinDistance(distanceMeters, "Driver must check in within 100m of the origin post office.");

        manifest.setDriverStartCheckinAt(LocalDateTime.now());
        manifest.setDriverStartLatitude(request.getLatitude());
        manifest.setDriverStartLongitude(request.getLongitude());
        manifest.setDriverStartDistanceM(distanceMeters);
    }

    private void recordDriverEndCheckin(
            HandoverManifest manifest,
            DriverHandoverCheckinRequest request
    ) {
        if (manifest.getDriverStartCheckinAt() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver departure check-in is required before arrival.");
        }
        if (manifest.getDriverEndCheckinAt() != null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver has already checked in at the hub.");
        }
        validateCheckinRequest(request);

        Hub hub = hubRepository.findById(manifest.getTargetHubId())
                .orElseThrow(() -> new AppException(ErrorCode.HUB_NOT_FOUND));
        if (!Objects.equals(hub.getTenantId(), manifest.getTenantId())) {
            throw new AppException(ErrorCode.HUB_NOT_FOUND);
        }
        Point hubLocation = hub.getLocation();
        if (hubLocation == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Target hub location is missing.");
        }
        double distanceMeters = calculateDistanceMeters(
                request.getLatitude(),
                request.getLongitude(),
                hubLocation.getY(),
                hubLocation.getX()
        );
        validateCheckinDistance(distanceMeters, "Driver must check in within 100m of the target hub.");

        manifest.setDriverEndCheckinAt(LocalDateTime.now());
        manifest.setDriverEndLatitude(request.getLatitude());
        manifest.setDriverEndLongitude(request.getLongitude());
        manifest.setDriverEndDistanceM(distanceMeters);
    }

    private void validateCheckinRequest(DriverHandoverCheckinRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver check-in location is required.");
        }
        validateGeoCoordinatePair(request.getLatitude(), request.getLongitude(), "Driver check-in location is invalid.");
    }

    private void validateCheckinDistance(double distanceMeters, String message) {
        if (distanceMeters > CHECKIN_RADIUS_METERS) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    String.format(Locale.ROOT, "%s Distance: %.1fm.", message, distanceMeters)
            );
        }
    }

    private double calculateDistanceMeters(
            double latitude,
            double longitude,
            double targetLatitude,
            double targetLongitude
    ) {
        double lat1 = Math.toRadians(latitude);
        double lat2 = Math.toRadians(targetLatitude);
        double deltaLat = Math.toRadians(targetLatitude - latitude);
        double deltaLon = Math.toRadians(targetLongitude - longitude);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private HandoverManifestResponse processOutboundCheckin(
            HandoverManifest manifest,
            boolean driverOnly,
            DriverHandoverCheckinRequest request
    ) {
        boolean validStatus = driverOnly
                ? manifest.getStatus() == HandoverManifestStatus.CREATED
                || manifest.getStatus() == HandoverManifestStatus.OUTBOUND_CONFIRMED
                : manifest.getStatus() == HandoverManifestStatus.CREATED;
        if (!validStatus) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }
        if (manifest.getVehicleId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Manifest must have an assigned vehicle before departure.");
        }
        Vehicle vehicle = loadVehicle(manifest.getVehicleId());
        if (vehicle == null) {
            throw new AppException(ErrorCode.VEHICLE_NOT_FOUND);
        }
        validateVehicleHasAssignedDriver(manifest.getTenantId(), vehicle);
        validateDriverAssignedToHub(manifest.getTenantId(), vehicle.getAssignedStaffId(), manifest.getTargetHubId());
        if (driverOnly) {
            secondMileAccessUtils.ensureCurrentUserIsAssignedDriverOrThrow(vehicle.getAssignedStaffId());
            recordDriverStartCheckin(manifest, request);
        }

        List<HandoverManifestOrder> manifestOrders = findManifestOrders(manifest.getId(), manifest.getTenantId());
        if (manifestOrders.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Manifest has no orders.");
        }
        LocalDateTime now = LocalDateTime.now();
        for (HandoverManifestOrder item : manifestOrders) {
            if (item.getScanOutTime() == null) {
                item.setScanOutTime(now);
            }
        }
        handoverManifestOrderRepository.saveAll(manifestOrders);

        manifest.setStatus(HandoverManifestStatus.OUTBOUND_CONFIRMED);
        HandoverManifest savedManifest = handoverManifestRepository.save(manifest);
        return toResponse(
                savedManifest,
                manifestOrders,
                vehicle,
                loadRoute(savedManifest.getRouteId())
        );
    }

    private Long resolveDriverScopedStaffId() {
        if (secondMileAccessUtils.hasHubOperationRole() || !secondMileAccessUtils.isHubDriver()) {
            return null;
        }
        return secondMileAccessUtils.getCurrentActiveDriverStaffIdOrThrow();
    }

    private HandoverManifestResponse processInboundCheckin(
            HandoverManifest manifest,
            ConfirmHandoverInboundRequest request,
            boolean driverOnly,
            DriverHandoverCheckinRequest checkinRequest
    ) {
        if (manifest.getStatus() != HandoverManifestStatus.OUTBOUND_CONFIRMED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }

        Vehicle vehicle = null;
        if (manifest.getVehicleId() != null) {
            vehicle = loadVehicle(manifest.getVehicleId());
            if (vehicle == null) {
                throw new AppException(ErrorCode.VEHICLE_NOT_FOUND);
            }
            validateVehicleHasAssignedDriver(manifest.getTenantId(), vehicle);
        } else if (driverOnly) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Driver check-in requires an assigned vehicle.");
        }
        if (driverOnly) {
            secondMileAccessUtils.ensureCurrentUserIsAssignedDriverOrThrow(vehicle.getAssignedStaffId());
            validateDriverAssignedToHub(manifest.getTenantId(), vehicle.getAssignedStaffId(), manifest.getTargetHubId());
            recordDriverEndCheckin(manifest, checkinRequest);
        }

        Long tenantId = manifest.getTenantId();
        List<HandoverManifestOrder> targets;
        if (request == null || request.getOrderCodes() == null || request.getOrderCodes().isEmpty()) {
            targets = findManifestOrders(manifest.getId(), tenantId);
        } else {
            List<String> normalizedCodes = normalizeOrderCodes(request.getOrderCodes());
            targets = handoverManifestOrderRepository.findByManifest_IdAndOrderCodeInAndTenantId(
                    manifest.getId(),
                    normalizedCodes,
                    tenantId
            );
            if (targets.size() != normalizedCodes.size()) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Some order codes are not present in this manifest.");
            }
        }

        if (targets.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();
        for (HandoverManifestOrder target : targets) {
            target.setScanInTime(now);
            target.setLastKnownStatus(OrderStatus.INBOUND_AT_ORIGIN_HUB.name());
            if (target.getScanOutTime() == null) {
                target.setScanOutTime(now);
            }
        }

        handoverManifestOrderRepository.saveAll(targets);
        TmsOrderStatusTransitionRequest.Context context = buildManifestContext(
                manifest,
                vehicle,
                loadRoute(manifest.getRouteId())
        );
        enqueueManifestTransition(
                tenantId,
                idempotencyKey("manifest", manifest.getId(), "inbound", now),
                targets.stream()
                        .map(item -> toTransitionItem(
                                item,
                                OrderStatus.INBOUND_AT_ORIGIN_HUB,
                                List.of(OrderStatus.OUTBOUND_READY_FROM_PO, OrderStatus.INBOUND_AT_ORIGIN_HUB),
                                "Second-mile hub inbound confirmed.",
                                context
                        ))
                        .toList()
        );

        List<HandoverManifestOrder> allManifestOrders = findManifestOrders(manifest.getId(), tenantId);
        boolean allInboundScanned = allManifestOrders.stream().allMatch(item -> item.getScanInTime() != null);
        if (allInboundScanned) {
            manifest.setStatus(HandoverManifestStatus.INBOUND_CONFIRMED);
        } else {
            manifest.setStatus(HandoverManifestStatus.OUTBOUND_CONFIRMED);
        }
        HandoverManifest savedManifest = handoverManifestRepository.save(manifest);
        HandoverManifestSyncEvent syncEvent = toInboundSyncEvent(savedManifest, allManifestOrders, now);
        TransactionAfterCommit.run(() -> handoverManifestSyncEventPublisher.publish(syncEvent));
        return toResponse(
                savedManifest,
                allManifestOrders,
                vehicle,
                loadRoute(savedManifest.getRouteId())
        );
    }

    private boolean shouldAdvanceToOutboundReady(OrderStatus currentStatus) {
        if (currentStatus == null) {
            return true;
        }
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.LOST_OR_DAMAGED) {
            return false;
        }
        return currentStatus.ordinal() <= OrderStatus.OUTBOUND_READY_FROM_PO.ordinal();
    }

    private HandoverManifestSyncEvent toInboundSyncEvent(
            HandoverManifest manifest,
            List<HandoverManifestOrder> manifestOrders,
            LocalDateTime inboundConfirmedAt
    ) {
        List<String> orderCodes = manifestOrders.stream()
                .map(HandoverManifestOrder::getOrderCode)
                .filter(Objects::nonNull)
                .toList();
        List<String> scannedOrderCodes = manifestOrders.stream()
                .filter(item -> item.getScanInTime() != null)
                .map(HandoverManifestOrder::getOrderCode)
                .filter(Objects::nonNull)
                .toList();
        return HandoverManifestSyncEvent.builder()
                .eventType(HandoverManifestSyncEventType.INBOUND_CONFIRMED)
                .origin(HandoverManifestSyncOrigin.SECOND_MILE)
                .tenantId(manifest.getTenantId())
                .manifestCode(manifest.getManifestCode())
                .originPostOfficeCode(manifest.getOriginPostOfficeCode())
                .targetHubId(manifest.getTargetHubId())
                .status(manifest.getStatus())
                .inboundConfirmedAt(inboundConfirmedAt)
                .orderCodes(orderCodes)
                .scannedOrderCodes(scannedOrderCodes)
                .build();
    }

    private HandoverManifestResponse toResponse(
            HandoverManifest manifest,
            List<HandoverManifestOrder> manifestOrders,
            Vehicle vehicle,
            Route route
    ) {
        List<HandoverManifestOrderResponse> mappedOrders = manifestOrders.stream()
                .map(item -> new HandoverManifestOrderResponse(
                        item.getId(),
                        item.getTmsOrderId(),
                        item.getOrderCode(),
                        item.getScanOutTime(),
                        item.getScanInTime()
                ))
                .toList();

        return new HandoverManifestResponse(
                manifest.getId(),
                manifest.getManifestCode(),
                manifest.getOriginPostOfficeCode(),
                manifest.getTargetHubId(),
                manifest.getVehicleId(),
                vehicle == null ? null : vehicle.getLicensePlate(),
                manifest.getAssignedDriverId(),
                manifest.getRouteId(),
                route == null ? null : route.getRouteCode(),
                manifest.getPlannedDepartureAt(),
                manifest.getPlannedArrivalAt(),
                manifest.getOriginPostOfficeLatitude(),
                manifest.getOriginPostOfficeLongitude(),
                manifest.getDriverStartCheckinAt(),
                manifest.getDriverStartLatitude(),
                manifest.getDriverStartLongitude(),
                manifest.getDriverStartDistanceM(),
                manifest.getDriverEndCheckinAt(),
                manifest.getDriverEndLatitude(),
                manifest.getDriverEndLongitude(),
                manifest.getDriverEndDistanceM(),
                manifest.getStatus(),
                mappedOrders,
                manifest.getCreatedAt(),
                manifest.getUpdatedAt()
        );
    }

    private void validateOrderOriginAndOutboundStatus(TmsOrderOperationView order, String originPostOfficeCode) {
        if (!Objects.equals(originPostOfficeCode, normalizeText(order.getOriginPostOfficeCode()))) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "All orders in a manifest must have the same origin post office code."
            );
        }
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.AT_ORIGIN_POST_OFFICE && status != OrderStatus.OUTBOUND_READY_FROM_PO) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Order must be AT_ORIGIN_POST_OFFICE or OUTBOUND_READY_FROM_PO to create handover manifest."
            );
        }
    }

    private List<TmsOrderOperationView> lookupOrdersByCodes(Long tenantId, List<String> orderCodes) {
        Map<String, TmsOrderOperationView> orderByCode = new LinkedHashMap<>();
        List<TmsOrderOperationView> orders = tmsOrderClient.lookupByCodes(tenantId, orderCodes);
        for (TmsOrderOperationView order : orders) {
            validateTmsOrderTenant(tenantId, order);
            String normalizedOrderCode = normalizeText(order.getOrderCode());
            if (normalizedOrderCode != null) {
                orderByCode.put(normalizedOrderCode, order);
            }
        }

        List<TmsOrderOperationView> ordered = new ArrayList<>();
        for (String orderCode : orderCodes) {
            TmsOrderOperationView order = orderByCode.get(orderCode);
            if (order == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Some order codes do not exist in tms-order.");
            }
            ordered.add(order);
        }
        return ordered;
    }

    private void validateTmsOrderTenant(Long tenantId, TmsOrderOperationView order) {
        if (order == null || order.getId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid tms-order lookup response.");
        }
        if (order.getTenantId() != null && !Objects.equals(order.getTenantId(), tenantId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private HandoverManifestOrder toManifestOrder(
            HandoverManifest manifest,
            TmsOrderOperationView order,
            Long tenantId
    ) {
        HandoverManifestOrder manifestOrder = HandoverManifestOrder.builder()
                .manifest(manifest)
                .tmsOrderId(order.getId())
                .tenantId(tenantId)
                .build();
        updateManifestOrderSnapshot(manifestOrder, order);
        return manifestOrder;
    }

    private void updateManifestOrderSnapshot(HandoverManifestOrder manifestOrder, TmsOrderOperationView order) {
        manifestOrder.setTmsOrderId(order.getId());
        manifestOrder.setOrderCode(normalizeText(order.getOrderCode()));
        manifestOrder.setCustomerOrderCode(normalizeText(order.getCustomerOrderCode()));
        manifestOrder.setLastKnownStatus(statusName(order.getStatus()));
        manifestOrder.setOriginPostOfficeCode(normalizeText(order.getOriginPostOfficeCode()));
        manifestOrder.setDestinationPostOfficeCode(normalizeText(order.getDestinationPostOfficeCode()));
        manifestOrder.setTotalWeightSnapshot(safeDouble(order.getTotalWeight()));
        manifestOrder.setTotalVolumeSnapshot(safeDouble(order.getTotalVolume()));
    }

    private void enqueueManifestTransition(
            Long tenantId,
            String idempotencyKey,
            List<TmsOrderStatusTransitionRequest.Item> items
    ) {
        if (items == null || items.isEmpty()) {
            return;
        }
        tmsOrderTransitionOutboxService.enqueue(TmsOrderStatusTransitionRequest.builder()
                .source(TRANSITION_SOURCE)
                .idempotencyKey(idempotencyKey)
                .items(items)
                .build(), tenantId);
    }

    private TmsOrderStatusTransitionRequest.Item toTransitionItem(
            TmsOrderOperationView order,
            OrderStatus targetStatus,
            List<OrderStatus> expectedStatuses,
            String description,
            TmsOrderStatusTransitionRequest.Context context
    ) {
        return TmsOrderStatusTransitionRequest.Item.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .expectedStatuses(expectedStatuses)
                .targetStatus(targetStatus)
                .description(description)
                .context(context)
                .build();
    }

    private TmsOrderStatusTransitionRequest.Item toTransitionItem(
            HandoverManifestOrder manifestOrder,
            OrderStatus targetStatus,
            List<OrderStatus> expectedStatuses,
            String description,
            TmsOrderStatusTransitionRequest.Context context
    ) {
        return TmsOrderStatusTransitionRequest.Item.builder()
                .orderId(manifestOrder.getTmsOrderId())
                .orderCode(manifestOrder.getOrderCode())
                .expectedStatuses(expectedStatuses)
                .targetStatus(targetStatus)
                .description(description)
                .context(context)
                .build();
    }

    private TmsOrderStatusTransitionRequest.Context buildManifestContext(
            HandoverManifest manifest,
            Vehicle vehicle,
            Route route
    ) {
        Hub hub = manifest.getTargetHubId() == null
                ? null
                : hubRepository.findById(manifest.getTargetHubId()).orElse(null);
        return TmsOrderStatusTransitionRequest.Context.builder()
                .eventTime(LocalDateTime.now())
                .hubId(hub == null ? manifest.getTargetHubId() : hub.getId())
                .hubCode(hub == null ? null : hub.getCode())
                .hubName(hub == null ? null : hub.getName())
                .manifestId(manifest.getId())
                .manifestCode(manifest.getManifestCode())
                .routeId(route == null ? manifest.getRouteId() : route.getId())
                .routeCode(route == null ? null : route.getRouteCode())
                .driverId(manifest.getAssignedDriverId())
                .vehicleId(vehicle == null ? manifest.getVehicleId() : vehicle.getId())
                .vehicleLicensePlate(vehicle == null ? null : vehicle.getLicensePlate())
                .build();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private List<String> normalizeOrderCodes(List<String> orderCodes) {
        if (orderCodes == null || orderCodes.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String orderCode : orderCodes) {
            String normalizedCode = normalizeText(orderCode);
            if (normalizedCode != null) {
                normalized.add(normalizedCode);
            }
        }
        return new ArrayList<>(normalized);
    }

    private String idempotencyKey(Object... parts) {
        return java.util.Arrays.stream(parts)
                .filter(Objects::nonNull)
                .map(part -> part.toString().trim())
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(":"));
    }

    private String statusName(OrderStatus status) {
        return status == null ? null : status.name();
    }

    private double safeDouble(Double value) {
        return value == null || value < 0 ? 0.0 : value;
    }
}
