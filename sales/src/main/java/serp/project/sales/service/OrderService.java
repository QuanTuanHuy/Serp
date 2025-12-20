package serp.project.sales.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.sales.constant.OrderStatus;
import serp.project.sales.constant.OrderType;
import serp.project.sales.dto.request.OrderCreationForm;
import serp.project.sales.dto.request.OrderUpdateForm;
import serp.project.sales.dto.request.OrderCreationForm.OrderItem;
import serp.project.sales.entity.InventoryItemDetailEntity;
import serp.project.sales.entity.InventoryItemEntity;
import serp.project.sales.entity.OrderEntity;
import serp.project.sales.entity.OrderItemEntity;
import serp.project.sales.entity.ProductEntity;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.repository.InventoryItemDetailRepository;
import serp.project.sales.repository.InventoryItemRepository;
import serp.project.sales.repository.OrderItemRepository;
import serp.project.sales.repository.OrderRepository;
import serp.project.sales.repository.ProductRepository;
import serp.project.sales.repository.specification.OrderSpecification;
import serp.project.sales.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class OrderService {

        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final InventoryItemRepository inventoryItemRepository;
        private final InventoryItemDetailRepository inventoryItemDetailRepository;
        private final ProductRepository productRepository;

        public void createSaleOrder(OrderCreationForm form, Long userId, Long tenantId) {
                OrderEntity order = new OrderEntity(form, userId, tenantId);
                for (OrderCreationForm.OrderItem itemForm : form.getItems()) {
                        ProductEntity product = productRepository.findById(itemForm.getProductId()).orElse(null);
                        if (product == null || !product.getTenantId().equals(tenantId)) {
                                log.error("[OrderService] Product ID {} not found for tenant {}",
                                                itemForm.getProductId(), tenantId);
                                throw new AppException(AppErrorCode.NOT_FOUND);
                        }

                        List<InventoryItemEntity> availableItems = inventoryItemRepository
                                        .findAvailableInventoryItemByProductIdAndExpireAfter(
                                                        itemForm.getProductId(),
                                                        itemForm.getExpireAfter());

                        order.addItems(itemForm, product, availableItems);
                }

                List<OrderItemEntity> orderItems = order.getItems();
                List<InventoryItemDetailEntity> allocaltedInventoryItems = orderItems.stream()
                                .flatMap(item -> item.getAllocatedInventoryItems().stream())
                                .collect(Collectors.toList());
                List<InventoryItemEntity> updatedInventoryItems = allocaltedInventoryItems.stream()
                                .map(InventoryItemDetailEntity::getInventoryItem)
                                .distinct()
                                .collect(Collectors.toList());

                orderRepository.save(order);
                log.info("[OrderService] Created order {} with ID {} for tenant {}", order.getOrderName(),
                                order.getId(),
                                tenantId);

                orderItemRepository.saveAll(orderItems);
                log.info("[OrderService] Created {} order items for order {} and tenant {}", orderItems.size(),
                                order.getId(),
                                tenantId);

                inventoryItemDetailRepository.saveAll(allocaltedInventoryItems);
                log.info("[OrderService] Created {} inventory item details for order {} and tenant {}",
                                allocaltedInventoryItems.size(),
                                order.getId(), tenantId);

                inventoryItemRepository.saveAll(updatedInventoryItems);
                log.info("[OrderService] Reserved inventory items for order {} for tenant {}", order.getId(), tenantId);
        }

        public void updateOrder(String orderId, OrderUpdateForm form, Long tenantId) {
                OrderEntity order = orderRepository.findById(orderId).orElse(null);
                if (order == null || !order.getTenantId().equals(tenantId)) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                order.update(form);

                orderRepository.save(order);
                log.info("[OrderService] Updated order {} with ID {} for tenant {}", order.getOrderName(), orderId,
                                tenantId);
        }

        public void approveOrder(String orderId, Long userId, Long tenantId) {
                OrderEntity order = getDetailOrder(orderId, tenantId);
                if (order == null) {
                        log.error("[OrderService] Order ID {} not found for tenant {}", orderId, tenantId);
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                order.approve(userId);

                List<InventoryItemEntity> updatedInventoryItems = order.getItems().stream()
                                .flatMap(item -> item.getAllocatedInventoryItems().stream())
                                .map(InventoryItemDetailEntity::getInventoryItem)
                                .distinct()
                                .collect(Collectors.toList());

                inventoryItemRepository.saveAll(updatedInventoryItems);
                log.info("[OrderService] Committed inventory items for order {} for tenant {}", order.getId(),
                                tenantId);

                orderRepository.save(order);
                log.info("[OrderService] Approved order {} with ID {} for tenant {}", order.getOrderName(), orderId,
                                tenantId);
        }

        public void cancelOrder(String orderId, String cancellationNote, Long userId, Long tenantId) {
                OrderEntity order = getDetailOrder(orderId, tenantId);
                if (order == null) {
                        log.error("[OrderService] Order ID {} not found for tenant {}", orderId, tenantId);
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                order.cancel(cancellationNote, userId);

                List<InventoryItemDetailEntity> allocatedInventoryItems = order.getItems().stream()
                                .flatMap(item -> item.getAllocatedInventoryItems().stream())
                                .collect(Collectors.toList());
                List<InventoryItemEntity> updatedInventoryItems = allocatedInventoryItems.stream()
                                .map(InventoryItemDetailEntity::getInventoryItem)
                                .distinct()
                                .collect(Collectors.toList());

                inventoryItemDetailRepository.deleteAll(allocatedInventoryItems);
                log.info("[OrderService] Deleted {} inventory item details for order {} and tenant {}",
                                allocatedInventoryItems.size(),
                                order.getId(), tenantId);

                inventoryItemRepository.saveAll(updatedInventoryItems);
                log.info("[OrderService] Released reserved inventory items for order {} for tenant {}", order.getId(),
                                tenantId);

                orderRepository.save(order);
                log.info("[OrderService] Cancelled order {} with ID {} for tenant {}", order.getOrderName(), orderId,
                                tenantId);
        }

        public void deleteOrder(String orderId, Long tenantId) {
                OrderEntity order = getDetailOrder(orderId, tenantId);
                if (order == null) {
                        log.error("[OrderService] Order ID {} not found for tenant {}", orderId, tenantId);
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                if (OrderStatus.valueOf(order.getStatusId()).ordinal() >= OrderStatus.APPROVED.ordinal()) {
                        log.error("[OrderService] Cannot delete order {} in status {} for tenant {}", orderId,
                                        order.getStatusId(), tenantId);
                        throw new AppException(AppErrorCode.CANNOT_DELETE_ORDER_IN_CURRENT_STATUS);
                }

                if (order.getStatusId().equals(OrderStatus.CREATED.name())) {
                        order.cancel(null, null);

                        List<InventoryItemDetailEntity> allocatedInventoryItems = order.getItems().stream()
                                        .flatMap(item -> item.getAllocatedInventoryItems().stream())
                                        .collect(Collectors.toList());
                        List<InventoryItemEntity> updatedInventoryItems = allocatedInventoryItems.stream()
                                        .map(InventoryItemDetailEntity::getInventoryItem)
                                        .distinct()
                                        .collect(Collectors.toList());

                        inventoryItemRepository.saveAll(updatedInventoryItems);
                        log.info("[OrderService] Released reserved inventory items for order {} for tenant {}",
                                        order.getId(),
                                        tenantId);

                        inventoryItemDetailRepository.deleteAll(allocatedInventoryItems);
                        log.info("[OrderService] Deleted {} inventory item details for order {} and tenant {}",
                                        allocatedInventoryItems.size(),
                                        order.getId(), tenantId);
                }

                List<OrderItemEntity> orderItems = order.getItems();

                orderItemRepository.deleteAll(orderItems);
                log.info("[OrderService] Deleted {} order items for order {} and tenant {}", orderItems.size(),
                                order.getId(),
                                tenantId);

                orderRepository.delete(order);
                log.info("[OrderService] Deleted order {} with ID {} for tenant {}", order.getOrderName(), orderId,
                                tenantId);
        }

        public OrderEntity getDetailOrder(String orderId, Long tenantId) {
                log.info("[OrderService] Getting order {} for tenant {}", orderId, tenantId);
                OrderEntity order = orderRepository.findById(orderId).orElse(null);
                if (order == null || !order.getTenantId().equals(tenantId)) {
                        log.info("[OrderService] Order {} not found for tenant {} or does not belong to tenant",
                                        orderId, tenantId);
                        return null;
                }

                List<OrderItemEntity> orderItems = orderItemRepository.findByTenantIdAndOrderId(tenantId, orderId);
                order.setItems(orderItems);

                order.getItems().forEach(item -> {
                        List<InventoryItemDetailEntity> allocatedItems = inventoryItemDetailRepository
                                        .findByTenantIdAndOrderItemId(tenantId, item.getId());
                        item.setAllocatedInventoryItems(allocatedItems);
                        item.getAllocatedInventoryItems().forEach(detail -> {
                                InventoryItemEntity inventoryItem = inventoryItemRepository
                                                .findById(detail.getInventoryItemId())
                                                .orElse(null);
                                detail.setInventoryItem(inventoryItem);
                        });
                });
                return order;
        }

        public Page<OrderEntity> findOrders(
                        String query,
                        String toCustomerId,
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
                                                OrderType.SALE.name(),
                                                null,
                                                toCustomerId,
                                                saleChannelId,
                                                orderDateAfter,
                                                orderDateBefore,
                                                deliveryBefore,
                                                deliveryAfter,
                                                statusId,
                                                tenantId),
                                PaginationUtils.createPageable(page, size, sortBy, sortDirection));
        }

        public void createOrderItem(OrderItem itemForm, String orderId, Long tenantId) {
                OrderEntity order = orderRepository.findById(orderId).orElse(null);
                if (order == null || !order.getTenantId().equals(tenantId)) {
                        log.error("[OrderService] Order ID {} not found for tenant {}", orderId, tenantId);
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                ProductEntity product = productRepository.findById(itemForm.getProductId()).orElse(null);
                if (product == null || !product.getTenantId().equals(tenantId)) {
                        log.error("[OrderService] Product ID {} not found for tenant {}", itemForm.getProductId(),
                                        tenantId);
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                List<InventoryItemEntity> availableItems = inventoryItemRepository
                                .findAvailableInventoryItemByProductIdAndExpireAfter(
                                                itemForm.getProductId(),
                                                itemForm.getExpireAfter());

                order.addItems(itemForm, product, availableItems);

                List<OrderItemEntity> orderItems = order.getItems();
                List<InventoryItemDetailEntity> allocaltedInventoryItems = orderItems.stream()
                                .flatMap(item -> item.getAllocatedInventoryItems().stream())
                                .collect(Collectors.toList());
                List<InventoryItemEntity> updatedInventoryItems = allocaltedInventoryItems.stream()
                                .map(InventoryItemDetailEntity::getInventoryItem)
                                .distinct()
                                .collect(Collectors.toList());

                orderRepository.save(order);
                log.info("[OrderService] Created order {} with ID {} for tenant {}", order.getOrderName(),
                                order.getId(),
                                tenantId);

                orderItemRepository.saveAll(orderItems);
                log.info("[OrderService] Created {} order items for order {} and tenant {}", orderItems.size(),
                                order.getId(),
                                tenantId);

                inventoryItemDetailRepository.saveAll(allocaltedInventoryItems);
                log.info("[OrderService] Created {} inventory item details for order {} and tenant {}",
                                allocaltedInventoryItems.size(),
                                order.getId(), tenantId);

                inventoryItemRepository.saveAll(updatedInventoryItems);
                log.info("[OrderService] Reserved inventory items for order {} for tenant {}", order.getId(), tenantId);
        }

        public void removeOrderItem(String orderItemId, String orderId, Long tenantId) {
                OrderEntity order = orderRepository.findById(orderId).orElse(null);
                if (order == null || !order.getTenantId().equals(tenantId)) {
                        log.error("[OrderService] Order ID {} not found for tenant {}", orderId, tenantId);
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                OrderItemEntity item = orderItemRepository.findById(orderItemId).orElse(null);
                if (item == null || !item.getTenantId().equals(tenantId) || !item.getOrderId().equals(orderId)) {
                        log.error("[OrderService] Order Item ID {} not found for Order ID {} and tenant {}",
                                        orderItemId, orderId,
                                        tenantId);
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }

                List<InventoryItemDetailEntity> allocatedInventoryItems = inventoryItemDetailRepository
                                .findByTenantIdAndOrderItemId(tenantId, orderItemId);
                List<InventoryItemEntity> updatedInventoryItems = allocatedInventoryItems.stream()
                                .map(InventoryItemDetailEntity::getInventoryItem)
                                .distinct()
                                .collect(Collectors.toList());

                order.removeItem(item);

                inventoryItemRepository.saveAll(updatedInventoryItems);
                log.info("[OrderService] Released reserved inventory items for order item {} for tenant {}",
                                orderItemId,
                                tenantId);

                inventoryItemDetailRepository.deleteAll(allocatedInventoryItems);
                log.info("[OrderService] Deleted {} inventory item details for order item {} and tenant {}",
                                allocatedInventoryItems.size(),
                                orderItemId, tenantId);

                orderItemRepository.delete(item);
                log.info("[OrderService] Deleted order item {} for order {} and tenant {}", orderItemId, orderId,
                                tenantId);

                orderRepository.save(order);
                log.info("[OrderService] Updated order {} with ID {} for tenant {}", order.getOrderName(), orderId,
                                tenantId);
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

}
