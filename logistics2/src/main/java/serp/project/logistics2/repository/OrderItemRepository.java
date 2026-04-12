package serp.project.logistics2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.logistics2.entity.OrderItemEntity;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, String> {

    List<OrderItemEntity> findByTenantIdAndOrderId(Long tenantId, String orderId);

}
