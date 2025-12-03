package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.logistics.entity.OrderItemEntity;
import serp.project.logistics.repository.OrderItemRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public List<OrderItemEntity> findByOrderId(String orderId, Long tenantId) {
        return orderItemRepository.findByTenantIdAndOrderId(tenantId, orderId);
    }

}
