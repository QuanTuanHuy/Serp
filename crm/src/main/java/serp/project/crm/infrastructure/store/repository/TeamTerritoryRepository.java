/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.TeamTerritoryModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamTerritoryRepository extends JpaRepository<TeamTerritoryModel, Long> {
    List<TeamTerritoryModel> findByTenantIdAndTeamIdAndActiveTrue(Long tenantId, Long teamId);
    Optional<TeamTerritoryModel> findByTenantIdAndTerritoryCodeAndActiveTrue(Long tenantId, String territoryCode);
    List<TeamTerritoryModel> findByTenantIdAndTeamId(Long tenantId, Long teamId);
}
