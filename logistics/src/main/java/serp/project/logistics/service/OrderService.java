package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import serp.project.logistics.entity.OrderEntity;
import serp.project.logistics.repository.OrderRepository;
import serp.project.logistics.repository.specification.OrderSpecification;
import serp.project.logistics.util.PaginationUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

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

}
