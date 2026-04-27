/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.domain.entity.TeamTerritoryEntity;
import serp.project.crm.core.domain.entity.TerritoryEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.ITeamTerritoryPort;
import serp.project.crm.core.service.ITeamService;
import serp.project.crm.core.service.ITeamTerritoryService;
import serp.project.crm.core.service.ITerritoryService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamTerritoryService implements ITeamTerritoryService {
    private final ITeamService teamService;
    private final ITerritoryService territoryService;
    private final ITeamTerritoryPort teamTerritoryPort;

    @Override
    @Transactional
    public List<TerritoryEntity> assignTerritories(Long teamId, List<String> territoryCodes, Long tenantId, Long assignedBy) {
        TeamEntity team = teamService.getTeamById(teamId, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_NOT_FOUND));
        if (team.getStatus() == null || !team.getStatus().name().equals("ACTIVE")) {
            throw new AppException(ErrorMessage.TEAM_INACTIVE);
        }

        List<TerritoryEntity> territories = territoryService.getTerritoriesByCodes(territoryCodes, tenantId);
        for (TerritoryEntity territory : territories) {
            var existing = teamTerritoryPort.findActiveByTerritoryCode(territory.getTerritoryCode(), tenantId);
            if (existing.isPresent() && !teamId.equals(existing.get().getTeamId())) {
                throw new AppException(ErrorMessage.TERRITORY_ALREADY_ASSIGNED);
            }
        }

        teamTerritoryPort.deactivateByTeamId(teamId, tenantId);
        List<TeamTerritoryEntity> mappings = new ArrayList<>();
        for (TerritoryEntity territory : territories) {
            mappings.add(TeamTerritoryEntity.builder()
                    .tenantId(tenantId)
                    .teamId(teamId)
                    .territoryCode(territory.getTerritoryCode())
                    .assignedBy(assignedBy)
                    .active(true)
                    .build());
        }
        teamTerritoryPort.saveAll(mappings);

        return territories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TerritoryEntity> getActiveTerritoriesByTeam(Long teamId, Long tenantId) {
        List<String> territoryCodes = teamTerritoryPort.findActiveByTeamId(teamId, tenantId)
                .stream()
                .map(TeamTerritoryEntity::getTerritoryCode)
                .toList();

        if (territoryCodes.isEmpty()) {
            return List.of();
        }

        return territoryService.getTerritoriesByCodes(territoryCodes, tenantId);
    }
}
