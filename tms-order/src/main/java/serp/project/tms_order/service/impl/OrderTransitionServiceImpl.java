/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.domain.OrderTransitionLog;
import serp.project.tms_order.dto.request.InternalOrderStatusTransitionRequest;
import serp.project.tms_order.dto.response.OrderStatusTransitionResponse;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kafka.OrderNotificationEventPublisher;
import serp.project.tms_order.kafka.OrderSyncEventPublisher;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.OrderTransitionLogRepository;
import serp.project.tms_order.service.OrderTimelineService;
import serp.project.tms_order.service.OrderTransitionService;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderTransitionServiceImpl implements OrderTransitionService {

    private static final Map<OrderStatus, EnumSet<OrderStatus>> ALLOWED_PREVIOUS_STATUSES =
            new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.CREATED,
                EnumSet.of(OrderStatus.ASSIGNED_TO_PICKUP)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.ASSIGNED_TO_PICKUP,
                EnumSet.of(OrderStatus.CREATED, OrderStatus.PICKUP_FAILED)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.PICKING_UP,
                EnumSet.of(OrderStatus.ASSIGNED_TO_PICKUP, OrderStatus.PICKING_UP)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.PICKUP_FAILED,
                EnumSet.of(OrderStatus.ASSIGNED_TO_PICKUP, OrderStatus.PICKING_UP)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND,
                EnumSet.of(OrderStatus.PICKING_UP, OrderStatus.PICKED_UP)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.AT_ORIGIN_POST_OFFICE,
                EnumSet.of(OrderStatus.PENDING_ORIGIN_POST_OFFICE_INBOUND, OrderStatus.OUTBOUND_READY_FROM_PO)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.OUTBOUND_READY_FROM_PO,
                EnumSet.of(OrderStatus.AT_ORIGIN_POST_OFFICE)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.INBOUND_AT_ORIGIN_HUB,
                EnumSet.of(OrderStatus.OUTBOUND_READY_FROM_PO, OrderStatus.BAGGED)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.BAGGING_IN_PROGRESS,
                EnumSet.of(OrderStatus.INBOUND_AT_ORIGIN_HUB, OrderStatus.INBOUND_AT_DESTINATION_HUB)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.BAGGED,
                EnumSet.of(
                        OrderStatus.INBOUND_AT_ORIGIN_HUB,
                        OrderStatus.INBOUND_AT_DESTINATION_HUB,
                        OrderStatus.BAGGING_IN_PROGRESS,
                        OrderStatus.BAG_SEALED
                )
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.BAG_SEALED,
                EnumSet.of(OrderStatus.BAGGED)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.BAG_IN_TRANSIT,
                EnumSet.of(OrderStatus.BAG_SEALED, OrderStatus.INBOUND_AT_DESTINATION_HUB)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.INBOUND_AT_DESTINATION_HUB,
                EnumSet.of(OrderStatus.BAG_IN_TRANSIT)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE,
                EnumSet.of(OrderStatus.BAG_IN_TRANSIT)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.READY_FOR_DELIVERY,
                EnumSet.of(OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.OUT_FOR_DELIVERY,
                EnumSet.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERY_FAILED)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.DELIVERED,
                EnumSet.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.OUT_FOR_DELIVERY)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.DELIVERY_FAILED,
                EnumSet.of(OrderStatus.OUT_FOR_DELIVERY)
        );
        ALLOWED_PREVIOUS_STATUSES.put(
                OrderStatus.RETURNED_TO_SENDER,
                EnumSet.of(OrderStatus.DELIVERY_FAILED)
        );
    }

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final OrderTransitionLogRepository transitionLogRepository;
    private final OrderTimelineService orderTimelineService;
    private final OrderSyncEventPublisher orderSyncEventPublisher;
    private final OrderNotificationEventPublisher orderNotificationEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderStatusTransitionResponse applyTransitions(
            InternalOrderStatusTransitionRequest request,
            Long tenantId
    ) {
        validateRequest(request);

        String idempotencyKey = normalizeText(request.getIdempotencyKey());
        OrderTransitionLog existingLog = transitionLogRepository
                .findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey)
                .orElse(null);
        if (existingLog != null) {
            return deserializeResponse(existingLog);
        }

        OrderStatusTransitionResponse response = applyTransitionItems(request, tenantId, idempotencyKey);
        saveTransitionLog(request, tenantId, idempotencyKey, response);
        return response;
    }

    private OrderStatusTransitionResponse applyTransitionItems(
            InternalOrderStatusTransitionRequest request,
            Long tenantId,
            String idempotencyKey
    ) {
        List<OrderStatusTransitionResponse.Item> results = new ArrayList<>();
        List<Order> changedOrders = new ArrayList<>();

        for (InternalOrderStatusTransitionRequest.Item item : request.getItems()) {
            validateItem(item);
            Order order = lockOrder(item, tenantId);
            OrderStatus previousStatus = order.getStatus();
            OrderStatus targetStatus = item.getTargetStatus();

            validateExpectedStatus(item, previousStatus);
            boolean changed = !targetStatus.equals(previousStatus);
            if (changed) {
                validateLifecycleTransition(previousStatus, targetStatus);
                order.setStatus(targetStatus);
                applyCurrentHub(order, targetStatus, item.getContext());
                Order savedOrder = orderRepository.save(order);
                changedOrders.add(savedOrder);
                orderTimelineService.recordStatusEvent(
                        savedOrder,
                        targetStatus,
                        item.getDescription(),
                        item.getContext()
                );
                publishStatusNotification(savedOrder, targetStatus, item.getContext());
                results.add(toResult(savedOrder, previousStatus, targetStatus, true));
            } else {
                if (Boolean.TRUE.equals(item.getRecordTimelineWhenUnchanged())) {
                    orderTimelineService.recordStatusEvent(
                            order,
                            targetStatus,
                            item.getDescription(),
                            item.getContext()
                    );
                    publishStatusNotification(order, targetStatus, item.getContext());
                }
                results.add(toResult(order, previousStatus, targetStatus, false));
            }
        }

        if (!changedOrders.isEmpty()) {
            changedOrders.forEach(orderSyncEventPublisher::publish);
        }

        return new OrderStatusTransitionResponse(
                normalizeText(request.getSource()),
                idempotencyKey,
                results
        );
    }

    private void saveTransitionLog(
            InternalOrderStatusTransitionRequest request,
            Long tenantId,
            String idempotencyKey,
            OrderStatusTransitionResponse response
    ) {
        try {
            transitionLogRepository.save(OrderTransitionLog.builder()
                    .tenantId(tenantId)
                    .idempotencyKey(idempotencyKey)
                    .source(normalizeText(request.getSource()))
                    .requestPayload(objectMapper.writeValueAsString(request))
                    .responsePayload(objectMapper.writeValueAsString(response))
                    .build());
        } catch (DataIntegrityViolationException exception) {
            OrderTransitionLog existingLog = transitionLogRepository
                    .findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey)
                    .orElseThrow(() -> exception);
            deserializeResponse(existingLog);
        } catch (JsonProcessingException exception) {
            log.error("Cannot serialize order transition response idempotencyKey={}", idempotencyKey, exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private Order lockOrder(InternalOrderStatusTransitionRequest.Item item, Long tenantId) {
        if (item.getOrderId() != null && item.getOrderId() > 0) {
            return orderRepository.findByIdAndTenantIdForUpdate(item.getOrderId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        }

        String orderCode = normalizeText(item.getOrderCode());
        if (orderCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return orderRepository.findByOrderCodeAndTenantIdForUpdate(orderCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private OrderStatusTransitionResponse.Item toResult(
            Order order,
            OrderStatus previousStatus,
            OrderStatus targetStatus,
            boolean changed
    ) {
        return new OrderStatusTransitionResponse.Item(
                order.getId(),
                order.getOrderCode(),
                previousStatus,
                targetStatus,
                order.getStatus(),
                changed,
                changed ? "Transition applied." : "Order already has target status."
        );
    }

    private void validateRequest(InternalOrderStatusTransitionRequest request) {
        if (request == null
                || !hasText(request.getSource())
                || !hasText(request.getIdempotencyKey())
                || request.getItems() == null
                || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateItem(InternalOrderStatusTransitionRequest.Item item) {
        if (item == null || item.getTargetStatus() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        boolean hasOrderId = item.getOrderId() != null && item.getOrderId() > 0;
        boolean hasOrderCode = hasText(item.getOrderCode());
        if (!hasOrderId && !hasOrderCode) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateExpectedStatus(
            InternalOrderStatusTransitionRequest.Item item,
            OrderStatus previousStatus
    ) {
        List<OrderStatus> expectedStatuses = item.getExpectedStatuses();
        if (expectedStatuses == null || expectedStatuses.isEmpty()) {
            return;
        }
        if (!expectedStatuses.contains(previousStatus)) {
            throw new AppException(
                    ErrorCode.ORDER_NOT_ASSIGNABLE,
                    String.format(
                            Locale.ROOT,
                            "Order status '%s' is not one of expected statuses %s.",
                            previousStatus,
                            expectedStatuses
                    )
            );
        }
    }

    private void validateLifecycleTransition(OrderStatus previousStatus, OrderStatus targetStatus) {
        EnumSet<OrderStatus> allowedPreviousStatuses = ALLOWED_PREVIOUS_STATUSES.get(targetStatus);
        if (allowedPreviousStatuses == null || !allowedPreviousStatuses.contains(previousStatus)) {
            throw new AppException(
                    ErrorCode.ORDER_NOT_ASSIGNABLE,
                    String.format(
                            Locale.ROOT,
                            "Transition from '%s' to '%s' is not allowed.",
                            previousStatus,
                            targetStatus
                    )
            );
        }
    }

    private void applyCurrentHub(
            Order order,
            OrderStatus targetStatus,
            InternalOrderStatusTransitionRequest.Context context
    ) {
        if (targetStatus == OrderStatus.INBOUND_AT_ORIGIN_HUB
                || targetStatus == OrderStatus.INBOUND_AT_DESTINATION_HUB) {
            if (context != null && context.getHubId() != null) {
                order.setCurrentHubId(context.getHubId());
                order.setCurrentHubCode(context.getHubCode());
            }
            return;
        }

        if (targetStatus == OrderStatus.AT_ORIGIN_POST_OFFICE
                || targetStatus == OrderStatus.OUTBOUND_READY_FROM_PO
                || targetStatus == OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE
                || targetStatus == OrderStatus.READY_FOR_DELIVERY
                || targetStatus == OrderStatus.OUT_FOR_DELIVERY
                || targetStatus == OrderStatus.DELIVERED
                || targetStatus == OrderStatus.DELIVERY_FAILED
                || targetStatus == OrderStatus.RETURNED_TO_SENDER
                || targetStatus == OrderStatus.CANCELLED) {
            order.setCurrentHubId(null);
            order.setCurrentHubCode(null);
        }
    }

    private void publishStatusNotification(
            Order order,
            OrderStatus targetStatus,
            InternalOrderStatusTransitionRequest.Context context
    ) {
        orderNotificationEventPublisher.publishOrderStatusTransition(order, targetStatus, context);
    }

    private OrderStatusTransitionResponse deserializeResponse(OrderTransitionLog transitionLog) {
        try {
            return objectMapper.readValue(transitionLog.getResponsePayload(), OrderStatusTransitionResponse.class);
        } catch (JsonProcessingException exception) {
            log.error("Cannot deserialize order transition log id={}", transitionLog.getId(), exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
