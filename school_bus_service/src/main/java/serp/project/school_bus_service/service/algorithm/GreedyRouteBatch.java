package serp.project.school_bus_service.service.algorithm;

import java.util.List;

/** One route-worth of stop assignments produced by the greedy algorithm. */
public final class GreedyRouteBatch {

    private final int batchIndex;
    private final List<GreedyStopAssignment> stopAssignments;

    public GreedyRouteBatch(int batchIndex, List<GreedyStopAssignment> stopAssignments) {
        this.batchIndex = batchIndex;
        this.stopAssignments = stopAssignments;
    }

    public int getBatchIndex() {
        return batchIndex;
    }

    public List<GreedyStopAssignment> getStopAssignments() {
        return stopAssignments;
    }

    public int getTotalStudents() {
        return stopAssignments.stream()
                .mapToInt(s -> s.getStudents().size())
                .sum();
    }
}
