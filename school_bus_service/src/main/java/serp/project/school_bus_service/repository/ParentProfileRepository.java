package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.repository.projection.ParentSummaryProjection;

import java.util.List;
import java.util.Optional;

public interface ParentProfileRepository extends BaseRepository<ParentProfileEntity, Long> {
    List<ParentProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query(value = """
            SELECT
                COUNT(p.id) AS totalParents,
                COALESCE(SUM(CASE WHEN p.email IS NOT NULL AND TRIM(p.email) <> '' THEN 1 ELSE 0 END), 0) AS withEmail,
                COALESCE(SUM(CASE WHEN p.phone IS NOT NULL AND TRIM(p.phone) <> '' THEN 1 ELSE 0 END), 0) AS withPhone,
                COALESCE(SUM(CASE WHEN p.is_active = true THEN 1 ELSE 0 END), 0) AS activeParents
              FROM public.school_bus_parent_profile p
             WHERE p.tenant_id = :tenantId
               AND p.is_deleted = false
            """, nativeQuery = true)
    ParentSummaryProjection getParentSummary(@Param("tenantId") Long tenantId);

    Optional<ParentProfileEntity> findByTenantIdAndUserIdAndIsDeletedFalse(Long tenantId, Long userId);

    @Query(value = """
            SELECT new serp.project.school_bus_service.dto.response.ParentProfileResponse(
                p.id, p.tenantId, p.isActive, p.isDeleted, p.createdAt, p.createdBy, p.updatedAt, p.updatedBy,
                u.id, u.accountUserId, p.fullName, p.phone, p.email, p.address
            )
            FROM ParentProfileEntity p
            LEFT JOIN SchoolBusUserEntity u ON u.id = p.userId AND u.isDeleted = false
            WHERE p.tenantId = :tenantId
              AND p.isDeleted = false
              AND (
                  :keywordPattern IS NULL
                  OR LOWER(p.fullName) LIKE :keywordPattern
                  OR LOWER(p.phone) LIKE :keywordPattern
                  OR LOWER(p.email) LIKE :keywordPattern
                  OR LOWER(p.address) LIKE :keywordPattern
              )
            """,
            countQuery = """
            SELECT COUNT(p)
            FROM ParentProfileEntity p
            WHERE p.tenantId = :tenantId
              AND p.isDeleted = false
              AND (
                  :keywordPattern IS NULL
                  OR LOWER(p.fullName) LIKE :keywordPattern
                  OR LOWER(p.phone) LIKE :keywordPattern
                  OR LOWER(p.email) LIKE :keywordPattern
                  OR LOWER(p.address) LIKE :keywordPattern
              )
            """)
    Page<ParentProfileResponse> findParentListItems(
            @Param("tenantId") Long tenantId,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable);
}
