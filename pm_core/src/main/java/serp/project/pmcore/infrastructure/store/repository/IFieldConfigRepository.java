/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.FieldConfigModel;

import java.util.Optional;

@Repository
public interface IFieldConfigRepository extends JpaRepository<FieldConfigModel, Long> {
    Optional<FieldConfigModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT f FROM FieldConfigModel f WHERE f.id = :id AND (f.tenantId = :tenantId OR f.tenantId = 0)")
    Optional<FieldConfigModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
