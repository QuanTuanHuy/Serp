package serp.project.school_bus_service.service.algorithm;

import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;

import java.util.List;

/** Assignment of students to a single stop for one route batch. */
public final class GreedyStopAssignment {

    private final PickupPointEntity pickupPoint;
    private final List<StudentSubscriptionEntity> students;
    private final int stopOrder;

    public GreedyStopAssignment(PickupPointEntity pickupPoint,
                                List<StudentSubscriptionEntity> students,
                                int stopOrder) {
        this.pickupPoint = pickupPoint;
        this.students = students;
        this.stopOrder = stopOrder;
    }

    public PickupPointEntity getPickupPoint() {
        return pickupPoint;
    }

    public List<StudentSubscriptionEntity> getStudents() {
        return students;
    }

    public int getStopOrder() {
        return stopOrder;
    }
}
