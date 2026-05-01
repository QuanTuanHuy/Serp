/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.response.user.UserProfileResponse;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;

import java.util.List;
import java.util.Optional;

public interface ITeamMemberService {

    TeamMemberEntity addTeamMember(TeamMemberEntity teamMember, Long tenantId);
    TeamMemberEntity updateTeamMember(Long id, TeamMemberEntity updates, Long tenantId);

    Optional<TeamMemberEntity> getTeamMemberById(Long id, Long tenantId);
    Optional<TeamMemberEntity> getTeamMemberByIdWithWorkingHours(Long id, Long tenantId);
    Optional<TeamMemberEntity> getTeamMemberByUserId(Long userId, Long tenantId);
    Optional<TeamMemberEntity> getTeamMemberByUserIdWithWorkingHours(Long userId, Long tenantId);
    Pair<List<TeamMemberEntity>, Long> getTeamMembersByTeam(Long teamId, Long tenantId, PageRequest pageRequest);
    Pair<List<TeamMemberEntity>, Long> getTeamMembersByTeamWithWorkingHours(Long teamId, Long tenantId,
            PageRequest pageRequest);
    Pair<List<TeamMemberEntity>, Long> getTeamMembersByRole(String role, Long tenantId, PageRequest pageRequest);
    Pair<List<TeamMemberEntity>, Long> getTeamMembersByStatus(TeamMemberStatus status, Long tenantId,
            PageRequest pageRequest);
    Optional<TeamMemberEntity> getTeamMemberByTeamAndUser(Long teamId, Long userId, Long tenantId);
    List<TeamMemberEntity> getAllMembersByTeam(Long teamId, Long tenantId);
    List<TeamMemberEntity> getAllMembersByTeamWithWorkingHours(Long teamId, Long tenantId);
    List<TeamMemberEntity> getActiveMembersByTeamWithWorkingHours(Long teamId, Long tenantId);

    void removeTeamMember(Long teamId, Long id, Long tenantId);

    List<UserProfileResponse> getAndValidateUserProfiles(List<Long> userIds, Long tenantId);

    TeamMemberEntity getActiveEligibleMember(Long teamId, Long userId, Long tenantId);
}
