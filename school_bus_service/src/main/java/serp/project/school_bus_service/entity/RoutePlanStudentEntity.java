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
import serp.project.school_bus_service.enums.RoutePlanStudentAction;

import java.time.LocalTime;

/**
 * Snapshot of a student's participation in a planned route at planning time.
 * OUTBOUND: student BOARDs at a pickup stop, alights at school.
 * RETURN:   student DROPOFFs at a drop-off stop, departs from school.
 */
@Entity
@Table(name = "school_bus_route_plan_student")
@Getter
@Setter
public class RoutePlanStudentEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id")
    private RoutePlanEntity route;

    @ManyToOne
    @JoinColumn(name = "route_stop_id")
    private RouteStopEntity routeStop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscription_id")
    private StudentSubscriptionEntity subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_action", nullable = false, length = 30)
    private RoutePlanStudentAction serviceAction;

    @Column(name = "planned_time")
    private LocalTime plannedTime;
}
