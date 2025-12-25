package serp.project.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics.constant.InventoryItemStatus;
import serp.project.logistics.dto.request.InventoryItemCreationForm;
import serp.project.logistics.dto.request.InventoryItemDetailUpdateForm;
import serp.project.logistics.dto.request.ShipmentCreationForm.InventoryItemDetail;
import serp.project.logistics.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

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

    @Transient
    private OrderItemEntity orderItem;

    public InventoryItemDetailEntity(InventoryItemDetail form, OrderItemEntity orderItem, Long tenantId) {
        this.id = IdUtils.generateInventoryItemDetailId();
        this.productId = orderItem.getProductId();
        this.quantity = form.getQuantity();
        this.orderItemId = form.getOrderItemId();
        this.note = form.getNote();
        this.lotId = form.getLotId();
        this.expirationDate = form.getExpirationDate();
        this.manufacturingDate = form.getManufacturingDate();
        this.facilityId = form.getFacilityId();
        this.unit = orderItem.getUnit();
        this.price = orderItem.getPrice();
        this.tenantId = tenantId;

        orderItem.addDeliveredQuantity(this.quantity);
        this.orderItem = orderItem;
    }

    public void update(InventoryItemDetailUpdateForm form) {
        if (form.getQuantity() != this.quantity)
            changeQuantity(form.getQuantity());
        if (StringUtils.hasText(form.getNote()))
            this.note = form.getNote();
        if (StringUtils.hasText(form.getLotId()))
            this.lotId = form.getLotId();
        if (form.getExpirationDate() != null)
            this.expirationDate = form.getExpirationDate();
        if (form.getManufacturingDate() != null)
            this.manufacturingDate = form.getManufacturingDate();
        if (StringUtils.hasText(form.getFacilityId()))
            this.facilityId = form.getFacilityId();
    }

    public void changeQuantity(int newQuantity) {
        this.orderItem.cancelDeliveredQuantity(this.quantity);
        this.orderItem.addDeliveredQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    public void importInventoryItem() {
        InventoryItemCreationForm form = new InventoryItemCreationForm();
        form.setProductId(this.productId);
        form.setQuantity(this.quantity);
        form.setLotId(this.lotId);
        form.setFacilityId(this.facilityId);
        form.setExpirationDate(this.expirationDate);
        form.setManufacturingDate(this.manufacturingDate);
        form.setStatusId(InventoryItemStatus.VALID.name());

        this.inventoryItem = new InventoryItemEntity(form, this.tenantId);
        this.inventoryItemId = this.inventoryItem.getId();
    }

}
