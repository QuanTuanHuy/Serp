/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkflowModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWorkflowRepository extends JpaRepository<WorkflowModel, Long> {

    Optional<WorkflowModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT w FROM WorkflowModel w WHERE w.id = :id AND (w.tenantId = :tenantId OR w.tenantId = 0)")
    Optional<WorkflowModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Optional<WorkflowModel> findFirstByTenantIdAndWorkflowKeyOrderByIdAsc(Long tenantId, String workflowKey);

    List<WorkflowModel> findAllByIdInAndTenantId(List<Long> workflowIds, Long tenantId);

    @Query(value = """
            SELECT w
            FROM WorkflowModel w
            WHERE (w.tenantId = :tenantId OR w.tenantId = 0)
              AND (:isSystem IS NULL OR w.isSystem = :isSystem)
              AND (
                    :isActive IS NULL
                    OR (:isActive = true AND w.currentPublishedVersionId IS NOT NULL)
                    OR (:isActive = false AND w.currentPublishedVersionId IS NULL)
              )
              AND (
                    :search IS NULL
                    OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(w.workflowKey) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(w)
            FROM WorkflowModel w
            WHERE (w.tenantId = :tenantId OR w.tenantId = 0)
              AND (:isSystem IS NULL OR w.isSystem = :isSystem)
              AND (
                    :isActive IS NULL
                    OR (:isActive = true AND w.currentPublishedVersionId IS NOT NULL)
                    OR (:isActive = false AND w.currentPublishedVersionId IS NULL)
              )
              AND (
                    :search IS NULL
                    OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(w.workflowKey) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<WorkflowModel> findAllVisibleWithFilters(@Param("tenantId") Long tenantId,
                                                  @Param("search") String search,
                                                  @Param("isActive") Boolean isActive,
                                                  @Param("isSystem") Boolean isSystem,
                                                  Pageable pageable);
}
