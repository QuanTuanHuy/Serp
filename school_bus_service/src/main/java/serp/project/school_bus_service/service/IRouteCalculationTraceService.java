package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.request.RouteCalculationTraceCreateCommand;
import serp.project.school_bus_service.entity.RouteCalculationTraceEntity;
import serp.project.school_bus_service.shared.base.IBaseService;

import java.util.List;
import java.util.Optional;

public interface IRouteCalculationTraceService extends IBaseService<RouteCalculationTraceEntity, Long> {

    RouteCalculationTraceEntity saveTrace(RouteCalculationTraceCreateCommand command);

    Optional<RouteCalculationTraceEntity> findLatestByRoutePlanId(Long routePlanId);

    List<RouteCalculationTraceEntity> findHistoryByRoutePlanId(Long routePlanId);
}
