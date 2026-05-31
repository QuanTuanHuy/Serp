package serp.project.school_bus_service.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.TripHistoryParamsRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripHistoryResponse;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.TripHistoryEntity;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.repository.TripHistoryRepository;
import serp.project.school_bus_service.service.ITripHistoryService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.util.Set;

@Service
public class TripHistoryServiceImpl extends AbstractBaseService<TripHistoryEntity, Long>
        implements ITripHistoryService {

    private final TripHistoryRepository tripHistoryRepository;
    private final SchoolBusMapper mapper;

    public TripHistoryServiceImpl(TripHistoryRepository tripHistoryRepository,
                                   SchoolBusMapper mapper) {
        this.tripHistoryRepository = tripHistoryRepository;
        this.mapper = mapper;
    }

    @Override
    protected BaseRepository<TripHistoryEntity, Long> getRepository() {
        return tripHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TripHistoryResponse> getTripHistory(TripHistoryParamsRequest params, Long tenantId) {
        return PageResponse.from(tripHistoryRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "routeCode", "status",
                        "bus.plateNumber", "driver.fullName", "attendant.fullName"),
                pageable(params, Set.of("id", "routeCode", "serviceDate", "status", "createdAt", "updatedAt"),
                        "serviceDate")),
                mapper::toTripHistoryResponse);
    }

    @Override
    @Transactional
    public void recordCompletedRoute(RoutePlanEntity route, RouteAssignmentEntity assignment,
                                      Long tenantId, Long actorId) {
        TripHistoryEntity tripHistory = tripHistoryRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalse(route.getId(), tenantId)
                .orElseGet(TripHistoryEntity::new);
        if (tripHistory.getId() == null) {
            tripHistory.markCreated(tenantId, actor(actorId));
        } else {
            tripHistory.markUpdated(actor(actorId));
        }
        tripHistory.setRoute(route);
        tripHistory.setRouteCode(route.getRouteCode());
        tripHistory.setServiceDate(route.getServiceDate());
        tripHistory.setStatus(route.getStatus().name());
        tripHistory.setStartedAt(route.getStartedAt());
        tripHistory.setCompletedAt(route.getCompletedAt());
        if (assignment != null) {
            tripHistory.setBus(assignment.getBus());
            tripHistory.setDriver(assignment.getDriver());
            tripHistory.setAttendant(assignment.getAttendant());
        }
        tripHistoryRepository.save(tripHistory);
    }

    private Specification<TripHistoryEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(BaseParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }

    @Override
    public long countByTenant(Long tenantId) {
        return tripHistoryRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }
}
