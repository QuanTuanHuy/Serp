package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.entity.RoutePlanStudentEntity;

import java.util.List;

public interface IRoutePlanStudentService extends IBaseService<RoutePlanStudentEntity, Long> {

    List<RoutePlanStudentEntity> findByRoute(Long routeId);

    List<RoutePlanStudentEntity> findByRouteStop(Long routeStopId);

    long countByRoute(Long routeId);

    /** Guard: true if student is already assigned to any route in the same session. */
    boolean existsBySessionAndStudent(Long sessionId, Long studentId);

    /** For session summary refresh: total planned students in session. */
    long countBySession(Long sessionId);

    /** For session summary refresh: total stops across all routes in session. */
    long countStopsBySession(Long sessionId);

    /** For session summary refresh: total routes in session. */
    long countRoutesBySession(Long sessionId);

    RoutePlanStudentEntity save(RoutePlanStudentEntity entity);

    void saveAll(List<RoutePlanStudentEntity> entities);
}

