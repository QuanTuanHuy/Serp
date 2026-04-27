package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.logistics.entity.InventoryItemDetailEntity;

import java.util.List;

public interface InventoryItemDetailRepository extends JpaRepository<InventoryItemDetailEntity, String> {

    public List<InventoryItemDetailEntity> findByTenantIdAndShipmentId(Long tenantId, String shipmentId);

    public List<InventoryItemDetailEntity> findByTenantIdAndOrderItemId(Long tenantId, String orderItemId);

    public List<InventoryItemDetailEntity> findByTenantIdAndInventoryItemId(Long tenantId, String inventoryItemId);

    public void deleteByOrderItemId(String orderItemId);

    public void deleteByShipmentId(String shipmentId);

    @Query("SELECT i FROM InventoryItemDetailEntity i WHERE i.orderItemId IN " +
            "(SELECT oi.id FROM OrderItemEntity oi WHERE oi.orderId = :orderId) AND i.tenantId = :tenantId")
    public List<InventoryItemDetailEntity> findByOrderIdAndTenantId(@Param("orderId") String orderId,  @Param("tenantId") Long tenantId);

}
