/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.HandoverManifest;
import serp.project.second_mile.domain.HandoverManifestOrder;
import serp.project.second_mile.domain.Order;
import serp.project.second_mile.dto.request.ConfirmHandoverInboundRequest;
import serp.project.second_mile.dto.request.CreateHandoverManifestRequest;
import serp.project.second_mile.dto.response.HandoverManifestOrderResponse;
import serp.project.second_mile.dto.response.HandoverManifestResponse;
import serp.project.second_mile.enums.HandoverManifestStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.kafka.OrderSyncEventPublisher;
import serp.project.second_mile.repository.HandoverManifestOrderRepository;
import serp.project.second_mile.repository.HandoverManifestRepository;
import serp.project.second_mile.repository.OrderRepository;
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
    private final SecondMileAccessUtils secondMileAccessUtils;
    private final OrderSyncEventPublisher orderSyncEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HandoverManifestResponse createManifest(CreateHandoverManifestRequest request) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        String normalizedOriginPostOfficeCode = normalizeText(request.getOriginPostOfficeCode());
        if (normalizedOriginPostOfficeCode == null || request.getTargetHubId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

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
        return toResponse(savedManifest, manifestOrders);
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

        manifest.setStatus(HandoverManifestStatus.OUTBOUND_CONFIRMED);
        HandoverManifest savedManifest = handoverManifestRepository.save(manifest);
        List<HandoverManifestOrder> manifestOrders = findManifestOrders(savedManifest.getId(), savedManifest.getTenantId());
        return toResponse(savedManifest, manifestOrders);
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
        return toResponse(savedManifest, allManifestOrders);
    }

    @Override
    @Transactional(readOnly = true)
    public HandoverManifestResponse getManifest(Long manifestId) {
        secondMileAccessUtils.ensureHubOperationRoleOrThrow();
        secondMileAccessUtils.ensureCurrentUserHasActiveHubStaffRoleOrThrow();

        HandoverManifest manifest = getManifestOrThrow(manifestId);
        List<HandoverManifestOrder> manifestOrders = findManifestOrders(manifest.getId(), manifest.getTenantId());
        return toResponse(manifest, manifestOrders);
    }

    private HandoverManifest getManifestOrThrow(Long manifestId) {
        Long tenantId = secondMileAccessUtils.getCurrentTenantIdOrThrow();
        return handoverManifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Handover manifest not found."));
    }

    private List<HandoverManifestOrder> findManifestOrders(Long manifestId, Long tenantId) {
        return handoverManifestOrderRepository.findByManifest_IdAndTenantId(manifestId, tenantId);
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

    private HandoverManifestResponse toResponse(HandoverManifest manifest, List<HandoverManifestOrder> manifestOrders) {
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
                manifest.getStatus(),
                mappedOrders,
                manifest.getCreatedAt(),
                manifest.getUpdatedAt()
        );
    }
}
