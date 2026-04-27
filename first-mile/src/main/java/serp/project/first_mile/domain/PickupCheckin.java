/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pickup_checkin")
@EntityListeners(AuditingEntityListener.class)
public class PickupCheckin extends AbstractAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_order_id", nullable = false)
    private Long tripOrderId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "courier_staff_id", nullable = false)
    private Long courierStaffId;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkinTime;

    @Column(name = "checkin_location", columnDefinition = "geography(Point, 4326)", nullable = false)
    private Point checkinLocation;

    @Column(name = "distance_m", nullable = false)
    private Double distanceM;

    @Column(name = "allowed_radius_m", nullable = false)
    private Double allowedRadiusM;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;
}
