package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import serp.project.purchase_service.repository.InventoryItemDetailRepository;
import serp.project.purchase_service.repository.OrderRepository;
import serp.project.purchase_service.repository.ShipmentRepository;
import serp.project.purchase_service.util.IdUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final InventoryItemDetailService inventoryItemDetailService;
    private final InvoiceService invoiceService;
    private final InventoryItemService inventoryItemService;
    private final OrderRepository orderRepository;
    private final InventoryItemDetailRepository inventoryItemDetailRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createShipment(ShipmentCreationForm form, Long userId, Long tenantId) {
        String orderStatus = orderRepository.getOrderStatus(form.getOrderId(), tenantId);
        if (!orderStatus.equals(OrderStatus.APPROVED.value())) {
            log.error("[ShipmentService] Cannot create shipment for order {} with status {} for tenant {}",
                    form.getOrderId(), orderStatus, tenantId);
            throw new AppException(AppErrorCode.ORDER_NOT_APPROVED_YET);
        }

        String shipmentId = IdUtils.generateShipmentId();
        ShipmentEntity shipment = ShipmentEntity.builder()
                .id(shipmentId)
                .shipmentTypeId("INBOUND")
                .fromSupplierId(form.getFromSupplierId())
                .createdByUserId(userId)
                .orderId(form.getOrderId())
                .shipmentName(StringUtils.hasText(form.getShipmentName()) ? form.getShipmentName()
                        : "Phiếu nhập tự động mã " + shipmentId)
                .statusId(ShipmentStatus.CREATED.value())
                .note(form.getNote())
                .expectedDeliveryDate(form.getExpectedDeliveryDate())
                .tenantId(tenantId)
                .build();
        shipmentRepository.save(shipment);
        log.info("[ShipmentService] Created shipment {} for order {} and tenant {}", shipmentId, form.getOrderId(),
                tenantId);

        for (ShipmentCreationForm.InventoryItemDetail itemDetail : form.getItems()) {
            inventoryItemDetailService.createInventoryItemDetails(
                    shipmentId,
                    itemDetail,
                    form.getFacilityId(),
                    tenantId);
        }
        log.info("[ShipmentService] Created {} items for shipment {} and tenant {}", form.getItems().size(), shipmentId,
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShipment(String shipmentId, ShipmentUpdateForm form, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[ShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        shipment.setShipmentName(form.getShipmentName());
        shipment.setNote(form.getNote());
        shipment.setExpectedDeliveryDate(form.getExpectedDeliveryDate());

        shipmentRepository.save(shipment);
        log.info("[ShipmentService] Updated shipment {} for tenant {}", shipmentId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShipmentBatch(List<ShipmentEntity> shipments) {
        shipmentRepository.saveAll(shipments);
        log.info("[ShipmentService] Updated batch of {} shipments", shipments.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFacility(String shipmentId, String facilityId, Long tenantId) {
        inventoryItemDetailService.updateFacility(shipmentId, facilityId, tenantId);
        log.info("[ShipmentService] Updated facility {} for shipment {} and tenant {}", facilityId, shipmentId,
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void importShipment(String shipmentId, Long userId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[ShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!shipment.getStatusId().equals(ShipmentStatus.READY.value())) {
            log.error("[ShipmentService] Invalid status transition for shipment {} with status {} for tenant {}",
                    shipmentId, shipment.getStatusId(), tenantId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }
        shipment.setStatusId(ShipmentStatus.IMPORTED.value());
        shipment.setHandledByUserId(userId);

        shipmentRepository.save(shipment);
        log.info("[ShipmentService] Imported shipment {} for tenant {}", shipmentId, tenantId);

        log.info("[ShipmentService] Creating inventory items for shipment {} and tenant {}", shipmentId, tenantId);
        List<InventoryItemDetailEntity> items = inventoryItemDetailService.getItemsByShipmentId(shipmentId, tenantId);
        for (var item : items) {
            inventoryItemService.createInventoryItem(item);
        }

        int shipmentNotDeliveredCount = shipmentRepository.countShipmentEntitiesByTenantIdAndOrderIdAndStatusId(
                tenantId, shipment.getOrderId(), ShipmentStatus.CREATED.value());
        if (shipmentNotDeliveredCount == 0) {
            log.info("[ShipmentService] All shipments delivered for order {} and tenant {}", shipment.getOrderId(),
                    tenantId);
            log.info("[ShipmentService] Creating invoice for order {} and tenant {}", shipment.getOrderId(), tenantId);
            invoiceService.createInvoice(shipment.getOrderId());

            log.info("[ShipmentService] Updated order status to FULLY_DELIVERED for order {} and tenant {}",
                    shipment.getOrderId(), tenantId);
            orderRepository.updateOrderStatus(shipment.getOrderId(), OrderStatus.FULLY_DELIVERED.value(), tenantId);
        }
    }

    public List<ShipmentEntity> findByOrderId(String orderId, Long tenantId) {
        return shipmentRepository.findByTenantIdAndOrderId(tenantId, orderId);
    }

    public ShipmentEntity getShipment(String shipmentId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.info("[ShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            return null;
        }
        return shipment;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteShipment(String shipmentId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[ShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        inventoryItemDetailRepository.deleteByShipmentId(shipmentId);
        log.info("[ShipmentService] Deleted inventory item details for shipment {} and tenant {}", shipmentId,
                tenantId);

        shipmentRepository.delete(shipment);
        log.info("[ShipmentService] Deleted shipment {} for tenant {}", shipmentId, tenantId);
    }

}
