package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.entity.*;
import serp.project.purchase_service.repository.InventoryItemDetailRepository;
import serp.project.purchase_service.repository.InvoiceItemRepository;
import serp.project.purchase_service.repository.OrderItemBillingRepository;
import serp.project.purchase_service.util.IdUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceItemService {

    private final InvoiceItemRepository invoiceItemRepository;
    private final InventoryItemDetailRepository inventoryItemDetailRepository;
    private final OrderItemBillingService orderItemBillingService;

    @Transactional(rollbackFor = Exception.class)
    public void createInvoiceItem(String invoiceId, ShipmentEntity shipment) {

        String invoiceItemId = IdUtils.generateInvoiceItemId();
        InvoiceItemEntity invoiceItem = InvoiceItemEntity.builder()
                .id(invoiceItemId)
                .invoiceId(invoiceId)
                .invoiceItemName("Mục hóa đơn tạo tự động từ phiếu nhập " + shipment.getId())
                .tenantId(shipment.getTenantId())
                .build();
        invoiceItemRepository.save(invoiceItem);

        List<InventoryItemDetailEntity> items = inventoryItemDetailRepository
                .findByTenantIdAndShipmentId(shipment.getTenantId(), shipment.getId());
        for (var item : items) {
            orderItemBillingService.createItemBill(invoiceItemId, item);
        }
    }

}
