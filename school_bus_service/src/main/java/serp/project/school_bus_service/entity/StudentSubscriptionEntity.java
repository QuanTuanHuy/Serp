package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.SubscriptionStatus;
import serp.project.school_bus_service.enums.TripOption;

import java.time.LocalDate;

@Entity
@Table(name = "school_bus_student_subscription")
@Getter
@Setter
public class StudentSubscriptionEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(name = "pickup_point_id")
    private PickupPointEntity pickupPoint;

    @ManyToOne
    @JoinColumn(name = "dropoff_point_id")
    private PickupPointEntity dropoffPoint;

    @Column(name = "subscription_code", nullable = false)
    private String subscriptionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_option", nullable = false)
    private TripOption tripOption;

    @Column(name = "is_monday", nullable = false)
    private Boolean monday = Boolean.TRUE;

    @Column(name = "is_tuesday", nullable = false)
    private Boolean tuesday = Boolean.TRUE;

    @Column(name = "is_wednesday", nullable = false)
    private Boolean wednesday = Boolean.TRUE;

    @Column(name = "is_thursday", nullable = false)
    private Boolean thursday = Boolean.TRUE;

    @Column(name = "is_friday", nullable = false)
    private Boolean friday = Boolean.TRUE;

    @Column(name = "is_saturday", nullable = false)
    private Boolean saturday = Boolean.FALSE;

    @Column(name = "is_sunday", nullable = false)
    private Boolean sunday = Boolean.FALSE;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;


    @ManyToOne
    @JoinColumn(name = "source_request_id")
    private TransportRequestEntity sourceRequest;

    public SchoolEntity getSchool() {
        return student != null ? student.getSchool() : null;
    }

    public void setSchool(SchoolEntity school) {
        // School is derived from student after normalization.
    }
}
