/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkItemCustomFieldValueModel;

import java.util.Collection;
import java.util.List;

@Repository
public interface IWorkItemCustomFieldValueRepository extends JpaRepository<WorkItemCustomFieldValueModel, Long> {

    List<WorkItemCustomFieldValueModel> findByWorkItemIdAndTenantIdOrderByCustomFieldIdAscSortOrderAscIdAsc(Long workItemId,
                                                                                                              Long tenantId);

    @Modifying
    @Query("""
            UPDATE WorkItemCustomFieldValueModel w
                SET w.deletedAt = :deletedAt,
                    w.updatedAt = :deletedAt,
                    w.updatedBy = :updatedBy
            WHERE w.workItemId = :workItemId
                AND w.customFieldId IN :customFieldIds
                AND w.deletedAt IS NULL
            """)
    int softDeleteByWorkItemIdAndCustomFieldIds(@Param("workItemId") Long workItemId,
                                                @Param("customFieldIds") Collection<Long> customFieldIds,
                                                @Param("updatedBy") Long updatedBy,
                                                @Param("deletedAt") Long deletedAt);
}
