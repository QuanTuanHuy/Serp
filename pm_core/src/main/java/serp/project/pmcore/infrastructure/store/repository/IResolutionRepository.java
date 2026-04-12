/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.ResolutionModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IResolutionRepository extends JpaRepository<ResolutionModel, Long> {

    Optional<ResolutionModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT r FROM ResolutionModel r WHERE r.id = :id AND (r.tenantId = :tenantId OR r.tenantId = 0)")
    Optional<ResolutionModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Optional<ResolutionModel> findFirstByTenantIdAndNameOrderByIdAsc(Long tenantId, String name);

    List<ResolutionModel> findAllByTenantIdOrderBySequenceAsc(Long tenantId);
}
