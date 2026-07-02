package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;
import serp.project.school_bus_service.entity.RouteStopEntity;

import java.util.List;
import java.util.Set;

@Data
public class PersistenceResult {

    private int addedStudents;
    private int addedStops;
    private int totalAssignedStudents;
    private List<RouteStopEntity> orderedStops;
    private Set<Long> addedStudentIds;
}
