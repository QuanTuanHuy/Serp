package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.repository.RoutePlanStudentRepository;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

@Service
public class RoutePlanStudentServiceImpl extends AbstractBaseService<RoutePlanStudentEntity, Long>
        implements IRoutePlanStudentService {

    private final RoutePlanStudentRepository routePlanStudentRepository;

    public RoutePlanStudentServiceImpl(RoutePlanStudentRepository routePlanStudentRepository) {
        this.routePlanStudentRepository = routePlanStudentRepository;
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
        return routePlanStudentRepository.findByRouteStopIdAndIsDeletedFalse(routeStopId);
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
    public void deletePhysical(Long id) {
        routePlanStudentRepository.deleteById(id);
    }
}

