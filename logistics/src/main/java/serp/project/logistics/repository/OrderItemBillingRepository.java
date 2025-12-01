package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.logistics.entity.OrderItemBillingEntity;

public interface OrderItemBillingRepository extends JpaRepository<OrderItemBillingEntity, String> {
}
