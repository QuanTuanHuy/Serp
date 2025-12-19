package serp.project.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.sales.entity.InventoryItemEntity;

public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, String>, JpaSpecificationExecutor<InventoryItemEntity> {

}
