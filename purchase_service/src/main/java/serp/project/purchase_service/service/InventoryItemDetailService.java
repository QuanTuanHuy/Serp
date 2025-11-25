package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.dto.request.InventoryItemDetailUpdateForm;
import serp.project.purchase_service.dto.request.ShipmentCreationForm;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;
import serp.project.purchase_service.entity.ProductEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.InventoryItemDetailRepository;
import serp.project.purchase_service.util.IdUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryItemDetailService {

    private final InventoryItemDetailRepository inventoryItemDetailRepository;
    private final ProductService productService;

    @Transactional(rollbackFor = Exception.class)
    public void createInventoryItemDetails(
            String shipmentId,
            ShipmentCreationForm.InventoryItemDetail form,
            String facilityId,
            Long tenantId) {
        ProductEntity product = productService.getProduct(form.getProductId(), tenantId);
        if (product == null || !product.getTenantId().equals(tenantId)) {
            log.info("[InventoryItemDetailService] Product ID {} not found for tenant {}", form.getProductId(),
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        String itemId = IdUtils.generateInventoryItemDetailId();
        InventoryItemDetailEntity entity = InventoryItemDetailEntity.builder()
                .id(itemId)
                .productId(form.getProductId())
                .quantity(form.getQuantity())
                .shipmentId(shipmentId)
                .orderItemId(form.getOrderItemId())
                .note(form.getNote())
                .lotId(form.getLotId())
                .expirationDate(form.getExpirationDate())
                .manufacturingDate(form.getManufacturingDate())
                .facilityId(facilityId)
                .unit(product.getUnit())
                .price(product.getCostPrice())
                .tenantId(tenantId)
                .build();
        inventoryItemDetailRepository.save(entity);

    }

    @Transactional(rollbackFor = Exception.class)
    public void updateInventoryItemDetail(String itemId, InventoryItemDetailUpdateForm form, String shipmentId,
            Long tenantId) {
        InventoryItemDetailEntity entity = inventoryItemDetailRepository.findById(itemId).orElse(null);
        if (entity == null || !entity.getTenantId().equals(tenantId) || !entity.getShipmentId().equals(shipmentId)) {
            log.info("[InventoryItemDetailService] Inventory Item Detail ID {} not found for tenant {}", itemId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        entity.setQuantity(form.getQuantity());
        entity.setNote(form.getNote());
        entity.setLotId(form.getLotId());
        entity.setExpirationDate(form.getExpirationDate());
        entity.setManufacturingDate(form.getManufacturingDate());

        inventoryItemDetailRepository.save(entity);
        log.info("[InventoryItemDetailService] Inventory Item Detail ID {} updated for tenant {}", itemId,
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFacility(String shipmentId, String facilityId, Long tenantId) {
        List<InventoryItemDetailEntity> items = inventoryItemDetailRepository.findByTenantIdAndShipmentId(tenantId,
                shipmentId);
        for (InventoryItemDetailEntity item : items) {
            item.setFacilityId(facilityId);
        }
        inventoryItemDetailRepository.saveAll(items);
        log.info("[InventoryItemDetailService] Inventory Item Details updated facility for tenant {}", tenantId);
    }

    public List<InventoryItemDetailEntity> getItemsByShipmentId(String shipmentId, Long tenantId) {
        return inventoryItemDetailRepository.findByTenantIdAndShipmentId(tenantId, shipmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(String itemId, String shipmentId, Long tenantId) {
        InventoryItemDetailEntity entity = inventoryItemDetailRepository.findById(itemId).orElse(null);
        if (entity == null || !entity.getTenantId().equals(tenantId) || !entity.getShipmentId().equals(shipmentId)) {
            log.error("[InventoryItemDetailService] Inventory Item Detail ID {} not found for tenant {}", itemId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        inventoryItemDetailRepository.delete(entity);
        log.info("[InventoryItemDetailService] Inventory Item Detail ID {} deleted", itemId);
    }

}
