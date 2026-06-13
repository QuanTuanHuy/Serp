package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;
import serp.project.school_bus_service.entity.PickupPointEntity;

import java.util.List;

@Data
public class StopDemand {

    private PickupPointEntity point;
    private List<StudentCandidate> candidates;
}
