/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.Order;
import serp.project.second_mile.enums.OrderSyncEventSource;
import serp.project.second_mile.kafka.event.OrderSyncEvent;
import serp.project.second_mile.repository.OrderRepository;
import serp.project.second_mile.service.OrderSyncService;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderSyncServiceImpl implements OrderSyncService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void syncOrder(OrderSyncEvent orderSyncEvent) {
        if (orderSyncEvent == null || orderSyncEvent.getOrderCode() == null) {
            log.warn("Received invalid OrderSyncEvent");
            return;
        }
        if (orderSyncEvent.getEventSource() == OrderSyncEventSource.SECOND_MILE) {
            log.debug("Skip sync-order consume: event originated from second-mile orderCode={}",
                    orderSyncEvent.getOrderCode());
            return;
        }

        String orderCode = orderSyncEvent.getOrderCode();
        log.info("Start syncing order with orderCode: {}", orderCode);

        // Tìm kiếm order theo orderCode, nếu không có thì tạo mới một instance
        Order order = orderRepository.findByOrderCodeAndTenantId(orderCode, orderSyncEvent.getTenantId())
                .orElseGet(() -> {
                    log.info("OrderCode {} doesn't exist, need creating.", orderCode);
                    Order newOrder = new Order();
                    newOrder.setOrderCode(orderCode);
                    return newOrder;
                });

        // Ánh xạ dữ liệu từ Event sang Entity
        mapEventToOrder(orderSyncEvent, order);

        orderRepository.save(order);

        log.info("Order with orderCode: {} synced successfully", orderCode);
    }

    /**
     * Hàm hỗ trợ map dữ liệu từ OrderSyncEvent sang entity Order.
     */
    private void mapEventToOrder(OrderSyncEvent event, Order order) {
        order.setCustomerOrderCode(event.getCustomerOrderCode());
        order.setOriginPostOfficeCode(event.getOriginPostOfficeCode());
        order.setDestinationPostOfficeCode(event.getDestinationPostOfficeCode());
        order.setStatus(event.getStatus());
        order.setTotalWeight(event.getTotalWeight());
        order.setDimensions(event.getDimensions());
        order.setTotalVolume(event.getTotalVolume());
        order.setOrderProductCategory(event.getOrderProductCategory());
        order.setOrderType(event.getOrderType());
        order.setNote(event.getNote());
        order.setCreatedBy(event.getCreatedBy());
        order.setUpdatedBy(event.getUpdatedBy());
        order.setTenantId(event.getTenantId());
        order.setCreatedAt(event.getCreatedAt());
        order.setUpdatedAt(event.getUpdatedAt());
    }
}