package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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

    /** Legacy column: acts as the student's default pickup point */
    @ManyToOne
    @JoinColumn(name = "pickup_point_id")
    private PickupPointEntity pickupPoint;

    @ManyToOne
    @JoinColumn(name = "default_dropoff_point_id")
    private PickupPointEntity defaultDropoffPoint;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "student_code")
    private String studentCode;

    private String grade;

    @Column(name = "class_name")
    private String className;

    @Column(name = "home_address", columnDefinition = "TEXT")
    private String homeAddress;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "special_note", columnDefinition = "TEXT")
    private String specialNote;

}
