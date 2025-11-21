package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.purchase_service.entity.OrderItemBillingEntity;

public interface OrderItemBillingRepository extends JpaRepository<OrderItemBillingEntity, String> {
}
