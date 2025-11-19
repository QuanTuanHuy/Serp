package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.purchase_service.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

}
