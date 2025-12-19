package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;
import serp.project.purchase_service.constant.OrderStatus;
import serp.project.purchase_service.constant.OrderType;
import serp.project.purchase_service.dto.request.OrderCreationForm;
import serp.project.purchase_service.dto.request.OrderUpdateForm;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.util.IdUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_order_header")
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

    @Transient
    private List<ShipmentEntity> shipments = new ArrayList<>();

    public OrderEntity(OrderCreationForm form, List<OrderItemEntity> orderItems, Long userId, Long tenantId) {
        String orderId = IdUtils.generateOrderId();
        this.id = orderId;
        this.orderTypeId = OrderType.PURCHASE.name();
        this.fromSupplierId = form.getFromSupplierId();
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

        this.items = orderItems;

        this.totalAmount = 0L;
        for (OrderItemEntity item : orderItems) {
            this.totalAmount += item.getAmount();
        }
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

    public void addOrderItem(OrderItemEntity item) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        this.items.add(item);
        this.totalAmount += item.getAmount();
    }

    public void removeOrderItem(OrderItemEntity item) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        this.items.remove(item);
        this.totalAmount -= item.getAmount();
    }

    public void approve(Long userId) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        this.statusId = OrderStatus.APPROVED.name();
        this.userApprovedId = userId;
    }

    public void cancel(String cancellationNote, Long userId) {
        if (OrderStatus.valueOf(this.statusId).ordinal() > OrderStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS);
        }

        this.statusId = OrderStatus.CANCELLED.name();
        this.cancellationNote = cancellationNote;
        this.userCancelledId = userId;
    }

}
