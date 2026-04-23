package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.logistics2.dto.request.InventoryItemCreationForm;
import serp.project.logistics2.dto.request.InventoryItemUpdateForm;
import serp.project.logistics2.entity.InventoryItemEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.InventoryItemRepository;
import serp.project.logistics2.repository.specification.InventoryItemSpecification;
import serp.project.logistics2.util.PaginationUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

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
