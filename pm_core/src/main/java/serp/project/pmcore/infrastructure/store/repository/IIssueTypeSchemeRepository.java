/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.IssueTypeSchemeModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIssueTypeSchemeRepository extends JpaRepository<IssueTypeSchemeModel, Long> {

    Optional<IssueTypeSchemeModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM IssueTypeSchemeModel s WHERE s.id = :id AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    Optional<IssueTypeSchemeModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    List<IssueTypeSchemeModel> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    @Query(value = """
    SELECT *
    FROM issue_type_schemes s
    WHERE (s.tenant_id = :tenantId OR s.tenant_id = 0)
      AND s.deleted_at IS NULL
      AND (:search IS NULL OR s.name ILIKE CONCAT('%', :search, '%'))
      AND (
            :isSystem IS NULL
            OR (:isSystem = true AND s.tenant_id = 0)
            OR (:isSystem = false AND s.tenant_id = :tenantId)
          )
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM issue_type_schemes s
    WHERE (s.tenant_id = :tenantId OR s.tenant_id = 0)
      AND s.deleted_at IS NULL
      AND (:search IS NULL OR s.name ILIKE CONCAT('%', :search, '%'))
      AND (
            :isSystem IS NULL
            OR (:isSystem = true AND s.tenant_id = 0)
            OR (:isSystem = false AND s.tenant_id = :tenantId)
          )
    """,
            nativeQuery = true)
    Page<IssueTypeSchemeModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                         @Param("search") String search,
                                                         @Param("isSystem") Boolean isSystem,
                                                         Pageable pageable);

    boolean existsByTenantIdAndName(Long tenantId, String name);

    boolean existsByDefaultIssueTypeIdAndTenantId(Long defaultIssueTypeId, Long tenantId);

    @Modifying
    @Query("UPDATE IssueTypeSchemeModel i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.id = :id AND i.tenantId = :tenantId AND i.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
