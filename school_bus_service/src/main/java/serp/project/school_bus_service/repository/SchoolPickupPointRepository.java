package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.repository.projection.SchoolPickupPointSummaryProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolPickupPointRepository extends BaseRepository<SchoolPickupPointEntity, Long> {

    Page<SchoolPickupPointEntity> findBySchoolIdAndTenantIdAndIsDeletedFalse(
            Long schoolId, Long tenantId, Pageable pageable);

    List<SchoolPickupPointEntity> findBySchoolIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(
            Long schoolId, Long tenantId);

    Optional<SchoolPickupPointEntity> findByIdAndTenantIdAndIsDeletedFalse(Long id, Long tenantId);

    Optional<SchoolPickupPointEntity> findBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalse(
            Long schoolId, Long pickupPointId, Long tenantId);

    boolean existsBySchoolIdAndPickupPointIdAndTenantIdAndIsDeletedFalseAndIsActiveTrue(
            Long schoolId, Long pickupPointId, Long tenantId);

    List<SchoolPickupPointEntity> findByTenantIdAndIsDeletedFalseAndIsActiveTrue(Long tenantId);

    @Query("SELECT s FROM SchoolPickupPointEntity s JOIN FETCH s.pickupPoint WHERE s.school.id IN :schoolIds AND s.tenantId = :tenantId AND s.isDeleted = false")
    List<SchoolPickupPointEntity> findBySchoolIdInAndTenantIdAndIsDeletedFalse(
            @Param("schoolIds") List<Long> schoolIds, @Param("tenantId") Long tenantId);

    @Query("SELECT s FROM SchoolPickupPointEntity s JOIN FETCH s.pickupPoint WHERE s.school.id = :schoolId AND s.tenantId = :tenantId AND s.isDeleted = false AND s.isActive = true AND s.pickupPoint.isDeleted = false AND s.pickupPoint.isActive = true")
    List<SchoolPickupPointEntity> findActiveLinkedPickupPoints(@Param("schoolId") Long schoolId, @Param("tenantId") Long tenantId);

    @Query("SELECT s FROM SchoolPickupPointEntity s JOIN FETCH s.school WHERE s.pickupPoint.id IN :pickupPointIds AND s.tenantId = :tenantId AND s.isDeleted = false AND s.isActive = true")
    List<SchoolPickupPointEntity> findByPickupPointIdInAndTenantIdAndIsDeletedFalse(
            @Param("pickupPointIds") List<Long> pickupPointIds, @Param("tenantId") Long tenantId);

    @Query(value = """
            SELECT spp.school_id AS schoolId,
                   CAST(COUNT(*) AS INTEGER) AS pickupPointCount,
                   BOOL_OR(pp.latitude IS NULL OR pp.longitude IS NULL) AS anyMissingCoordinates
              FROM public.school_bus_school_pickup_point spp
              JOIN public.school_bus_pickup_point pp ON pp.id = spp.pickup_point_id
             WHERE spp.tenant_id = :tenantId
               AND spp.is_deleted = false
               AND spp.school_id IN (:schoolIds)
             GROUP BY spp.school_id
            """, nativeQuery = true)
    List<SchoolPickupPointSummaryProjection> summarizeBySchoolIds(
            @Param("schoolIds") List<Long> schoolIds,
            @Param("tenantId") Long tenantId);
}
