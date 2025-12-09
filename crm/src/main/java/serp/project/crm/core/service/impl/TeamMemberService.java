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
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.core.service.ITeamService;

import java.util.List;
import java.util.Optional;

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
        teamService.getTeamById(teamMember.getTeamId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        teamMemberPort.findByTeamIdAndUserId(teamMember.getTeamId(), teamMember.getUserId(), tenantId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("User is already a member of this team");
                });

        // TODO: Validate user exists

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
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        existing.updateFrom(updates);

        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }

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
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByUser(Long userId, Long tenantId,
            PageRequest pageRequest) {
        log.warn("getTeamMembersByUser not implemented - port missing findByUserId method");
        return Pair.of(List.of(), 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<TeamMemberEntity>, Long> getTeamMembersByRole(String role, Long tenantId,
            PageRequest pageRequest) {
        log.warn("getTeamMembersByRole not implemented - port missing findByRole method");
        return Pair.of(List.of(), 0L);
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
    public TeamMemberEntity changeRole(Long id, String newRole, Long tenantId) {

        TeamMemberEntity teamMember = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        if (!List.of("LEADER", "MEMBER", "VIEWER").contains(newRole)) {
            throw new IllegalArgumentException("Invalid role. Must be LEADER, MEMBER, or VIEWER");
        }

        teamMember.changeRole(newRole, tenantId);
        TeamMemberEntity updated = teamMemberPort.save(teamMember);

        publishTeamMemberUpdatedEvent(updated);

        return updated;
    }

    @Override
    @Transactional
    public TeamMemberEntity changeStatus(Long id, TeamMemberStatus newStatus, Long tenantId) {
        TeamMemberEntity teamMember = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        switch (newStatus) {
            case CONFIRMED -> teamMember.confirmMember(tenantId);
            case ARCHIVED -> teamMember.archiveMember(tenantId);
            case INVITED -> teamMember.setStatus(TeamMemberStatus.INVITED);
        }

        TeamMemberEntity updated = teamMemberPort.save(teamMember);

        publishTeamMemberUpdatedEvent(updated);

        return updated;
    }

    @Override
    @Transactional
    public void removeTeamMember(Long id, Long tenantId) {
        TeamMemberEntity teamMember = teamMemberPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        teamMemberPort.deleteById(id, tenantId);

        publishTeamMemberRemovedEvent(teamMember);

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
