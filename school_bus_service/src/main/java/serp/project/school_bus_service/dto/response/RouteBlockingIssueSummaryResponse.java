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
public class RouteBlockingIssueSummaryResponse {
    private Long routeId;
    private String routeCode;
    private String routeName;
    private Integer blockingIssueCount;
    private List<RouteIssueDetailResponse> issues;
}
