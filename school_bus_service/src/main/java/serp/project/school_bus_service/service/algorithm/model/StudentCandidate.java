package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;

@Data
public class StudentCandidate {

    private StudentSubscriptionEntity subscription;
    private PickupPointEntity point;
}
