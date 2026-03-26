/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICustomFieldContextRepository extends JpaRepository<CustomFieldContextModel, Long> {

    @Query("SELECT c FROM CustomFieldContextModel c WHERE c.customFieldId = :customFieldId AND (c.tenantId = :tenantId OR c.tenantId = 0) ORDER BY c.id ASC")
    List<CustomFieldContextModel> findAllByCustomFieldIdAndTenantIdOrSystemTenant(@Param("customFieldId") Long customFieldId,
                                                                                   @Param("tenantId") Long tenantId);

    Optional<CustomFieldContextModel> findFirstByTenantIdAndCustomFieldIdAndNameOrderByIdAsc(Long tenantId,
                                                                                               Long customFieldId,
                                                                                               String name);

    @Query(value = """
            SELECT DISTINCT c.*
            FROM custom_field_contexts c
            LEFT JOIN custom_field_context_projects cp
              ON cp.context_id = c.id
             AND cp.tenant_id = c.tenant_id
             AND cp.deleted_at IS NULL
            LEFT JOIN custom_field_context_issue_types ci
              ON ci.context_id = c.id
             AND ci.tenant_id = c.tenant_id
             AND ci.deleted_at IS NULL
            WHERE c.deleted_at IS NULL
              AND c.tenant_id = :tenantId
              AND c.custom_field_id = :customFieldId
              AND (c.applies_to_all_projects = TRUE OR cp.project_id = :projectId)
              AND (c.applies_to_all_issue_types = TRUE OR ci.issue_type_id = :issueTypeId)
             ORDER BY c.id ASC
             """, nativeQuery = true)
    List<CustomFieldContextModel> findApplicableContexts(@Param("customFieldId") Long customFieldId,
                                                         @Param("projectId") Long projectId,
                                                         @Param("issueTypeId") Long issueTypeId,
                                                         @Param("tenantId") Long tenantId);
}
