/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.FieldConfigItemModel;

import java.util.List;

@Repository
public interface IFieldConfigItemRepository extends JpaRepository<FieldConfigItemModel, Long> {
    @Query("SELECT i FROM FieldConfigItemModel i WHERE i.fieldConfigId = :fieldConfigId AND (i.tenantId = :tenantId OR i.tenantId = 0) ORDER BY i.sequence ASC, i.id ASC")
    List<FieldConfigItemModel> findAllByFieldConfigIdAndTenantIdOrSystemTenant(@Param("fieldConfigId") Long fieldConfigId,
                                                                                @Param("tenantId") Long tenantId);
}
