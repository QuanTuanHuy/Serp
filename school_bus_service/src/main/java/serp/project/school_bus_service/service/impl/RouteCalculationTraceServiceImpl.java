package serp.project.school_bus_service.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.RouteCalculationTraceCreateCommand;
import serp.project.school_bus_service.entity.RouteCalculationTraceEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.enums.RouteCalculationType;
import serp.project.school_bus_service.repository.RouteCalculationTraceRepository;
import serp.project.school_bus_service.service.IRouteCalculationTraceService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RouteCalculationTraceServiceImpl extends AbstractBaseService<RouteCalculationTraceEntity, Long>
        implements IRouteCalculationTraceService {

    private static final Logger log = LoggerFactory.getLogger(RouteCalculationTraceServiceImpl.class);

    private final RouteCalculationTraceRepository repository;
    private final IRouteService routeService;
    private final IRoutePlanningSessionService sessionService;

    public RouteCalculationTraceServiceImpl(
            RouteCalculationTraceRepository repository,
            @Lazy IRouteService routeService,
            @Lazy IRoutePlanningSessionService sessionService) {
        this.repository = repository;
        this.routeService = routeService;
        this.sessionService = sessionService;
    }

    @Override
    protected BaseRepository<RouteCalculationTraceEntity, Long> getRepository() {
        return repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RouteCalculationTraceEntity saveTrace(RouteCalculationTraceCreateCommand command) {
        RouteCalculationTraceEntity entity = new RouteCalculationTraceEntity();
        
        if (command.getRoutePlanId() != null) {
            RoutePlanEntity routePlan = routeService.getRouteEntity(command.getRoutePlanId(), command.getTenantId());
            entity.setRoutePlan(routePlan);
        }

        if (command.getPlanningSessionId() != null) {
            RoutePlanningSessionEntity session = sessionService.requireSession(command.getPlanningSessionId(), command.getTenantId());
            entity.setPlanningSession(session);
        }

        entity.setTenantId(command.getTenantId());
        entity.setCalculationType(command.getCalculationType());
        entity.setCalculationStatus(command.getCalculationStatus());
        
        entity.setInputJson(command.getInputJson());
        entity.setMatrixJson(command.getMatrixJson());
        entity.setTimelineJson(command.getTimelineJson());
        entity.setIssuesJson(command.getIssuesJson());
        entity.setConfigSnapshotJson(command.getConfigSnapshotJson());
        entity.setSourceSummary(command.getSourceSummary());

        entity.markCreated(command.getTenantId(), "SYSTEM");
        
        RouteCalculationTraceEntity saved = repository.save(entity);
        log.info("Route calculation trace saved: routeId={}, traceId={}", command.getRoutePlanId(), saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RouteCalculationTraceEntity> findLatestByRoutePlanId(Long routePlanId) {
        // Find latest trace of type MATRIX_AND_TIMELINE, falling back to TIMELINE or any other calculation type
        Optional<RouteCalculationTraceEntity> trace = repository.findFirstByRoutePlanIdAndCalculationTypeAndIsDeletedFalseOrderByCreatedAtDesc(
                routePlanId, RouteCalculationType.MATRIX_AND_TIMELINE);
        if (trace.isPresent()) {
            return trace;
        }
        return repository.findFirstByRoutePlanIdAndCalculationTypeAndIsDeletedFalseOrderByCreatedAtDesc(
                routePlanId, RouteCalculationType.TIMELINE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteCalculationTraceEntity> findHistoryByRoutePlanId(Long routePlanId) {
        return repository.findByRoutePlanIdAndIsDeletedFalseOrderByCreatedAtDesc(routePlanId);
    }
}
