package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderCreationForm form, Long userId, Long tenantId) {

        List<OrderItemEntity> orderItems = new ArrayList<>();
        for (OrderCreationForm.OrderItem itemForm : form.getOrderItems()) {
            ProductEntity product = productRepository.findById(itemForm.getProductId()).orElse(null);
            if (product == null || !product.getTenantId().equals(tenantId)) {
                log.info("[OrderService] Product ID {} not found for tenant {}", itemForm.getProductId(), tenantId);
                throw new AppException(AppErrorCode.NOT_FOUND);
            }
            orderItems.add(new OrderItemEntity(itemForm, product, tenantId));
        }

        OrderEntity order = new OrderEntity(form, orderItems, userId, tenantId);
        orderRepository.save(order);
        log.info("[OrderService] Created order {} with ID {} for tenant {}", order.getOrderName(), order.getId(),
                tenantId);

        for (OrderItemEntity item : orderItems) {
            item.setOrderId(order.getId());
            orderItemRepository.save(item);
            log.info("[OrderService] Created order item {} for order {} and tenant {}", item.getId(), order.getId(),
                    tenantId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(String orderId, OrderUpdateForm form, Long tenantId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
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
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        order.cancel(cancellationNote, userId);
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

    @Transactional(rollbackFor = Exception.class)
    public void createOrderItems(OrderCreationForm.OrderItem itemForm, String orderId, Long tenantId) {
        ProductEntity product = productRepository.findById(itemForm.getProductId()).orElse(null);
        if (product == null || !product.getTenantId().equals(tenantId)) {
            log.info("[OrderItemService] Product ID {} not found for tenant {}", itemForm.getProductId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        OrderItemEntity orderItem = new OrderItemEntity(itemForm, product, tenantId);

        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.info("[OrderItemService] Order ID {} not found for tenant {}", orderId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        order.addOrderItem(orderItem);
        orderRepository.save(order);
        log.info("[OrderItemService] Modified order {} due to add order item {} for tenant {}", orderItem.getId(),
                orderId,
                tenantId);

        orderItem.setOrderId(orderId);
        orderItemRepository.save(orderItem);
        log.info("[OrderItemService] Created order item {} for order {} and tenant {}", orderItem.getId(), orderId,
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

        OrderItemEntity oldOrderItem = new OrderItemEntity(orderItem);
        orderItem.update(form);

        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.info("[OrderItemService] Order ID {} not found for tenant {}", orderId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        order.updateOrderItem(oldOrderItem, orderItem);
        orderRepository.save(order);
        log.info("[OrderItemService] Modified order {} due to update order item {} for tenant {}", orderItemId,
                orderId,
                tenantId);

        orderItemRepository.save(orderItem);
        log.info("[OrderItemService] Updated order item {} for order {} and tenant {}", orderItemId, orderId, tenantId);
    }

    public List<OrderItemEntity> findByOrderId(String orderId, Long tenantId) {
        return orderItemRepository.findByTenantIdAndOrderId(tenantId, orderId);
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
        order.removeOrderItem(orderItem);
        orderRepository.save(order);
        log.info("[OrderItemService] Modified order {} due to delete order item {} for tenant {}", orderItemId,
                orderId,
                tenantId);

        orderItemRepository.delete(orderItem);
        log.info("[OrderItemService] Deleted order item {} for order {} and tenant {}", orderItemId, orderId,
                tenantId);
    }

}
