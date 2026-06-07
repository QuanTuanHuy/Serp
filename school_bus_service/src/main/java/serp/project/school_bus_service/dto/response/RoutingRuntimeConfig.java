package serp.project.school_bus_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRuntimeConfig {
    private double averageSpeedKmph;
    private int dwellTimeMinutes;
    private double roadFactor;
    private boolean osrmEnabled;
}
