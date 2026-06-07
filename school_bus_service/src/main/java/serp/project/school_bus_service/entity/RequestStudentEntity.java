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
import serp.project.school_bus_service.enums.TripOption;

@Entity
@Table(name = "school_bus_request_student")
@Getter
@Setter
public class RequestStudentEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id")
    private TransportRequestEntity request;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(name = "pickup_point_id")
    private PickupPointEntity pickupPoint;

    @ManyToOne
    @JoinColumn(name = "dropoff_point_id")
    private PickupPointEntity dropoffPoint;

    @ManyToOne
    @JoinColumn(name = "school_schedule_id")
    private SchoolScheduleEntity schoolSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_option")
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

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private StudentSubscriptionEntity subscription;

    @ManyToOne
    @JoinColumn(name = "target_subscription_id")
    private StudentSubscriptionEntity targetSubscription;

    @Column(name = "student_note", columnDefinition = "TEXT")
    private String studentNote;
}
