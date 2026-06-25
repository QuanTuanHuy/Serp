/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.CheckinType;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "checkin")
@EntityListeners(AuditingEntityListener.class)
public class Checkin extends AbstractAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkin_type", nullable = false, length = 30)
    private CheckinType checkinType;

    @Column(name = "trip_order_id")
    private Long tripOrderId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_code", length = 100)
    private String orderCode;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "delivery_manifest_id")
    private Long deliveryManifestId;

    @Column(name = "delivery_manifest_order_id")
    private Long deliveryManifestOrderId;

    @Column(name = "courier_staff_id", nullable = false)
    private Long courierStaffId;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkinTime;

    @Column(name = "checkin_location", columnDefinition = "geography(Point, 4326)", nullable = false)
    private Point checkinLocation;

    @Column(name = "distance_m", nullable = false)
    private Double distanceM;

    @Column(name = "allowed_radius_m")
    private Double allowedRadiusM;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;
}
