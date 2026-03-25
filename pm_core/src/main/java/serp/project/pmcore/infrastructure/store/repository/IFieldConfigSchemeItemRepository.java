/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.FieldConfigSchemeItemModel;

import java.util.List;

@Repository
public interface IFieldConfigSchemeItemRepository extends JpaRepository<FieldConfigSchemeItemModel, Long> {
    @Query("SELECT i FROM FieldConfigSchemeItemModel i WHERE i.schemeId = :schemeId AND (i.tenantId = :tenantId OR i.tenantId = 0) ORDER BY i.id ASC")
    List<FieldConfigSchemeItemModel> findAllBySchemeIdAndTenantIdOrSystemTenant(@Param("schemeId") Long schemeId,
                                                                                 @Param("tenantId") Long tenantId);

    @Query("SELECT i FROM FieldConfigSchemeItemModel i WHERE i.schemeId = :schemeId AND i.tenantId = :tenantId ORDER BY i.id ASC")
    List<FieldConfigSchemeItemModel> findAllBySchemeIdAndTenantId(@Param("schemeId") Long schemeId,
                                                                  @Param("tenantId") Long tenantId);
}
