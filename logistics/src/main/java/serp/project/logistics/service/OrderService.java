package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import serp.project.logistics.entity.OrderEntity;
import serp.project.logistics.entity.OrderItemEntity;
import serp.project.logistics.entity.ProductEntity;
import serp.project.logistics.repository.OrderItemRepository;
import serp.project.logistics.repository.OrderRepository;
import serp.project.logistics.repository.ProductRepository;
import serp.project.logistics.repository.specification.OrderSpecification;
import serp.project.logistics.util.PaginationUtils;

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
