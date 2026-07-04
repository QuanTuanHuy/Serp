package serp.project.school_bus_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Snapshot of a student's participation in a planned route at planning time.
 * One row per student per route. pickupStop = where student boards, dropoffStop = where student alights.
 */
@Entity
@Table(name = "school_bus_route_plan_student")
@Getter
@Setter
public class RoutePlanStudentEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscription_id")
    private StudentSubscriptionEntity subscription;

    @ManyToOne
    @JoinColumn(name = "pickup_stop_id")
    private RouteStopEntity pickupStop;

    @ManyToOne
    @JoinColumn(name = "dropoff_stop_id")
    private RouteStopEntity dropoffStop;

    public StudentEntity getStudent() {
        return subscription != null ? subscription.getStudent() : null;
    }

    public void setStudent(StudentEntity student) {
        // Student is derived from subscription after normalization.
    }
}
