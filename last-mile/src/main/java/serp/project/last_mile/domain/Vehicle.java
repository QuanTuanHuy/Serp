/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.last_mile.enums.VehicleStatus;
import serp.project.last_mile.enums.VehicleType;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vehicles")
@EntityListeners(AuditingEntityListener.class)
public class Vehicle extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "license_plate", nullable = false, length = 50)
    private String licensePlate;

    @Column(name = "max_weight")
    private Double maxWeight;

    @Column(name = "max_volume")
    private Double maxVolume;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_office_id")
    private PostOffice postOffice;

    @Column(name = "post_office_staff_id")
    private Long postOfficeStaffId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    @Column(name = "vehicle_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
}
