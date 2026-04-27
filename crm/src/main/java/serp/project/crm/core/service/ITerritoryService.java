/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import serp.project.crm.core.domain.dto.request.TerritoryFilterRequest;
import serp.project.crm.core.domain.entity.TerritoryEntity;

import java.util.List;
import java.util.Optional;

public interface ITerritoryService {
    TerritoryEntity createTerritory(TerritoryEntity territory, Long tenantId, Long userId);
    TerritoryEntity updateTerritory(String territoryCode, TerritoryEntity updates, Long tenantId, Long userId);
    TerritoryEntity activateTerritory(String territoryCode, Long tenantId, Long userId);
    TerritoryEntity deactivateTerritory(String territoryCode, Long tenantId, Long userId);
    Optional<TerritoryEntity> getTerritoryByCode(String territoryCode, Long tenantId);
    List<TerritoryEntity> getTerritories(TerritoryFilterRequest filter, Long tenantId);
    List<TerritoryEntity> getTerritoriesByCodes(List<String> territoryCodes, Long tenantId);
    Optional<TerritoryEntity> resolveTerritory(String territoryCode, String state, String city, Long tenantId);
}
