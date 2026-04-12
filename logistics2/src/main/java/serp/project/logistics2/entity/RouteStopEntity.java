package serp.project.logistics2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.logistics2.constant.RouteStopStatus;
import serp.project.logistics2.util.IdUtils;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "wms2_route_stop")
public class RouteStopEntity {

    @Id
    private String id;

    @Column(name = "route_id")
    private String routeId;

    @Column(name = "delivery_slip_id")
    private String deliverySlipId;

    @Column(name = "sequence")
    private Integer sequence;

    private String status;

    @Column(name = "encoded_polyline")
    private String encodedPolyline;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Transient
    private DeliverySlipEntity deliverySlip;

    public RouteStopEntity(String id, String routeId, String deliverySlipId, Integer sequence, String status,
            String encodedPolyline, Long tenantId) {
        this.id = id;
        this.routeId = routeId;
        this.deliverySlipId = deliverySlipId;
        this.sequence = sequence;
        this.status = status;
        this.encodedPolyline = encodedPolyline;
        this.tenantId = tenantId;
    }

    public static RouteStopEntity create(
            String routeId,
            String deliverySlipId,
            Integer stopSequence,
            String encodedPolyline,
            Long tenantId) {
        String id = IdUtils.generateRouteStopId();
        String status = RouteStopStatus.WAITING.name();
        return new RouteStopEntity(id, routeId, deliverySlipId, stopSequence, status, encodedPolyline, tenantId);
    }
}
