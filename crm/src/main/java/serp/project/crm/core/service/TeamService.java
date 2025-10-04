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
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.ITeamPort;

import java.util.List;
import java.util.Optional;

/**
 * Team Service - Business logic for team management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService implements ITeamService {

    private final ITeamPort teamPort;
    private final IKafkaPublisher kafkaPublisher;

    @Override
    @Transactional
    public TeamEntity createTeam(TeamEntity team, Long tenantId) {
        log.info("Creating team {} for tenant {}", team.getName(), tenantId);

        // Validation: Team name uniqueness
        if (teamPort.existsByName(team.getName(), tenantId)) {
            throw new IllegalArgumentException("Team with name " + team.getName() + " already exists");
        }

        // TODO: Validate leader exists in account service

        // Set defaults using entity method
        team.setTenantId(tenantId);
        team.setDefaults();

        // Save
        TeamEntity saved = teamPort.save(team);

        // Publish event
        publishTeamCreatedEvent(saved);

        log.info("Team created successfully with ID {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public TeamEntity updateTeam(Long id, TeamEntity updates, Long tenantId) {
        log.info("Updating team {} for tenant {}", id, tenantId);

        TeamEntity existing = teamPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        // Validation: Team name uniqueness if changed
        if (updates.getName() != null && !updates.getName().equals(existing.getName())) {
            if (teamPort.existsByName(updates.getName(), tenantId)) {
                throw new IllegalArgumentException("Team with name " + updates.getName() + " already exists");
            }
        }

        // Use entity method for update
        existing.updateFrom(updates);

        // Save
        TeamEntity updated = teamPort.save(existing);

        // Publish event
        publishTeamUpdatedEvent(updated);

        log.info("Team {} updated successfully", id);
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
        log.info("Deleting team {} for tenant {}", id, tenantId);

        TeamEntity team = teamPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        // TODO: Check if team has members before deleting

        teamPort.deleteById(id, tenantId);

        // Publish event
        publishTeamDeletedEvent(team);

        log.info("Team {} deleted successfully", id);
    }

    // ========== Event Publishing ==========

    private void publishTeamCreatedEvent(TeamEntity team) {
        // TODO: Implement event publishing
        log.debug("Event: Team created - ID: {}, Topic: {}", team.getId(), Constants.KafkaTopic.TEAM);
    }

    private void publishTeamUpdatedEvent(TeamEntity team) {
        // TODO: Implement event publishing
        log.debug("Event: Team updated - ID: {}, Topic: {}", team.getId(), Constants.KafkaTopic.TEAM);
    }

    private void publishTeamDeletedEvent(TeamEntity team) {
        // TODO: Implement event publishing
        log.debug("Event: Team deleted - ID: {}, Topic: {}", team.getId(), Constants.KafkaTopic.TEAM);
    }
}
