package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;

@Data
public class GreedyChoice {

    private StopDemand demand;
    private int takeCount;
    private double addedDistanceKm;
    private double score;
    private boolean existingStop;
}
