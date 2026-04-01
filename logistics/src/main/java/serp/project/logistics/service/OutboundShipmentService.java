package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.logistics.constant.OrderStatus;
import serp.project.logistics.constant.ShipmentStatus;
import serp.project.logistics.dto.request.*;
import serp.project.logistics.entity.*;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.repository.*;
import serp.project.logistics.util.PaginationUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundShipmentService {

    private final OutboundShipmentRepository shipmentRepository;
    private final OutboundShipmentItemRepository shipmentItemRepository;
    private final OrderRepository orderRepository;
    private final InventoryItemDetailRepository inventoryItemDetailRepository;
    private final FacilityRepository facilityRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createShipment(OutboundShipmentCreationForm form, Long userId, Long tenantId) {
        OutboundShipmentEntity shipment = OutboundShipmentEntity.create(form, userId, tenantId);

        for (OutboundShipmentCreationForm.ItemForm itemForm : form.getItems()) {
            shipment.addItem(itemForm);
        }

        shipmentRepository.save(shipment);
        log.info("[OutboundShipmentService] Created outbound shipment {} for order {} and tenant {}", shipment.getId(),
                form.getOrderId(),
                tenantId);

        shipmentItemRepository.saveAll(shipment.getItems());
        log.info("[OutboundShipmentService] Created {} shipment items for shipment {} and tenant {}",
                shipment.getItems().size(), shipment.getId(), tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShipment(String shipmentId, OutboundShipmentUpdateForm form, Long tenantId) {
        OutboundShipmentEntity shipment = shipmentRepository.findByIdAndTenantIdWithLock(shipmentId, tenantId).orElse(null);
        if (shipment == null) {
            log.info("[OutboundShipmentService] Outbound shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        shipment.update(form);

        shipmentRepository.save(shipment);
        log.info("[OutboundShipmentService] Updated outbound shipment {} for tenant {}", shipmentId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void exportShipment(String shipmentId, Long userId, Long tenantId) {
        OutboundShipmentEntity shipment = shipmentRepository.findByIdAndTenantIdWithLock(shipmentId, tenantId).orElse(null);
        if (shipment == null) {
            log.info("[OutboundShipmentService] Outbound shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!ShipmentStatus.CREATED.name().equals(shipment.getStatus())) {
            log.info("[OutboundShipmentService] Outbound shipment {} is not in a right status to be exported", shipmentId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        shipmentRepository.updateStatusByIdAndTenantId(ShipmentStatus.EXPORTED.name(), shipmentId, tenantId);
        log.info("[OutboundShipmentService] Marked shipment {} as EXPORTED for tenant {}", shipmentId, tenantId);

        long pendingShipmentCount = shipmentRepository.countPendingShipmentByOrderId(shipment.getId());

        if (pendingShipmentCount == 0) {
            orderRepository.updateOrderStatus(shipment.getOrderId(), OrderStatus.FULLY_DELIVERED.name(), tenantId);
            log.info("[OutboundShipmentService] Marked order {} as FULLY_DELIVERED for tenant {}", shipment.getOrderId(), tenantId);
        } else {
            log.info("[OutboundShipmentService] Order {} has {} pending shipments for tenant {}", shipment.getOrderId(), pendingShipmentCount, tenantId);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteShipment(String shipmentId, Long tenantId) {
        OutboundShipmentEntity shipment = shipmentRepository.findByIdAndTenantIdWithLock(shipmentId, tenantId).orElse(null);
        if (shipment == null) {
            log.info("[OutboundShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        if (ShipmentStatus.valueOf(shipment.getStatus()).ordinal() > ShipmentStatus.CREATED.ordinal()) {
            log.error("[OutboundShipmentService] Invalid status transition for shipment {} with status {} for tenant {}",
                    shipmentId, shipment.getStatus(), tenantId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        shipmentItemRepository.deleteByOutboundShipmentId(shipmentId);
        log.info("[OutboundShipmentService] Deleted shipment items for outbound shipment {} and tenant {}", shipmentId,
                tenantId);

        shipmentRepository.delete(shipment);
        log.info("[OutboundShipmentService] Deleted outbound shipment {} for tenant {}", shipmentId, tenantId);
    }

    public OutboundShipmentEntity getShipment(String shipmentId, Long tenantId) {
        OutboundShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            log.info("[OutboundShipmentService] Outbound shipment {} not found for tenant {}", shipmentId, tenantId);
            return null;
        }

        List<OutboundShipmentItemEntity> items = shipmentItemRepository.findByTenantIdAndOutboundShipmentId(tenantId, shipmentId);
        shipment.setItems(items);

        FacilityEntity facility = facilityRepository.findById(shipment.getFacilityId()).orElse(null);
        shipment.setFacility(facility);

        return shipment;
    }

    public Page<OutboundShipmentEntity> findShipments(
            String status,
            String orderId,
            String facilityId,
            String query,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return shipmentRepository.search(
                status,
                orderId,
                facilityId,
                query,
                tenantId,
                pageable);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addShipmentItem(
            OutboundShipmentCreationForm.ItemForm form,
            String shipmentId,
            Long tenantId) {
        OutboundShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.info("[InventoryItemDetailService] Shipment ID {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        shipment.addItem(form);
        shipmentItemRepository.saveAll(shipment.getItems());
        log.info("[InventoryItemDetailService] Created shipment item for shipment {} and tenant {}", shipmentId,
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShipmentItem(String itemId, OutboundShipmentItemUpdateForm form, String shipmentId,
                                          Long tenantId) {
        OutboundShipmentItemEntity item = shipmentItemRepository.findByIdAndTenantIdWithLock(itemId, tenantId).orElse(null);
        if (item == null || !item.getOutboundShipmentId().equals(shipmentId)) {
            log.info("[InventoryItemDetailService] Shipment item ID {} not found for tenant {}", itemId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        item.update(form);

        shipmentItemRepository.save(item);
        log.info("[InventoryItemDetailService] Shipment Item ID {} updated for tenant {}", itemId,
                tenantId);
    }

    public List<OutboundShipmentItemEntity> getItemsByShipmentId(String shipmentId, Long tenantId) {
        return shipmentItemRepository.findByTenantIdAndOutboundShipmentId(tenantId, shipmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(String itemId, String shipmentId, Long tenantId) {
        OutboundShipmentItemEntity item = shipmentItemRepository.findByIdAndTenantIdWithLock(itemId, tenantId).orElse(null);
        if (item == null || !item.getOutboundShipmentId().equals(shipmentId)) {
            log.error("[InventoryItemDetailService] Shipment Item ID {} not found for tenant {}", itemId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        shipmentItemRepository.delete(item);
    }

}
