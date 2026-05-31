package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.SchoolPickupPointWindowEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolPickupPointWindowRepository extends BaseRepository<SchoolPickupPointWindowEntity, Long> {

    Optional<SchoolPickupPointWindowEntity> findByIdAndTenantIdAndIsDeletedFalse(Long id, Long tenantId);

    List<SchoolPickupPointWindowEntity> findBySchoolPickupPointIdAndTenantIdAndIsDeletedFalse(
            Long schoolPickupPointId, Long tenantId);

    List<SchoolPickupPointWindowEntity> findBySchoolScheduleIdAndTenantIdAndIsDeletedFalse(
            Long schoolScheduleId, Long tenantId);

    List<SchoolPickupPointWindowEntity> findBySchoolPickupPointIdAndSchoolScheduleIdAndTenantIdAndIsDeletedFalse(
            Long schoolPickupPointId, Long schoolScheduleId, Long tenantId);

    /**
     * Batch query: returns the pickup_point IDs (from the given set) that have
     * a valid window for the specified school + schedule + direction.
     * Joins through school_pickup_point to resolve the link.
     */
    @Query("""
            SELECT DISTINCT spp.pickupPoint.id
              FROM SchoolPickupPointWindowEntity w
              JOIN w.schoolPickupPoint spp
             WHERE spp.school.id = :schoolId
               AND spp.pickupPoint.id IN :pointIds
               AND spp.tenantId = :tenantId
               AND spp.isDeleted = false
               AND spp.isActive = true
               AND w.schoolSchedule.id = :scheduleId
               AND w.tenantId = :tenantId
               AND w.isDeleted = false
               AND w.direction = :direction
            """)
    List<Long> findPointIdsWithWindow(
            @Param("schoolId") Long schoolId,
            @Param("pointIds") List<Long> pointIds,
            @Param("scheduleId") Long scheduleId,
            @Param("direction") String direction,
            @Param("tenantId") Long tenantId);
}

