package serp.project.first_mile.domain;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.PostOfficeStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
// import org.locationtech.jts.geom.Polygon; 

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@Entity
@Table(name = "post_offices")
@EntityListeners(AuditingEntityListener.class)
public class PostOffice {
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
    private Integer serviceRadiusM = 5000;

    // @Column(name = "coverage_polygon", columnDefinition = "geometry(Polygon,4326)")
    // private Polygon coveragePolygon;

    @Column(name = "daily_capacity", nullable = false)
    private Integer dailyCapacity = 0;

    @Column(name = "current_load", nullable = false)
    private Integer currentLoad = 0;

    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostOfficeStatus status = PostOfficeStatus.INACTIVE;

    @Version
    @Column(name = "version")
    private Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "tenant_id")
    private Long tenantId;

    public boolean isActive() {
        return PostOfficeStatus.ACTIVE.equals(status);
    }

    public boolean canAccept(int incomingOrders) {
        if (incomingOrders <= 0) {
            return false;
        }
        return isActive() && (this.currentLoad + incomingOrders <= this.dailyCapacity);
    }

    public void addLoad(int incomingOrders) {
        if (!canAccept(incomingOrders)) {
            throw new AppException(ErrorCode.POST_OFFICE_OVERLOADED);
        }
        this.currentLoad += incomingOrders;
    }
}