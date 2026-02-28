/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.BlueprintSchemeDefaultModel;

import java.util.List;

@Repository
public interface IBlueprintSchemeDefaultRepository extends JpaRepository<BlueprintSchemeDefaultModel, Long> {

    List<BlueprintSchemeDefaultModel> findAllByBlueprintIdAndTenantId(Long blueprintId, Long tenantId);

    @Query("SELECT d FROM BlueprintSchemeDefaultModel d WHERE d.blueprintId = :blueprintId AND (d.tenantId = :tenantId OR d.tenantId = 0)")
    List<BlueprintSchemeDefaultModel> findAllByBlueprintIdAndTenantIdOrSystemTenant(@Param("blueprintId") Long blueprintId, @Param("tenantId") Long tenantId);

    @Modifying
    @Query("UPDATE BlueprintSchemeDefaultModel d SET d.deletedAt = CURRENT_TIMESTAMP WHERE d.id = :id AND d.tenantId = :tenantId AND d.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
