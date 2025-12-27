package serp.project.sales.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.sales.dto.request.InventoryItemCreationForm;
import serp.project.sales.dto.request.InventoryItemUpdateForm;
import serp.project.sales.entity.InventoryItemEntity;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.repository.InventoryItemRepository;
import serp.project.sales.repository.specification.InventoryItemSpecification;
import serp.project.sales.util.PaginationUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    public void createInventoryItem(InventoryItemCreationForm form, Long tenantId) {
        InventoryItemEntity inventoryItem = new InventoryItemEntity(form, tenantId);
        inventoryItemRepository.save(inventoryItem);
        log.info("[InventoryItemService] Created inventory item with ID {} for product ID: {}", inventoryItem.getId(),
                form.getProductId());
    }

    public void updateInventoryItem(String id, InventoryItemUpdateForm form, Long tenantId) {
        InventoryItemEntity inventoryItem = getInventoryItem(id, tenantId);
        if (inventoryItem == null) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        inventoryItem.update(form);
        inventoryItemRepository.save(inventoryItem);
        log.info("[InventoryItemService] Updated inventory item with ID {}", id);
    }

    public void deleteInventoryItem(String id, Long tenantId) {
        InventoryItemEntity inventoryItem = getInventoryItem(id, tenantId);
        if (inventoryItem == null) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        inventoryItem.cleanup();

        inventoryItemRepository.delete(inventoryItem);
        log.info("[InventoryItemService] Deleted inventory item with ID {}", id);
    }

    public InventoryItemEntity getInventoryItem(String id, Long tenantId) {
        InventoryItemEntity inventoryItem = inventoryItemRepository.findById(id).orElse(null);
        if (inventoryItem == null || !inventoryItem.getTenantId().equals(tenantId)) {
            log.info("[InventoryItemService] Inventory item with ID {} not found or tenant ID mismatch", id);
            return null;
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
            String sortDirection) {
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
                        tenantId),
                pageable);
    }

}
