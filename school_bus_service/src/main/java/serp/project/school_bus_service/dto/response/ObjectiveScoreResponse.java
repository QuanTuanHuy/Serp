package serp.project.school_bus_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveScoreResponse {
    private BigDecimal objectiveValue;
    private BigDecimal displayScore;
    private Boolean feasible;

    private BigDecimal distanceCost;
    private BigDecimal durationCost;
    private BigDecimal routeCountCost;
    private BigDecimal unassignedCost;
    private BigDecimal waitTimeCost;
    private BigDecimal blockingIssueCost;
    private BigDecimal warningIssueCost;
    private BigDecimal capacityExcessCost;
    private BigDecimal balanceCost;

    private Map<String, BigDecimal> weights;
}
