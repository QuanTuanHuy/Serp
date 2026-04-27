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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public Optional<TerritoryEntity> findByCodeAndTenantId(String territoryCode, Long tenantId) {
        return territoryRepository.findByTenantIdAndTerritoryCode(tenantId, territoryCode)
                .map(territoryMapper::toEntity);
    }

    @Override
    public Optional<TerritoryEntity> findMergedByCode(String territoryCode, Long tenantId) {
        return mergeByCode(territoryRepository.findByTenantIdIn(List.of(tenantId, 0L)).stream()
                .filter(model -> territoryCode.equals(model.getTerritoryCode()))
                .map(territoryMapper::toEntity)
                .toList(), tenantId).stream().findFirst();
    }

    @Override
    public List<TerritoryEntity> findMergedByCodes(List<String> territoryCodes, Long tenantId) {
        return mergeByCode(territoryRepository.findByTenantIdInAndTerritoryCodeInAndActiveTrue(List.of(tenantId, 0L), territoryCodes)
                .stream()
                .map(territoryMapper::toEntity)
                .toList(), tenantId);
    }

    @Override
    public Optional<TerritoryEntity> findMergedByStateOrCity(String state, String city, Long tenantId) {
        return mergeByCode(territoryRepository.findMergedByStateOrCity(List.of(tenantId, 0L), tenantId, state, city)
                .stream()
                .map(territoryMapper::toEntity)
                .toList(), tenantId).stream().findFirst();
    }

    @Override
    public List<TerritoryEntity> findAllByTenantIds(List<Long> tenantIds) {
        return territoryRepository.findByTenantIdIn(tenantIds).stream()
                .map(territoryMapper::toEntity)
                .toList();
    }

    private List<TerritoryEntity> mergeByCode(List<TerritoryEntity> territories, Long tenantId) {
        Map<String, TerritoryEntity> tenantRecords = territories.stream()
                .filter(entity -> tenantId.equals(entity.getTenantId()))
                .collect(Collectors.toMap(TerritoryEntity::getTerritoryCode, Function.identity(), (left, right) -> right));

        Map<String, TerritoryEntity> merged = territories.stream()
                .filter(entity -> Long.valueOf(0L).equals(entity.getTenantId()))
                .collect(Collectors.toMap(TerritoryEntity::getTerritoryCode, Function.identity(), (left, right) -> right));

        merged.putAll(tenantRecords);
        return merged.values().stream().toList();
    }
}
