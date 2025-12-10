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
import serp.project.crm.core.domain.dto.response.user.UserProfileResponse;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.core.service.ITeamService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamMemberService implements ITeamMemberService {

    private final ITeamMemberPort teamMemberPort;
    private final IUserProfileClient userProfileClient;

    private final ITeamService teamService;

    private static final List<String> ALLOWED_ROLES = List.of("LEADER", "MEMBER", "VIEWER");

    @Override
    @Transactional
    public TeamMemberEntity addTeamMember(TeamMemberEntity teamMember, Long tenantId) {
        if (teamMember.getUserId() == null) {
            throw new AppException(String.format(ErrorMessage.REQUIRED_FIELD_MISSING, "userId"));
        }

        teamService.getTeamById(teamMember.getTeamId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_NOT_FOUND));

        teamMemberPort.findByUserId(teamMember.getUserId(), tenantId)
                .ifPresent(existing -> {
                    throw new AppException(ErrorMessage.MEMBER_ALREADY_IN_TEAM);
                });

        teamMember.setTenantId(tenantId);
        teamMember.setDefaults();
        if (teamMember.getRole() == null) {
            teamMember.setRole("MEMBER");
        }

        TeamMemberEntity saved = teamMemberPort.save(teamMember);

        publishTeamMemberAddedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public TeamMemberEntity updateTeamMember(Long id, TeamMemberEntity updates, Long tenantId) {
        TeamMemberEntity existing = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));

        existing.updateFrom(updates);

        TeamMemberEntity updated = teamMemberPort.save(existing);

        publishTeamMemberUpdatedEvent(updated);

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamMemberEntity> getTeamMemberById(Long id, Long tenantId) {
        return teamMemberPort.findById(id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamMemberEntity> getTeamMemberByUserId(Long userId, Long tenantId) {
        return teamMemberPort.findByUserId(userId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByTeam(Long teamId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return teamMemberPort.findByTeamId(teamId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByRole(String role, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        if (!ALLOWED_ROLES.contains(role)) {
            throw new AppException("Invalid role. Must be LEADER, MEMBER, or VIEWER");
        }

        return teamMemberPort.findByRole(role, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByStatus(TeamMemberStatus status, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return teamMemberPort.findByStatus(status, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamMemberEntity> getTeamMemberByTeamAndUser(Long teamId, Long userId, Long tenantId) {
        return teamMemberPort.findByTeamIdAndUserId(teamId, userId, tenantId);
    }

    @Override
    @Transactional
    public void removeTeamMember(Long id, Long tenantId) {
        TeamMemberEntity teamMember = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));

        teamMemberPort.deleteById(id, tenantId);

        publishTeamMemberRemovedEvent(teamMember);

    }

    @Override
    public List<UserProfileResponse> getAndValidateUserProfiles(List<Long> userIds, Long tenantId) {
        List<UserProfileResponse> profiles = new ArrayList<>();
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
            profiles.add(userProfile);
        }
        return profiles;
    }

    private void publishTeamMemberAddedEvent(TeamMemberEntity teamMember) {
        log.debug("Event: Team member added - ID: {}, Topic: {}", teamMember.getId(), Constants.KafkaTopic.TEAM);
    }

    private void publishTeamMemberUpdatedEvent(TeamMemberEntity teamMember) {
        log.debug("Event: Team member updated - ID: {}, Topic: {}", teamMember.getId(), Constants.KafkaTopic.TEAM);
    }

    private void publishTeamMemberRemovedEvent(TeamMemberEntity teamMember) {
        log.debug("Event: Team member removed - ID: {}, Topic: {}", teamMember.getId(), Constants.KafkaTopic.TEAM);
    }
}
