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
                .orderName(
                        StringUtils.hasText(form.getOrderName()) ? form.getOrderName() : "Đơn hàng mua mã " + orderId)
                .priority(form.getPriority() != 0 ? form.getPriority() : 20)
                .saleChannelId(form.getSaleChannelId())
                .tenantId(tenantId)
                .build();
        orderRepository.save(order);
        log.info("[OrderService] Created order {} with ID {} for tenant {}", order.getOrderName(), orderId, tenantId);

        for (OrderCreationForm.OrderItem itemForm : form.getOrderItems()) {
            orderItemService.createOrderItems(itemForm, orderId, tenantId);
            log.info("[OrderService] Added order item for product ID {} to order ID {}",
                    itemForm.getProductId(), orderId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(String orderId, OrderUpdateForm form, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (OrderStatus.fromValue(order.getStatusId()).ordinal() > OrderStatus.CREATED.ordinal()) {
            log.error("[OrderService] Cannot update order with status {}", order.getStatusId());
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        order.setDeliveryBeforeDate(form.getDeliveryBeforeDate());
        order.setDeliveryAfterDate(form.getDeliveryAfterDate());
        order.setNote(form.getNote());
        order.setOrderName(form.getOrderName());
        order.setPriority(form.getPriority());
        order.setSaleChannelId(form.getSaleChannelId());
        orderRepository.save(order);
        log.info("[OrderService] Updated order {} with ID {} for tenant {}", order.getOrderName(), orderId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveOrder(String orderId, Long userId, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!order.getStatusId().equals(OrderStatus.CREATED.value())) {
            log.error("[OrderService] Cannot approve order with status {}", order.getStatusId());
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }
        order.setStatusId(OrderStatus.APPROVED.value());
        order.setUserApprovedId(userId);
        orderRepository.save(order);
        log.info("[OrderService] Approved order {} with ID {} for tenant {}", order.getOrderName(), orderId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderId, String cancellationNote, Long userId, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!order.getStatusId().equals(OrderStatus.CREATED.value())) {
            log.error("[OrderService] Cannot cancel order with status {}", order.getStatusId());
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }
        order.setCancellationNote(cancellationNote);
        order.setStatusId(OrderStatus.CANCELLED.value());
        order.setUserCancelledId(userId);
        orderRepository.save(order);
        log.info("[OrderService] Cancelled order {} with ID {} for tenant {}", order.getOrderName(), orderId, tenantId);
    }

    public OrderEntity getOrder(String orderId, Long tenantId) {
        log.info("[OrderService] Getting order {} for tenant {}", orderId, tenantId);
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.info("[OrderService] Order {} not found for tenant {} or does not belong to tenant", orderId, tenantId);
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
            String sortDirection) {
        return orderRepository.findAll(
                OrderSpecification.satisfy(
                        query,
                        fromSupplierId,
                        saleChannelId,
                        orderDateAfter,
                        orderDateBefore,
                        deliveryBefore,
                        deliveryAfter,
                        statusId,
                        tenantId),
                PaginationUtils.createPageable(page, size, sortBy, sortDirection));
    }

}
