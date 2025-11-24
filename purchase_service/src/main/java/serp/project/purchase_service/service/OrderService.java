package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import serp.project.purchase_service.constant.OrderStatus;
import serp.project.purchase_service.constant.ShipmentStatus;
import serp.project.purchase_service.dto.request.OrderCreationForm;
import serp.project.purchase_service.dto.request.OrderUpdateForm;
import serp.project.purchase_service.entity.OrderEntity;
import serp.project.purchase_service.entity.OrderItemEntity;
import serp.project.purchase_service.entity.ShipmentEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.OrderRepository;
import serp.project.purchase_service.repository.specification.OrderSpecification;
import serp.project.purchase_service.util.IdUtils;
import serp.project.purchase_service.util.PaginationUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final ShipmentService shipmentService;
    private final InventoryItemDetailService inventoryItemDetailService;

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderCreationForm form, Long userId, Long tenantId) {
        String orderId = IdUtils.generateOrderId();
        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .orderTypeId("PURCHASE")
                .fromSupplierId(form.getFromSupplierId())
                .createdByUserId(userId)
                .orderDate(LocalDate.now())
                .statusId(OrderStatus.CREATED.value())
                .deliveryBeforeDate(form.getDeliveryBeforeDate())
                .deliveryAfterDate(form.getDeliveryAfterDate())
                .note(form.getNote())
                .orderName(StringUtils.hasText(form.getOrderName()) ? form.getOrderName() : "Đơn hàng mua mã " + orderId)
                .priority(form.getPriority() != 0 ? form.getPriority() : 20)
                .saleChannelId(form.getSaleChannelId())
                .tenantId(tenantId)
                .build();
        orderRepository.save(order);

        for (OrderCreationForm.OrderItem itemForm : form.getOrderItems()) {
            orderItemService.createOrderItems(itemForm, orderId, tenantId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(String orderId, OrderUpdateForm form, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (OrderStatus.fromValue(order.getStatusId()).ordinal() > OrderStatus.CANCELLED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        order.setDeliveryBeforeDate(form.getDeliveryBeforeDate());
        order.setDeliveryAfterDate(form.getDeliveryAfterDate());
        order.setNote(form.getNote());
        order.setOrderName(form.getOrderName());
        order.setPriority(form.getPriority());
        order.setSaleChannelId(form.getSaleChannelId());
        order.setStatusId(OrderStatus.CREATED.value());
        orderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveOrder(String orderId, Long userId, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!order.getStatusId().equals(OrderStatus.CREATED.value())) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }
        order.setStatusId(OrderStatus.APPROVED.value());
        order.setUserApprovedId(userId);
        orderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderId, String cancellationNote, Long userId, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!order.getStatusId().equals(OrderStatus.CREATED.value())) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }
        order.setCancellationNote(cancellationNote);
        order.setStatusId(OrderStatus.CANCELLED.value());
        order.setUserCancelledId(userId);
        orderRepository.save(order);
    }

    public OrderEntity getOrder(String orderId, Long tenantId) {
        log.info("Getting order {} for tenant {}", orderId, tenantId);
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            return null;
        }
        return order;
    }

    public Page<OrderEntity> findOrders(
            String query,
            String fromSupplierId,
            String saleChannelId,
            LocalDate orderDateAfter,
            LocalDate orderDateBefore,
            LocalDate deliveryBefore,
            LocalDate deliveryAfter,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection
            ) {
        return  orderRepository.findAll(
                OrderSpecification.satisfy(
                        query,
                        fromSupplierId,
                        saleChannelId,
                        orderDateAfter,
                        orderDateBefore,
                        deliveryBefore,
                        deliveryAfter,
                        statusId,
                        tenantId
                ),
                PaginationUtils.createPageable(page, size, sortBy, sortDirection));
    }

    @Transactional(rollbackFor = Exception.class)
    public void readyForDeliveryOrder(String orderId, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        List<OrderItemEntity> orderItems = orderItemService.findByOrderId(orderId, tenantId);
        Map<String, Integer> itemQuantityMap = orderItems.stream().collect(Collectors.toMap(
                OrderItemEntity::getId,
                OrderItemEntity::getQuantity
        ));
        Map<String, Integer> deliveredQuantityMap = new HashMap<>();
        List<ShipmentEntity> shipments = shipmentService.findByOrderId(orderId, tenantId);
        if (shipments.isEmpty()) {
            throw new AppException(AppErrorCode.ORDER_NOT_READY_FOR_DELIVERY);
        }
        for (ShipmentEntity shipment : shipments) {
            var items = inventoryItemDetailService.getItemsByShipmentId(shipment.getId(), tenantId);
            for (var item : items) {
                deliveredQuantityMap.put(
                        item.getOrderItemId(),
                        deliveredQuantityMap.getOrDefault(item.getOrderItemId(), 0) + item.getQuantity()
                );
            }
        }
        for (var entry : itemQuantityMap.entrySet()) {
            String orderItemId = entry.getKey();
            int orderedQuantity = entry.getValue();
            int deliveredQuantity = deliveredQuantityMap.getOrDefault(orderItemId, 0);
            if (deliveredQuantity < orderedQuantity) {
                throw new AppException(AppErrorCode.ORDER_NOT_READY_FOR_DELIVERY);
            }
        }
        order.setStatusId(OrderStatus.READY_FOR_DELIVERY.value());
        orderRepository.save(order);

        shipments.forEach(shipment -> {shipment.setStatusId(ShipmentStatus.READY.value());});
        shipmentService.updateShipmentBatch(shipments);
    }

}
