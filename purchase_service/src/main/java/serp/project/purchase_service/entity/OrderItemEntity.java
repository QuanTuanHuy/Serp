package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.purchase_service.constant.OrderItemStatus;
import serp.project.purchase_service.dto.request.OrderCreationForm;
import serp.project.purchase_service.dto.request.OrderItemUpdateForm;
import serp.project.purchase_service.util.CalculatorUtils;
import serp.project.purchase_service.util.IdUtils;

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

    public OrderItemEntity(OrderItemEntity other) {
        this.id = other.id;
        this.orderId = other.orderId;
        this.orderItemSeqId = other.orderItemSeqId;
        this.productId = other.productId;
        this.quantity = other.quantity;
        this.amount = other.amount;
        this.statusId = other.statusId;
        this.createdStamp = other.createdStamp;
        this.lastUpdatedStamp = other.lastUpdatedStamp;
        this.price = other.price;
        this.tax = other.tax;
        this.discount = other.discount;
        this.unit = other.unit;
        this.tenantId = other.tenantId;
        this.product = other.product;
    }

    public OrderItemEntity(OrderCreationForm.OrderItem itemForm, ProductEntity product, Long tenantId) {

        String orderItemId = IdUtils.generateOrderItemId();
        this.id = orderItemId;
        this.orderItemSeqId = itemForm.getOrderItemSeqId();
        this.productId = itemForm.getProductId();
        this.quantity = itemForm.getQuantity();
        this.price = product.getCostPrice();
        this.tax = itemForm.getTax();
        this.discount = itemForm.getDiscount();
        this.amount = CalculatorUtils.calculateTotalAmount(product.getCostPrice(), this.getQuantity(),
                this.getDiscount(), this.getTax());
        this.statusId = OrderItemStatus.CREATED.name();
        this.unit = product.getUnit();
        this.tenantId = tenantId;

        this.product = product;

    }

    public void update(OrderItemUpdateForm form) {
        if (form.getOrderItemSeqId() != 0)
            this.orderItemSeqId = form.getOrderItemSeqId();
        if (form.getQuantity() != 0)
            this.quantity = form.getQuantity();

        this.tax = form.getTax();
        this.discount = form.getDiscount();
        this.amount = CalculatorUtils.calculateTotalAmount(this.price, this.getQuantity(),
                this.getDiscount(), this.getTax());
    }

}
