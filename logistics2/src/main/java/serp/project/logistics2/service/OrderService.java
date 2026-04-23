package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import serp.project.logistics2.constant.OrderType;
import serp.project.logistics2.entity.*;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.*;
import serp.project.logistics2.repository.specification.OrderSpecification;
import serp.project.logistics2.util.PaginationUtils;

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

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemDetailRepository inventoryItemDetailRepository;

    public Page<OrderEntity> findOrders(
            String query,
            String orderTypeId,
            String fromSupplierId,
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
                        orderTypeId,
                        fromSupplierId,
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

    public OrderEntity getOrder(String orderId, Long tenantId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElse(null);
        if (orderEntity == null || !orderEntity.getTenantId().equals(tenantId)) {
            log.info("[OrderService] Order with ID {} not found for tenantId {}", orderId, tenantId);
            return null;
        }
        return orderEntity;
    }

    public OrderEntity getSaleOrder(String orderId, Long tenantId) {
        log.info("[OrderService] Getting order {} for tenant {}", orderId, tenantId);
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.info("[OrderService] Order {} not found for tenant {} or does not belong to tenant",
                    orderId, tenantId);
            return null;
        } if (order.getOrderTypeId().equals(OrderType.SALES.name())) {
            log.info("[OrderService] Order {} is not a sales order", orderId);
            throw new AppException(AppErrorCode.NOT_FOUND);
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

            ProductEntity product = productRepository.findById(item.getProductId())
                    .orElse(null);
            item.setProduct(product);
        });
        return order;
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
