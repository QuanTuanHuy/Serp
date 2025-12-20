package serp.project.sales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.sales.constant.OrderStatus;
import serp.project.sales.constant.OrderType;
import serp.project.sales.dto.request.OrderCreationForm;
import serp.project.sales.dto.request.OrderCreationForm.OrderItem;
import serp.project.sales.dto.request.OrderUpdateForm;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_order_header")
@Slf4j
public class OrderEntity {

    @Id
    private String id;

    @Column(name = "order_type_id")
    private String orderTypeId;

    @Column(name = "from_supplier_id")
    private String fromSupplierId;

    @Column(name = "to_customer_id")
    private String toCustomerId;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "status_id")
    private String statusId;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "delivery_before_date")
    private LocalDate deliveryBeforeDate;

    @Column(name = "delivery_after_date")
    private LocalDate deliveryAfterDate;

    private String note;

    @Column(name = "order_name")
    private String orderName;

    private int priority;

    @Column(name = "delivery_address_id")
    private String deliveryAddressId;

    @Column(name = "delivery_phone")
    private String deliveryPhone;

    @Column(name = "sale_channel_id")
    private String saleChannelId;

    @Column(name = "delivery_full_address")
    private String deliveryFullAddress;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "total_amount")
    private Long totalAmount;

    private String costs;

    @Column(name = "user_approved_id")
    private Long userApprovedId;

    @Column(name = "user_cancelled_id")
    private Long userCancelledId;

    @Column(name = "cancellation_note")
    private String cancellationNote;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private List<OrderItemEntity> items = new ArrayList<>();

    public OrderEntity(OrderCreationForm form, Long userId, Long tenantId) {
        String orderId = IdUtils.generateOrderId();
        this.id = orderId;
        this.orderTypeId = OrderType.PURCHASE.name();
        this.toCustomerId = form.getToCustomerId();
        this.createdByUserId = userId;
        this.orderDate = LocalDate.now();
        this.statusId = OrderStatus.CREATED.name();
        this.deliveryBeforeDate = form.getDeliveryBeforeDate();
        this.deliveryAfterDate = form.getDeliveryAfterDate();
        this.note = form.getNote();
        this.orderName = StringUtils.hasText(form.getOrderName()) ? form.getOrderName() : "Đơn hàng mua mã " + orderId;
        this.priority = form.getPriority() != 0 ? form.getPriority() : 20;
        this.saleChannelId = form.getSaleChannelId();
        this.tenantId = tenantId;

        this.totalAmount = 0L;
    }

    public void update(OrderUpdateForm form) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        if (form.getDeliveryBeforeDate() != null)
            this.deliveryBeforeDate = form.getDeliveryBeforeDate();
        if (form.getDeliveryAfterDate() != null)
            this.deliveryAfterDate = form.getDeliveryAfterDate();
        if (StringUtils.hasText(form.getNote()))
            this.note = form.getNote();
        if (StringUtils.hasText(form.getOrderName()))
            this.orderName = form.getOrderName();
        if (form.getPriority() != 0)
            this.priority = form.getPriority();
        if (StringUtils.hasText(form.getSaleChannelId()))
            this.saleChannelId = form.getSaleChannelId();
    }

    public void addItem(OrderItemEntity item) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        item.setOrderId(this.id);
        this.items.add(item);
        this.totalAmount += item.getAmount();
    }

    public void addItems(OrderItem itemForm, ProductEntity product,
            List<InventoryItemEntity> availableInventoryItems) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        OrderItemEntity item = new OrderItemEntity(itemForm, product, availableInventoryItems, this.tenantId);
        item.setOrderId(this.id);
        this.items.add(item);
        this.totalAmount += item.getAmount();
    }

    public void removeItem(OrderItemEntity item) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        this.items.remove(item);
        this.totalAmount -= item.getAmount();

        item.cleanup();
    }

    public void approve(Long userId) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        this.statusId = OrderStatus.APPROVED.name();
        this.userApprovedId = userId;

        for (OrderItemEntity item : this.items) {
            item.commit();
        }
    }

    public void cancel(String cancellationNote, Long userId) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        this.statusId = OrderStatus.CANCELLED.name();
        this.cancellationNote = cancellationNote;
        this.userCancelledId = userId;

        for (OrderItemEntity item : this.items) {
            item.cleanup();
        }
    }

}
