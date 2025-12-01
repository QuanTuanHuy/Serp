package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import serp.project.logistics.constant.OrderStatus;
import serp.project.logistics.constant.ShipmentStatus;
import serp.project.logistics.dto.request.ShipmentCreationForm;
import serp.project.logistics.dto.request.ShipmentUpdateForm;
import serp.project.logistics.entity.InventoryItemDetailEntity;
import serp.project.logistics.entity.OrderItemEntity;
import serp.project.logistics.entity.ShipmentEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.repository.InventoryItemDetailRepository;
import serp.project.logistics.repository.OrderItemRepository;
import serp.project.logistics.repository.OrderRepository;
import serp.project.logistics.repository.ShipmentRepository;
import serp.project.logistics.repository.specification.ShipmentSpecification;
import serp.project.logistics.util.IdUtils;
import serp.project.logistics.util.PaginationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final InventoryItemDetailService inventoryItemDetailService;
    private final InventoryItemService inventoryItemService;
    private final OrderRepository orderRepository;
    private final InventoryItemDetailRepository inventoryItemDetailRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createShipment(ShipmentCreationForm form, Long userId, Long tenantId) {
        String orderStatus = orderRepository.getOrderStatus(form.getOrderId(), tenantId);
        if (!orderStatus.equals(OrderStatus.APPROVED.value())) {
            log.error("[ShipmentService] Cannot create shipment for order {} with status {} for tenant {}",
                    form.getOrderId(), orderStatus, tenantId);
            throw new AppException(AppErrorCode.ORDER_NOT_APPROVED_YET);
        }

        String shipmentId = IdUtils.generateShipmentId();
        String shipmentName = StringUtils.hasText(form.getShipmentName()) ? form.getShipmentName()
                : "INBOUND".equals(form.getShipmentTypeId()) ? "Phiếu nhập tự động mã " + shipmentId
                : "Phiếu xuất tự động mã " + shipmentId;
        ShipmentEntity shipment = ShipmentEntity.builder()
                .id(shipmentId)
                .shipmentTypeId(form.getShipmentTypeId())
                .toCustomerId(form.getToCustomerId())
                .fromSupplierId(form.getFromSupplierId())
                .createdByUserId(userId)
                .orderId(form.getOrderId())
                .shipmentName(shipmentName)
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
    public void importShipment(String shipmentId, Long userId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[ShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!shipment.getStatusId().equals(ShipmentStatus.CREATED.value())) {
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

        List<OrderItemEntity> orderItems = orderItemRepository.findByTenantIdAndOrderId(tenantId, shipmentId);
        Map<String, Integer> itemQuantityMap = orderItems.stream().collect(Collectors.toMap(
                OrderItemEntity::getId,
                OrderItemEntity::getQuantity));
        Map<String, Integer> deliveredQuantityMap = new HashMap<>();
        List<ShipmentEntity> importedShipments = shipmentRepository.findByTenantIdAndOrderIdAndStatusId(tenantId, shipment.getOrderId(), ShipmentStatus.IMPORTED.value());
        if (importedShipments.isEmpty()) {
            log.warn("[OrderService] No importedShipments found for order {} and tenant {}", shipment.getOrderId(), tenantId);
            return;
        }
        for (ShipmentEntity importedShipment : importedShipments) {
            var _items = inventoryItemDetailService.getItemsByShipmentId(importedShipment.getId(), tenantId);
            for (var item : _items) {
                deliveredQuantityMap.put(
                        item.getOrderItemId(),
                        deliveredQuantityMap.getOrDefault(item.getOrderItemId(), 0) + item.getQuantity());
            }
        }
        for (var entry : itemQuantityMap.entrySet()) {
            String orderItemId = entry.getKey();
            int orderedQuantity = entry.getValue();
            int deliveredQuantity = deliveredQuantityMap.getOrDefault(orderItemId, 0);
            if (deliveredQuantity != orderedQuantity) {
                log.info(
                        "[OrderService] Order {} is not fully delivery. Order item {} has delivered quantity {}/{}",
                        shipment.getOrderId(), orderItemId, deliveredQuantity, orderedQuantity);
                return;
            }
        }
        orderRepository.updateOrderStatus(shipment.getOrderId(), OrderStatus.FULLY_DELIVERED.value(), tenantId);
        log.info("[OrderService] Marked order {} as fully delivery for tenant {}", shipment.getOrderId(), tenantId);
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

    public Page<ShipmentEntity> findShipments(
            String query,
            String shipmentTypeId,
            String fromSupplierId,
            String toCustomerId,
            String orderId,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return shipmentRepository.findAll(
                ShipmentSpecification.satisfy(query, shipmentTypeId, fromSupplierId, toCustomerId, orderId, statusId, tenantId),
                pageable
        );
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
