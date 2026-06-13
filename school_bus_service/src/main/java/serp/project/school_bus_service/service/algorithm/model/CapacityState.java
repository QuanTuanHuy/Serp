package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;

@Data
public class CapacityState {

    private int capacity;
    private int assignedStudents;
    private int remainingCapacity;
}
