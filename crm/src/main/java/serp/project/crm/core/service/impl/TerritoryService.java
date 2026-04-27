/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.request.TerritoryFilterRequest;
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
    @Transactional
    public TerritoryEntity createTerritory(TerritoryEntity territory, Long tenantId, Long userId) {
        territoryPort.findByCodeAndTenantId(territory.getTerritoryCode(), tenantId)
                .ifPresent(existing -> {
                    throw new AppException(ErrorMessage.TERRITORY_CODE_ALREADY_EXISTS);
                });

        territory.setTenantId(tenantId);
        territory.setCreatedBy(userId);
        territory.setUpdatedBy(userId);
        territory.setDefaults();
        return territoryPort.save(territory);
    }

    @Override
    @Transactional
    public TerritoryEntity updateTerritory(String territoryCode, TerritoryEntity updates, Long tenantId, Long userId) {
        TerritoryEntity target = territoryPort.findByCodeAndTenantId(territoryCode, tenantId)
                .orElseGet(() -> createOverrideFromGlobal(territoryCode, tenantId, userId));

        target.updateFrom(updates);
        target.setUpdatedBy(userId);
        target.setDefaults();
        return territoryPort.save(target);
    }

    @Override
    @Transactional
    public TerritoryEntity activateTerritory(String territoryCode, Long tenantId, Long userId) {
        return updateTerritory(territoryCode, TerritoryEntity.builder().active(true).build(), tenantId, userId);
    }

    @Override
    @Transactional
    public TerritoryEntity deactivateTerritory(String territoryCode, Long tenantId, Long userId) {
        return updateTerritory(territoryCode, TerritoryEntity.builder().active(false).build(), tenantId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TerritoryEntity> getTerritoryByCode(String territoryCode, Long tenantId) {
        return territoryPort.findMergedByCode(territoryCode, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TerritoryEntity> getTerritories(TerritoryFilterRequest filter, Long tenantId) {
        TerritoryFilterRequest safeFilter = filter != null ? filter : TerritoryFilterRequest.builder().build();
        List<Long> tenantIds = switch (safeFilter.getSource() == null ? "ALL" : safeFilter.getSource().toUpperCase()) {
            case "GLOBAL" -> List.of(0L);
            case "TENANT" -> List.of(tenantId);
            default -> List.of(tenantId, 0L);
        };

        return territoryPort.findAllByTenantIds(tenantIds).stream()
                .collect(java.util.stream.Collectors.toMap(TerritoryEntity::getTerritoryCode, entity -> entity,
                        (left, right) -> tenantId.equals(right.getTenantId()) ? right : left))
                .values().stream()
                .filter(entity -> safeFilter.getActive() == null || safeFilter.getActive().equals(entity.getActive()))
                .filter(entity -> safeFilter.getParentTerritoryCode() == null
                        || safeFilter.getParentTerritoryCode().equalsIgnoreCase(entity.getParentTerritoryCode()))
                .filter(entity -> safeFilter.getKeyword() == null || safeFilter.getKeyword().isBlank()
                        || entity.getTerritoryCode().toLowerCase().contains(safeFilter.getKeyword().toLowerCase())
                        || entity.getTerritoryName().toLowerCase().contains(safeFilter.getKeyword().toLowerCase()))
                .sorted(java.util.Comparator.comparing(TerritoryEntity::getTerritoryCode))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TerritoryEntity> getTerritoriesByCodes(List<String> territoryCodes, Long tenantId) {
        List<TerritoryEntity> territories = territoryPort.findMergedByCodes(territoryCodes, tenantId);
        if (territories.size() != territoryCodes.size()) {
            throw new AppException(ErrorMessage.INVALID_TERRITORY_CODE);
        }
        return territories;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TerritoryEntity> resolveTerritory(String territoryCode, String state, String city, Long tenantId) {
        if (territoryCode != null && !territoryCode.isBlank()) {
            return territoryPort.findMergedByCode(territoryCode, tenantId)
                    .filter(TerritoryEntity::getActive);
        }

        if ((state == null || state.isBlank()) && (city == null || city.isBlank())) {
            return Optional.empty();
        }

        return territoryPort.findMergedByStateOrCity(state, city, tenantId);
    }

    private TerritoryEntity createOverrideFromGlobal(String territoryCode, Long tenantId, Long userId) {
        TerritoryEntity global = territoryPort.findByCodeAndTenantId(territoryCode, 0L)
                .orElseThrow(() -> new AppException(ErrorMessage.TERRITORY_NOT_FOUND));

        TerritoryEntity override = TerritoryEntity.builder()
                .tenantId(tenantId)
                .territoryCode(global.getTerritoryCode())
                .territoryName(global.getTerritoryName())
                .territoryLevel(global.getTerritoryLevel())
                .countryCode(global.getCountryCode())
                .parentTerritoryCode(global.getParentTerritoryCode())
                .active(global.getActive())
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        override.setDefaults();
        return override;
    }
}
