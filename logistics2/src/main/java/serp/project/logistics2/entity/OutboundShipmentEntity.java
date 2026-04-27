package serp.project.logistics2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;
import serp.project.logistics2.constant.ShipmentStatus;
import serp.project.logistics2.dto.request.OutboundShipmentCreationForm;
import serp.project.logistics2.dto.request.OutboundShipmentUpdateForm;
import serp.project.logistics2.util.IdUtils;

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

    @Column(name = "customer_id")
    private String customerId;

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
            String customerId,
            String facilityId,
            String name,
            String status,
            Long createdByUserId,
            Long tenantId) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.facilityId = facilityId;
        this.name = name;
        this.status = status;
        this.createdByUserId = createdByUserId;
        this.tenantId = tenantId;
    }

    public static OutboundShipmentEntity create(OrderEntity order, String facilityId, String name, Long createdByUserId,
            Long tenantId) {
        String id = IdUtils.generateOutboundShipmentId();
        if (StringUtils.hasText(name)) {
            name = "Phiếu xuất " + id.substring(0, 6);
        }
        String status = ShipmentStatus.CREATED.name();
        return new OutboundShipmentEntity(id, order.getId(), order.getToCustomerId(), facilityId, name, status, createdByUserId, tenantId);
    }

    public void update(String name) {
        if (StringUtils.hasText(name)) {
            this.name = name;
        }
    }

    public void update(OutboundShipmentUpdateForm form) {
        this.update(form.getName());
    }

    public void addItem(OutboundShipmentCreationForm.ItemForm form) {
        OutboundShipmentItemEntity item = OutboundShipmentItemEntity.create(form, this.id, this.tenantId);
        this.items.add(item);
    }

}
