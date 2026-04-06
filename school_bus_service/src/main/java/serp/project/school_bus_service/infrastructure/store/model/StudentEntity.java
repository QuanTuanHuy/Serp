package serp.project.school_bus_service.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "school_bus_student")
@Getter
@Setter
public class StudentEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_profile_id")
    private ParentProfileEntity parentProfile;

    @ManyToOne
    @JoinColumn(name = "pickup_point_id")
    private PickupPointEntity pickupPoint;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "student_code")
    private String studentCode;

    private String grade;

    @Column(name = "home_address", columnDefinition = "TEXT")
    private String homeAddress;

}
