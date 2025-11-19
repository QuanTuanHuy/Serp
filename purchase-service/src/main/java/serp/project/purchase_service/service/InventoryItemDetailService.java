package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
public class InventoryItemDetailService {

    private final InventoryItemDetailRepository inventoryItemDetailRepository;
    private final ProductService productService;

    public void createInventoryItemDetails(
            String shipmentId,
            ShipmentCreationForm.InventoryItemDetail form,
            Long tenantId
    ) {
        ProductEntity product = productService.getProduct(form.getProductId(), tenantId);
        if (product == null || !product.getTenantId().equals(tenantId)) {
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
                .facilityId(form.getFacilityId())
                .unit(product.getUnit())
                .price(product.getCostPrice())
                .tenantId(tenantId)
                .build();
        inventoryItemDetailRepository.save(entity);

    }

    public void updateInventoryItemDetail(String itemId, InventoryItemDetailUpdateForm form, String shipmentId, Long tenantId) {
        InventoryItemDetailEntity entity = inventoryItemDetailRepository.findById(itemId).orElse(null);
        if (entity == null || !entity.getTenantId().equals(tenantId) || !entity.getShipmentId().equals(shipmentId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        entity.setQuantity(form.getQuantity());
        entity.setNote(form.getNote());
        entity.setLotId(form.getLotId());
        entity.setExpirationDate(form.getExpirationDate());
        entity.setManufacturingDate(form.getManufacturingDate());

        inventoryItemDetailRepository.save(entity);
    }

    public void updateFacility(String shipmentId, String facilityId, Long tenantId) {
        List<InventoryItemDetailEntity> items = inventoryItemDetailRepository.findByTenantIdAndShipmentId(tenantId, shipmentId);
        for (InventoryItemDetailEntity item : items) {
            item.setFacilityId(facilityId);
        }
        inventoryItemDetailRepository.saveAll(items);
    }

    public List<InventoryItemDetailEntity> getItemsByShipmentId(String shipmentId, Long tenantId) {
        return inventoryItemDetailRepository.findByTenantIdAndShipmentId(tenantId, shipmentId);
    }

    public void deleteItem(String itemId, String shipmentId, Long tenantId) {
        InventoryItemDetailEntity entity = inventoryItemDetailRepository.findById(itemId).orElse(null);
        if (entity == null || !entity.getTenantId().equals(tenantId) || !entity.getShipmentId().equals(shipmentId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        inventoryItemDetailRepository.delete(entity);
    }

}
