/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.entity.TerritoryEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.ITerritoryPort;
import serp.project.crm.core.service.ITerritoryService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TerritoryService implements ITerritoryService {
    private final ITerritoryPort territoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<TerritoryEntity> getTerritoriesByCodes(List<String> territoryCodes, Long tenantId) {
        List<TerritoryEntity> territories = territoryPort.findByCodes(territoryCodes, tenantId);
        if (territories.size() != territoryCodes.size()) {
            throw new AppException(ErrorMessage.INVALID_TERRITORY_CODE);
        }
        return territories;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TerritoryEntity> resolveTerritory(String territoryCode, String state, String city, Long tenantId) {
        if (territoryCode != null && !territoryCode.isBlank()) {
            return territoryPort.findByCode(territoryCode, tenantId);
        }

        if ((state == null || state.isBlank()) && (city == null || city.isBlank())) {
            return Optional.empty();
        }

        return territoryPort.findByStateOrCity(state, city, tenantId);
    }
}
