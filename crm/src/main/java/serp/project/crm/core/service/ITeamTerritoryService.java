/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import serp.project.crm.core.domain.dto.response.TerritoryOwnerResponse;
import serp.project.crm.core.domain.entity.TerritoryEntity;

import java.util.List;
import java.util.Optional;

public interface ITeamTerritoryService {
    List<TerritoryEntity> assignTerritories(Long teamId, List<String> territoryCodes, Long tenantId, Long assignedBy);
    List<TerritoryEntity> getActiveTerritoriesByTeam(Long teamId, Long tenantId);
    Optional<TerritoryOwnerResponse> getTerritoryOwner(String territoryCode, Long tenantId);
}
