/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.constant.TeamMemberRole;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.AssignTeamTerritoriesRequest;
import serp.project.crm.core.domain.dto.request.ChangeTeamManagerRequest;
import serp.project.crm.core.domain.dto.request.CreateTeamRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamRequest;
import serp.project.crm.core.domain.dto.response.TeamTerritoryResponse;
import serp.project.crm.core.domain.dto.response.TeamResponse;
import serp.project.crm.core.domain.dto.response.TeamSummaryResponse;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.TeamDtoMapper;
import serp.project.crm.core.mapper.TeamMemberDtoMapper;
import serp.project.crm.core.mapper.TerritoryDtoMapper;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.core.service.ITeamService;
import serp.project.crm.core.service.ITeamTerritoryService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamUseCase {

    private final ITeamService teamService;
    private final ITeamMemberService teamMemberService;
    private final ITeamTerritoryService teamTerritoryService;

    private final TeamDtoMapper teamDtoMapper;
    private final TeamMemberDtoMapper memberDtoMapper;
    private final TerritoryDtoMapper territoryDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createTeam(CreateTeamRequest request, Long tenantId) {
        try {
            if (teamService.isTeamNameExists(request.getName(), tenantId)) {
                throw new AppException(ErrorMessage.TEAM_NAME_ALREADY_EXISTS);
            }

            var leaderProfile = teamMemberService.getAndValidateUserProfiles(
                    List.of(request.getManagerUserId()), tenantId).stream().findFirst().orElse(null);
            if (leaderProfile == null) {
                throw new AppException(ErrorMessage.TEAM_MANAGER_NOT_FOUND);
            }
            teamMemberService.getTeamMemberByUserId(leaderProfile.getId(), tenantId)
                    .ifPresent(existing -> {
                        throw new AppException(ErrorMessage.MEMBER_ALREADY_IN_ANOTHER_TEAM);
                    });

            TeamEntity teamEntity = teamDtoMapper.toEntity(request);
            TeamEntity createdTeam = teamService.createTeam(teamEntity, tenantId);

            TeamMemberEntity leaderMember = memberDtoMapper.toEntity(leaderProfile, createdTeam.getId());
            leaderMember = teamMemberService.addTeamMember(leaderMember, tenantId);

            createdTeam.setManagerUserId(leaderProfile.getId());
            createdTeam = teamService.updateTeam(createdTeam.getId(), createdTeam, tenantId);
            createdTeam.setMembers(List.of(leaderMember));
            TeamResponse response = teamDtoMapper.toResponse(createdTeam);

            log.info("[TeamUseCase] Team created successfully with ID: {}", createdTeam.getId());
            return responseUtils.success(response, "Team created successfully");

        } catch (AppException e) {
            log.error("[TeamUseCase] Error creating team: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error creating team: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public GeneralResponse<?> updateTeam(Long id, UpdateTeamRequest request, Long tenantId) {
        try {
            // TODO: Validate if leader is being changed and handle accordingly
            TeamEntity updates = teamDtoMapper.toEntity(request);
            TeamEntity updatedTeam = teamService.updateTeam(id, updates, tenantId);
            TeamResponse response = teamDtoMapper.toResponse(updatedTeam);

            log.info("[TeamUseCase] Team updated successfully: {}", id);
            return responseUtils.success(response, "Team updated successfully");

        } catch (AppException e) {
            log.error("[TeamUseCase] Error updating team: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error updating team: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getTeamById(Long id, Long tenantId) {
        try {
            TeamEntity team = teamService.getTeamById(id, tenantId).orElse(null);

            if (team == null) {
                return responseUtils.notFound(ErrorMessage.TEAM_NOT_FOUND);
            }

            team.setMembers(teamMemberService.getAllMembersByTeamWithWorkingHours(id, tenantId));

            TeamResponse response = teamDtoMapper.toResponse(team);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error fetching team: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch team");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllTeams(Long tenantId, PageRequest pageRequest, String status) {
        try {
            var result = teamService.getAllTeams(tenantId, pageRequest);

            List<TeamSummaryResponse> teamResponses = result.getFirst().stream()
                    .filter(team -> status == null || team.getStatus() == TeamStatus.valueOf(status.toUpperCase()))
                    .map(team -> {
                        team.setMembers(teamMemberService.getAllMembersByTeamWithWorkingHours(team.getId(), tenantId));
                        return teamDtoMapper.toSummaryResponse(team);
                    })
                    .toList();

            PageResponse<TeamSummaryResponse> pageResponse = PageResponse.of(
                    teamResponses, pageRequest, (long) teamResponses.size());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error fetching teams: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch teams");
        }
    }

    @Transactional
    public GeneralResponse<?> deleteTeam(Long id, Long tenantId) {
        try {
            teamService.deleteTeam(id, tenantId);

            log.info("[TeamUseCase] Team deleted successfully: {}", id);
            return responseUtils.status("Team deleted successfully");

        } catch (AppException e) {
            log.error("[TeamUseCase] Error deleting team: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error deleting team: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete team");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> changeManager(Long teamId, ChangeTeamManagerRequest request, Long tenantId) {
        try {
            if (!TeamMemberRole.MANAGER_DEMOTION_TARGETS
                    .contains(request.getPreviousManagerRole())) {
                throw new AppException(ErrorMessage.TEAM_MANAGER_ROLE_CHANGE_INVALID);
            }

            TeamEntity team = teamService.getTeamById(teamId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_NOT_FOUND));

            TeamMemberEntity newManager = teamMemberService
                    .getTeamMemberByTeamAndUser(teamId, request.getNewManagerUserId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MANAGER_MUST_BELONG_TO_TEAM));

            TeamMemberEntity previousManager = teamMemberService
                    .getTeamMemberByTeamAndUser(teamId, team.getManagerUserId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MANAGER_NOT_FOUND));

            teamMemberService.updateTeamMember(previousManager.getId(), TeamMemberEntity.builder()
                    .role(request.getPreviousManagerRole())
                    .build(), tenantId);

            teamMemberService.updateTeamMember(newManager.getId(), TeamMemberEntity.builder()
                    .role(TeamMemberRole.MANAGER)
                    .build(), tenantId);

            TeamEntity updatedTeam = teamService.updateTeam(teamId, TeamEntity.builder()
                    .managerUserId(request.getNewManagerUserId())
                    .build(), tenantId);
            updatedTeam.setMembers(teamMemberService.getAllMembersByTeamWithWorkingHours(teamId, tenantId));

            return responseUtils.success(teamDtoMapper.toResponse(updatedTeam), "Team manager changed successfully");
        } catch (AppException e) {
            log.error("[TeamUseCase] Error changing team manager: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error changing team manager: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignTerritories(Long teamId, AssignTeamTerritoriesRequest request, Long tenantId,
            Long userId) {
        try {
            var territories = teamTerritoryService.assignTerritories(teamId, request.getTerritoryCodes(), tenantId,
                    userId);
            TeamTerritoryResponse response = territoryDtoMapper.toTeamTerritoryResponse(teamId, territories);
            return responseUtils.success(response, "Team territories assigned successfully");
        } catch (AppException e) {
            log.error("[TeamUseCase] Error assigning territories: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error assigning territories: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getTeamTerritories(Long teamId, Long tenantId) {
        try {
            var territories = teamTerritoryService.getActiveTerritoriesByTeam(teamId, tenantId);
            return responseUtils.success(territoryDtoMapper.toTeamTerritoryResponse(teamId, territories));
        } catch (AppException e) {
            log.error("[TeamUseCase] Error fetching team territories: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error fetching team territories: {}", e.getMessage(), e);
            throw e;
        }
    }
}
