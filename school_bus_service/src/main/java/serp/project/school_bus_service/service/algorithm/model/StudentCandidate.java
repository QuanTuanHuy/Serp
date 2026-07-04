package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;
import serp.project.school_bus_service.entity.PickupPointEntity;

@Data
public class StudentCandidate {

    private Long subscriptionId;
    private Long studentId;
    private PickupPointEntity point;
}
