package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.purchase_service.entity.InventoryItemEntity;

public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, String> {

}
