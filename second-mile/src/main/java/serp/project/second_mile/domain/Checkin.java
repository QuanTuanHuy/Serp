/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.second_mile.enums.CheckinType;

import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "checkin")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Checkin extends AbstractAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkin_type", nullable = false, length = 40)
    private CheckinType checkinType;

    @Column(name = "bag_distribution_manifest_id")
    private Long bagDistributionManifestId;

    @Column(name = "driver_staff_id", nullable = false)
    private Long driverStaffId;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkinTime;

    @Column(name = "checkin_location", columnDefinition = "geography(Point, 4326)", nullable = false)
    private Point checkinLocation;

    @Column(name = "distance_m")
    private Double distanceM;

    @Column(name = "allowed_radius_m")
    private Double allowedRadiusM;

    @Column(name = "location_label")
    private String locationLabel;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;
}
