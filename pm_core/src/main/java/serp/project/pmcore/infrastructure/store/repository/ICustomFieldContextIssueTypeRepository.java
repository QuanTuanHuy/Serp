/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextIssueTypeModel;

import java.util.List;

@Repository
public interface ICustomFieldContextIssueTypeRepository extends JpaRepository<CustomFieldContextIssueTypeModel, Long> {

    @Query("SELECT i FROM CustomFieldContextIssueTypeModel i WHERE i.contextId = :contextId AND i.tenantId = :tenantId ORDER BY i.id ASC")
    List<CustomFieldContextIssueTypeModel> findAllByContextIdAndTenantId(@Param("contextId") Long contextId,
                                                                         @Param("tenantId") Long tenantId);

    @Query("SELECT i FROM CustomFieldContextIssueTypeModel i WHERE i.contextId = :contextId AND (i.tenantId = :tenantId OR i.tenantId = 0) ORDER BY i.id ASC")
    List<CustomFieldContextIssueTypeModel> findAllByContextIdAndTenantIdOrSystemTenant(@Param("contextId") Long contextId,
                                                                                         @Param("tenantId") Long tenantId);
}
