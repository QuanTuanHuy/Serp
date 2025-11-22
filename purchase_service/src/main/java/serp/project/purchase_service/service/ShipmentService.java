package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import serp.project.purchase_service.constant.OrderStatus;
import serp.project.purchase_service.constant.ShipmentStatus;
import serp.project.purchase_service.dto.request.ShipmentCreationForm;
import serp.project.purchase_service.dto.request.ShipmentUpdateForm;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;
import serp.project.purchase_service.entity.ShipmentEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.OrderRepository;
import serp.project.purchase_service.repository.ShipmentRepository;
import serp.project.purchase_service.util.IdUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final InventoryItemDetailService inventoryItemDetailService;
    private final InvoiceService invoiceService;
    private final InventoryItemService inventoryItemService;
    private final OrderRepository orderRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createShipment(ShipmentCreationForm form, Long userId, Long tenantId) {
        String orderStatus = orderRepository.getOrderStatus(form.getOrderId(), tenantId);
        if (!orderStatus.equals(OrderStatus.APPROVED.value())) {
            throw new AppException(AppErrorCode.ORDER_NOT_APPROVED_YET);
        }

        String shipmentId = IdUtils.generateShipmentId();
        ShipmentEntity shipment = ShipmentEntity.builder()
                .id(shipmentId)
                .shipmentTypeId("INBOUND")
                .fromSupplierId(form.getFromSupplierId())
                .createdByUserId(userId)
                .orderId(form.getOrderId())
                .shipmentName(StringUtils.hasText(form.getShipmentName()) ? form.getShipmentName() : "Phiếu nhập tự động mã " + shipmentId)
                .statusId(ShipmentStatus.CREATED.value())
                .note(form.getNote())
                .expectedDeliveryDate(form.getExpectedDeliveryDate())
                .tenantId(tenantId)
                .build();
        shipmentRepository.save(shipment);

        for (ShipmentCreationForm.InventoryItemDetail itemDetail : form.getItems()) {
            inventoryItemDetailService.createInventoryItemDetails(
                    shipmentId,
                    itemDetail,
                    form.getFacilityId(),
                    tenantId
            );
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShipment(String shipmentId, ShipmentUpdateForm form, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        shipment.setShipmentName(form.getShipmentName());
        shipment.setNote(form.getNote());
        shipment.setExpectedDeliveryDate(form.getExpectedDeliveryDate());

        shipmentRepository.save(shipment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShipmentBatch(List<ShipmentEntity> shipments) {
        shipmentRepository.saveAll(shipments);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFacility(String shipmentId, String facilityId, Long tenantId) {
        inventoryItemDetailService.updateFacility(shipmentId, facilityId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void importShipment(String shipmentId, Long userId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!shipment.getStatusId().equals(ShipmentStatus.READY.value())) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }
        shipment.setStatusId(ShipmentStatus.IMPORTED.value());
        shipment.setHandledByUserId(userId);

        shipmentRepository.save(shipment);

        List<InventoryItemDetailEntity> items = inventoryItemDetailService.getItemsByShipmentId(shipmentId, tenantId);
        for (var item : items) {
            inventoryItemService.createInventoryItem(item);
        }

        int shipmentNotDeliveredCount = shipmentRepository.countShipmentEntitiesByTenantIdAndOrderIdAndStatusId(tenantId, shipment.getOrderId(), ShipmentStatus.CREATED.value());
        if (shipmentNotDeliveredCount == 0) {
            invoiceService.createInvoice(shipment.getOrderId());
            orderRepository.updateOrderStatus(shipment.getOrderId(), OrderStatus.FULLY_DELIVERED.value(), tenantId);
        }
    }

    public List<ShipmentEntity> findByOrderId(String orderId, Long tenantId) {
        return shipmentRepository.findByTenantIdAndOrderId(tenantId, orderId);
    }

    public ShipmentEntity getShipment(String shipmentId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            return null;
        }
        return shipment;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteShipment(String shipmentId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        shipmentRepository.delete(shipment);
    }

}
