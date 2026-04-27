/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.TeamMemberRole;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamStatus;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.port.store.ITeamPort;
import serp.project.crm.core.port.store.ITeamTerritoryPort;
import serp.project.crm.core.service.ITeamRoutingService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamRoutingService implements ITeamRoutingService {

    private static final List<String> ROUTING_ROLES = List.of(TeamMemberRole.MANAGER, TeamMemberRole.SALES_REP);

    private final ITeamTerritoryPort teamTerritoryPort;
    private final ITeamPort teamPort;
    private final ITeamMemberPort teamMemberPort;

    @Override
    @Transactional
    public Optional<Long> routeLeadAssignee(String territoryCode, Long tenantId) {
        if (territoryCode == null || territoryCode.isBlank()) {
            return Optional.empty();
        }

        Optional<TeamEntity> teamOptional = teamTerritoryPort.findActiveByTerritoryCode(territoryCode, tenantId)
                .flatMap(mapping -> teamPort.findById(mapping.getTeamId(), tenantId))
                .filter(team -> TeamStatus.ACTIVE.equals(team.getStatus()));

        if (teamOptional.isEmpty()) {
            return Optional.empty();
        }

        TeamEntity team = teamOptional.get();
        List<TeamMemberEntity> eligibleMembers = teamMemberPort.findActiveMembersByTeamIdAndRoles(team.getId(),
                        ROUTING_ROLES, tenantId)
                .stream()
                .sorted(Comparator.comparing(TeamMemberEntity::getId))
                .toList();

        if (eligibleMembers.isEmpty()) {
            return Optional.empty();
        }

        Long nextAssignee = selectRoundRobinAssignee(eligibleMembers, team.getLastAssignedMemberUserId());
        team.setLastAssignedMemberUserId(nextAssignee);
        teamPort.save(team);

        return Optional.of(nextAssignee);
    }

    private Long selectRoundRobinAssignee(List<TeamMemberEntity> members, Long lastAssignedMemberUserId) {
        if (members.size() == 1 || lastAssignedMemberUserId == null) {
            return members.get(0).getUserId();
        }

        for (int i = 0; i < members.size(); i++) {
            if (lastAssignedMemberUserId.equals(members.get(i).getUserId())) {
                return members.get((i + 1) % members.size()).getUserId();
            }
        }

        return members.get(0).getUserId();
    }
}
