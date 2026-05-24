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
import serp.project.pmcore.infrastructure.store.model.ProjectComponentModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProjectComponentRepository extends JpaRepository<ProjectComponentModel, Long> {

    Optional<ProjectComponentModel> findByIdAndProjectIdAndTenantId(Long id, Long projectId, Long tenantId);

    List<ProjectComponentModel> findAllByIdInAndProjectIdAndTenantId(List<Long> ids, Long projectId, Long tenantId);

    @Query(value = """
    SELECT *
    FROM project_components c
    WHERE c.tenant_id = :tenantId
      AND c.project_id = :projectId
      AND c.deleted_at IS NULL
      AND (:search IS NULL OR c.name ILIKE CONCAT('%', :search, '%'))
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM project_components c
    WHERE c.tenant_id = :tenantId
      AND c.project_id = :projectId
      AND c.deleted_at IS NULL
      AND (:search IS NULL OR c.name ILIKE CONCAT('%', :search, '%'))
    """,
            nativeQuery = true)
    Page<ProjectComponentModel> findAllWithFilters(@Param("projectId") Long projectId,
                                                   @Param("tenantId") Long tenantId,
                                                   @Param("search") String search,
                                                   Pageable pageable);

    @Query(value = """
            SELECT c.id AS componentId,
                   COUNT(wi.id) AS issueCount
            FROM project_components c
            LEFT JOIN work_item_components wic
              ON wic.component_id = c.id
             AND wic.tenant_id = :tenantId
             AND wic.deleted_at IS NULL
            LEFT JOIN work_items wi
              ON wi.id = wic.work_item_id
             AND wi.tenant_id = :tenantId
             AND wi.project_id = :projectId
             AND wi.deleted_at IS NULL
            WHERE c.tenant_id = :tenantId
              AND c.project_id = :projectId
              AND c.deleted_at IS NULL
              AND c.id IN (:componentIds)
            GROUP BY c.id
            """, nativeQuery = true)
    List<ComponentIssueCountProjection> countActiveIssuesByComponentIds(
            @Param("projectId") Long projectId,
            @Param("tenantId") Long tenantId,
            @Param("componentIds") List<Long> componentIds);

    boolean existsByProjectIdAndTenantIdAndName(Long projectId, Long tenantId, String name);

    @Modifying
    @Query(value = """
            DELETE FROM work_item_components
             WHERE tenant_id = :tenantId
               AND component_id = :componentId
            """, nativeQuery = true)
    void deleteWorkItemLinks(@Param("componentId") Long componentId, @Param("tenantId") Long tenantId);

    interface ComponentIssueCountProjection {
        Long getComponentId();

        Long getIssueCount();
    }
}
