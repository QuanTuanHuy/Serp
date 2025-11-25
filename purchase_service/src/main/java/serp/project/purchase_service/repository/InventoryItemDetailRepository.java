package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;

import java.util.List;

public interface InventoryItemDetailRepository extends JpaRepository<InventoryItemDetailEntity, String> {

    public List<InventoryItemDetailEntity> findByTenantIdAndShipmentId(Long tenantId, String shipmentId);

    public void deleteByOrderItemId(String orderItemId);

    public void deleteByShipmentId(String shipmentId);

}
