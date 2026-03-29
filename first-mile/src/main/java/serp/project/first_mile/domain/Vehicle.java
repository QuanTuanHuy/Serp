package serp.project.first_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.first_mile.enums.VehicleStatus;

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
    @Column(name = "license_plate")
    private String licensePlate;

    @Column(name = "max_weight")
    private Double maxWeight;

    @Column(name = "max_volume")
    private Double maxVolume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_office_id")
    private PostOffice postOffice;

    @Column(name = "post_office_staff_id")
    private Long postOfficeStaffId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private VehicleStatus status;
}
