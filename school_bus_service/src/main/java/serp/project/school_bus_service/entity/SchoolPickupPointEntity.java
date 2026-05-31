package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "school_bus_school_pickup_point")
@Getter
@Setter
public class SchoolPickupPointEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pickup_point_id")
    private PickupPointEntity pickupPoint;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefaultPoint = Boolean.FALSE;
}
