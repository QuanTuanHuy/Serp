package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.RouteDirection;

import java.time.LocalDate;
import java.util.List;

public interface IRoutePlanStudentService extends IBaseService<RoutePlanStudentEntity, Long> {

    List<RoutePlanStudentEntity> findByRoute(Long routeId);

    List<RoutePlanStudentEntity> findByRouteStop(Long routeStopId);

    long countByRoute(Long routeId);

    /** Count distinct students on a route (not double-counted from BOARD+DROPOFF dual entries). */
    long countDistinctStudentsByRoute(Long routeId);

    /** Count distinct students across all routes in a session. */
    long countDistinctStudentsBySession(Long sessionId);

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

    List<RoutePlanStudentEntity> findStudentsInOtherRoutesOfSession(Long sessionId, Long routeId);

    boolean existsInOtherRoutesOfSessionAndDirection(Long sessionId, Long routeId, Long studentId, RouteDirection direction);

    boolean existsByRouteAndStudent(Long routeId, Long studentId);

    List<RoutePlanStudentEntity> findStudentsInOtherRoutesOfSessionAndDirection(Long sessionId, Long routeId, RouteDirection direction);

    List<StudentSubscriptionEntity> findEligibleSubscriptions(Long schoolId, Long scheduleId, String direction, LocalDate serviceDate, Long tenantId);

    /** Internal: physically deletes a route plan student entry (used by greedy simulation). */
    void deletePhysical(Long id);
}

