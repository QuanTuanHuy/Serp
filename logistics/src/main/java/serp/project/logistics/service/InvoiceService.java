package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.logistics.constant.InvoiceStatus;
import serp.project.logistics.entity.InvoiceEntity;
import serp.project.logistics.entity.OrderEntity;
import serp.project.logistics.entity.OrderItemEntity;
import serp.project.logistics.entity.ShipmentEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.repository.InvoiceRepository;
import serp.project.logistics.repository.OrderItemRepository;
import serp.project.logistics.repository.OrderRepository;
import serp.project.logistics.repository.ShipmentRepository;
import serp.project.logistics.util.IdUtils;

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
