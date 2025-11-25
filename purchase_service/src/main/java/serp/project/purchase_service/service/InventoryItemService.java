package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;
import serp.project.purchase_service.entity.InventoryItemEntity;
import serp.project.purchase_service.repository.InventoryItemRepository;
import serp.project.purchase_service.util.IdUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    @Transactional(rollbackFor = Exception.class)
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
        log.info("[InventoryItemService] Created inventory item with ID {} for product ID: {}", inventoryItemId,
                item.getProductId());
    }

}
