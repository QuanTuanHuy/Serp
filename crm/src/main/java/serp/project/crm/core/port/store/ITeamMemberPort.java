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

public interface ITeamMemberPort {

    TeamMemberEntity save(TeamMemberEntity teamMemberEntity);

    Optional<TeamMemberEntity> findById(Long id, Long tenantId);

    Pair<List<TeamMemberEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    Pair<List<TeamMemberEntity>, Long> findByTeamId(Long teamId, Long tenantId, PageRequest pageRequest);

    Optional<TeamMemberEntity> findByTeamIdAndUserId(Long teamId, Long userId, Long tenantId);

    Pair<List<TeamMemberEntity>, Long> findByStatus(TeamMemberStatus status, Long tenantId, PageRequest pageRequest);

    Long countByTeamIdAndStatus(Long teamId, TeamMemberStatus status, Long tenantId);

    Boolean existsByTeamIdAndUserId(Long teamId, Long userId, Long tenantId);

    void deleteById(Long id, Long tenantId);

    List<TeamMemberEntity> findAllByTeamId(Long teamId, Long tenantId);

    void deleteAllByTeamId(Long teamId, Long tenantId);
}
