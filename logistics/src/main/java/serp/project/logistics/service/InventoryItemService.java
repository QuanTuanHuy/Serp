package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.logistics.dto.request.InventoryItemCreationForm;
import serp.project.logistics.dto.request.InventoryItemUpdateForm;
import serp.project.logistics.entity.InventoryItemDetailEntity;
import serp.project.logistics.entity.InventoryItemEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.repository.InventoryItemRepository;
import serp.project.logistics.repository.specification.InventoryItemSpecification;
import serp.project.logistics.util.IdUtils;
import serp.project.logistics.util.PaginationUtils;

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

    public void createInventoryItem(InventoryItemCreationForm form, Long tenantId) {
        String inventoryItemId = IdUtils.generateInventoryItemId();
        InventoryItemEntity inventoryItem = InventoryItemEntity.builder()
                .id(inventoryItemId)
                .productId(form.getProductId())
                .quantity(form.getQuantity())
                .facilityId(form.getFacilityId())
                .expirationDate(form.getExpirationDate())
                .manufacturingDate(form.getManufacturingDate())
                .statusId("VALID")
                .tenantId(tenantId)
                .build();
        inventoryItemRepository.save(inventoryItem);
        log.info("[InventoryItemService] Created inventory item with ID {} for product ID: {}", inventoryItemId,
                form.getProductId());
    }

    public void updateInventoryItem(String id, InventoryItemUpdateForm form, Long tenantId) {
        InventoryItemEntity inventoryItem = getInventoryItem(id, tenantId);

        inventoryItem.setQuantity(form.getQuantity());
        inventoryItem.setExpirationDate(form.getExpirationDate());
        inventoryItem.setManufacturingDate(form.getManufacturingDate());
        inventoryItem.setStatusId(form.getStatusId());

        inventoryItemRepository.save(inventoryItem);
        log.info("[InventoryItemService] Updated inventory item with ID {}", id);
    }

    public InventoryItemEntity getInventoryItem(String id, Long tenantId) {
        InventoryItemEntity inventoryItem = inventoryItemRepository.findById(id).orElse(null);
        if (inventoryItem == null || !inventoryItem.getTenantId().equals(tenantId)) {
            log.error("[InventoryItemService] Inventory item with ID {} not found or tenant ID mismatch", id);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        return inventoryItem;
    }

    public Page<InventoryItemEntity> getInventoryItems(
            String query,
            String productId,
            String facilityId,
            LocalDate expirationDateFrom,
            LocalDate expirationDateTo,
            LocalDate manufacturingDateFrom,
            LocalDate manufacturingDateTo,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return inventoryItemRepository.findAll(
                InventoryItemSpecification.satisfy(
                        query,
                        productId,
                        facilityId,
                        expirationDateFrom,
                        expirationDateTo,
                        manufacturingDateFrom,
                        manufacturingDateTo,
                        statusId,
                        tenantId
                ),
                pageable
        );
    }

}
