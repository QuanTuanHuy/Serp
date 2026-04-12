package serp.project.logistics2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.logistics2.entity.InventoryItemDetailEntity;

import java.util.List;

public interface InventoryItemDetailRepository extends JpaRepository<InventoryItemDetailEntity, String> {

    public List<InventoryItemDetailEntity> findByTenantIdAndShipmentId(Long tenantId, String shipmentId);

    public List<InventoryItemDetailEntity> findByTenantIdAndOrderItemId(Long tenantId, String orderItemId);

    public List<InventoryItemDetailEntity> findByTenantIdAndInventoryItemId(Long tenantId, String inventoryItemId);

    public void deleteByOrderItemId(String orderItemId);

    public void deleteByShipmentId(String shipmentId);

}
