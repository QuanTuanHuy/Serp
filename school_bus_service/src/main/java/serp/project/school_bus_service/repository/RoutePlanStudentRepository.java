package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.enums.RoutePlanStudentAction;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

public interface RoutePlanStudentRepository extends BaseRepository<RoutePlanStudentEntity, Long> {

    List<RoutePlanStudentEntity> findByRouteIdAndIsDeletedFalse(Long routeId);

    List<RoutePlanStudentEntity> findByRouteStopIdAndIsDeletedFalse(Long routeStopId);

    boolean existsByRouteIdAndStudentIdAndSubscriptionIdAndServiceActionAndIsDeletedFalse(
            Long routeId, Long studentId, Long subscriptionId, RoutePlanStudentAction serviceAction);

    long countByRouteIdAndIsDeletedFalse(Long routeId);

    /** Count DISTINCT students assigned to a route (excludes double-counting from dual BOARD+DROPOFF entries). */
    @Query("""
            SELECT COUNT(DISTINCT ps.student.id) FROM RoutePlanStudentEntity ps
            WHERE ps.route.id = :routeId
              AND ps.isDeleted = false
            """)
    long countDistinctStudentsByRoute(@Param("routeId") Long routeId);

    /** Count DISTINCT students across all routes in a session (excludes double-counting). */
    @Query("""
            SELECT COUNT(DISTINCT ps.student.id) FROM RoutePlanStudentEntity ps
            WHERE ps.route.planningSession.id = :sessionId
              AND ps.isDeleted = false
            """)
    long countDistinctStudentsBySession(@Param("sessionId") Long sessionId);

    /** Check if a student is already assigned anywhere in a given planning session (duplicate guard). */
    @Query("""
            SELECT COUNT(ps) > 0 FROM RoutePlanStudentEntity ps
            WHERE ps.route.planningSession.id = :sessionId
              AND ps.student.id = :studentId
              AND ps.isDeleted = false
            """)
    boolean existsBySessionAndStudent(@Param("sessionId") Long sessionId,
                                      @Param("studentId") Long studentId);

    /** Count all planned students across all routes in a session. */
    @Query("""
            SELECT COUNT(ps) FROM RoutePlanStudentEntity ps
            WHERE ps.route.planningSession.id = :sessionId
              AND ps.isDeleted = false
            """)
    long countBySession(@Param("sessionId") Long sessionId);

    /** Count distinct stops across all routes in a session. */
    @Query("""
            SELECT COUNT(s) FROM RouteStopEntity s
            WHERE s.route.planningSession.id = :sessionId
              AND s.isDeleted = false
            """)
    long countStopsBySession(@Param("sessionId") Long sessionId);

    /** Count routes in a session. */
    @Query("""
            SELECT COUNT(r) FROM RoutePlanEntity r
            WHERE r.planningSession.id = :sessionId
              AND r.isDeleted = false
            """)
    long countRoutesBySession(@Param("sessionId") Long sessionId);
}
