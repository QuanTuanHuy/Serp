package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "school_bus_bus")
@Getter
@Setter
public class BusEntity extends BaseModel {

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    @Column(name = "bus_type")
    private String busType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "home_depot_id")
    private DepotEntity homeDepot;
}
