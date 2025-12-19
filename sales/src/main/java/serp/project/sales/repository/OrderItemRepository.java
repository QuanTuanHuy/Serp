package serp.project.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.sales.entity.OrderItemEntity;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity,String> {

    List<OrderItemEntity> findByTenantIdAndOrderId(Long tenantId, String orderId);

}
