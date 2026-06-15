package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.enums.RequestStatus;


import java.time.LocalDate;
import java.util.List;

public interface TransportRequestRepository extends BaseRepository<TransportRequestEntity, Long> {
    List<TransportRequestEntity> findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(Long tenantId);

    List<TransportRequestEntity> findBySchool_IdAndTenantIdAndStatusAndIsDeletedFalseOrderByCreatedAtAsc(Long schoolId, Long tenantId,
            RequestStatus status);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RequestStatus status);

    long countBySchoolIdAndTenantIdAndIsDeletedFalse(Long schoolId, Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query("""
        SELECT r.status, COUNT(r) FROM TransportRequestEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND (:schoolId IS NULL OR r.school.id = :schoolId)
        GROUP BY r.status
    """)
    List<Object[]> countRequestsByStatusFiltered(
        @Param("tenantId") Long tenantId,
        @Param("schoolId") Long schoolId
    );

    @Query("""
        SELECT r FROM TransportRequestEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND (:schoolId IS NULL OR r.school.id = :schoolId)
        ORDER BY r.createdAt DESC
    """)
    List<TransportRequestEntity> findRequestsFiltered(
        @Param("tenantId") Long tenantId,
        @Param("schoolId") Long schoolId
    );

    List<TransportRequestEntity> findByTenantIdAndParentProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(Long tenantId, Long parentProfileId);

    long countByTenantIdAndParentProfileIdAndStatusAndIsDeletedFalse(Long tenantId, Long parentProfileId, RequestStatus status);

    long countByTenantIdAndParentProfileIdAndIsDeletedFalse(Long tenantId, Long parentProfileId);

    @Query("""
        SELECT r.status, COUNT(r) FROM TransportRequestEntity r
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND (:schoolId IS NULL OR r.school.id = :schoolId)
          AND (:parentProfileId IS NULL OR r.parentProfile.id = :parentProfileId)
          AND r.effectiveFrom <= :serviceDate
          AND (r.effectiveTo IS NULL OR r.effectiveTo >= :serviceDate)
        GROUP BY r.status
    """)
    List<Object[]> countDashboardRequestsByStatus(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT COUNT(r) FROM TransportRequestEntity r
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND r.status = :status
          AND (:schoolId IS NULL OR r.school.id = :schoolId)
          AND (:parentProfileId IS NULL OR r.parentProfile.id = :parentProfileId)
          AND r.effectiveFrom <= :serviceDate
          AND (r.effectiveTo IS NULL OR r.effectiveTo >= :serviceDate)
    """)
    long countDashboardRequests(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("parentProfileId") Long parentProfileId,
            @Param("status") RequestStatus status);
}
