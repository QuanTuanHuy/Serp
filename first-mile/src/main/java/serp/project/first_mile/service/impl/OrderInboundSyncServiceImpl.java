/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.OrderSyncEventSource;
import serp.project.first_mile.kafka.event.OrderSyncEvent;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.service.OrderInboundSyncService;

import java.util.EnumSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderInboundSyncServiceImpl implements OrderInboundSyncService {

    private static final Set<OrderStatus> SECOND_MILE_MAIN_STATUSES = EnumSet.of(
            OrderStatus.OUTBOUND_READY_FROM_PO,
            OrderStatus.INBOUND_AT_ORIGIN_HUB,
            OrderStatus.BAGGING_IN_PROGRESS,
            OrderStatus.BAGGED,
            OrderStatus.BAG_SEALED
    );

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void applyInboundStatus(OrderSyncEvent event) {
        if (event == null || event.getOrderCode() == null || event.getTenantId() == null) {
            log.warn("Skip inbound order sync: missing orderCode or tenantId");
            return;
        }
        if (!isFromSecondMile(event)) {
            log.debug("Skip inbound order sync: event_source={}", event.getEventSource());
            return;
        }
        if (event.getStatus() == null || !SECOND_MILE_MAIN_STATUSES.contains(event.getStatus())) {
            log.debug("Skip inbound order sync: status {} not a second-mile main status", event.getStatus());
            return;
        }

        String orderCode = event.getOrderCode();
        Long tenantId = event.getTenantId();
        Order order = orderRepository.findByOrderCodeAndTenantId(orderCode, tenantId)
                .orElse(null);
        if (order == null) {
            log.warn("Inbound order sync: order not found orderCode={} tenantId={}", orderCode, tenantId);
            return;
        }

        OrderStatus current = order.getStatus();
        OrderStatus incoming = event.getStatus();
        if (!shouldApplyStatus(current, incoming)) {
            log.info("Inbound order sync skipped (status not advanced): orderCode={} current={} incoming={}",
                    orderCode, current, incoming);
            return;
        }

        order.setStatus(incoming);
        if (event.getUpdatedAt() != null) {
            order.setUpdatedAt(event.getUpdatedAt());
        }
        if (event.getUpdatedBy() != null) {
            order.setUpdatedBy(event.getUpdatedBy());
        }
        orderRepository.save(order);
        log.info("Inbound order status synced: orderCode={} status={}", orderCode, incoming);
    }

    private static boolean isFromSecondMile(OrderSyncEvent event) {
        return event.getEventSource() == OrderSyncEventSource.SECOND_MILE;
    }

    private static boolean shouldApplyStatus(OrderStatus current, OrderStatus incoming) {
        if (incoming == null) {
            return false;
        }
        if (current == incoming) {
            return true;
        }
        if (current == OrderStatus.CANCELLED || current == OrderStatus.LOST_OR_DAMAGED) {
            return false;
        }
        if (current == null) {
            return incoming == OrderStatus.OUTBOUND_READY_FROM_PO;
        }
        if (current.ordinal() < OrderStatus.AT_ORIGIN_POST_OFFICE.ordinal()) {
            return incoming == OrderStatus.OUTBOUND_READY_FROM_PO;
        }
        return incoming.ordinal() >= current.ordinal();
    }
}
