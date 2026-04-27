/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import serp.project.crm.core.domain.entity.TeamTerritoryEntity;

import java.util.List;
import java.util.Optional;

public interface ITeamTerritoryPort {
    TeamTerritoryEntity save(TeamTerritoryEntity entity);
    List<TeamTerritoryEntity> saveAll(List<TeamTerritoryEntity> entities);
    List<TeamTerritoryEntity> findActiveByTeamId(Long teamId, Long tenantId);
    Optional<TeamTerritoryEntity> findActiveByTerritoryCode(String territoryCode, Long tenantId);
    void deactivateByTeamId(Long teamId, Long tenantId);
}
