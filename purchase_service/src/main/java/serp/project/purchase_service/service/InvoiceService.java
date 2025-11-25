package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.constant.InvoiceStatus;
import serp.project.purchase_service.entity.InvoiceEntity;
import serp.project.purchase_service.entity.OrderEntity;
import serp.project.purchase_service.entity.OrderItemEntity;
import serp.project.purchase_service.entity.ShipmentEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.InvoiceRepository;
import serp.project.purchase_service.repository.OrderItemRepository;
import serp.project.purchase_service.repository.OrderRepository;
import serp.project.purchase_service.repository.ShipmentRepository;
import serp.project.purchase_service.util.IdUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final InvoiceItemService invoiceItemService;

    @Transactional(rollbackFor = Exception.class)
    public void createInvoice(String orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.error("[InvoiceService] Order {} not found", orderId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        List<OrderItemEntity> orderItem = orderItemRepository.findByTenantIdAndOrderId(order.getTenantId(), orderId);
        long totalAmount = orderItem.stream()
                .mapToLong(OrderItemEntity::getAmount)
                .sum();

        String invoiceId = IdUtils.generateInvoiceId();
        InvoiceEntity invoice = InvoiceEntity.builder()
                .id(invoiceId)
                .fromSupplierId(order.getFromSupplierId())
                .invoiceTypeId("PURCHASE")
                .invoiceName("Hóa đơn tạo tự động từ đơn hàng " + order.getId())
                .statusId(InvoiceStatus.CREATED.value())
                .amount(totalAmount)
                .tenantId(order.getTenantId())
                .build();

        invoiceRepository.save(invoice);
        log.info("[InvoiceService] Created invoice {} for order {} and tenant {}", invoiceId, orderId,
                order.getTenantId());

        List<ShipmentEntity> shipments = shipmentRepository.findByTenantIdAndOrderId(order.getTenantId(), orderId);
        for (ShipmentEntity shipment : shipments) {
            invoiceItemService.createInvoiceItem(invoiceId, shipment);
        }
    }

}
