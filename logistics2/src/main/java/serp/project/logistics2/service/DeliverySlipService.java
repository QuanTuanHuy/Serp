package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.logistics2.constant.DeliverySlipStatus;
import serp.project.logistics2.constant.ShipmentStatus;
import serp.project.logistics2.dto.request.DeliveryItemUpdateForm;
import serp.project.logistics2.dto.request.DeliverySlipCreationForm;
import serp.project.logistics2.entity.*;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.*;
import serp.project.logistics2.util.PaginationUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliverySlipService {

    private final DeliverySlipRepository deliverySlipRepository;
    private final DeliveryItemRepository deliveryItemRepository;

    private final OutboundShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final FacilityRepository facilityRepository;
    private final AddressRepository addressRepository;

    private final InventoryItemRepository inventoryItemRepository;
    private final OutboundShipmentItemRepository outboundShipmentItemRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createSlip(DeliverySlipCreationForm form, Long userId, Long tenantId) {
        OutboundShipmentEntity shipment = shipmentRepository.findById(form.getOutboundShipmentId()).orElse(null);
        if (shipment == null) {
            log.info("[DeliverySlipService] Outbound shipment {} not found for tenant {}", form.getOutboundShipmentId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!shipment.getStatus().equals(ShipmentStatus.READY_TO_EXPORT.name())) {
            log.info("[DeliverySlipService] Outbound shipment {} is not in a right status to create delivery slip for tenant {}",
                    form.getOutboundShipmentId(), tenantId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        List<String> productIds = form.getItems().stream().map(DeliverySlipCreationForm.ItemForm::getProductId)
                .toList();
        List<ProductEntity> products = productRepository.findByIdInAndTenantId(productIds, tenantId);
        Map<String, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));
        List<DeliveryItemEntity> items = form.getItems().stream().map(itemForm -> {
            ProductEntity product = productMap.get(itemForm.getProductId());
            if (product == null) {
                log.info("[DeliverySlipService] Product {} not found for tenant {}", itemForm.getProductId(), tenantId);
                throw new AppException(AppErrorCode.NOT_FOUND);
            }

            OutboundShipmentItemEntity shipmentItem = outboundShipmentItemRepository.findById(itemForm.getOutboundShipmentItemId()).orElse(null);
            if (shipmentItem == null) {
                log.info("[DeliverySlipService] Outbound shipment item {} not found for tenant {}", itemForm.getOutboundShipmentItemId(), tenantId);
                throw new AppException(AppErrorCode.NOT_FOUND);
            }
            if (shipmentItem.getQuantityRemaining() < itemForm.getQuantity()) {
                log.info("[DeliverySlipService] Insufficient quantity for outbound shipment item {} for tenant {}. Remaining: {}, requested: {}",
                        itemForm.getOutboundShipmentItemId(), tenantId, shipmentItem.getQuantityRemaining(), itemForm.getQuantity());
                throw new AppException(AppErrorCode.INSUFFICIENT_QUANTITY);
            }

            return DeliveryItemEntity.create(
                    null,
                    itemForm.getOutboundShipmentItemId(),
                    itemForm.getInventoryItemId(),
                    itemForm.getQuantity(),
                    product,
                    tenantId);
        }).toList();

        CustomerEntity customer = customerRepository.findByIdAndTenantId(shipment.getCustomerId(), tenantId).orElse(null);
        if (customer == null) {
            log.info("[DeliverySlipService] Customer {} not found for tenant {}", shipment.getCustomerId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        FacilityEntity facility = facilityRepository.findByIdAndTenantId(form.getFacilityId(), tenantId).orElse(null);
        if (facility == null) {
            log.info("[DeliverySlipService] Facility {} not found for tenant {}", form.getFacilityId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        DeliverySlipEntity slip = DeliverySlipEntity.create(
                form.getOutboundShipmentId(),
                customer,
                facility,
                userId,
                tenantId,
                items);

        deliverySlipRepository.save(slip);
        log.info("[DeliverySlipService] Created delivery slip {} for tenant {}", slip.getId(), tenantId);

        deliveryItemRepository.saveAll(slip.getItems());
        log.info("[DeliverySlipService] Created {} delivery slip items for delivery slip {} and tenant {}",
                slip.getItems().size(), slip.getId(), tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void exportSlip(String slipId, Long tenantId) {
        deliverySlipRepository.updateStatusByIdAndTenantId(DeliverySlipStatus.DELIVERING.name(), slipId, tenantId);
        log.info("[DeliverySlipService] Marked delivery slip {} as DELIVERING for tenant {}", slipId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recallSlip(String slipId, Long tenantId) {
        deliverySlipRepository.updateStatusByIdAndTenantId(DeliverySlipStatus.RECALLING.name(), slipId, tenantId);
        log.info("[DeliverySlipService] Marked delivery slip {} as RECALLING for tenant {}", slipId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnSlip(String slipId, Long tenantId) {
        deliverySlipRepository.updateStatusByIdAndTenantId(DeliverySlipStatus.PENDING.name(), slipId, tenantId);
        log.info("[DeliverySlipService] Marked delivery slip {} as PENDING for tenant {}", slipId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deliverSlip(String slipId, Long tenantId) {
        DeliverySlipEntity slip = deliverySlipRepository.findByIdAndTenantIdWithLock(slipId, tenantId)
                .orElse(null);
        if (slip == null) {
            log.info("[DeliverySlipService] Delivery slip {} not found for tenant {}", slipId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!DeliverySlipStatus.DELIVERING.name().equals(slip.getStatus())) {
            log.info("[DeliverySlipService] Delivery slip {} is not in a right status to be delivered", slipId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        deliverySlipRepository.updateStatusByIdAndTenantId(DeliverySlipStatus.DELIVERED.name(), slipId, tenantId);
        log.info("[DeliverySlipService] Marked delivery slip {} as DELIVERED for tenant {}", slipId, tenantId);

        // Tru hang trong kho
        List<DeliveryItemEntity> items = deliveryItemRepository.findByTenantIdAndDeliverySlipId(tenantId, slipId);
        List<String> inventoryItemIds = items.stream().map(DeliveryItemEntity::getInventoryItemId).toList();
        List<InventoryItemEntity> inventoryItems = inventoryItemRepository.findByIdInAndTenantIdWithLock(
                inventoryItemIds,
                tenantId);
        Map<String, InventoryItemEntity> inventoryItemMap = inventoryItems.stream()
                .collect(Collectors.toMap(InventoryItemEntity::getId, i -> i));
        items.forEach(item -> {
            InventoryItemEntity inventoryItem = inventoryItemMap.get(item.getInventoryItemId());
            inventoryItem.deliver(item.getQuantity());
        });
        inventoryItemRepository.saveAll(inventoryItems);
        log.info("[DeliverySlipService] Performed inventory item deduction for {} inventory items for tenant {}",
                inventoryItems.size(), tenantId);

        // Cap nhat trang thai phieu xuat
        List<OutboundShipmentItemEntity> shipmentItems = outboundShipmentItemRepository.findByTenantIdAndOutboundShipmentId(
                tenantId, slip.getOutboundShipmentId());
        for (OutboundShipmentItemEntity shipmentItem : shipmentItems) {
            if (shipmentItem.getQuantityRemaining() > 0) {
                log.info("[DeliverySlipService] Shipment Item ID {} has remaining quantity {} for tenant {}", shipmentItem.getId(), shipmentItem.getQuantityRemaining(), tenantId);
                return;
            }
        }

        long pendingSlipsCount = deliverySlipRepository
                .countPendingSlipByOutboundShipmentId(slip.getOutboundShipmentId());

        if (pendingSlipsCount == 0) {
            shipmentRepository.updateStatusByIdAndTenantId(ShipmentStatus.DELIVERED.name(), slipId, tenantId);
            log.info("[DeliverySlipService] Marked shipment {} as DELIVERED for tenant {}", slip.getOutboundShipmentId(), tenantId);
        } else {
            log.info("[DeliverySlipService] Shipment {} has {} pending delivery slips for tenant {}",
                    slip.getOutboundShipmentId(),
                    pendingSlipsCount, tenantId);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSlip(String slipId, Long tenantId) {
        DeliverySlipEntity slip = deliverySlipRepository.findByIdAndTenantIdWithLock(slipId, tenantId)
                .orElse(null);
        if (slip == null) {
            log.info("[DeliverySlipService] Delivery slip {} not found for tenant {}", slipId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        if (DeliverySlipStatus.valueOf(slip.getStatus()).ordinal() > DeliverySlipStatus.ASSIGNED.ordinal()) {
            log.info(
                    "[DeliverySlipService] Invalid status transition for delivery slip {} with status {} for tenant {}",
                    slipId, slip.getStatus(), tenantId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        deliveryItemRepository.deleteByDeliverySlipId(slipId);
        log.info("[DeliverySlipService] Deleted delivery items for delivery slip {} and tenant {}", slipId, tenantId);

        deliverySlipRepository.delete(slip);
        log.info("[DeliverySlipService] Deleted delivery slip {} for tenant {}", slipId, tenantId);
    }

    public DeliverySlipEntity getSlip(String slipId, Long tenantId) {
        DeliverySlipEntity slip = deliverySlipRepository.findByIdAndTenantId(slipId, tenantId).orElse(null);
        if (slip == null) {
            log.info("[DeliverySlipService] Delivery slip {} not found for tenant {}", slipId, tenantId);
            return null;
        }

        List<DeliveryItemEntity> items = deliveryItemRepository.findByTenantIdAndDeliverySlipId(tenantId, slipId);
        slip.setItems(items);

        facilityRepository.findById(slip.getFacilityId()).ifPresent(slip::setFacility);
        addressRepository.findById(slip.getCustomerAddressId()).ifPresent(slip::setCustomerAddress);
        addressRepository.findById(slip.getFacilityAddressId()).ifPresent(slip::setFacilityAddress);

        return slip;
    }

    public Page<DeliverySlipEntity> findSlips(
            String status,
            String outboundShipmentId,
            String customerId,
            String facilityId,
            String query,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        Page<DeliverySlipEntity> slipPage = deliverySlipRepository.search(
                status,
                outboundShipmentId,
                customerId,
                facilityId,
                query,
                tenantId,
                pageable);
        List<String> facilityIds = slipPage.getContent().stream().map(DeliverySlipEntity::getFacilityId).toList();
        List<FacilityEntity> facilities = facilityRepository.findAllById(facilityIds);
        Map<String, FacilityEntity> facilityMap = facilities.stream()
                .collect(Collectors.toMap(FacilityEntity::getId, f -> f));
        slipPage.getContent().forEach(slip -> slip.setFacility(facilityMap.get(slip.getFacilityId())));

        List<String> addressIds = slipPage.getContent().stream()
                .flatMap(slip -> Stream.of(slip.getCustomerAddressId(), slip.getFacilityAddressId()))
                .toList();
        List<AddressEntity> addresses = addressRepository.findAllById(addressIds);
        Map<String, AddressEntity> addressMap = addresses.stream()
                .collect(Collectors.toMap(AddressEntity::getId, a -> a));
        slipPage.getContent().forEach(slip -> {
            slip.setCustomerAddress(addressMap.get(slip.getCustomerAddressId()));
            slip.setFacilityAddress(addressMap.get(slip.getFacilityAddressId()));
        });

        return slipPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addDeliveryItem(
            DeliverySlipCreationForm.ItemForm form,
            String slipId,
            Long tenantId) {
        DeliverySlipEntity slip = deliverySlipRepository.findByIdAndTenantId(slipId, tenantId).orElse(null);
        if (slip == null) {
            log.info("[DeliverySlipService] Delivery slip {} not found for tenant {}", slipId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        ProductEntity product = productRepository.findByIdAndTenantId(form.getProductId(), tenantId).orElse(null);
        if (product == null) {
            log.info("[DeliverySlipService] Product {} not found for tenant {}", form.getProductId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        OutboundShipmentItemEntity shipmentItem = outboundShipmentItemRepository.findById(form.getOutboundShipmentItemId()).orElse(null);
        if (shipmentItem == null) {
            log.info("[DeliverySlipService] Outbound shipment item {} not found for tenant {}", form.getOutboundShipmentItemId(), tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (shipmentItem.getQuantityRemaining() < form.getQuantity()) {
            log.info("[DeliverySlipService] Insufficient quantity for outbound shipment item {} for tenant {}. Remaining: {}, requested: {}",
                    form.getOutboundShipmentItemId(), tenantId, shipmentItem.getQuantityRemaining(), form.getQuantity());
            throw new AppException(AppErrorCode.INSUFFICIENT_QUANTITY);
        }

        DeliveryItemEntity item = DeliveryItemEntity.create(
                slipId,
                form.getOutboundShipmentItemId(),
                form.getInventoryItemId(),
                form.getQuantity(),
                product,
                tenantId);

        slip.addItem(item);
        deliveryItemRepository.saveAll(slip.getItems());
        log.info("[DeliverySlipService] Created delivery item for delivery slip {} and tenant {}", slipId,
                tenantId);
        deliverySlipRepository.save(slip);
        log.info("[DeliverySlipService] Updated delivery slip {} for tenant {}", slipId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryItem(String itemId, DeliveryItemUpdateForm form, String slipId,
            Long tenantId) {
        DeliveryItemEntity item = deliveryItemRepository.findByIdAndTenantIdWithLock(itemId, tenantId)
                .orElse(null);
        if (item == null || !item.getDeliverySlipId().equals(slipId)) {
            log.info("[DeliverySlipService] Delivery item ID {} not found for tenant {}", itemId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        DeliverySlipEntity slip = deliverySlipRepository.findByIdAndTenantIdWithLock(slipId, tenantId).orElse(null);
        if (slip == null) {
            log.info("[DeliverySlipService] Delivery slip {} not found for tenant {}", slipId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        if (form.getQuantity() > item.getQuantity()) {
            OutboundShipmentItemEntity shipmentItem = outboundShipmentItemRepository.findById(item.getOutboundShipmentItemId()).orElse(null);
            if (shipmentItem == null) {
                log.info("[DeliverySlipService] Outbound shipment item {} not found for tenant {}", item.getOutboundShipmentItemId(), tenantId);
                throw new AppException(AppErrorCode.NOT_FOUND);
            }
            if (shipmentItem.getQuantityRemaining() < form.getQuantity() - item.getQuantity()) {
                log.info("[DeliverySlipService] Insufficient quantity for outbound shipment item {} for tenant {}. Remaining: {}, requested: {}",
                        item.getOutboundShipmentItemId(), tenantId, shipmentItem.getQuantityRemaining(), form.getQuantity() - item.getQuantity());
                throw new AppException(AppErrorCode.INSUFFICIENT_QUANTITY);
            }
        }

        slip.updateQuantity(item, form.getQuantity());

        deliveryItemRepository.save(item);
        log.info("[DeliverySlipService] Delivery item ID {} updated for tenant {}", itemId,
                tenantId);

        deliverySlipRepository.save(slip);
        log.info("[DeliverySlipService] Updated delivery slip {} for tenant {}", slipId, tenantId);
    }

    public List<DeliveryItemEntity> getItemsByShipmentId(String slipId, Long tenantId) {
        return deliveryItemRepository.findByTenantIdAndDeliverySlipId(tenantId, slipId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(String itemId, String slipId, Long tenantId) {
        DeliveryItemEntity item = deliveryItemRepository.findByIdAndTenantIdWithLock(itemId, tenantId)
                .orElse(null);
        if (item == null || !item.getDeliverySlipId().equals(slipId)) {
            log.error("[DeliverySlipService] Delivery Item ID {} not found for tenant {}", itemId,
                    tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        DeliverySlipEntity slip = deliverySlipRepository.findByIdAndTenantIdWithLock(slipId, tenantId).orElse(null);
        if (slip == null) {
            log.info("[DeliverySlipService] Delivery slip {} not found for tenant {}", slipId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }

        slip.removeItem(item);
        deliveryItemRepository.delete(item);
        log.info("[DeliverySlipService] Deleted delivery item {} for delivery slip {} and tenant {}", itemId, slipId,
                tenantId);

        deliverySlipRepository.save(slip);
        log.info("[DeliverySlipService] Updated delivery slip {} for tenant {}", slipId, tenantId);
    }

}
