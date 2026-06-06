package serp.project.school_bus_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingMatrixCell {
    private String fromKey;
    private String toKey;
    private Double distanceKm;
    private Integer durationSeconds;
    private String source; // OSRM or STRAIGHT_LINE_FALLBACK
}
