package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.logistics.constant.ShipmentStatus;
import serp.project.logistics.dto.request.InventoryItemDetailUpdateForm;
import serp.project.logistics.dto.request.ShipmentCreationForm;
import serp.project.logistics.dto.request.ShipmentUpdateForm;
import serp.project.logistics.entity.InventoryItemDetailEntity;
import serp.project.logistics.entity.InventoryItemEntity;
import serp.project.logistics.entity.OrderEntity;
import serp.project.logistics.entity.OrderItemEntity;
import serp.project.logistics.entity.ShipmentEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.repository.InventoryItemDetailRepository;
import serp.project.logistics.repository.InventoryItemRepository;
import serp.project.logistics.repository.OrderItemRepository;
import serp.project.logistics.repository.OrderRepository;
import serp.project.logistics.repository.ShipmentRepository;
import serp.project.logistics.repository.specification.ShipmentSpecification;
import serp.project.logistics.util.PaginationUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final InventoryItemDetailRepository inventoryItemDetailRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createShipment(ShipmentCreationForm form, Long userId, Long tenantId) {
        OrderEntity order = orderRepository.findById(form.getOrderId()).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.error("[ShipmentService] Order {} not found for tenant {}", form.getOrderId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        ShipmentEntity shipment = new ShipmentEntity(form, order, userId, tenantId);

        for (ShipmentCreationForm.InventoryItemDetail itemForm : form.getItems()) {
            OrderItemEntity orderItem = orderItemRepository.findById(itemForm.getOrderItemId()).orElse(null);
            if (orderItem == null || !orderItem.getTenantId().equals(tenantId)) {
                log.error("[ShipmentService] Order Item ID {} not found for tenant {}", itemForm.getOrderItemId(),
                        tenantId);
                throw new AppException(AppErrorCode.NOT_FOUND);
            }

            shipment.addItem(itemForm, orderItem);
        }

        shipmentRepository.save(shipment);
        log.info("[ShipmentService] Created shipment {} for order {} and tenant {}", shipment.getId(),
                form.getOrderId(),
                tenantId);

        inventoryItemDetailRepository.saveAll(shipment.getItems());
        log.info("[ShipmentService] Created {} inventory item details for shipment {} and tenant {}",
                shipment.getItems().size(), shipment.getId(), tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateShipment(String shipmentId, ShipmentUpdateForm form, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[ShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        shipment.update(form);

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

        List<InventoryItemDetailEntity> items = inventoryItemDetailRepository.findByTenantIdAndShipmentId(tenantId,
                shipmentId);
        shipment.setItems(items);

        shipment.importShipment(userId);

        List<InventoryItemDetailEntity> itemsToUpdate = shipment.getItems();
        List<InventoryItemEntity> inventoryItemsToSave = itemsToUpdate.stream()
                .map(InventoryItemDetailEntity::getInventoryItem)
                .toList();

        shipmentRepository.save(shipment);
        log.info("[ShipmentService] Marked shipment {} as imported for tenant {}", shipmentId, tenantId);

        inventoryItemRepository.saveAll(inventoryItemsToSave);
        log.info("[ShipmentService] Saved {} inventory items for shipment {} and tenant {}",
                inventoryItemsToSave.size(),
                shipmentId, tenantId);

        inventoryItemDetailRepository.saveAll(itemsToUpdate);
        log.info("[ShipmentService] Updated {} inventory item details for shipment {} and tenant {}",
                itemsToUpdate.size(),
                shipmentId, tenantId);

        // Check and update order status
        OrderEntity order = orderRepository.findById(shipment.getOrderId()).orElse(null);
        if (order == null || !order.getTenantId().equals(tenantId)) {
            log.error("[ShipmentService] Order {} not found for tenant {}", shipment.getOrderId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        order.setItems(orderItemRepository.findByTenantIdAndOrderId(tenantId, order.getId()));
        order.setShipments(
                shipmentRepository.findByTenantIdAndOrderId(tenantId, order.getId()));
        if (!order.tryMarkAsFullyDelivered()) {
            log.info("[ShipmentService] Order {} is not fully delivered yet for tenant {}", order.getId(), tenantId);
            return;
        }
        orderRepository.save(order);
        log.info("[ShipmentService] Marked order {} as FULLY_DELIVERED for tenant {}", order.getId(), tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteShipment(String shipmentId, Long tenantId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[ShipmentService] Shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        if (ShipmentStatus.valueOf(shipment.getStatusId()).ordinal() > ShipmentStatus.CREATED.ordinal()) {
            log.error("[ShipmentService] Invalid status transition for shipment {} with status {} for tenant {}",
                    shipmentId, shipment.getStatusId(), tenantId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        inventoryItemDetailRepository.deleteByShipmentId(shipmentId);
        log.info("[ShipmentService] Deleted inventory item details for shipment {} and tenant {}", shipmentId,
                tenantId);

        shipmentRepository.delete(shipment);
        log.info("[ShipmentService] Deleted shipment {} for tenant {}", shipmentId, tenantId);
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
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return shipmentRepository.findAll(
                ShipmentSpecification.satisfy(query, shipmentTypeId, fromSupplierId, toCustomerId, orderId, statusId,
                        tenantId),
                pageable);
    }

    public List<ShipmentEntity> findByOrderId(String orderId, Long tenantId) {
        return shipmentRepository.findByTenantIdAndOrderId(tenantId, orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createInventoryItemDetails(
            ShipmentCreationForm.InventoryItemDetail form,
            String shipmentId,
            Long tenantId) {
        OrderItemEntity orderItem = orderItemRepository.findById(form.getOrderItemId()).orElse(null);
        if (orderItem == null || !orderItem.getTenantId().equals(tenantId)) {
            log.error("[InventoryItemDetailService] Order Item ID {} not found for tenant {}", form.getOrderItemId(),
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[InventoryItemDetailService] Shipment ID {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        shipment.addItem(form, orderItem);
        inventoryItemDetailRepository.saveAll(shipment.getItems());
        log.info("[InventoryItemDetailService] Created inventory item detail for shipment {} and tenant {}", shipmentId,
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateInventoryItemDetail(String itemId, InventoryItemDetailUpdateForm form, String shipmentId,
            Long tenantId) {
        InventoryItemDetailEntity item = inventoryItemDetailRepository.findById(itemId).orElse(null);
        if (item == null || !item.getTenantId().equals(tenantId) || !item.getShipmentId().equals(shipmentId)) {
            log.info("[InventoryItemDetailService] Inventory Item Detail ID {} not found for tenant {}", itemId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        OrderItemEntity orderItem = orderItemRepository.findById(item.getOrderItemId()).orElse(null);
        if (orderItem == null || !orderItem.getTenantId().equals(tenantId)) {
            log.error("[InventoryItemDetailService] Order Item ID {} not found for tenant {}", item.getOrderItemId(),
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[InventoryItemDetailService] Shipment ID {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        item.setOrderItem(orderItem);

        shipment.updateItem(item, form);

        inventoryItemDetailRepository.save(item);
        log.info("[InventoryItemDetailService] Inventory Item Detail ID {} updated for tenant {}", itemId,
                tenantId);
    }

    public List<InventoryItemDetailEntity> getItemsByShipmentId(String shipmentId, Long tenantId) {
        return inventoryItemDetailRepository.findByTenantIdAndShipmentId(tenantId, shipmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(String itemId, String shipmentId, Long tenantId) {
        InventoryItemDetailEntity item = inventoryItemDetailRepository.findById(itemId).orElse(null);
        if (item == null || !item.getTenantId().equals(tenantId) || !item.getShipmentId().equals(shipmentId)) {
            log.error("[InventoryItemDetailService] Inventory Item Detail ID {} not found for tenant {}", itemId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null || !shipment.getTenantId().equals(tenantId)) {
            log.error("[InventoryItemDetailService] Shipment ID {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        shipment.removeItem(item);

        inventoryItemDetailRepository.delete(item);
        log.info("[InventoryItemDetailService] Inventory Item Detail ID {} deleted", itemId);
    }

}
