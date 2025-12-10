/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.core.port.store.ITeamPort;
import serp.project.crm.core.service.ITeamService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService implements ITeamService {

    private final ITeamPort teamPort;
    private final IUserProfileClient userProfileClient;

    @Override
    @Transactional
    public TeamEntity createTeam(TeamEntity team, Long tenantId) {
        team.setTenantId(tenantId);
        team.setDefaults();

        TeamEntity saved = teamPort.save(team);

        publishTeamCreatedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public TeamEntity updateTeam(Long id, TeamEntity updates, Long tenantId) {
        TeamEntity existing = teamPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_NOT_FOUND));

        if (updates.getName() != null && !updates.getName().equals(existing.getName())) {
            if (teamPort.existsByName(updates.getName(), tenantId)) {
                throw new AppException(ErrorMessage.TEAM_NAME_ALREADY_EXISTS);
            }
        }

        existing.updateFrom(updates);
        TeamEntity updated = teamPort.save(existing);

        publishTeamUpdatedEvent(updated);

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamEntity> getTeamById(Long id, Long tenantId) {
        return teamPort.findById(id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamEntity>, Long> getAllTeams(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return teamPort.findAll(tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamEntity>, Long> getTeamsByLeader(Long leaderId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return teamPort.findByLeaderId(leaderId, tenantId, pageRequest);
    }

    @Override
    @Transactional
    public void deleteTeam(Long id, Long tenantId) {
        TeamEntity team = teamPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        // TODO: Check if team has members before deleting

        teamPort.deleteById(id, tenantId);

        publishTeamDeletedEvent(team);
    }

    @Override
    public void validateUserIdsExist(List<Long> userIds, Long tenantId) {
        for (Long userId : userIds) {
            var userProfile = userProfileClient.getUserProfileById(userId);
            if (userProfile == null || !userProfile.belongsToOrganization(tenantId)) {
                throw new AppException(ErrorMessage.MEMBER_NOT_BELONG_TO_ORGANIZATION);
            }
            if (!userProfile.isActive()) {
                throw new AppException(ErrorMessage.MEMBER_IS_NOT_ACTIVE);
            }
            if (!userProfile.canBeAssignedToCrm()) {
                throw new AppException(ErrorMessage.MEMBER_NOT_HAS_CRM_ROLE);
            }
        }
    }

    @Override
    public boolean isTeamNameExists(String name, Long tenantId) {
        return teamPort.existsByName(name, tenantId);
    }

    private void publishTeamCreatedEvent(TeamEntity team) {
        log.debug("Event: Team created - ID: {}, Topic: {}", team.getId(), Constants.KafkaTopic.TEAM);
    }

    private void publishTeamUpdatedEvent(TeamEntity team) {
        log.debug("Event: Team updated - ID: {}, Topic: {}", team.getId(), Constants.KafkaTopic.TEAM);
    }

    private void publishTeamDeletedEvent(TeamEntity team) {
        log.debug("Event: Team deleted - ID: {}, Topic: {}", team.getId(), Constants.KafkaTopic.TEAM);
    }
}
