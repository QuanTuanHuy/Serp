/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.enums.VehicleType;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "vehicles")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "vehicle_type")
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Column(name = "max_weight")
    private double maxWeight;

    @Column(name = "max_volume")
    private double maxVolume;

    @Column(name = "max_bags")
    private int maxBags;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "hub_id")
    private Long hubId;

    @Column(name = "assigned_staff_id")
    private Long assignedStaffId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private VehicleStatus status;
}
