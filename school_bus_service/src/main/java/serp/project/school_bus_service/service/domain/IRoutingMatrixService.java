package serp.project.school_bus_service.service.domain;

import serp.project.school_bus_service.dto.request.RoutingPointRequest;
import serp.project.school_bus_service.dto.response.RoutingMatrixResponse;

import java.util.List;

public interface IRoutingMatrixService {
    RoutingMatrixResponse buildMatrix(Long tenantId, List<RoutingPointRequest> points);
}
