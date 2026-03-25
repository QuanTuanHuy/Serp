/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextDefaultValueModel;

import java.util.List;

@Repository
public interface ICustomFieldContextDefaultValueRepository extends JpaRepository<CustomFieldContextDefaultValueModel, Long> {

    @Query("SELECT d FROM CustomFieldContextDefaultValueModel d WHERE d.contextId = :contextId AND d.tenantId = :tenantId ORDER BY d.sortOrder ASC, d.id ASC")
    List<CustomFieldContextDefaultValueModel> findAllByContextIdAndTenantId(@Param("contextId") Long contextId,
                                                                            @Param("tenantId") Long tenantId);
}
