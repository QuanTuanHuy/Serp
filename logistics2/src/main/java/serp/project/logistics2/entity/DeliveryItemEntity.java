package serp.project.logistics2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics2.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_delivery_item")
public class DeliveryItemEntity {

    @Id
    private String id;

    private String code;

    @Column(name = "delivery_slip_id")
    private String deliverySlipId;

    @Column(name = "outbound_shipment_item_id")
    private String outboundShipmentItemId;

    @Column(name = "inventory_item_id")
    private String inventoryItemId;

    @Column(name = "product_id")
    private String productId;

    private int quantity;

    // Additional
    @Column(name = "product_name")
    private String productName;

    @Column(name = "weight_kg")
    private double weightKg;

    @Column(name = "height_m")
    private double heightM;

    @Column(name = "width_m")
    private double widthM;

    @Column(name = "length_m")
    private double lengthM;

    // Essential
    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

    public DeliveryItemEntity(String id, String code, String deliverySlipId, String outboundShipmentItemId,
                              String inventoryItemId, String productId, int quantity,
                              String productName, double weightKg, double heightM, double widthM, double lengthM,
                              Long tenantId) {
        this.id = id;
        this.code = code;
        this.deliverySlipId = deliverySlipId;
        this.outboundShipmentItemId = outboundShipmentItemId;
        this.inventoryItemId = inventoryItemId;
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.weightKg = weightKg;
        this.heightM = heightM;
        this.widthM = widthM;
        this.lengthM = lengthM;
        this.tenantId = tenantId;
    }

    public static DeliveryItemEntity create(
            String deliverySlipId,
            String outboundShipmentItemId,
            String inventoryItemId,
            int quantity,
            ProductEntity product,
            Long tenantId) {
        String id = IdUtils.generateDeliveryItemId();
        String code = "DI-" + id.substring(6, 12); // Example: DI-XXXXXX
        return new DeliveryItemEntity(
                id,
                code,
                deliverySlipId,
                outboundShipmentItemId,
                inventoryItemId,
                product.getId(),
                quantity,
                product.getName(),
                product.getWeight(),
                product.getHeight() / 100,
                product.getHeight() / 100,
                product.getHeight() / 100,
                tenantId);
    }

    public void update(int newQuantity) {
        this.quantity = newQuantity;
    }

}
