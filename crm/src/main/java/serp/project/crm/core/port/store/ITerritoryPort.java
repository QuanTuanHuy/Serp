/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import serp.project.crm.core.domain.entity.TerritoryEntity;

import java.util.List;
import java.util.Optional;

public interface ITerritoryPort {
    TerritoryEntity save(TerritoryEntity territoryEntity);
    List<TerritoryEntity> saveAll(List<TerritoryEntity> territoryEntities);
    Optional<TerritoryEntity> findByCode(String territoryCode, Long tenantId);
    List<TerritoryEntity> findByCodes(List<String> territoryCodes, Long tenantId);
    Optional<TerritoryEntity> findByStateOrCity(String state, String city, Long tenantId);
}
