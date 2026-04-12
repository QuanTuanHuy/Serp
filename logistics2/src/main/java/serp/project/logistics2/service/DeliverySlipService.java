package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.logistics2.constant.DeliverySlipStatus;
import serp.project.logistics2.constant.OrderStatus;
import serp.project.logistics2.constant.ShipmentStatus;
import serp.project.logistics2.dto.request.DeliveryItemUpdateForm;
import serp.project.logistics2.dto.request.DeliverySlipCreationForm;
import serp.project.logistics2.entity.CustomerEntity;
import serp.project.logistics2.entity.DeliveryItemEntity;
import serp.project.logistics2.entity.DeliverySlipEntity;
import serp.project.logistics2.entity.FacilityEntity;
import serp.project.logistics2.entity.OutboundShipmentEntity;
import serp.project.logistics2.entity.ProductEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.repository.AddressRepository;
import serp.project.logistics2.repository.CustomerRepository;
import serp.project.logistics2.repository.DeliveryItemRepository;
import serp.project.logistics2.repository.DeliverySlipRepository;
import serp.project.logistics2.repository.FacilityRepository;
import serp.project.logistics2.repository.OrderRepository;
import serp.project.logistics2.repository.OutboundShipmentRepository;
import serp.project.logistics2.repository.ProductRepository;
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

    @Transactional(rollbackFor = Exception.class)
    public void createSlip(DeliverySlipCreationForm form, Long userId, Long tenantId) {
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
            return DeliveryItemEntity.create(
                    null,
                    itemForm.getOutbountShipmentItemId(),
                    itemForm.getInventoryItemId(),
                    itemForm.getQuantity(),
                    product,
                    tenantId);
        }).toList();

        CustomerEntity customer = customerRepository.findByIdAndTenantId(form.getCustomerId(), tenantId).orElse(null);
        if (customer == null) {
            log.info("[DeliverySlipService] Customer {} not found for tenant {}", form.getCustomerId(), tenantId);
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

    public void exportSlip(String slipId, Long userId, Long tenantId) {
        DeliverySlipEntity slip = deliverySlipRepository.findByIdAndTenantIdWithLock(slipId, tenantId)
                .orElse(null);
        if (slip == null) {
            log.info("[DeliverySlipService] Delivery slip {} not found for tenant {}", slipId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (DeliverySlipStatus.valueOf(slip.getStatus()).ordinal() >= DeliverySlipStatus.DELIVERING.ordinal()) {
            log.info("[DeliverySlipService] Delivery slip {} is not in a right status to be exported", slipId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        deliverySlipRepository.updateStatusByIdAndTenantId(DeliverySlipStatus.DELIVERING.name(), slipId, tenantId);
        log.info("[DeliverySlipService] Marked delivery slip {} as DELIVERING for tenant {}", slipId, tenantId);

    }

    @Transactional(rollbackFor = Exception.class)
    public void deliverSlip(String slipId, Long userId, Long tenantId) {
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

        long pendingSlipsCount = deliverySlipRepository
                .countPendingSlipByOutboundShipmentId(slip.getOutboundShipmentId());

        if (pendingSlipsCount == 0) {
            exportShipment(slip.getOutboundShipmentId(), userId, tenantId);
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
        return deliverySlipRepository.search(
                status,
                outboundShipmentId,
                customerId,
                facilityId,
                query,
                tenantId,
                pageable);
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

        DeliveryItemEntity item = DeliveryItemEntity.create(
                slipId,
                form.getOutbountShipmentItemId(),
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

    private void exportShipment(String shipmentId, Long userId, Long tenantId) {
        OutboundShipmentEntity shipment = shipmentRepository.findByIdAndTenantIdWithLock(shipmentId, tenantId)
                .orElse(null);
        if (shipment == null) {
            log.info("[DeliverySlipService] Outbound shipment {} not found for tenant {}", shipmentId, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        if (!ShipmentStatus.CREATED.name().equals(shipment.getStatus())) {
            log.info("[DeliverySlipService] Outbound shipment {} is not in a right status to be exported",
                    shipmentId);
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        shipmentRepository.updateStatusByIdAndTenantId(ShipmentStatus.EXPORTED.name(), shipmentId, tenantId);
        log.info("[DeliverySlipService] Marked shipment {} as EXPORTED for tenant {}", shipmentId, tenantId);

        long pendingShipmentCount = shipmentRepository.countPendingShipmentByOrderId(shipment.getId());

        if (pendingShipmentCount == 0) {
            orderRepository.updateOrderStatus(shipment.getOrderId(), OrderStatus.FULLY_DELIVERED.name(), tenantId);
            log.info("[DeliverySlipService] Marked order {} as FULLY_DELIVERED for tenant {}",
                    shipment.getOrderId(), tenantId);
        } else {
            log.info("[DeliverySlipService] Order {} has {} pending shipments for tenant {}", shipment.getOrderId(),
                    pendingShipmentCount, tenantId);
        }

    }

}
