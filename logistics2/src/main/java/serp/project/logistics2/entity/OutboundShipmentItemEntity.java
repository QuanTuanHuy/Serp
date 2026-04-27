package serp.project.logistics2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;
import serp.project.logistics2.dto.request.OutboundShipmentCreationForm;
import serp.project.logistics2.dto.request.OutboundShipmentItemUpdateForm;
import serp.project.logistics2.util.IdUtils;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_outbound_shipment_item")
@Slf4j
public class OutboundShipmentItemEntity {

    @Id
    private String id;

    @Column(name = "outbound_shipment_id")
    private String outboundShipmentId;

    @Column(name = "inventory_item_detail_id")
    private String inventoryItemDetailId;

    @Column(name = "inventory_item_id")
    private String inventoryItemId;

    @Column(name = "product_id")
    private String productId;

    private int quantity;

    @Formula("quantity - COALESCE((SELECT SUM(di.quantity) " +
            "FROM wms2_delivery_item di " +
            "WHERE di.outbound_shipment_item_id = id), 0)")
    private int quantityRemaining;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private ProductEntity product;

    @Transient
    private InventoryItemEntity inventoryItem;

    public OutboundShipmentItemEntity(
            String id,
            String outboundShipmentId,
            String inventoryItemDetailId,
            String inventoryItemId,
            String productId,
            int quantity,
            Long tenantId) {
        this.id = id;
        this.outboundShipmentId = outboundShipmentId;
        this.inventoryItemDetailId = inventoryItemDetailId;
        this.inventoryItemId = inventoryItemId;
        this.productId = productId;
        this.quantity = quantity;
        this.tenantId = tenantId;
    }

    public static OutboundShipmentItemEntity create(
            String outboundShipmentId,
            String inventoryItemDetailId,
            String inventoryItemId,
            String productId,
            int quantity,
            Long tenantId) {
        String id = IdUtils.generateOutboundShipmentItemId();
        return new OutboundShipmentItemEntity(id, outboundShipmentId, inventoryItemDetailId, inventoryItemId, productId,
                quantity, tenantId);
    }

    public static OutboundShipmentItemEntity create(OutboundShipmentCreationForm.ItemForm form, String shipmentId,
            Long tenantId) {
        return OutboundShipmentItemEntity.create(shipmentId, form.getInventoryItemDetailId(), form.getInventoryItemId(),
                form.getProductId(), form.getQuantity(), tenantId);
    }

    public void update(int quantity) {
        this.quantity = quantity;
    }

    public void update(OutboundShipmentItemUpdateForm form) {
        this.update(form.getQuantity());
    }

}
