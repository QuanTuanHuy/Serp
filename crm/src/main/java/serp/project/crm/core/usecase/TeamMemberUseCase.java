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
import serp.project.crm.core.domain.dto.request.CreateTeamMemberRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamMemberRequest;
import serp.project.crm.core.domain.dto.response.TeamMemberResponse;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.TeamMemberDtoMapper;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamMemberUseCase {

    private final ITeamMemberService teamMemberService;
    private final TeamMemberDtoMapper teamMemberDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional
    public GeneralResponse<?> addTeamMember(CreateTeamMemberRequest request, Long tenantId) {
        try {
            TeamMemberEntity teamMemberEntity = teamMemberDtoMapper.toEntity(request);
            TeamMemberEntity createdMember = teamMemberService.addTeamMember(teamMemberEntity, tenantId);
            TeamMemberResponse response = teamMemberDtoMapper.toResponse(createdMember);

            log.info("Team member added successfully with ID: {}", createdMember.getId());
            return responseUtils.success(response, "Team member added successfully");

        } catch (AppException e) {
            log.error("Validation error adding team member: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error adding team member: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public GeneralResponse<?> updateTeamMember(Long id, UpdateTeamMemberRequest request, Long tenantId) {
        try {
            TeamMemberEntity updates = teamMemberDtoMapper.toEntity(request);
            TeamMemberEntity updatedMember = teamMemberService.updateTeamMember(id, updates, tenantId);
            TeamMemberResponse response = teamMemberDtoMapper.toResponse(updatedMember);

            log.info("Team member updated successfully: {}", id);
            return responseUtils.success(response, "Team member updated successfully");

        } catch (AppException e) {
            log.error("Validation error updating team member: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating team member: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getTeamMemberById(Long id, Long tenantId) {
        try {
            TeamMemberEntity teamMember = teamMemberService.getTeamMemberById(id, tenantId).orElse(null);

            if (teamMember == null) {
                return responseUtils.notFound("Team member not found");
            }

            TeamMemberResponse response = teamMemberDtoMapper.toResponse(teamMember);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("Error fetching team member: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch team member");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getTeamMembersByTeam(Long teamId, Long tenantId, PageRequest pageRequest) {
        try {
            var result = teamMemberService.getTeamMembersByTeam(teamId, tenantId, pageRequest);

            List<TeamMemberResponse> memberResponses = result.getFirst().stream()
                    .map(teamMemberDtoMapper::toResponse)
                    .toList();

            PageResponse<TeamMemberResponse> pageResponse = PageResponse.of(
                    memberResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("Error fetching team members: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch team members");
        }
    }

    @Transactional
    public GeneralResponse<?> changeRole(Long id, String newRole, Long tenantId) {
        try {
            TeamMemberEntity updatedMember = teamMemberService.changeRole(id, newRole, tenantId);
            TeamMemberResponse response = teamMemberDtoMapper.toResponse(updatedMember);

            log.info("Team member role changed successfully: {}", id);
            return responseUtils.success(response, "Role changed successfully");

        } catch (AppException e) {
            log.error("Validation error changing role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error changing role: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public GeneralResponse<?> changeStatus(Long id, TeamMemberStatus newStatus, Long tenantId) {
        try {
            TeamMemberEntity updatedMember = teamMemberService.changeStatus(id, newStatus, tenantId);
            TeamMemberResponse response = teamMemberDtoMapper.toResponse(updatedMember);

            log.info("Team member status changed successfully: {}", id);
            return responseUtils.success(response, "Status changed successfully");

        } catch (AppException e) {
            log.error("Validation error changing status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error changing status: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public GeneralResponse<?> removeTeamMember(Long id, Long tenantId) {
        try {
            teamMemberService.removeTeamMember(id, tenantId);

            log.info("Team member removed successfully: {}", id);
            return responseUtils.status("Team member removed successfully");

        } catch (AppException e) {
            log.error("Validation error removing team member: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error removing team member: {}", e.getMessage(), e);
            throw e;
        }
    }
}
