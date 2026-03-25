/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.ScreenSchemeItemModel;

import java.util.List;

@Repository
public interface IScreenSchemeItemRepository extends JpaRepository<ScreenSchemeItemModel, Long> {
    @Query("SELECT i FROM ScreenSchemeItemModel i WHERE i.screenSchemeId = :screenSchemeId AND (i.tenantId = :tenantId OR i.tenantId = 0) ORDER BY i.operationKey ASC, i.id ASC")
    List<ScreenSchemeItemModel> findAllByScreenSchemeIdAndTenantIdOrSystemTenant(@Param("screenSchemeId") Long screenSchemeId,
                                                                                  @Param("tenantId") Long tenantId);

    @Query("SELECT i FROM ScreenSchemeItemModel i WHERE i.screenSchemeId = :screenSchemeId AND i.tenantId = :tenantId ORDER BY i.operationKey ASC, i.id ASC")
    List<ScreenSchemeItemModel> findAllByScreenSchemeIdAndTenantId(@Param("screenSchemeId") Long screenSchemeId,
                                                                   @Param("tenantId") Long tenantId);
}
