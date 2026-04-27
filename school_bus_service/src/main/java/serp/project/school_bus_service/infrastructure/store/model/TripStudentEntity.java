package serp.project.school_bus_service.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import serp.project.school_bus_service.enums.TripStudentStatus;

@Entity
@Table(name = "school_bus_trip_student")
@Getter
@Setter
public class TripStudentEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id")
    private TripExecutionEntity trip;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(name = "pickup_stop_id")
    private RouteStopEntity pickupStop;

    @ManyToOne
    @JoinColumn(name = "dropoff_stop_id")
    private RouteStopEntity dropoffStop;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private StudentSubscriptionEntity subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStudentStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;
}

