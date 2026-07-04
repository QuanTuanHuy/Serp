package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.repository.projection.TripAttendanceStudentProjection;
import serp.project.school_bus_service.repository.projection.TripStudentStatusCountProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;


import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface TripStudentRepository extends BaseRepository<TripStudentEntity, Long> {

    @Query("""
        SELECT ts FROM TripStudentEntity ts
        WHERE ts.trip.id = :tripId
          AND ts.tenantId = :tenantId
          AND ts.isDeleted = false
        ORDER BY ts.subscription.student.fullName ASC
    """)
    List<TripStudentEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT ts FROM TripStudentEntity ts
        WHERE ts.trip.id IN :tripIds
          AND ts.tenantId = :tenantId
          AND ts.isDeleted = false
        ORDER BY ts.subscription.student.fullName ASC
    """)
    List<TripStudentEntity> findByTripIdInAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(
            @Param("tripIds") Collection<Long> tripIds,
            @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT ts.status AS status,
                   CAST(COUNT(*) AS INTEGER) AS total
              FROM public.school_bus_trip_student ts
              JOIN public.school_bus_student_subscription sub
                ON sub.id = ts.subscription_id
               AND sub.is_deleted = false
              JOIN public.school_bus_student student
                ON student.id = sub.student_id
               AND student.is_deleted = false
             WHERE ts.trip_id = :tripId
               AND ts.tenant_id = :tenantId
               AND ts.is_deleted = false
               AND (:parentProfileId IS NULL OR student.parent_profile_id = :parentProfileId)
             GROUP BY ts.status
            """, nativeQuery = true)
    List<TripStudentStatusCountProjection> countStatusByTripIdForOperationOverview(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId);

    @Query(value = """
            SELECT ts.id AS tripStudentId,
                   student.id AS studentId,
                   student.full_name AS studentName,
                   student.student_code AS studentCode,
                   ts.status AS status,
                   ts.pickup_stop_id AS pickupStopId,
                   ts.dropoff_stop_id AS dropoffStopId,
                   ts.subscription_id AS subscriptionId,
                   ts.note AS note
              FROM public.school_bus_trip_student ts
              JOIN public.school_bus_trip_execution trip
                ON trip.id = ts.trip_id
               AND trip.tenant_id = :tenantId
               AND trip.is_deleted = false
              JOIN public.school_bus_route_plan route
                ON route.id = trip.route_id
               AND route.is_deleted = false
              JOIN public.school_bus_route_planning_session session
                ON session.id = route.planning_session_id
               AND session.is_deleted = false
              JOIN public.school_bus_student_subscription subscription
                ON subscription.id = ts.subscription_id
               AND subscription.is_deleted = false
              JOIN public.school_bus_student student
                ON student.id = subscription.student_id
               AND student.is_deleted = false
              LEFT JOIN public.school_bus_route_stop selected_stop
                ON selected_stop.id = :routeStopId
               AND selected_stop.route_id = route.id
               AND selected_stop.tenant_id = :tenantId
               AND selected_stop.is_deleted = false
             WHERE ts.trip_id = :tripId
               AND ts.tenant_id = :tenantId
               AND ts.is_deleted = false
               AND (:parentProfileId IS NULL OR student.parent_profile_id = :parentProfileId)
               AND (
                   :routeStopId IS NULL
                   OR (
                       selected_stop.id IS NOT NULL
                       AND selected_stop.location_type <> 'DEPOT'
                       AND (
                           (selected_stop.stop_purpose = 'PICKUP'
                               AND ts.pickup_stop_id = :routeStopId)
                           OR (selected_stop.stop_purpose = 'DROPOFF'
                               AND ts.dropoff_stop_id = :routeStopId)
                           OR (selected_stop.stop_purpose = 'END_TERMINAL'
                               AND selected_stop.location_type = 'SCHOOL'
                               AND session.route_direction = 'OUTBOUND'
                               AND ts.status IN ('BOARDED', 'DROPPED_OFF'))
                           OR (selected_stop.stop_purpose = 'START_TERMINAL'
                               AND selected_stop.location_type = 'SCHOOL'
                               AND session.route_direction = 'RETURN'
                               AND ts.status = 'PLANNED')
                       )
                   )
               )
             ORDER BY student.full_name ASC, student.id ASC
            """, nativeQuery = true)
    List<TripAttendanceStudentProjection> findAttendanceStudentsForTrip(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId,
            @Param("routeStopId") Long routeStopId,
            @Param("parentProfileId") Long parentProfileId);

    @Query(value = """
        SELECT COUNT(*)
          FROM public.school_bus_trip_student ts
         WHERE ts.trip_id = :tripId
           AND ts.pickup_stop_id = :routeStopId
           AND ts.tenant_id = :tenantId
           AND ts.is_deleted = false
    """, nativeQuery = true)
    long countByTripAndPickupStop(
            @Param("tripId") Long tripId,
            @Param("routeStopId") Long routeStopId,
            @Param("tenantId") Long tenantId);

    @Query(value = """
        SELECT COUNT(*)
          FROM public.school_bus_trip_student ts
         WHERE ts.trip_id = :tripId
           AND ts.dropoff_stop_id = :routeStopId
           AND ts.tenant_id = :tenantId
           AND ts.is_deleted = false
    """, nativeQuery = true)
    long countByTripAndDropoffStop(
            @Param("tripId") Long tripId,
            @Param("routeStopId") Long routeStopId,
            @Param("tenantId") Long tenantId);

    @Query(value = """
        SELECT COUNT(*)
          FROM public.school_bus_trip_student ts
         WHERE ts.trip_id = :tripId
           AND ts.pickup_stop_id = :routeStopId
           AND ts.status = 'PLANNED'
           AND ts.tenant_id = :tenantId
           AND ts.is_deleted = false
    """, nativeQuery = true)
    long countPendingPickupStopStudents(
            @Param("tripId") Long tripId,
            @Param("routeStopId") Long routeStopId,
            @Param("tenantId") Long tenantId);

    @Query(value = """
        SELECT COUNT(*)
          FROM public.school_bus_trip_student ts
         WHERE ts.trip_id = :tripId
           AND ts.dropoff_stop_id = :routeStopId
           AND ts.status = 'BOARDED'
           AND ts.tenant_id = :tenantId
           AND ts.is_deleted = false
    """, nativeQuery = true)
    long countPendingDropoffStopStudents(
            @Param("tripId") Long tripId,
            @Param("routeStopId") Long routeStopId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT ts FROM TripStudentEntity ts
        WHERE ts.trip.id = :tripId
          AND ts.subscription.student.id = :studentId
          AND ts.tenantId = :tenantId
          AND ts.isDeleted = false
    """)
    Optional<TripStudentEntity> findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalse(
            @Param("tripId") Long tripId,
            @Param("studentId") Long studentId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT ts FROM TripStudentEntity ts
        JOIN FETCH ts.subscription sub
        JOIN FETCH sub.student student
        WHERE ts.trip.id = :tripId
          AND student.id IN :studentIds
          AND ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND sub.isDeleted = false
          AND student.isDeleted = false
    """)
    List<TripStudentEntity> findByTripIdAndStudentIds(
            @Param("tripId") Long tripId,
            @Param("studentIds") Collection<Long> studentIds,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT COUNT(ts) > 0 FROM TripStudentEntity ts
        WHERE ts.trip.id = :tripId
          AND ts.subscription.student.parentProfile.id = :parentProfileId
          AND ts.isDeleted = false
    """)
    boolean existsByTripIdAndStudentParentProfileIdAndIsDeletedFalse(
            @Param("tripId") Long tripId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
        GROUP BY ts.status
    """)
    List<Object[]> countAttendanceByStatusFiltered(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") RouteDirection direction
    );

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND ts.subscription.student.parentProfile.id = :parentProfileId
        GROUP BY ts.status
    """)
    List<Object[]> countAttendanceByStatusForParent(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("parentProfileId") Long parentProfileId
    );

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = t.route.id
                AND a.driver.id = :driverId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
        GROUP BY ts.status
    """)
    List<Object[]> countAttendanceByStatusForDriver(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("driverId") Long driverId
    );

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = t.route.id
                AND a.attendant.id = :attendantId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
        GROUP BY ts.status
    """)
    List<Object[]> countAttendanceByStatusForAttendant(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("attendantId") Long attendantId
    );

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND ts.subscription.student.isDeleted = false
          AND ts.subscription.student.isActive = true
          AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:parentProfileId IS NOT NULL AND ts.subscription.student.parentProfile.id = :parentProfileId)
          )
        GROUP BY ts.status
    """)
    List<Object[]> countDashboardAttendanceByStatus(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT COUNT(DISTINCT ts.subscription.student.id) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND ts.subscription.student.isDeleted = false
          AND ts.subscription.student.isActive = true
          AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:parentProfileId IS NOT NULL AND ts.subscription.student.parentProfile.id = :parentProfileId)
          )
    """)
    long countDashboardStudents(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT COUNT(DISTINCT ts.subscription.student.parentProfile.id) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND ts.subscription.student.isDeleted = false
          AND ts.subscription.student.isActive = true
          AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:parentProfileId IS NOT NULL AND ts.subscription.student.parentProfile.id = :parentProfileId)
          )
    """)
    long countDashboardParents(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);
}

