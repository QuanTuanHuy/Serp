package serp.project.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.sales.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_inventory_item_detail")
public class InventoryItemDetailEntity {

    @Id
    private String id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "inventory_item_id")
    private String inventoryItemId;

    private int quantity;

    @Column(name = "shipment_id")
    private String shipmentId;

    @Column(name = "order_item_id")
    private String orderItemId;

    private String note;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "lot_id")
    private String lotId;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "facility_id")
    private String facilityId;

    private String unit;

    private long price;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private InventoryItemEntity inventoryItem;

    public InventoryItemDetailEntity(InventoryItemEntity iventoryItem, ProductEntity product, String orderItemId,
            int quantity) {
        this.id = IdUtils.generateInventoryItemDetailId();
        this.productId = iventoryItem.getProductId();
        this.inventoryItemId = iventoryItem.getId();
        this.quantity = quantity;
        this.orderItemId = orderItemId;
        this.lotId = iventoryItem.getLotId();
        this.expirationDate = iventoryItem.getExpirationDate();
        this.manufacturingDate = iventoryItem.getManufacturingDate();
        this.facilityId = iventoryItem.getFacilityId();
        this.unit = product.getUnit();
        this.price = product.getWholeSalePrice();
        this.tenantId = iventoryItem.getTenantId();

        iventoryItem.reserveQuantity(quantity);
        this.inventoryItem = iventoryItem;
    }

    public void commit() {
        if (this.inventoryItem != null) {
            this.inventoryItem.commitQuantity(this.quantity);
        }
    }

    public void cleanup() {
        if (this.inventoryItem != null) {
            this.inventoryItem.releaseReservedQuantity(this.quantity);
        }
    }

}
