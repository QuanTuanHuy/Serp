/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import serp.project.crm.core.domain.entity.TerritoryEntity;

import java.util.List;
import java.util.Optional;

public interface ITerritoryService {
    List<TerritoryEntity> getTerritoriesByCodes(List<String> territoryCodes, Long tenantId);
    Optional<TerritoryEntity> resolveTerritory(String territoryCode, String state, String city, Long tenantId);
}
