package serp.project.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics.constant.OrderStatus;
import serp.project.logistics.constant.OrderType;
import serp.project.logistics.constant.ShipmentStatus;
import serp.project.logistics.constant.ShipmentType;
import serp.project.logistics.dto.request.InventoryItemDetailUpdateForm;
import serp.project.logistics.dto.request.ShipmentCreationForm;
import serp.project.logistics.dto.request.ShipmentCreationForm.InventoryItemDetail;
import serp.project.logistics.dto.request.ShipmentUpdateForm;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Phieu nhap / xuat hang
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_shipment")
public class ShipmentEntity {

    @Id
    private String id;

    @Column(name = "shipment_type_id")
    private String shipmentTypeId;

    @Column(name = "from_supplier_id")
    private String fromSupplierId;

    @Column(name = "to_customer_id")
    private String toCustomerId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "order_id")
    private String orderId;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "shipment_name")
    private String shipmentName;

    @Column(name = "status_id")
    private String statusId;

    @Column(name = "handled_by_user_id")
    private Long handledByUserId;

    private String note;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "user_cancelled_id")
    private Long userCancelledId;

    @Column(name = "total_weight")
    private long totalWeight;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private List<InventoryItemDetailEntity> items = new ArrayList<>();

    public ShipmentEntity(ShipmentCreationForm form, OrderEntity order, Long userId, Long tenantId) {
        if (!order.getStatusId().equals(OrderStatus.APPROVED.name())) {
            throw new AppException(AppErrorCode.ORDER_NOT_APPROVED_YET);
        }

        String shipmentId = IdUtils.generateShipmentId();
        String shipmentType = OrderType.PURCHASE.name().equals(order.getOrderTypeId()) ? ShipmentType.INBOUND.name()
                : ShipmentType.OUTBOUND.name();
        String shipmentName = StringUtils.hasText(form.getShipmentName()) ? form.getShipmentName()
                : ShipmentType.INBOUND.name().equals(shipmentType) ? "Phiếu nhập tự động mã " + shipmentId
                        : "Phiếu xuất tự động mã " + shipmentId;
        this.id = shipmentId;
        this.shipmentTypeId = shipmentType;
        this.toCustomerId = StringUtils.hasText(order.getToCustomerId()) ? order.getToCustomerId() : null;
        this.fromSupplierId = StringUtils.hasText(order.getFromSupplierId()) ? order.getFromSupplierId() : null;
        this.createdByUserId = userId;
        this.orderId = order.getId();
        this.shipmentName = shipmentName;
        this.statusId = ShipmentStatus.CREATED.name();
        this.note = form.getNote();
        this.expectedDeliveryDate = form.getExpectedDeliveryDate();
        this.tenantId = tenantId;
    }

    public void update(ShipmentUpdateForm form) {
        if (ShipmentStatus.valueOf(this.statusId).ordinal() > ShipmentStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        if (StringUtils.hasText(form.getShipmentName()))
            this.shipmentName = form.getShipmentName();
        if (StringUtils.hasText(form.getNote()))
            this.note = form.getNote();
        if (form.getExpectedDeliveryDate() != null)
            this.expectedDeliveryDate = form.getExpectedDeliveryDate();
    }

    public void addItem(InventoryItemDetailEntity item) {
        if (ShipmentStatus.valueOf(this.statusId).ordinal() > ShipmentStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }
        item.setShipmentId(this.id);
        this.items.add(item);
    }

    public void addItem(InventoryItemDetail form, OrderItemEntity orderItem) {
        if (ShipmentStatus.valueOf(this.statusId).ordinal() > ShipmentStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        InventoryItemDetailEntity item = new InventoryItemDetailEntity(form, orderItem, this.tenantId);
        item.setShipmentId(this.id);
        this.items.add(item);
    }

    public void removeItem(InventoryItemDetailEntity item) {
        if (ShipmentStatus.valueOf(this.statusId).ordinal() > ShipmentStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        this.items.remove(item);
    }

    public void updateItem(InventoryItemDetailEntity item, InventoryItemDetailUpdateForm form) {
        if (ShipmentStatus.valueOf(this.statusId).ordinal() > ShipmentStatus.CREATED.ordinal()) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }

        item.update(form);
    }

    public void importShipment(Long userId) {
        if (!this.statusId.equals(ShipmentStatus.CREATED.name())) {
            throw new AppException(AppErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.statusId = ShipmentStatus.IMPORTED.name();
        this.handledByUserId = userId;

        this.items.forEach(InventoryItemDetailEntity::importInventoryItem);
    }

}
