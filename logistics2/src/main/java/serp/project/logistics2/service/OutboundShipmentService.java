package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.logistics2.dto.request.*;
import serp.project.logistics2.entity.*;
import serp.project.logistics2.repository.*;
import serp.project.logistics2.util.PaginationUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundShipmentService {

    private final OutboundShipmentRepository shipmentRepository;
    private final OutboundShipmentItemRepository shipmentItemRepository;
    private final FacilityRepository facilityRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public OutboundShipmentEntity getShipment(String shipmentId, Long tenantId) {
        OutboundShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) {
            log.info("[OutboundShipmentService] Outbound shipment {} not found for tenant {}", shipmentId, tenantId);
            return null;
        }

        List<OutboundShipmentItemEntity> items = shipmentItemRepository.findByTenantIdAndOutboundShipmentId(tenantId, shipmentId);
        for (OutboundShipmentItemEntity item : items) {
            ProductEntity product = productRepository.findById(item.getProductId()).orElse(null);
            item.setProduct(product);

            InventoryItemEntity inventoryItem = inventoryItemRepository.findById(item.getInventoryItemId()).orElse(null);
            item.setInventoryItem(inventoryItem);
        }
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

}

