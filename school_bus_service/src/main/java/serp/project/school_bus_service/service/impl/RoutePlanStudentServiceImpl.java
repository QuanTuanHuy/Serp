package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.TripOption;
import serp.project.school_bus_service.repository.RoutePlanStudentRepository;
import serp.project.school_bus_service.repository.StudentSubscriptionRepository;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoutePlanStudentServiceImpl extends AbstractBaseService<RoutePlanStudentEntity, Long>
        implements IRoutePlanStudentService {

    private final RoutePlanStudentRepository routePlanStudentRepository;
    private final StudentSubscriptionRepository subscriptionRepository;

    public RoutePlanStudentServiceImpl(RoutePlanStudentRepository routePlanStudentRepository,
                                       StudentSubscriptionRepository subscriptionRepository) {
        this.routePlanStudentRepository = routePlanStudentRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    protected BaseRepository<RoutePlanStudentEntity, Long> getRepository() {
        return routePlanStudentRepository;
    }

    @Override
    public List<RoutePlanStudentEntity> findByRoute(Long routeId) {
        return routePlanStudentRepository.findByRouteIdAndIsDeletedFalse(routeId);
    }

    @Override
    public List<RoutePlanStudentEntity> findByRouteStop(Long routeStopId) {
        return routePlanStudentRepository.findByRouteIdAndIsDeletedFalse(routeStopId);
    }

    @Override
    public List<StudentSubscriptionEntity> findEligibleSubscriptions(Long schoolId, String direction, LocalDate serviceDate, Long tenantId) {
        boolean isOutbound = "OUTBOUND".equalsIgnoreCase(direction);
        int dayIndex = serviceDate.getDayOfWeek().getValue(); // 1=MON..7=SUN
        List<TripOption> allowedTrips = isOutbound
                ? List.of(TripOption.MORNING, TripOption.ROUND_TRIP)
                : List.of(TripOption.AFTERNOON, TripOption.ROUND_TRIP);
        return subscriptionRepository.findEligibleForPlanning(schoolId, tenantId, serviceDate, dayIndex, allowedTrips, isOutbound);
    }

    @Override
    public long countByRoute(Long routeId) {
        return routePlanStudentRepository.countByRouteIdAndIsDeletedFalse(routeId);
    }

    @Override
    public long countDistinctStudentsByRoute(Long routeId) {
        return routePlanStudentRepository.countDistinctStudentsByRoute(routeId);
    }

    @Override
    public long countDistinctStudentsBySession(Long sessionId) {
        return routePlanStudentRepository.countDistinctStudentsBySession(sessionId);
    }

    @Override
    public boolean existsBySessionAndStudent(Long sessionId, Long studentId) {
        return routePlanStudentRepository.existsBySessionAndStudent(sessionId, studentId);
    }

    @Override
    public long countBySession(Long sessionId) {
        return routePlanStudentRepository.countBySession(sessionId);
    }

    @Override
    public long countStopsBySession(Long sessionId) {
        return routePlanStudentRepository.countStopsBySession(sessionId);
    }

    @Override
    public long countRoutesBySession(Long sessionId) {
        return routePlanStudentRepository.countRoutesBySession(sessionId);
    }

    @Override
    public RoutePlanStudentEntity save(RoutePlanStudentEntity entity) {
        return routePlanStudentRepository.save(entity);
    }

    @Override
    public void saveAll(List<RoutePlanStudentEntity> entities) {
        routePlanStudentRepository.saveAll(entities);
    }

    @Override
    public List<RoutePlanStudentEntity> findStudentsInOtherRoutesOfSession(Long sessionId, Long routeId) {
        return routePlanStudentRepository.findStudentsInOtherRoutesOfSession(sessionId, routeId);
    }

    @Override
    public boolean existsInOtherRoutesOfSessionAndDirection(Long sessionId, Long routeId, Long studentId, RouteDirection direction) {
        return routePlanStudentRepository.existsInOtherRoutesOfSessionAndDirection(sessionId, routeId, studentId, direction);
    }

    @Override
    public boolean existsByRouteAndStudent(Long routeId, Long studentId) {
        return routePlanStudentRepository.existsByRouteAndStudent(routeId, studentId);
    }

    @Override
    public List<RoutePlanStudentEntity> findStudentsInOtherRoutesOfSessionAndDirection(Long sessionId, Long routeId, RouteDirection direction) {
        return routePlanStudentRepository.findStudentsInOtherRoutesOfSessionAndDirection(sessionId, routeId, direction);
    }

    @Override
    public void deletePhysical(Long id) {
        routePlanStudentRepository.deleteById(id);
    }
}

