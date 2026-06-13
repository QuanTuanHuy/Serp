package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CandidateResolution {

    private List<StudentCandidate> assignable;
    private Set<Long> eligibleStudentIds;
    private Set<Long> currentRouteStudents;
    private Set<Long> otherRouteStudents;
    private int assignedElsewhere;
    private int missingCoordinates;
    private int invalidPoint;
}
