package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.dto.response.TransportRequestResponse;
import serp.project.school_bus_service.repository.projection.TransportRequestSummaryProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.enums.RequestStatus;


import java.time.LocalDate;
import java.util.List;

public interface TransportRequestRepository extends BaseRepository<TransportRequestEntity, Long> {
    List<TransportRequestEntity> findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(Long tenantId);

    @Query(value = """
            SELECT new serp.project.school_bus_service.dto.response.TransportRequestResponse(
                r.id, r.tenantId, r.isActive, r.isDeleted, r.createdAt, r.createdBy, r.updatedAt, r.updatedBy,
                parent.id, parent.fullName,
                r.requestCode, r.requestedAt, r.requestSource, r.requestType, r.status,
                r.effectiveFrom, r.effectiveTo, r.notes, r.approvedBy, r.approvedAt, r.rejectionReason, r.changeReason
            )
            FROM TransportRequestEntity r
            JOIN r.parentProfile parent
            WHERE r.tenantId = :tenantId
              AND r.isDeleted = false
              AND (:parentProfileId IS NULL OR parent.id = :parentProfileId)
              AND (
                  :keywordPattern IS NULL
                  OR LOWER(parent.fullName) LIKE :keywordPattern
                  OR LOWER(STR(r.requestType)) LIKE :keywordPattern
                  OR LOWER(STR(r.status)) LIKE :keywordPattern
                  OR LOWER(r.notes) LIKE :keywordPattern
              )
            """,
            countQuery = """
            SELECT COUNT(r)
            FROM TransportRequestEntity r
            JOIN r.parentProfile parent
            WHERE r.tenantId = :tenantId
              AND r.isDeleted = false
              AND (:parentProfileId IS NULL OR parent.id = :parentProfileId)
              AND (
                  :keywordPattern IS NULL
                  OR LOWER(parent.fullName) LIKE :keywordPattern
                  OR LOWER(STR(r.requestType)) LIKE :keywordPattern
                  OR LOWER(STR(r.status)) LIKE :keywordPattern
                  OR LOWER(r.notes) LIKE :keywordPattern
              )
            """)
    Page<TransportRequestResponse> findTransportRequestListItems(
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable);

    @Query("""
        SELECT DISTINCT r FROM TransportRequestEntity r
        JOIN RequestStudentEntity rs ON rs.request.id = r.id
        WHERE rs.student.school.id = :schoolId
          AND r.tenantId = :tenantId
          AND r.status = :status
          AND r.isDeleted = false
          AND rs.isDeleted = false
        ORDER BY r.createdAt ASC
    """)
    List<TransportRequestEntity> findBySchool_IdAndTenantIdAndStatusAndIsDeletedFalseOrderByCreatedAtAsc(
            @Param("schoolId") Long schoolId,
            @Param("tenantId") Long tenantId,
            @Param("status") RequestStatus status);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RequestStatus status);

    @Query("""
        SELECT COUNT(DISTINCT r.id) FROM TransportRequestEntity r
        JOIN RequestStudentEntity rs ON rs.request.id = r.id
        WHERE rs.student.school.id = :schoolId
          AND r.tenantId = :tenantId
          AND r.isDeleted = false
          AND rs.isDeleted = false
    """)
    long countBySchoolIdAndTenantIdAndIsDeletedFalse(@Param("schoolId") Long schoolId, @Param("tenantId") Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query("""
        SELECT r.status, COUNT(r) FROM TransportRequestEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND (:schoolId IS NULL OR EXISTS (
              SELECT rs FROM RequestStudentEntity rs
              WHERE rs.request.id = r.id
                AND rs.student.school.id = :schoolId
                AND rs.isDeleted = false
          ))
        GROUP BY r.status
    """)
    List<Object[]> countRequestsByStatusFiltered(
        @Param("tenantId") Long tenantId,
        @Param("schoolId") Long schoolId
    );

    @Query("""
        SELECT r FROM TransportRequestEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND (:schoolId IS NULL OR EXISTS (
              SELECT rs FROM RequestStudentEntity rs
              WHERE rs.request.id = r.id
                AND rs.student.school.id = :schoolId
                AND rs.isDeleted = false
          ))
        ORDER BY r.createdAt DESC
    """)
    List<TransportRequestEntity> findRequestsFiltered(
        @Param("tenantId") Long tenantId,
        @Param("schoolId") Long schoolId
    );

    List<TransportRequestEntity> findByTenantIdAndParentProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(Long tenantId, Long parentProfileId);

    long countByTenantIdAndParentProfileIdAndStatusAndIsDeletedFalse(Long tenantId, Long parentProfileId, RequestStatus status);

    long countByTenantIdAndParentProfileIdAndIsDeletedFalse(Long tenantId, Long parentProfileId);

    @Query(value = """
            SELECT
                COUNT(r.id) AS totalRequests,
                COALESCE(SUM(CASE WHEN r.status = 'SUBMITTED' THEN 1 ELSE 0 END), 0) AS submittedRequests,
                COALESCE(SUM(CASE WHEN r.status = 'APPROVED' THEN 1 ELSE 0 END), 0) AS approvedRequests,
                COALESCE(SUM(CASE WHEN r.status = 'REJECTED' THEN 1 ELSE 0 END), 0) AS rejectedRequests
              FROM public.school_bus_transport_request r
             WHERE r.tenant_id = :tenantId
               AND r.is_deleted = false
               AND (CAST(:parentProfileId AS bigint) IS NULL OR r.parent_profile_id = :parentProfileId)
            """, nativeQuery = true)
    TransportRequestSummaryProjection getTransportRequestSummary(
            @Param("tenantId") Long tenantId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT r.status, COUNT(r) FROM TransportRequestEntity r
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND (:schoolId IS NULL OR EXISTS (
              SELECT rs FROM RequestStudentEntity rs
              WHERE rs.request.id = r.id
                AND rs.student.school.id = :schoolId
                AND rs.isDeleted = false
          ))
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
          AND (:schoolId IS NULL OR EXISTS (
              SELECT rs FROM RequestStudentEntity rs
              WHERE rs.request.id = r.id
                AND rs.student.school.id = :schoolId
                AND rs.isDeleted = false
          ))
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
