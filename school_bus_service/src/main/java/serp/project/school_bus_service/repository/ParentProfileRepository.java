package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.ParentProfileEntity;

import java.util.List;
import java.util.Optional;

public interface ParentProfileRepository extends BaseRepository<ParentProfileEntity, Long> {
    List<ParentProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

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
