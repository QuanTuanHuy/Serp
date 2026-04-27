/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.TerritoryEntity;
import serp.project.crm.core.port.store.ITerritoryPort;
import serp.project.crm.infrastructure.store.mapper.TerritoryMapper;
import serp.project.crm.infrastructure.store.repository.TerritoryRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TerritoryAdapter implements ITerritoryPort {
    private final TerritoryRepository territoryRepository;
    private final TerritoryMapper territoryMapper;

    @Override
    public TerritoryEntity save(TerritoryEntity territoryEntity) {
        return territoryMapper.toEntity(territoryRepository.save(territoryMapper.toModel(territoryEntity)));
    }

    @Override
    public List<TerritoryEntity> saveAll(List<TerritoryEntity> territoryEntities) {
        return territoryRepository.saveAll(territoryEntities.stream().map(territoryMapper::toModel).toList())
                .stream()
                .map(territoryMapper::toEntity)
                .toList();
    }

    @Override
    public Optional<TerritoryEntity> findByCode(String territoryCode, Long tenantId) {
        return territoryRepository.findByTenantIdAndTerritoryCodeAndActiveTrue(tenantId, territoryCode)
                .map(territoryMapper::toEntity);
    }

    @Override
    public List<TerritoryEntity> findByCodes(List<String> territoryCodes, Long tenantId) {
        return territoryRepository.findByTenantIdAndTerritoryCodeInAndActiveTrue(tenantId, territoryCodes)
                .stream()
                .map(territoryMapper::toEntity)
                .toList();
    }

    @Override
    public Optional<TerritoryEntity> findByStateOrCity(String state, String city, Long tenantId) {
        return territoryRepository.findByStateOrCity(tenantId, state, city)
                .map(territoryMapper::toEntity);
    }
}
