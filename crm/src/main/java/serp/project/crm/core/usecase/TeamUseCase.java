/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateTeamRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamRequest;
import serp.project.crm.core.domain.dto.response.TeamResponse;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.mapper.TeamDtoMapper;
import serp.project.crm.core.service.ITeamService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamUseCase {

    private final ITeamService teamService;
    private final TeamDtoMapper teamDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional
    public GeneralResponse<?> createTeam(CreateTeamRequest request, Long tenantId) {
        try {
            TeamEntity teamEntity = teamDtoMapper.toEntity(request);
            TeamEntity createdTeam = teamService.createTeam(teamEntity, tenantId);
            TeamResponse response = teamDtoMapper.toResponse(createdTeam);

            log.info("Team created successfully with ID: {}", createdTeam.getId());
            return responseUtils.success(response, "Team created successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating team: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating team: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to create team");
        }
    }

    @Transactional
    public GeneralResponse<?> updateTeam(Long id, UpdateTeamRequest request, Long tenantId) {
        try {
            TeamEntity updates = teamDtoMapper.toEntity(request);
            TeamEntity updatedTeam = teamService.updateTeam(id, updates, tenantId);
            TeamResponse response = teamDtoMapper.toResponse(updatedTeam);

            log.info("Team updated successfully: {}", id);
            return responseUtils.success(response, "Team updated successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating team: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating team: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to update team");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getTeamById(Long id, Long tenantId) {
        try {
            TeamEntity team = teamService.getTeamById(id, tenantId).orElse(null);

            if (team == null) {
                return responseUtils.notFound("Team not found");
            }

            TeamResponse response = teamDtoMapper.toResponse(team);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("Error fetching team: {}", e.getMessage(), e);
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
            log.error("Error fetching teams: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch teams");
        }
    }

    @Transactional
    public GeneralResponse<?> deleteTeam(Long id, Long tenantId) {
        try {
            teamService.deleteTeam(id, tenantId);

            log.info("Team deleted successfully: {}", id);
            return responseUtils.status("Team deleted successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error deleting team: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting team: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete team");
        }
    }
}
