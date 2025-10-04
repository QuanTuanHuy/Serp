/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.ITeamMemberPort;

import java.util.List;
import java.util.Optional;

/**
 * Team Member Service - Business logic for team member management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeamMemberService implements ITeamMemberService {

    private final ITeamMemberPort teamMemberPort;
    private final IKafkaPublisher kafkaPublisher;
    private final ITeamService teamService;

    @Override
    @Transactional
    public TeamMemberEntity addTeamMember(TeamMemberEntity teamMember, Long tenantId) {
        log.info("Adding team member {} to team {} for tenant {}", 
                teamMember.getUserId(), teamMember.getTeamId(), tenantId);

        // Validation: Team must exist
        teamService.getTeamById(teamMember.getTeamId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        // Validation: User cannot be in same team twice
        teamMemberPort.findByTeamIdAndUserId(teamMember.getTeamId(), teamMember.getUserId(), tenantId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User is already a member of this team");
                });

        // TODO: Validate user exists in account service

        // Set defaults
        teamMember.setTenantId(tenantId);
        if (teamMember.getStatus() == null) {
            teamMember.setStatus(TeamMemberStatus.CONFIRMED);
        }
        if (teamMember.getRole() == null) {
            teamMember.setRole("MEMBER");
        }

        // Save
        TeamMemberEntity saved = teamMemberPort.save(teamMember);

        // Publish event
        publishTeamMemberAddedEvent(saved);

        log.info("Team member added successfully with ID {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TeamMemberEntity updateTeamMember(Long id, TeamMemberEntity updates, Long tenantId) {
        log.info("Updating team member {} for tenant {}", id, tenantId);

        TeamMemberEntity existing = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        // Update fields
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
        if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
        if (updates.getRole() != null) existing.setRole(updates.getRole());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());

        // Save
        TeamMemberEntity updated = teamMemberPort.save(existing);

        // Publish event
        publishTeamMemberUpdatedEvent(updated);

        log.info("Team member {} updated successfully", id);
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamMemberEntity> getTeamMemberById(Long id, Long tenantId) {
        return teamMemberPort.findById(id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByTeam(Long teamId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return teamMemberPort.findByTeamId(teamId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByUser(Long userId, Long tenantId, PageRequest pageRequest) {
        // Port doesn't have findByUserId, return empty result
        // TODO: Implement when port is updated
        log.warn("getTeamMembersByUser not implemented - port missing findByUserId method");
        return Pair.of(List.of(), 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByRole(String role, Long tenantId, PageRequest pageRequest) {
        // Port doesn't have findByRole, return empty result
        // TODO: Implement when port is updated
        log.warn("getTeamMembersByRole not implemented - port missing findByRole method");
        return Pair.of(List.of(), 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByStatus(TeamMemberStatus status, Long tenantId, PageRequest pageRequest) {
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
    public TeamMemberEntity changeRole(Long id, String newRole, Long tenantId) {
        log.info("Changing team member {} role to {} for tenant {}", id, newRole, tenantId);

        TeamMemberEntity teamMember = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        // Validation: Role values (LEADER, MEMBER, VIEWER)
        if (!List.of("LEADER", "MEMBER", "VIEWER").contains(newRole)) {
            throw new IllegalArgumentException("Invalid role. Must be LEADER, MEMBER, or VIEWER");
        }

        teamMember.setRole(newRole);
        TeamMemberEntity updated = teamMemberPort.save(teamMember);

        // Publish event
        publishTeamMemberUpdatedEvent(updated);

        log.info("Team member {} role changed to {}", id, newRole);
        return updated;
    }

    @Override
    @Transactional
    public TeamMemberEntity changeStatus(Long id, TeamMemberStatus newStatus, Long tenantId) {
        log.info("Changing team member {} status to {} for tenant {}", id, newStatus, tenantId);

        TeamMemberEntity teamMember = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        teamMember.setStatus(newStatus);
        TeamMemberEntity updated = teamMemberPort.save(teamMember);

        // Publish event
        publishTeamMemberUpdatedEvent(updated);

        log.info("Team member {} status changed to {}", id, newStatus);
        return updated;
    }

    @Override
    @Transactional
    public void removeTeamMember(Long id, Long tenantId) {
        log.info("Removing team member {} for tenant {}", id, tenantId);

        TeamMemberEntity teamMember = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        teamMemberPort.deleteById(id, tenantId);

        // Publish event
        publishTeamMemberRemovedEvent(teamMember);

        log.info("Team member {} removed successfully", id);
    }

    // ========== Event Publishing ==========

    private void publishTeamMemberAddedEvent(TeamMemberEntity teamMember) {
        // TODO: Implement event publishing
        log.debug("Event: Team member added - ID: {}, Topic: {}", teamMember.getId(), Constants.KafkaTopic.TEAM);
    }

    private void publishTeamMemberUpdatedEvent(TeamMemberEntity teamMember) {
        // TODO: Implement event publishing
        log.debug("Event: Team member updated - ID: {}, Topic: {}", teamMember.getId(), Constants.KafkaTopic.TEAM);
    }

    private void publishTeamMemberRemovedEvent(TeamMemberEntity teamMember) {
        // TODO: Implement event publishing
        log.debug("Event: Team member removed - ID: {}, Topic: {}", teamMember.getId(), Constants.KafkaTopic.TEAM);
    }
}
