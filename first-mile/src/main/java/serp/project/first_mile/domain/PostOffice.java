/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.PostOfficeStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
// import org.locationtech.jts.geom.Polygon; 

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post_offices")
@EntityListeners(AuditingEntityListener.class)
public class PostOffice extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "province_code", nullable = false)
    private String provinceCode;

    @Column(name = "ward_code", nullable = false)
    private String wardCode;

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "operational_start_date")
    private LocalDate operationalStartDate;

    @Column(name = "operational_end_date")
    private LocalDate operationalEndDate;

    @Column(name = "working_start_time")
    private LocalTime workingStartTime;

    @Column(name = "working_end_time")
    private LocalTime workingEndTime;

    @Column(name = "location", columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Column(name = "service_radius_m", nullable = false)
    @Builder.Default
    private Integer serviceRadiusM = 5000;

    // @Column(name = "coverage_polygon", columnDefinition = "geometry(Polygon,4326)")
    // private Polygon coveragePolygon;

    // Total pickup-order load this post office can hold before outbound handover.
    @Column(name = "daily_capacity", nullable = false)
    @Builder.Default
    private Integer dailyCapacity = 0;

    // Current pickup-order load reserved or waiting at this post office.
    @Column(name = "current_load", nullable = false)
    @Builder.Default
    private Integer currentLoad = 0;

    @Column(name = "delivery_capacity", nullable = false)
    @Builder.Default
    private Integer deliveryCapacity = 0;

    @Column(name = "current_delivery_load", nullable = false)
    @Builder.Default
    private Integer currentDeliveryLoad = 0;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PostOfficeStatus status = PostOfficeStatus.INACTIVE;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Hub id in second-mile (same tenant); optional — post office managed under a hub.
     */
    @Column(name = "hub_id")
    private Long hubId;

    public boolean isActive() {
        return PostOfficeStatus.ACTIVE.equals(status);
    }

    public boolean canAccept(int incomingOrders) {
        if (incomingOrders <= 0) {
            return false;
        }
        return isActive() && (safeInt(this.currentLoad) + incomingOrders <= safeInt(this.dailyCapacity));
    }

    public void addLoad(int incomingOrders) {
        if (!canAccept(incomingOrders)) {
            throw new AppException(ErrorCode.POST_OFFICE_OVERLOADED);
        }
        this.currentLoad = safeInt(this.currentLoad) + incomingOrders;
    }

    public void releaseLoad(int outgoingOrders) {
        if (outgoingOrders <= 0) {
            return;
        }
        this.currentLoad = Math.max(safeInt(this.currentLoad) - outgoingOrders, 0);
    }

    public boolean canAcceptDelivery(int incomingOrders) {
        if (incomingOrders <= 0) {
            return false;
        }
        return isActive()
                && (safeInt(this.currentDeliveryLoad) + incomingOrders <= safeInt(this.deliveryCapacity));
    }

    public void addDeliveryLoad(int incomingOrders) {
        if (!canAcceptDelivery(incomingOrders)) {
            throw new AppException(ErrorCode.POST_OFFICE_OVERLOADED);
        }
        this.currentDeliveryLoad = safeInt(this.currentDeliveryLoad) + incomingOrders;
    }

    public void releaseDeliveryLoad(int outgoingOrders) {
        if (outgoingOrders <= 0) {
            return;
        }
        this.currentDeliveryLoad = Math.max(safeInt(this.currentDeliveryLoad) - outgoingOrders, 0);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
