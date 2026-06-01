/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeHandoverManifest;
import serp.project.first_mile.domain.PostOfficeHandoverManifestOrder;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.message.HandoverManifestSyncEvent;
import serp.project.first_mile.dto.message.HandoverManifestSyncEventType;
import serp.project.first_mile.dto.message.HandoverManifestSyncOrigin;
import serp.project.first_mile.dto.request.CreatePostOfficeHandoverManifestRequest;
import serp.project.first_mile.dto.request.DispatchPostOfficeHandoverManifestRequest;
import serp.project.first_mile.dto.request.ScanOutHandoverOrderRequest;
import serp.project.first_mile.dto.response.PostOfficeHandoverManifestOrderResponse;
import serp.project.first_mile.dto.response.PostOfficeHandoverManifestResponse;
import serp.project.first_mile.enums.HandoverManifestStatus;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kafka.HandoverManifestSyncEventPublisher;
import serp.project.first_mile.kafka.impl.order.SyncOrder;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.TransactionAfterCommit;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PostOfficeHandoverManifestOrderRepository;
import serp.project.first_mile.repository.PostOfficeHandoverManifestRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.service.OrderTimelineService;
import serp.project.first_mile.service.PostOfficeHandoverManifestService;
import serp.project.first_mile.service.dto.OrderTimelineContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
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
@Slf4j
@Transactional(readOnly = true)
public class PostOfficeHandoverManifestServiceImpl implements PostOfficeHandoverManifestService {
    private static final DateTimeFormatter MANIFEST_CODE_SUFFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Set<HandoverManifestStatus> ACTIVE_MANIFEST_STATUSES = Set.of(
            HandoverManifestStatus.CREATED,
            HandoverManifestStatus.OUTBOUND_CONFIRMED
    );

    private final FirstMileAccessUtils firstMileAccessUtils;
    private final PostOfficeRepository postOfficeRepository;
    private final OrderRepository orderRepository;
    private final PostOfficeHandoverManifestRepository manifestRepository;
    private final PostOfficeHandoverManifestOrderRepository manifestOrderRepository;
    private final OrderTimelineService orderTimelineService;
    private final SyncOrder syncOrder;
    private final HandoverManifestSyncEventPublisher handoverManifestSyncEventPublisher;

