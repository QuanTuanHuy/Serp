/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;

import java.util.List;
import java.util.Optional;

/**
 * Port interface for TeamMember operations
 * Defines contract between domain layer and infrastructure layer
 */
public interface ITeamMemberPort {

    /**
     * Save or update team member
     */
    TeamMemberEntity save(TeamMemberEntity teamMemberEntity);

    /**
     * Find team member by ID and tenant ID
     */
    Optional<TeamMemberEntity> findById(Long id, Long tenantId);

    /**
     * Find all team members by tenant ID with pagination
     * 
     * @return Pair of team member list and total count
     */
    Pair<List<TeamMemberEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    /**
     * Find team members by team ID with pagination
     * 
     * @return Pair of team member list and total count
     */
    Pair<List<TeamMemberEntity>, Long> findByTeamId(Long teamId, Long tenantId, PageRequest pageRequest);

    /**
     * Find team member by team ID and user ID
     */
    Optional<TeamMemberEntity> findByTeamIdAndUserId(Long teamId, Long userId, Long tenantId);

    /**
     * Find team members by status with pagination
     * 
     * @return Pair of team member list and total count
     */
    Pair<List<TeamMemberEntity>, Long> findByStatus(TeamMemberStatus status, Long tenantId, PageRequest pageRequest);

    /**
     * Count team members by team ID and status
     */
    Long countByTeamIdAndStatus(Long teamId, TeamMemberStatus status, Long tenantId);

    /**
     * Check if team member exists by team ID and user ID
     */
    Boolean existsByTeamIdAndUserId(Long teamId, Long userId, Long tenantId);

    /**
     * Delete team member by ID and tenant ID
     */
    void deleteById(Long id, Long tenantId);

    /**
     * Find all team members by team ID (no pagination)
     */
    List<TeamMemberEntity> findAllByTeamId(Long teamId, Long tenantId);

    /**
     * Delete all team members by team ID
     */
    void deleteAllByTeamId(Long teamId, Long tenantId);
}
