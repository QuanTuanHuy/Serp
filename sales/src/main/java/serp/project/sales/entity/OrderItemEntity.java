package serp.project.sales.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.sales.constant.OrderItemStatus;
import serp.project.sales.dto.request.OrderCreationForm.OrderItem;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.util.CalculatorUtils;
import serp.project.sales.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
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

    @Transient
    private List<InventoryItemDetailEntity> allocatedInventoryItems = new ArrayList<>();

    public OrderItemEntity(OrderItem itemForm, ProductEntity product, List<InventoryItemEntity> availableInventoryItems,
            Long tenantId) {
        this.id = IdUtils.generateOrderItemId();
        this.orderItemSeqId = itemForm.getOrderItemSeqId();
        this.productId = itemForm.getProductId();
        this.quantity = itemForm.getQuantity();
        this.amount = CalculatorUtils.calculateTotalAmount(product.getWholeSalePrice(), this.getQuantity(),
                this.getDiscount(), this.getTax());
        this.statusId = OrderItemStatus.CREATED.name();
        this.price = product.getWholeSalePrice();
        this.tax = itemForm.getTax();
        this.discount = itemForm.getDiscount();
        this.unit = product.getUnit();
        this.tenantId = tenantId;

        this.product = product;
        product.addReservedQuantity(this.quantity);

        int remainingQty = this.quantity;
        for (InventoryItemEntity inventoryItem : availableInventoryItems) {
            if (remainingQty <= 0) {
                break;
            }
            int allocQty = Math.min(remainingQty, inventoryItem.getQuantityOnHand()
                    - inventoryItem.getQuantityReserved()
                    - inventoryItem.getQuantityCommitted());
            InventoryItemDetailEntity detail = new InventoryItemDetailEntity(inventoryItem, product, this.id, allocQty);

            this.allocatedInventoryItems.add(detail);
            remainingQty -= allocQty;
        }
        if (remainingQty > 0) {
            throw new AppException(AppErrorCode.INSUFFICIENT_INVENTORY_TO_ALLOCATE);
        }
    }

    public void commit() {
        for (InventoryItemDetailEntity item : this.allocatedInventoryItems) {
            item.commit();
        }
    }

    public void cleanup() {
        for (InventoryItemDetailEntity item : this.allocatedInventoryItems) {
            item.cleanup();
        }
    }

}
