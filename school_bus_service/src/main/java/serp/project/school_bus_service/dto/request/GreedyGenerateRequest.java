package serp.project.school_bus_service.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for greedy route generation on an existing planning session. */
@Getter
@Setter
@NoArgsConstructor
public class GreedyGenerateRequest {

    /**
     * Maximum number of students per bus/route.
     * Defaults to 30 if not provided; a DEFAULT_CAPACITY_USED issue will be emitted.
     */
    private Integer defaultBusCapacity;

    /**
     * Depot used as the bus start/end point for generated routes.
     * Required when the session direction involves a DEPOT endpoint
     * (OUTBOUND: routes start from depot; RETURN: routes end at depot).
     */
    private Long depotId;
}
