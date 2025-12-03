package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.logistics.entity.OrderItemEntity;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity,String> {

    List<OrderItemEntity> findByTenantIdAndOrderId(Long tenantId, String orderId);

}
