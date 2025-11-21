package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.purchase_service.entity.ShipmentEntity;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, String> {

    public List<ShipmentEntity> findByTenantIdAndOrderId(Long tenantId, String orderId);

    public int countShipmentEntitiesByTenantIdAndOrderIdAndStatusId(Long tenantId, String orderId, String statusId);



}
