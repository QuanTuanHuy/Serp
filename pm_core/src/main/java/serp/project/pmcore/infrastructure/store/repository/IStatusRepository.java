/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.StatusModel;

import java.util.Optional;

@Repository
public interface IStatusRepository extends JpaRepository<StatusModel, Long> {

    Optional<StatusModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM StatusModel s WHERE s.id = :id AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    Optional<StatusModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
