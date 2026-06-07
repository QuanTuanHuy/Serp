package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripExecutionRepository extends BaseRepository<TripExecutionEntity, Long> {

    Optional<TripExecutionEntity> findByRouteIdAndTenantIdAndIsDeletedFalse(Long routeId, Long tenantId);

    List<TripExecutionEntity> findByTenantIdAndServiceDateAndIsDeletedFalseOrderByIdDesc(Long tenantId,
            LocalDate serviceDate);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, TripStatus status);

    @Query("SELECT MAX(t.serviceDate) FROM TripExecutionEntity t WHERE t.tenantId = :tenantId AND t.isDeleted = false")
    Optional<LocalDate> findLatestServiceDate(@Param("tenantId") Long tenantId);

    @Query("""
        SELECT t.status, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.school.id = :schoolId)
          AND (:direction IS NULL OR t.routeDirection = :direction)
        GROUP BY t.status
    """)
    List<Object[]> countTripsByStatusFiltered(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") serp.project.school_bus_service.enums.RouteDirection direction
    );

    @Query("""
        SELECT t.serviceDate, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.serviceDate >= :fromDate AND t.serviceDate <= :toDate
          AND (:schoolId IS NULL OR t.route.school.id = :schoolId)
          AND (:direction IS NULL OR t.routeDirection = :direction)
        GROUP BY t.serviceDate
        ORDER BY t.serviceDate ASC
    """)
    List<Object[]> countTripsByDateFiltered(
        @Param("tenantId") Long tenantId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") serp.project.school_bus_service.enums.RouteDirection direction
    );

    @Query("""
        SELECT t.routeDirection, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.school.id = :schoolId)
          AND (:direction IS NULL OR t.routeDirection = :direction)
        GROUP BY t.routeDirection
    """)
    List<Object[]> countTripsByDirectionFiltered(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") serp.project.school_bus_service.enums.RouteDirection direction
    );

    @Query("""
        SELECT t FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.school.id = :schoolId)
          AND (:direction IS NULL OR t.routeDirection = :direction)
    """)
    List<TripExecutionEntity> findTripsFiltered(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") serp.project.school_bus_service.enums.RouteDirection direction
    );

}

