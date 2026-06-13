package serp.project.school_bus_service.service.algorithm.model;

import lombok.Data;

import java.util.List;

@Data
public class SelectionResult {

    private List<SelectedDemand> selectedDemands;
    private int remainingCapacity;
}
