/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.ScreenTabModel;

import java.util.List;

@Repository
public interface IScreenTabRepository extends JpaRepository<ScreenTabModel, Long> {
    @Query("SELECT t FROM ScreenTabModel t WHERE t.screenId = :screenId AND (t.tenantId = :tenantId OR t.tenantId = 0) ORDER BY t.sequence ASC, t.id ASC")
    List<ScreenTabModel> findAllByScreenIdAndTenantIdOrSystemTenant(@Param("screenId") Long screenId,
                                                                    @Param("tenantId") Long tenantId);
}
