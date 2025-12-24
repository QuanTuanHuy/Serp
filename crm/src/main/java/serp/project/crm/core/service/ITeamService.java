/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.TeamEntity;

import java.util.List;
import java.util.Optional;

public interface ITeamService {

    TeamEntity createTeam(TeamEntity team, Long tenantId);
    TeamEntity updateTeam(Long id, TeamEntity updates, Long tenantId);
    void deleteTeam(Long id, Long tenantId);

    Optional<TeamEntity> getTeamById(Long id, Long tenantId);
    Pair<List<TeamEntity>, Long> getAllTeams(Long tenantId, PageRequest pageRequest);
    Pair<List<TeamEntity>, Long> getTeamsByLeader(Long leaderId, Long tenantId, PageRequest pageRequest);

    boolean isTeamNameExists(String name, Long tenantId);

    void validateUserIdsExist(List<Long> userIds, Long tenantId);
}