    @Override
    public PageResponse<PostOfficeHandoverManifestResponse> listManifests(
            int page,
            int size,
            Long postOfficeId,
            Long targetHubId,
            HandoverManifestStatus status
    ) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<PostOfficeHandoverManifest> manifestPage;
        if (firstMileAccessUtils.isAdmin()) {
            manifestPage = manifestRepository.findPage(tenantId, postOfficeId, targetHubId, status, pageable);
        } else if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);
            if (postOfficeId != null && !managedPostOfficeIds.contains(postOfficeId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            Collection<Long> visiblePostOfficeIds = postOfficeId == null ? managedPostOfficeIds : List.of(postOfficeId);
            if (visiblePostOfficeIds.isEmpty()) {
                return PageResponse.<PostOfficeHandoverManifestResponse>builder()
                        .items(List.of())
                        .page(safePage)
                        .size(safeSize)
                        .totalElements(0)
                        .totalPages(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build();
            }
            manifestPage = manifestRepository.findPageInPostOffices(
                    tenantId,
                    visiblePostOfficeIds,
                    targetHubId,
                    status,
                    pageable
            );
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return PageResponse.<PostOfficeHandoverManifestResponse>builder()
                .items(manifestPage.getContent().stream().map(this::toResponse).toList())
                .page(manifestPage.getNumber())
                .size(manifestPage.getSize())
                .totalElements(manifestPage.getTotalElements())
                .totalPages(manifestPage.getTotalPages())
                .hasNext(manifestPage.hasNext())
                .hasPrevious(manifestPage.hasPrevious())
                .build();
    }

    @Override
    public PostOfficeHandoverManifestResponse getManifest(Long manifestId) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        PostOfficeHandoverManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Handover manifest not found."));
        ensureCanOperateManifest(tenantId, manifest);
        return toResponse(manifest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeHandoverManifestResponse createManifest(CreatePostOfficeHandoverManifestRequest request) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        if (request == null || request.getPostOfficeId() == null || request.getPostOfficeId() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        PostOffice postOffice = postOfficeRepository.findByIdAndTenantIdForUpdate(request.getPostOfficeId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
        ensureCanOperatePostOffice(tenantId, postOffice);

        Long targetHubId = resolveTargetHubId(postOffice, request.getTargetHubId());
        List<String> normalizedOrderCodes = normalizeOrderCodes(request.getOrderCodes());
        if (normalizedOrderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "order_codes must not be empty.");
        }

        List<Order> orders = orderRepository.findByTenantIdAndUpperOrderCodeInWithLock(tenantId, normalizedOrderCodes);
        validateCreateOrders(tenantId, postOffice, normalizedOrderCodes, orders);

        String manifestCode = generateManifestCode(tenantId, postOffice.getCode(), targetHubId);
        PostOfficeHandoverManifest manifest = PostOfficeHandoverManifest.builder()
                .manifestCode(manifestCode)
                .originPostOfficeId(postOffice.getId())
                .originPostOfficeCode(normalizeText(postOffice.getCode()))
                .targetHubId(targetHubId)
                .status(HandoverManifestStatus.CREATED)
                .note(normalizeNullable(request.getNote()))
                .tenantId(tenantId)
                .build();
        PostOfficeHandoverManifest savedManifest = manifestRepository.save(manifest);

        List<PostOfficeHandoverManifestOrder> manifestOrders = new ArrayList<>();
        for (Order order : orders) {
            manifestOrders.add(PostOfficeHandoverManifestOrder.builder()
                    .manifest(savedManifest)
                    .order(order)
                    .tenantId(tenantId)
                    .build());
        }
        manifestOrderRepository.saveAll(manifestOrders);
        return toResponse(savedManifest, manifestOrders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeHandoverManifestResponse scanOrderOut(Long manifestId, ScanOutHandoverOrderRequest request) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        PostOfficeHandoverManifest manifest = resolveManifestForUpdate(manifestId, tenantId);
        ensureCanOperateManifest(tenantId, manifest);
        ensureManifestStatus(manifest, HandoverManifestStatus.CREATED);

        String orderCode = normalizeText(request == null ? null : request.getOrderCode());
        if (orderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        PostOfficeHandoverManifestOrder manifestOrder = manifestOrderRepository
                .findByManifest_IdAndOrder_OrderCodeIgnoreCaseAndTenantId(manifest.getId(), orderCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Order is not in this manifest."));

        if (manifestOrder.getScanOutTime() == null) {
            LocalDateTime now = LocalDateTime.now();
            manifestOrder.setScanOutTime(now);
            Order order = manifestOrder.getOrder();
            if (order != null && OrderStatus.AT_ORIGIN_POST_OFFICE.equals(order.getStatus())) {
                order.setStatus(OrderStatus.OUTBOUND_READY_FROM_PO);
                orderRepository.save(order);
                recordTimeline(order, manifest, OrderStatus.OUTBOUND_READY_FROM_PO, now, "Order scanned out from origin post office.");
                TransactionAfterCommit.run(() -> syncOrder.sendOrderEvent(order));
            }
            manifestOrderRepository.save(manifestOrder);
        }

        return toResponse(manifest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeHandoverManifestResponse dispatchManifest(
            Long manifestId,
            DispatchPostOfficeHandoverManifestRequest request
    ) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        PostOfficeHandoverManifest manifest = resolveManifestForUpdate(manifestId, tenantId);
        ensureCanOperateManifest(tenantId, manifest);
        ensureManifestStatus(manifest, HandoverManifestStatus.CREATED);

        List<PostOfficeHandoverManifestOrder> manifestOrders = findManifestOrders(manifest);
        if (manifestOrders.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Manifest has no orders.");
        }

        List<PostOfficeHandoverManifestOrder> unscannedOrders = manifestOrders.stream()
                .filter(item -> item.getScanOutTime() == null)
                .toList();
        if (!unscannedOrders.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "All manifest orders must be scanned out before dispatch.");
        }
        validateDispatchAssignment(request);
        Point originPostOfficeLocation = resolveRequiredOriginPostOfficeLocation(manifest);

        LocalDateTime now = LocalDateTime.now();
        manifest.setVehicleId(request.getVehicleId());
        manifest.setRouteId(request.getRouteId());
        manifest.setPlannedDepartureAt(request.getPlannedDepartureAt());
        manifest.setPlannedArrivalAt(request.getPlannedArrivalAt());
        manifest.setStatus(HandoverManifestStatus.OUTBOUND_CONFIRMED);
        manifest.setDispatchedAt(now);
        manifest.setSealCode(normalizeNullable(request.getSealCode()));
        manifest.setNote(normalizeNullable(request.getNote()));
        PostOfficeHandoverManifest savedManifest = manifestRepository.save(manifest);

        List<Order> orders = manifestOrders.stream()
                .map(PostOfficeHandoverManifestOrder::getOrder)
                .filter(Objects::nonNull)
                .toList();
        HandoverManifestSyncEvent event = toOutboundSyncEvent(savedManifest, manifestOrders, originPostOfficeLocation);
        TransactionAfterCommit.run(() -> {
            orders.forEach(syncOrder::sendOrderEvent);
            handoverManifestSyncEventPublisher.publish(event);
        });
        return toResponse(savedManifest, manifestOrders);
    }

    private void validateDispatchAssignment(DispatchPostOfficeHandoverManifestRequest request) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Dispatch assignment is required.");
        }
        if (request.getVehicleId() == null || request.getVehicleId() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "vehicle_id is required.");
        }
        if (request.getRouteId() == null || request.getRouteId() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "route_id is required.");
        }
        LocalDateTime plannedDepartureAt = request.getPlannedDepartureAt();
        LocalDateTime plannedArrivalAt = request.getPlannedArrivalAt();
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeHandoverManifestResponse cancelManifest(Long manifestId) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        PostOfficeHandoverManifest manifest = resolveManifestForUpdate(manifestId, tenantId);
        ensureCanOperateManifest(tenantId, manifest);
        ensureManifestStatus(manifest, HandoverManifestStatus.CREATED);

        List<PostOfficeHandoverManifestOrder> manifestOrders = findManifestOrders(manifest);
        List<Order> changedOrders = new ArrayList<>();
        for (PostOfficeHandoverManifestOrder manifestOrder : manifestOrders) {
            Order order = manifestOrder.getOrder();
            if (order != null && OrderStatus.OUTBOUND_READY_FROM_PO.equals(order.getStatus())) {
                order.setStatus(OrderStatus.AT_ORIGIN_POST_OFFICE);
                changedOrders.add(order);
            }
        }
        if (!changedOrders.isEmpty()) {
            orderRepository.saveAll(changedOrders);
        }
        manifest.setStatus(HandoverManifestStatus.CANCELLED);
        PostOfficeHandoverManifest savedManifest = manifestRepository.save(manifest);
        HandoverManifestSyncEvent event = toSyncEvent(
                savedManifest,
                manifestOrders,
                HandoverManifestSyncEventType.CANCELLED,
                HandoverManifestSyncOrigin.FIRST_MILE
        );
        TransactionAfterCommit.run(() -> {
            changedOrders.forEach(syncOrder::sendOrderEvent);
            handoverManifestSyncEventPublisher.publish(event);
        });
        return toResponse(savedManifest, manifestOrders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyInboundSync(HandoverManifestSyncEvent event) {
        if (event == null || !HandoverManifestSyncOrigin.SECOND_MILE.equals(event.getOrigin())) {
            return;
        }
        if (event.getTenantId() == null || normalizeText(event.getManifestCode()) == null) {
            log.warn("Skip inbound handover sync: invalid event {}", event);
            return;
        }

        PostOfficeHandoverManifest manifest = manifestRepository
                .findByTenantIdAndManifestCodeIgnoreCase(event.getTenantId(), event.getManifestCode())
                .orElse(null);
        if (manifest == null) {
            log.warn("Inbound handover sync ignored: manifest not found manifestCode={} tenantId={}",
                    event.getManifestCode(),
                    event.getTenantId());
            return;
        }

        List<PostOfficeHandoverManifestOrder> manifestOrders = findManifestOrders(manifest);
        Set<String> scannedCodes = normalizeOrderCodes(event.getScannedOrderCodes()).stream().collect(Collectors.toSet());
        LocalDateTime inboundTime = event.getInboundConfirmedAt() == null
                ? LocalDateTime.now()
                : event.getInboundConfirmedAt();
        if (!scannedCodes.isEmpty()) {
            for (PostOfficeHandoverManifestOrder manifestOrder : manifestOrders) {
                Order order = manifestOrder.getOrder();
                if (order != null && scannedCodes.contains(normalizeText(order.getOrderCode()))) {
                    manifestOrder.setScanInTime(inboundTime);
                }
            }
            manifestOrderRepository.saveAll(manifestOrders);
        }

        boolean allInbound = !manifestOrders.isEmpty()
                && manifestOrders.stream().allMatch(item -> item.getScanInTime() != null);
        if (HandoverManifestStatus.INBOUND_CONFIRMED.equals(event.getStatus()) || allInbound) {
            manifest.setStatus(HandoverManifestStatus.INBOUND_CONFIRMED);
            manifest.setInboundConfirmedAt(inboundTime);
            manifestRepository.save(manifest);
        }
    }

    private void validateCreateOrders(
            Long tenantId,
            PostOffice postOffice,
            List<String> expectedOrderCodes,
            List<Order> orders
    ) {
        if (orders.size() != expectedOrderCodes.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Some order codes do not exist in current tenant.");
        }

        Map<String, Order> orderByCode = orders.stream()
                .collect(Collectors.toMap(order -> normalizeText(order.getOrderCode()), Function.identity()));
        for (String expectedOrderCode : expectedOrderCodes) {
            Order order = orderByCode.get(expectedOrderCode);
            if (order == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Some order codes do not exist in current tenant.");
            }
            if (!OrderStatus.AT_ORIGIN_POST_OFFICE.equals(order.getStatus())) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "Only AT_ORIGIN_POST_OFFICE orders can be added to a post office handover manifest."
                );
            }
            if (!Objects.equals(normalizeText(postOffice.getCode()), normalizeText(order.getOriginPostOfficeCode()))) {
                throw new AppException(
                        ErrorCode.INVALID_REQUEST,
                        "All orders must belong to the manifest origin post office."
                );
            }
            if (manifestOrderRepository.existsByOrder_IdAndTenantIdAndManifest_StatusIn(
                    order.getId(),
                    tenantId,
                    ACTIVE_MANIFEST_STATUSES
            )) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Order is already in an active handover manifest.");
            }
        }
    }

    private Long resolveTargetHubId(PostOffice postOffice, Long requestedTargetHubId) {
        Long assignedHubId = postOffice.getHubId();
        if (assignedHubId == null || assignedHubId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Origin post office is not assigned to a hub.");
        }
        if (requestedTargetHubId != null && !Objects.equals(requestedTargetHubId, assignedHubId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Target hub must match the hub assigned to the post office.");
        }
        return assignedHubId;
    }

    private void ensureCanOperatePostOffice(Long tenantId, PostOffice postOffice) {
        if (firstMileAccessUtils.isAdmin()) {
            return;
        }
        if (firstMileAccessUtils.isPostOfficerManager()) {
            firstMileAccessUtils.ensureCurrentManagerAssignedToPostOfficeOrThrow(postOffice.getId(), tenantId);
            return;
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void ensureCanOperateManifest(Long tenantId, PostOfficeHandoverManifest manifest) {
        if (firstMileAccessUtils.isAdmin()) {
            return;
        }
        if (firstMileAccessUtils.isPostOfficerManager()) {
            firstMileAccessUtils.ensureCurrentManagerAssignedToPostOfficeOrThrow(
                    manifest.getOriginPostOfficeId(),
                    tenantId
            );
            return;
        }
        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private PostOfficeHandoverManifest resolveManifestForUpdate(Long manifestId, Long tenantId) {
        if (manifestId == null || manifestId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return manifestRepository.findByIdAndTenantIdForUpdate(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Handover manifest not found."));
    }

    private void ensureManifestStatus(PostOfficeHandoverManifest manifest, HandoverManifestStatus expectedStatus) {
        if (!expectedStatus.equals(manifest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Current manifest status does not allow this action.");
        }
    }

    private void recordTimeline(
            Order order,
            PostOfficeHandoverManifest manifest,
            OrderStatus status,
            LocalDateTime eventTime,
            String description
    ) {
        orderTimelineService.recordStatusEvent(
                order,
                status,
                description,
                new OrderTimelineContext(
                        eventTime,
                        null,
                        manifest.getManifestCode(),
                        manifest.getOriginPostOfficeId(),
                        manifest.getOriginPostOfficeCode(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Post office handover manifest"
                )
        );
    }

    private PostOfficeHandoverManifestResponse toResponse(PostOfficeHandoverManifest manifest) {
        return toResponse(manifest, findManifestOrders(manifest));
    }

    private PostOfficeHandoverManifestResponse toResponse(
            PostOfficeHandoverManifest manifest,
            List<PostOfficeHandoverManifestOrder> manifestOrders
    ) {
        List<PostOfficeHandoverManifestOrderResponse> orderResponses = manifestOrders.stream()
                .map(this::toOrderResponse)
                .toList();
        int scannedOutOrders = (int) manifestOrders.stream().filter(item -> item.getScanOutTime() != null).count();
        int scannedInOrders = (int) manifestOrders.stream().filter(item -> item.getScanInTime() != null).count();
        return new PostOfficeHandoverManifestResponse(
                manifest.getId(),
                manifest.getManifestCode(),
                manifest.getOriginPostOfficeId(),
                manifest.getOriginPostOfficeCode(),
                manifest.getTargetHubId(),
                manifest.getVehicleId(),
                manifest.getRouteId(),
                manifest.getPlannedDepartureAt(),
                manifest.getPlannedArrivalAt(),
                manifest.getStatus(),
                manifestOrders.size(),
                scannedOutOrders,
                scannedInOrders,
                manifest.getDispatchedAt(),
                manifest.getInboundConfirmedAt(),
                manifest.getSealCode(),
                manifest.getNote(),
                orderResponses,
                manifest.getCreatedAt(),
                manifest.getUpdatedAt()
        );
    }

    private PostOfficeHandoverManifestOrderResponse toOrderResponse(PostOfficeHandoverManifestOrder manifestOrder) {
        Order order = manifestOrder.getOrder();
        return new PostOfficeHandoverManifestOrderResponse(
                manifestOrder.getId(),
                order == null ? null : order.getId(),
                order == null ? null : order.getOrderCode(),
                order == null ? null : order.getCustomerOrderCode(),
                order == null ? null : order.getStatus(),
                manifestOrder.getScanOutTime(),
                manifestOrder.getScanInTime()
        );
    }

    private List<PostOfficeHandoverManifestOrder> findManifestOrders(PostOfficeHandoverManifest manifest) {
        return manifestOrderRepository.findByManifest_IdAndTenantId(manifest.getId(), manifest.getTenantId());
    }

    private HandoverManifestSyncEvent toOutboundSyncEvent(
            PostOfficeHandoverManifest manifest,
            List<PostOfficeHandoverManifestOrder> manifestOrders,
            Point originPostOfficeLocation
    ) {
        return toSyncEvent(
                manifest,
                manifestOrders,
                HandoverManifestSyncEventType.OUTBOUND_CONFIRMED,
                HandoverManifestSyncOrigin.FIRST_MILE,
                originPostOfficeLocation
        );
    }

    private HandoverManifestSyncEvent toSyncEvent(
            PostOfficeHandoverManifest manifest,
            List<PostOfficeHandoverManifestOrder> manifestOrders,
            HandoverManifestSyncEventType eventType,
            HandoverManifestSyncOrigin origin
    ) {
        return toSyncEvent(manifest, manifestOrders, eventType, origin, resolveOriginPostOfficeLocation(manifest));
    }

    private HandoverManifestSyncEvent toSyncEvent(
            PostOfficeHandoverManifest manifest,
            List<PostOfficeHandoverManifestOrder> manifestOrders,
            HandoverManifestSyncEventType eventType,
            HandoverManifestSyncOrigin origin,
            Point originPostOfficeLocation
    ) {
        List<String> orderCodes = manifestOrders.stream()
                .map(PostOfficeHandoverManifestOrder::getOrder)
                .filter(Objects::nonNull)
                .map(Order::getOrderCode)
                .filter(Objects::nonNull)
                .toList();
        List<String> scannedOrderCodes = manifestOrders.stream()
                .filter(item -> item.getScanOutTime() != null || item.getScanInTime() != null)
                .map(PostOfficeHandoverManifestOrder::getOrder)
                .filter(Objects::nonNull)
                .map(Order::getOrderCode)
                .filter(Objects::nonNull)
                .toList();
        return HandoverManifestSyncEvent.builder()
                .eventType(eventType)
                .origin(origin)
                .tenantId(manifest.getTenantId())
                .manifestCode(manifest.getManifestCode())
                .originPostOfficeCode(manifest.getOriginPostOfficeCode())
                .targetHubId(manifest.getTargetHubId())
                .vehicleId(manifest.getVehicleId())
                .routeId(manifest.getRouteId())
                .plannedDepartureAt(manifest.getPlannedDepartureAt())
                .plannedArrivalAt(manifest.getPlannedArrivalAt())
                .originPostOfficeLatitude(originPostOfficeLocation == null ? null : originPostOfficeLocation.getY())
                .originPostOfficeLongitude(originPostOfficeLocation == null ? null : originPostOfficeLocation.getX())
                .status(manifest.getStatus())
                .dispatchedAt(manifest.getDispatchedAt())
                .inboundConfirmedAt(manifest.getInboundConfirmedAt())
                .orderCodes(orderCodes)
                .scannedOrderCodes(scannedOrderCodes)
                .sealCode(manifest.getSealCode())
                .note(manifest.getNote())
                .build();
    }

    private Point resolveOriginPostOfficeLocation(PostOfficeHandoverManifest manifest) {
        if (manifest == null || manifest.getOriginPostOfficeId() == null || manifest.getTenantId() == null) {
            return null;
        }
        return postOfficeRepository.findByIdAndTenantId(manifest.getOriginPostOfficeId(), manifest.getTenantId())
                .map(PostOffice::getLocation)
                .orElse(null);
    }

    private Point resolveRequiredOriginPostOfficeLocation(PostOfficeHandoverManifest manifest) {
        Point location = resolveOriginPostOfficeLocation(manifest);
        if (location == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Origin post office location is required for driver check-in."
            );
        }
        return location;
    }

    private String generateManifestCode(Long tenantId, String postOfficeCode, Long hubId) {
        String normalizedPostOfficeCode = normalizeText(postOfficeCode);
        String suffix = LocalDateTime.now().format(MANIFEST_CODE_SUFFIX_FORMATTER);
        String candidate = String.format("POH-%d-%s-%d-%s", tenantId, normalizedPostOfficeCode, hubId, suffix);
        if (!manifestRepository.existsByTenantIdAndManifestCodeIgnoreCase(tenantId, candidate)) {
            return candidate;
        }
        return candidate + "-" + System.currentTimeMillis() % 1000;
    }

    private List<String> normalizeOrderCodes(List<String> orderCodes) {
        if (orderCodes == null || orderCodes.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String orderCode : orderCodes) {
            String normalizedCode = normalizeText(orderCode);
            if (normalizedCode != null) {
                normalized.add(normalizedCode);
            }
        }
        return new ArrayList<>(normalized);
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

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
