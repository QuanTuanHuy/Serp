package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import serp.project.purchase_service.entity.ShipmentEntity;
import serp.project.purchase_service.repository.ShipmentRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public List<ShipmentEntity> findByOrderId(String orderId, Long tenantId) {
        return shipmentRepository.findByTenantIdAndOrderId(tenantId, orderId);
    }

    public ShipmentEntity getShipment(String shipmentId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.info("[ShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            return null;
        }
        return shipment;
    }

}
