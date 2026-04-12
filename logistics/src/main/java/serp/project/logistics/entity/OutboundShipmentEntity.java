package serp.project.logistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;
import serp.project.logistics.constant.ShipmentStatus;
import serp.project.logistics.dto.request.OutboundShipmentCreationForm;
import serp.project.logistics.dto.request.OutboundShipmentUpdateForm;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.util.IdUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_outbound_shipment")
@Slf4j
public class OutboundShipmentEntity {

    @Id
    private String id;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "facility_id")
    private String facilityId;

    private String name;

    private String status;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private List<OutboundShipmentItemEntity> items = new ArrayList<>();

    @Transient
    private FacilityEntity facility;

    public OutboundShipmentEntity(
            String id,
            String orderId,
            String facilityId,
            String name,
            String status,
            Long createdByUserId,
            Long tenantId) {
        this.id = id;
        this.orderId = orderId;
        this.facilityId = facilityId;
        this.name = name;
        this.status = status;
        this.createdByUserId = createdByUserId;
        this.tenantId = tenantId;
    }

    public static OutboundShipmentEntity create(String orderId, String facilityId, String name, Long createdByUserId, Long tenantId) {
        String id = IdUtils.generateOutboundShipmentId();
        if (!StringUtils.hasText(name)) {
            name = "Phiếu xuất " + id.substring(0, 6);
        }
        String status = ShipmentStatus.CREATED.name();
        return new OutboundShipmentEntity(id, orderId, facilityId, name, status, createdByUserId, tenantId);
    }

    public static OutboundShipmentEntity create(OutboundShipmentCreationForm form, Long userId, Long tenantId) {
        return OutboundShipmentEntity.create(form.getOrderId(), form.getFacilityId(), form.getName(), userId, tenantId);
    }

    public void update(String name) {
        if (!this.status.equals(ShipmentStatus.CREATED.name())) {
            throw new AppException(AppErrorCode.INVALID_SHIPMENT_STATUS);
        }
        if(StringUtils.hasText(name)) {
            this.name = name;
        }
    }

    public void update(OutboundShipmentUpdateForm form) {
        this.update(form.getName());
    }

    public void addItem(OutboundShipmentCreationForm.ItemForm form) {
        if (!this.status.equals(ShipmentStatus.CREATED.name())) {
            throw new AppException(AppErrorCode.INVALID_SHIPMENT_STATUS);
        }
        OutboundShipmentItemEntity item = OutboundShipmentItemEntity.create(form, this.id, this.tenantId);
        this.items.add(item);
    }

    public void deleteItem(OutboundShipmentItemEntity item) {
        if (!this.status.equals(ShipmentStatus.CREATED.name())) {
            throw new AppException(AppErrorCode.INVALID_SHIPMENT_STATUS);
        }
    }

}
