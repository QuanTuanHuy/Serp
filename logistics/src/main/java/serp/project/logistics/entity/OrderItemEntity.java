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
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_order_item")
public class OrderItemEntity {

    @Id
    private String id;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "order_item_seq_id")
    private int orderItemSeqId;

    @Column(name = "product_id")
    private String productId;

    private int quantity;

    @Formula("(quantity - (" +
            "   SELECT COALESCE(SUM(iid.quantity), 0) " +
            "   FROM wms2_inventory_item_detail iid " +
            "   WHERE iid.order_item_id = id " +
            "))")
    private int quantityRemaining;

    private long amount;

    @Column(name = "status_id")
    private String statusId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    private long price;

    private float tax;

    private long discount;

    private String unit;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private ProductEntity product;

    public void addDeliveredQuantity(int quantity) {
        if (this.quantityRemaining < quantity) {
            throw new AppException(AppErrorCode.EXCEED_REMAINING_QUANTITY);
        }
        this.quantityRemaining -= quantity;
    }

    public void cancelDeliveredQuantity(int quantity) {
        this.quantityRemaining += quantity;
    }

}
