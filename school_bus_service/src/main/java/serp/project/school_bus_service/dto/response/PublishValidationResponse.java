package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublishValidationResponse {
    private Long sessionId;
    private Integer blockingRouteCount;
    private Integer blockingIssueCount;
    private List<RouteBlockingIssueSummaryResponse> routes;
}
