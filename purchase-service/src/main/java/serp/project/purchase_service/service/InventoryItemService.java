package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;
import serp.project.purchase_service.entity.InventoryItemEntity;
import serp.project.purchase_service.repository.InventoryItemRepository;
import serp.project.purchase_service.util.IdUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    public void createInventoryItem(InventoryItemDetailEntity item) {
        String inventoryItemId = IdUtils.generateInventoryItemId();
        InventoryItemEntity inventoryItem = InventoryItemEntity.builder()
                .id(inventoryItemId)
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .facilityId(item.getFacilityId())
                .lotId(item.getLotId())
                .expirationDate(item.getExpirationDate())
                .manufacturingDate(item.getManufacturingDate())
                .statusId("VALID")
                .receivedDate(LocalDate.now())
                .tenantId(item.getTenantId())
                .build();
        inventoryItemRepository.save(inventoryItem);
    }

}
