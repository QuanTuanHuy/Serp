/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.ScreenTabFieldModel;

import java.util.Collection;
import java.util.List;

@Repository
public interface IScreenTabFieldRepository extends JpaRepository<ScreenTabFieldModel, Long> {
    @Query("SELECT f FROM ScreenTabFieldModel f WHERE f.screenTabId = :screenTabId AND (f.tenantId = :tenantId OR f.tenantId = 0) ORDER BY f.sequence ASC, f.id ASC")
    List<ScreenTabFieldModel> findAllByScreenTabIdAndTenantIdOrSystemTenant(@Param("screenTabId") Long screenTabId,
                                                                             @Param("tenantId") Long tenantId);

    @Query("SELECT f FROM ScreenTabFieldModel f WHERE f.screenTabId = :screenTabId AND f.tenantId = :tenantId ORDER BY f.sequence ASC, f.id ASC")
    List<ScreenTabFieldModel> findAllByScreenTabIdAndTenantId(@Param("screenTabId") Long screenTabId,
                                                              @Param("tenantId") Long tenantId);

    @Query("SELECT f FROM ScreenTabFieldModel f WHERE f.tenantId = :tenantId AND f.screenTabId IN :screenTabIds ORDER BY f.sequence ASC, f.id ASC")
    List<ScreenTabFieldModel> findAllByScreenTabIdsAndTenantId(Collection<Long> screenTabIds, Long tenantId);
}
