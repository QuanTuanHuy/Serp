package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;
import serp.project.purchase_service.repository.InventoryItemDetailRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryItemDetailService {

    private final InventoryItemDetailRepository inventoryItemDetailRepository;

    public List<InventoryItemDetailEntity> getItemsByShipmentId(String shipmentId, Long tenantId) {
        return inventoryItemDetailRepository.findByTenantIdAndShipmentId(tenantId, shipmentId);
    }

}
