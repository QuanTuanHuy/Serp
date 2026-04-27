/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.TeamTerritoryEntity;
import serp.project.crm.core.port.store.ITeamTerritoryPort;
import serp.project.crm.infrastructure.store.mapper.TeamTerritoryMapper;
import serp.project.crm.infrastructure.store.repository.TeamTerritoryRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TeamTerritoryAdapter implements ITeamTerritoryPort {
    private final TeamTerritoryRepository teamTerritoryRepository;
    private final TeamTerritoryMapper teamTerritoryMapper;

    @Override
    public TeamTerritoryEntity save(TeamTerritoryEntity entity) {
        return teamTerritoryMapper.toEntity(teamTerritoryRepository.save(teamTerritoryMapper.toModel(entity)));
    }

    @Override
    public List<TeamTerritoryEntity> saveAll(List<TeamTerritoryEntity> entities) {
        return teamTerritoryRepository.saveAll(entities.stream().map(teamTerritoryMapper::toModel).toList())
                .stream()
                .map(teamTerritoryMapper::toEntity)
                .toList();
    }

    @Override
    public List<TeamTerritoryEntity> findActiveByTeamId(Long teamId, Long tenantId) {
        return teamTerritoryRepository.findByTenantIdAndTeamIdAndActiveTrue(tenantId, teamId)
                .stream()
                .map(teamTerritoryMapper::toEntity)
                .toList();
    }

    @Override
    public Optional<TeamTerritoryEntity> findActiveByTerritoryCode(String territoryCode, Long tenantId) {
        return teamTerritoryRepository.findByTenantIdAndTerritoryCodeAndActiveTrue(tenantId, territoryCode)
                .map(teamTerritoryMapper::toEntity);
    }

    @Override
    public void deactivateByTeamId(Long teamId, Long tenantId) {
        var mappings = teamTerritoryRepository.findByTenantIdAndTeamId(tenantId, teamId);
        mappings.forEach(mapping -> mapping.setActive(false));
        teamTerritoryRepository.saveAll(mappings);
    }
}
