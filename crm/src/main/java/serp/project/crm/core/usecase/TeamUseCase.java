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
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateTeamRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamRequest;
import serp.project.crm.core.domain.dto.response.TeamResponse;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.TeamDtoMapper;
import serp.project.crm.core.mapper.TeamMemberDtoMapper;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.core.service.ITeamService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamUseCase {

    private final ITeamService teamService;
    private final ITeamMemberService teamMemberService;

    private final TeamDtoMapper teamDtoMapper;
    private final TeamMemberDtoMapper memberDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createTeam(CreateTeamRequest request, Long tenantId) {
        try {
            if (teamService.isTeamNameExists(request.getName(), tenantId)) {
                throw new AppException(ErrorMessage.TEAM_NAME_ALREADY_EXISTS);
            }

            var leaderProfile = teamMemberService.getAndValidateUserProfiles(
                    List.of(request.getLeaderId()), tenantId).stream().findFirst().orElse(null);
            if (leaderProfile == null) {
                return null; // throw exception in getAndValidateUserProfiles
            }
            teamMemberService.getTeamMemberByUserId(leaderProfile.getId(), tenantId)
                    .ifPresent(existing -> {
                        throw new AppException(ErrorMessage.MEMBER_ALREADY_IN_ANOTHER_TEAM);
                    });

            TeamEntity teamEntity = teamDtoMapper.toEntity(request);
            TeamEntity createdTeam = teamService.createTeam(teamEntity, tenantId);

            TeamMemberEntity leaderMember = memberDtoMapper.toEntity(leaderProfile, createdTeam.getId());
            leaderMember = teamMemberService.addTeamMember(leaderMember, tenantId);

            createdTeam.setLeaderId(leaderMember.getId());
            createdTeam = teamService.updateTeam(createdTeam.getId(), createdTeam, tenantId);
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

            TeamResponse response = teamDtoMapper.toResponse(team);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("[TeamUseCase] Unexpected error fetching team: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch team");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllTeams(Long tenantId, PageRequest pageRequest) {
        try {
            var result = teamService.getAllTeams(tenantId, pageRequest);

            List<TeamResponse> teamResponses = result.getFirst().stream()
                    .map(teamDtoMapper::toResponse)
                    .toList();

            PageResponse<TeamResponse> pageResponse = PageResponse.of(
                    teamResponses, pageRequest, result.getSecond());

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
}
