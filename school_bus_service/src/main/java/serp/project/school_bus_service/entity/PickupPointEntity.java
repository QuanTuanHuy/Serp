package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "school_bus_pickup_point")
@Getter
@Setter
public class PickupPointEntity extends BaseModel {

    @Column(name = "code")
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    private Double latitude;
    private Double longitude;

    @Column(name = "usage_type")
    private String usageType;

    @Column(name = "pickup_instruction", columnDefinition = "TEXT")
    private String pickupInstruction;
}
