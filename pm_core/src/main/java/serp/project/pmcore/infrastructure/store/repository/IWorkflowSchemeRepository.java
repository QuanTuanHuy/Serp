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
import serp.project.pmcore.infrastructure.store.model.WorkflowSchemeModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWorkflowSchemeRepository extends JpaRepository<WorkflowSchemeModel, Long> {

    Optional<WorkflowSchemeModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM WorkflowSchemeModel s WHERE s.id = :id AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    Optional<WorkflowSchemeModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    List<WorkflowSchemeModel> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    @Query(value = """
    SELECT *
    FROM workflow_schemes s
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
    FROM workflow_schemes s
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
    Page<WorkflowSchemeModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                        @Param("search") String search,
                                                        @Param("isSystem") Boolean isSystem,
                                                        Pageable pageable);

    boolean existsByTenantIdAndName(Long tenantId, String name);

    @Modifying
    @Query("UPDATE WorkflowSchemeModel s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :id AND s.tenantId = :tenantId AND s.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
