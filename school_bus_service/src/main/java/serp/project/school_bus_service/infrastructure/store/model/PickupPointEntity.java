package serp.project.school_bus_service.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "school_bus_pickup_point")
@Getter
@Setter
public class PickupPointEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    private Double latitude;
    private Double longitude;

    @Column(name = "pickup_window_start")
    private LocalTime pickupWindowStart;

    @Column(name = "pickup_window_end")
    private LocalTime pickupWindowEnd;

}
