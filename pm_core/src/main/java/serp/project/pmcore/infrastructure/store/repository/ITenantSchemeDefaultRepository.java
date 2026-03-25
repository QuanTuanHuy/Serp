/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.TenantSchemeDefaultModel;

import java.util.List;

@Repository
public interface ITenantSchemeDefaultRepository extends JpaRepository<TenantSchemeDefaultModel, Long> {

    List<TenantSchemeDefaultModel> findAllByTenantId(Long tenantId);

    @Query("SELECT d FROM TenantSchemeDefaultModel d WHERE d.tenantId = :tenantId OR d.tenantId = 0")
    List<TenantSchemeDefaultModel> findAllByTenantIdIncludingSystem(@Param("tenantId") Long tenantId);
}
