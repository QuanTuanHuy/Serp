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
import serp.project.crm.core.domain.constant.TeamMemberRole;
import serp.project.crm.core.domain.constant.WorkingHoursDefaults;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.response.user.UserProfileResponse;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.entity.WorkingHoursEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.client.IUserProfileClient;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.core.service.ITeamService;
import serp.project.crm.core.service.IWorkingHoursService;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamMemberService implements ITeamMemberService {

    private final ITeamMemberPort teamMemberPort;
    private final IUserProfileClient userProfileClient;

    private final ITeamService teamService;
    private final IWorkingHoursService workingHoursService;

    private void validateTeamRole(String role) {
        if (role == null || !TeamMemberRole.ALL.contains(role)) {
            throw new AppException(ErrorMessage.INVALID_TEAM_MEMBER_ROLE);
        }
    }

    @Override
    @Transactional
    public TeamMemberEntity addTeamMember(TeamMemberEntity teamMember, Long tenantId) {
        if (teamMember.getUserId() == null) {
            throw new AppException(String.format(ErrorMessage.REQUIRED_FIELD_MISSING, "userId"));
        }

        teamService.getTeamById(teamMember.getTeamId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_NOT_FOUND));

        validateTeamRole(teamMember.getRole());
        validateWorkingHours(teamMember.getWorkingHours());

        teamMemberPort.findByUserId(teamMember.getUserId(), tenantId)
                .ifPresent(existing -> {
                    throw new AppException(ErrorMessage.MEMBER_ALREADY_IN_TEAM);
                });

        teamMember.setTenantId(tenantId);
        teamMember.setDefaults();
        applyDefaultWorkingHours(teamMember);

        // Save team member first to get ID
        TeamMemberEntity saved = teamMemberPort.save(teamMember);
        
        // Save working hours in same transaction to maintain consistency
        List<WorkingHoursEntity> savedWorkingHours = workingHoursService.replaceByTeamMemberId(
                saved.getId(), teamMember.getWorkingHours());
        saved.setWorkingHours(savedWorkingHours);

        publishTeamMemberAddedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public TeamMemberEntity updateTeamMember(Long id, TeamMemberEntity updates, Long tenantId) {
        TeamMemberEntity existing = getTeamMemberByIdWithWorkingHours(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));

        if (updates.getRole() != null) {
            validateTeamRole(updates.getRole());
        }
        validateWorkingHours(updates.getWorkingHours());

        existing.updateFrom(updates);
        applyDefaultWorkingHours(existing);

        // Save team member first
        TeamMemberEntity updated = teamMemberPort.save(existing);
        
        // Save working hours in same transaction to maintain consistency
        List<WorkingHoursEntity> savedWorkingHours = workingHoursService.replaceByTeamMemberId(
                updated.getId(), existing.getWorkingHours());
        updated.setWorkingHours(savedWorkingHours);

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
    public Optional<TeamMemberEntity> getTeamMemberByIdWithWorkingHours(Long id, Long tenantId) {
        return teamMemberPort.findById(id, tenantId)
                .map(this::attachWorkingHours);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamMemberEntity> getTeamMemberByUserId(Long userId, Long tenantId) {
        return teamMemberPort.findByUserId(userId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamMemberEntity> getTeamMemberByUserIdWithWorkingHours(Long userId, Long tenantId) {
        return teamMemberPort.findByUserId(userId, tenantId)
                .map(this::attachWorkingHours);
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
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByTeamWithWorkingHours(Long teamId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        Pair<List<TeamMemberEntity>, Long> result = teamMemberPort.findByTeamId(teamId, tenantId, pageRequest);
        return Pair.of(attachWorkingHours(result.getFirst()), result.getSecond());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByRole(String role, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        if (!TeamMemberRole.ALL.contains(role)) {
            throw new AppException(ErrorMessage.INVALID_TEAM_MEMBER_ROLE);
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
    @Transactional(readOnly = true)
    public List<TeamMemberEntity> getAllMembersByTeam(Long teamId, Long tenantId) {
        return teamMemberPort.findAllByTeamId(teamId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberEntity> getAllMembersByTeamWithWorkingHours(Long teamId, Long tenantId) {
        return attachWorkingHours(teamMemberPort.findAllByTeamId(teamId, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberEntity> getActiveMembersByTeamWithWorkingHours(Long teamId, Long tenantId) {
        return attachWorkingHours(teamMemberPort.findAllByTeamId(teamId, tenantId))
                .stream()
                .filter(member -> TeamMemberStatus.ACTIVE.equals(member.getStatus()))
                .toList();
    }

    @Override
    @Transactional
    public void removeTeamMember(Long teamId, Long id, Long tenantId) {
        TeamMemberEntity teamMember = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));

        if (!teamId.equals(teamMember.getTeamId())) {
            throw new AppException(ErrorMessage.TEAM_MEMBER_DOES_NOT_BELONG_TO_TEAM);
        }

        teamMember.inactivate(tenantId);
        teamMemberPort.save(teamMember);

        // Clean up working hours (redundant if using ON DELETE CASCADE, but explicit for clarity)
        workingHoursService.deleteByTeamMemberId(id);

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

    @Override
    @Transactional(readOnly = true)
    public TeamMemberEntity getActiveEligibleMember(Long teamId, Long userId, Long tenantId) {
        return teamMemberPort.findByTeamIdAndUserId(teamId, userId, tenantId)
                .filter(member -> TeamMemberStatus.ACTIVE.equals(member.getStatus()))
                .filter(member -> TeamMemberRole.MANAGER.equals(member.getRole())
                        || TeamMemberRole.SALES_REP.equals(member.getRole()))
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
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

    private TeamMemberEntity attachWorkingHours(TeamMemberEntity teamMemberEntity) {
        if (teamMemberEntity == null || teamMemberEntity.getId() == null) {
            return teamMemberEntity;
        }

        List<WorkingHoursEntity> workingHours = workingHoursService.getByTeamMemberId(teamMemberEntity.getId());
        teamMemberEntity.setWorkingHours(workingHours != null ? workingHours : Collections.emptyList());
        return teamMemberEntity;
    }

    private List<TeamMemberEntity> attachWorkingHours(List<TeamMemberEntity> teamMemberEntities) {
        if (teamMemberEntities == null || teamMemberEntities.isEmpty()) {
            return teamMemberEntities;
        }

        Map<Long, List<WorkingHoursEntity>> workingHoursByMemberId = workingHoursService.getByTeamMemberIds(
                teamMemberEntities.stream().map(TeamMemberEntity::getId).toList());

        // Return new list to avoid mutating input
        return teamMemberEntities.stream()
                .map(entity -> {
                    entity.setWorkingHours(workingHoursByMemberId.getOrDefault(entity.getId(), Collections.emptyList()));
                    return entity;
                })
                .toList();
    }

    private void applyDefaultWorkingHours(TeamMemberEntity teamMember) {
        if (teamMember.getWorkingHours() != null && !teamMember.getWorkingHours().isEmpty()) {
            return;
        }

        teamMember.setWorkingHours(WorkingHoursDefaults.createDefaultWeek());
    }

    // Validation exists at both application and database layers:
    // - Application layer (here): fail-fast validation for immediate feedback to API clients
    // - Database layer (migration constraints): enforce data integrity at persistence level
    private void validateWorkingHours(List<WorkingHoursEntity> workingHours) {
        if (workingHours == null) {
            return;
        }

        for (WorkingHoursEntity item : workingHours) {
            if (item.getDayOfWeek() == null || item.getWorkingDay() == null) {
                throw new AppException(ErrorMessage.INVALID_WORKING_HOURS);
            }
            if (!Boolean.TRUE.equals(item.getWorkingDay())) {
                continue;
            }
            if (item.getStartMinute() == null || item.getEndMinute() == null
                    || item.getStartMinute() < 0 || item.getEndMinute() > 1440
                    || item.getStartMinute() >= item.getEndMinute()) {
                throw new AppException(ErrorMessage.INVALID_WORKING_HOURS);
            }
        }
    }
}
