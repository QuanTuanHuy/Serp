package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "school_bus_driver_profile")
@Getter
@Setter
public class DriverProfileEntity extends BaseModel {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_class")
    private String licenseClass;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(nullable = false)
    private String status;
}
