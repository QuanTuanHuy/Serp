package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;
import serp.project.purchase_service.entity.OrderItemBillingEntity;
import serp.project.purchase_service.repository.OrderItemBillingRepository;
import serp.project.purchase_service.util.IdUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemBillingService {

    private final OrderItemBillingRepository orderItemBillingRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createItemBill(String invoiceItemId, InventoryItemDetailEntity item) {
        String itemBillId = IdUtils.generateOrderItemBillingId();
        OrderItemBillingEntity itemBill = OrderItemBillingEntity.builder()
                .id(itemBillId)
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .orderItemId(item.getOrderItemId())
                .invoiceItemId(invoiceItemId)
                .inventoryItemDetailId(item.getId())
                .facilityId(item.getFacilityId())
                .unit(item.getUnit())
                .orderItemBillingTypeId("PURCHASE")
                .tenantId(item.getTenantId())
                .build();
        orderItemBillingRepository.save(itemBill);
        log.info("[OrderItemBillingService] Created order item billing {} for invoice item {} and tenant {}",
                itemBillId, invoiceItemId, item.getTenantId());
    }

}
