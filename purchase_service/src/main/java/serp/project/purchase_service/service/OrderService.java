package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.purchase_service.constant.OrderStatus;
import serp.project.purchase_service.dto.request.OrderCreationForm;
import serp.project.purchase_service.dto.request.OrderItemUpdateForm;
import serp.project.purchase_service.dto.request.OrderUpdateForm;
import serp.project.purchase_service.entity.OrderEntity;
import serp.project.purchase_service.entity.OrderItemEntity;
import serp.project.purchase_service.entity.ProductEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.OrderItemRepository;
import serp.project.purchase_service.repository.OrderRepository;
import serp.project.purchase_service.repository.ProductRepository;
import serp.project.purchase_service.repository.specification.OrderSpecification;
import serp.project.purchase_service.util.PaginationUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createPurchaseOrder(OrderCreationForm form, Long userId, Long tenantId) {
        OrderEntity order = new OrderEntity(form, userId, tenantId);
        for (OrderCreationForm.OrderItem itemForm : form.getItems()) {
            ProductEntity product = productRepository.findById(itemForm.getProductId()).orElse(null);
            if (product == null || !product.getTenantId().equals(tenantId)) {
                log.info("[OrderService] Product ID {} not found for tenant {}", itemForm.getProductId(), tenantId);
                throw new AppException(AppErrorCode.NOT_FOUND);
            }

            order.addItem(itemForm, product);
        }

        orderRepository.save(order);
        log.info("[OrderService] Created order {} with ID {} for tenant {}", order.getOrderName(), order.getId(),
                tenantId);

        orderItemRepository.saveAll(order.getItems());
        log.info("[OrderService] Created {} order items for order {} and tenant {}", order.getItems().size(),
                order.getId(),
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(String orderId, OrderUpdateForm form, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.error("[OrderService] Order {} not found for tenant {} or does not belong to tenant", orderId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        order.update(form);

        orderRepository.save(order);
        log.info("[OrderService] Updated order {} with ID {} for tenant {}", order.getOrderName(), orderId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveOrder(String orderId, Long userId, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.error("[OrderService] Order {} not found for tenant {} or does not belong to tenant", orderId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        order.approve(userId);

        orderRepository.save(order);
        log.info("[OrderService] Approved order {} with ID {} for tenant {}", order.getOrderName(), orderId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderId, String cancellationNote, Long userId, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.error("[OrderService] Order {} not found for tenant {} or does not belong to tenant", orderId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        order.cancel(cancellationNote, userId);

        orderRepository.save(order);
        log.info("[OrderService] Cancelled order {} with ID {} for tenant {}", order.getOrderName(), orderId, tenantId);
    }

    public void deleteOrder(String orderId, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.error("[OrderService] Order {} not found for tenant {} or does not belong to tenant", orderId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        if (OrderStatus.valueOf(order.getStatusId()).ordinal() >= OrderStatus.APPROVED.ordinal()) {
            log.error("[OrderService] Cannot delete order {} in current status {} for tenant {}", orderId,
                    order.getStatusId(), tenantId);
            throw new AppException(AppErrorCode.CANNOT_DELETE_ORDER_IN_CURRENT_STATUS);
        }

        orderRepository.delete(order);
        log.info("[OrderService] Deleted order {} with ID {} for tenant {}", order.getOrderName(), orderId, tenantId);
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

    @Transactional(rollbackFor = Exception.class)
    public void createOrderItem(OrderCreationForm.OrderItem itemForm, String orderId, Long tenantId) {
        ProductEntity product = productRepository.findById(itemForm.getProductId()).orElse(null);
        if (product == null || !product.getTenantId().equals(tenantId)) {
            log.info("[OrderItemService] Product ID {} not found for tenant {}", itemForm.getProductId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.info("[OrderItemService] Order ID {} not found for tenant {}", orderId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        order.addItem(itemForm, product);

        orderRepository.save(order);
        log.info("[OrderItemService] Modified order {} due to add order item for tenant {}", order.getId(),
                tenantId);

        orderItemRepository.saveAll(order.getItems());
        log.info("[OrderItemService] Created {} order item for order {} and tenant {}", order.getItems().size(),
                orderId,
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrderItem(String orderItemId, OrderItemUpdateForm form, String orderId, Long tenantId) {
        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId).orElse(null);
        if (orderItem == null || !orderItem.getTenantId().equals(tenantId) || !orderItem.getOrderId().equals(orderId)) {
            log.info("[OrderItemService] Order item ID {} not found for order {} and tenant {}", orderItemId, orderId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.info("[OrderItemService] Order ID {} not found for tenant {}", orderId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        order.updateItem(orderItem, form);

        orderRepository.save(order);
        log.info("[OrderItemService] Modified order {} due to update order item {} for tenant {}", orderItemId,
                orderId,
                tenantId);

        orderItemRepository.save(orderItem);
        log.info("[OrderItemService] Updated order item {} for order {} and tenant {}", orderItemId, orderId, tenantId);
    }

    public List<OrderItemEntity> findByOrderId(String orderId, Long tenantId) {
        List<OrderItemEntity> orderItems = orderItemRepository.findByTenantIdAndOrderId(tenantId, orderId);
        List<String> productIds = orderItems.stream()
                .map(OrderItemEntity::getProductId)
                .distinct()
                .toList();
        List<ProductEntity> products = productRepository.findAllById(productIds);
        Map<String, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));
        orderItems.forEach(item -> item.setProduct(productMap.get(item.getProductId())));
        return orderItems;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOrderItem(String orderItemId, String orderId, Long tenantId) {
        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId).orElse(null);
        if (orderItem == null || !orderItem.getTenantId().equals(tenantId) || !orderItem.getOrderId().equals(orderId)) {
            log.info("[OrderItemService] Order item ID {} not found for order {} and tenant {}", orderItemId, orderId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.info("[OrderItemService] Order ID {} not found for tenant {}", orderId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        order.removeItem(orderItem);

        orderRepository.save(order);
        log.info("[OrderItemService] Modified order {} due to delete order item {} for tenant {}", orderItemId,
                orderId,
                tenantId);

        orderItemRepository.delete(orderItem);
        log.info("[OrderItemService] Deleted order item {} for order {} and tenant {}", orderItemId, orderId,
                tenantId);
    }

}
