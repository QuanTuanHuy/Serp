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
import serp.project.second_mile.domain.HandoverManifest;
import serp.project.second_mile.domain.HandoverManifestOrder;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.domain.Order;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.ConfirmHandoverInboundRequest;
import serp.project.second_mile.dto.request.CreateHandoverManifestRequest;
import serp.project.second_mile.dto.request.HandoverManifestFilterRequest;
import serp.project.second_mile.dto.response.HandoverManifestOrderResponse;
import serp.project.second_mile.dto.response.HandoverManifestResponse;
import serp.project.second_mile.enums.HandoverManifestStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.kafka.OrderSyncEventPublisher;
import serp.project.second_mile.repository.HandoverManifestOrderRepository;
import serp.project.second_mile.repository.HandoverManifestRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.OrderRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.repository.specification.HandoverManifestSpecification;
import serp.project.second_mile.service.HandoverManifestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HandoverManifestServiceImpl implements HandoverManifestService {
    private static final DateTimeFormatter MANIFEST_CODE_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final HandoverManifestRepository handoverManifestRepository;
    private final HandoverManifestOrderRepository handoverManifestOrderRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final HubPostOfficeMappingRepository hubPostOfficeMappingRepository;
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final OrderSyncEventPublisher orderSyncEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HandoverManifestResponse createManifest(CreateHandoverManifestRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        String normalizedOriginPostOfficeCode = normalizeText(request.getOriginPostOfficeCode());
        if (normalizedOriginPostOfficeCode == null
                || request.getTargetHubId() == null
                || request.getVehicleId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        validatePostOfficeMappedToHub(tenantId, normalizedOriginPostOfficeCode, request.getTargetHubId());
        Vehicle vehicle = validateVehicleForManifest(tenantId, request.getTargetHubId(), request.getVehicleId());
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

        List<Order> orders = orderRepository.findByTenantIdAndOrderCodeIn(tenantId, normalizedOrderCodes);
        if (orders.size() != normalizedOrderCodes.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Some order codes do not exist in current tenant.");
        }

        for (Order order : orders) {
            if (!Objects.equals(normalizedOriginPostOfficeCode, normalizeText(order.getOriginPostOfficeCode()))) {
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
            order.setStatus(OrderStatus.OUTBOUND_READY_FROM_PO);
        }

        String manifestCode = generateManifestCode(tenantId, request.getTargetHubId());
        HandoverManifest manifest = HandoverManifest.builder()
                .manifestCode(manifestCode)
                .originPostOfficeCode(normalizedOriginPostOfficeCode)
                .targetHubId(request.getTargetHubId())
                .vehicleId(vehicle.getId())
                .routeId(route == null ? null : route.getId())
                .status(HandoverManifestStatus.CREATED)
                .tenantId(tenantId)
                .build();
        HandoverManifest savedManifest = handoverManifestRepository.save(manifest);

        LocalDateTime now = LocalDateTime.now();
        List<HandoverManifestOrder> manifestOrders = new ArrayList<>();
        for (Order order : orders) {
            HandoverManifestOrder manifestOrder = HandoverManifestOrder.builder()
                    .manifest(savedManifest)
                    .order(order)
                    .scanOutTime(now)
                    .tenantId(tenantId)
                    .build();
            manifestOrders.add(manifestOrder);
        }

        orderRepository.saveAll(orders);
        handoverManifestOrderRepository.saveAll(manifestOrders);
        orderSyncEventPublisher.publishAll(orders);
        return toResponse(savedManifest, manifestOrders, vehicle, route);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HandoverManifestResponse> listManifests(
            int page,
            int size,
            HandoverManifestFilterRequest filterRequest
    ) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<HandoverManifest> manifestPage = handoverManifestRepository.findAll(
                HandoverManifestSpecification.byFilter(tenantId, filterRequest),
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
        if (manifest.getStatus() == HandoverManifestStatus.CANCELLED
                || manifest.getStatus() == HandoverManifestStatus.INBOUND_CONFIRMED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }
        if (manifest.getVehicleId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Manifest must have an assigned vehicle before departure.");
        }

        manifest.setStatus(HandoverManifestStatus.OUTBOUND_CONFIRMED);
        HandoverManifest savedManifest = handoverManifestRepository.save(manifest);
        List<HandoverManifestOrder> manifestOrders = findManifestOrders(savedManifest.getId(), savedManifest.getTenantId());
        return toResponse(
                savedManifest,
                manifestOrders,
                loadVehicle(savedManifest.getVehicleId()),
                loadRoute(savedManifest.getRouteId())
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HandoverManifestResponse confirmInbound(Long manifestId, ConfirmHandoverInboundRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        HandoverManifest manifest = getManifestOrThrow(manifestId);
        if (manifest.getStatus() == HandoverManifestStatus.CANCELLED) {
            throw new AppException(ErrorCode.BAG_STATUS_INVALID);
        }

        Long tenantId = manifest.getTenantId();
        List<HandoverManifestOrder> targets;
        if (request == null || request.getOrderCodes() == null || request.getOrderCodes().isEmpty()) {
            targets = findManifestOrders(manifest.getId(), tenantId);
        } else {
            List<String> normalizedCodes = normalizeOrderCodes(request.getOrderCodes());
            targets = handoverManifestOrderRepository.findByManifest_IdAndOrder_OrderCodeInAndTenantId(
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
        }

        Map<Long, Order> orderById = targets.stream()
                .map(HandoverManifestOrder::getOrder)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Order::getId, Function.identity(), (left, right) -> left));

        for (Order order : orderById.values()) {
            order.setStatus(OrderStatus.INBOUND_AT_ORIGIN_HUB);
        }
        orderRepository.saveAll(orderById.values());
        handoverManifestOrderRepository.saveAll(targets);
        orderSyncEventPublisher.publishAll(orderById.values());

        List<HandoverManifestOrder> allManifestOrders = findManifestOrders(manifest.getId(), tenantId);
        boolean allInboundScanned = allManifestOrders.stream().allMatch(item -> item.getScanInTime() != null);
        if (allInboundScanned) {
            manifest.setStatus(HandoverManifestStatus.INBOUND_CONFIRMED);
        } else {
            manifest.setStatus(HandoverManifestStatus.OUTBOUND_CONFIRMED);
        }
        HandoverManifest savedManifest = handoverManifestRepository.save(manifest);
        return toResponse(
                savedManifest,
                allManifestOrders,
                loadVehicle(savedManifest.getVehicleId()),
                loadRoute(savedManifest.getRouteId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HandoverManifestResponse getManifest(Long manifestId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        HandoverManifest manifest = getManifestOrThrow(manifestId);
        List<HandoverManifestOrder> manifestOrders = findManifestOrders(manifest.getId(), manifest.getTenantId());
        return toResponse(
                manifest,
                manifestOrders,
                loadVehicle(manifest.getVehicleId()),
                loadRoute(manifest.getRouteId())
        );
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

    private Route validateRouteForManifest(
            Long tenantId,
            Long targetHubId,
            String originPostOfficeCode,
            Long routeId,
            Long vehicleId
    ) {
        if (routeId == null) {
            return null;
        }

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));

        if (!tenantId.equals(route.getTenantId()) || route.getStatus() != RouteStatus.ACTIVE) {
            throw new AppException(ErrorCode.ROUTE_DEFINITION_INVALID);
        }
        if (!Objects.equals(route.getOriginHubId(), targetHubId)) {
            throw new AppException(ErrorCode.ROUTE_HUB_INVALID);
        }
        if (route.getDestinationType() != RouteDestinationType.POST_OFFICE) {
            throw new AppException(
                    ErrorCode.ROUTE_DEFINITION_INVALID,
                    "Route must target a post office for post office collection runs."
            );
        }
        if (!Objects.equals(normalizeText(route.getDestinationPostOfficeCode()), originPostOfficeCode)) {
            throw new AppException(
                    ErrorCode.ROUTE_POST_OFFICE_INVALID,
                    "Route destination post office must match manifest origin post office."
            );
        }
        if (route.getVehicleId() != null && !Objects.equals(route.getVehicleId(), vehicleId)) {
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

    private HandoverManifestResponse toResponse(
            HandoverManifest manifest,
            List<HandoverManifestOrder> manifestOrders,
            Vehicle vehicle,
            Route route
    ) {
        List<HandoverManifestOrderResponse> mappedOrders = manifestOrders.stream()
                .map(item -> new HandoverManifestOrderResponse(
                        item.getId(),
                        item.getOrder() == null ? null : item.getOrder().getId(),
                        item.getOrder() == null ? null : item.getOrder().getOrderCode(),
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
                manifest.getRouteId(),
                route == null ? null : route.getRouteCode(),
                manifest.getStatus(),
                mappedOrders,
                manifest.getCreatedAt(),
                manifest.getUpdatedAt()
        );
    }
}
