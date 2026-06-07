package serp.project.school_bus_service.shared.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    private String exportCode;
    private Long routePlanId;
    private Long traceId;
    private Map<String, Object> params;
}
