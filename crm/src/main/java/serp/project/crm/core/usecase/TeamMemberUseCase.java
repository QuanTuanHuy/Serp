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
import serp.project.crm.core.domain.dto.request.CreateTeamMemberRequest;
import serp.project.crm.core.domain.dto.request.ReassignInactiveMemberRecordsRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamMemberRequest;
import serp.project.crm.core.domain.dto.response.MemberReassignmentResponse;
import serp.project.crm.core.domain.dto.response.TeamMemberResponse;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.LeadStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.TeamMemberDtoMapper;
import serp.project.crm.core.service.IActivityService;
import serp.project.crm.core.service.ILeadService;
import serp.project.crm.core.service.IOpportunityService;
import serp.project.crm.core.service.ITeamMemberService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamMemberUseCase {

    private final ITeamMemberService teamMemberService;
    private final ILeadService leadService;
    private final IOpportunityService opportunityService;
    private final IActivityService activityService;
    private final TeamMemberDtoMapper teamMemberDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> addTeamMember(CreateTeamMemberRequest request, Long tenantId) {
        try {
            var userProfile = teamMemberService.getAndValidateUserProfiles(List.of(request.getUserId()), tenantId)
                    .stream().findFirst().orElse(null);
            if (userProfile == null) {
                throw new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND);
            }
            if (teamMemberService.getTeamMemberByUserId(userProfile.getId(), tenantId).isPresent()) {
                throw new AppException(ErrorMessage.MEMBER_ALREADY_IN_TEAM);
            }

            TeamMemberEntity teamMemberEntity = teamMemberDtoMapper.toEntity(request, userProfile);
            TeamMemberEntity createdMember = teamMemberService.addTeamMember(teamMemberEntity, tenantId);
            TeamMemberResponse response = teamMemberDtoMapper.toResponse(createdMember);

            log.info("[TeamMemberUseCase] Team member added successfully with ID: {}", createdMember.getId());
            return responseUtils.success(response, "Team member added successfully");

        } catch (AppException e) {
            log.error("[TeamMemberUseCase] Error adding team member: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamMemberUseCase] Unexpected error adding team member: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public GeneralResponse<?> updateTeamMember(Long teamId, Long id, UpdateTeamMemberRequest request, Long tenantId) {
        try {
            TeamMemberEntity existing = teamMemberService.getTeamMemberById(id, tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
            if (!teamId.equals(existing.getTeamId())) {
                throw new AppException(ErrorMessage.TEAM_MEMBER_DOES_NOT_BELONG_TO_TEAM);
            }

            TeamMemberEntity updates = teamMemberDtoMapper.toEntity(request);
            TeamMemberEntity updatedMember = teamMemberService.updateTeamMember(id, updates, tenantId);
            TeamMemberResponse response = teamMemberDtoMapper.toResponse(updatedMember);

            log.info("[TeamMemberUseCase] Team member updated successfully: {}", id);
            return responseUtils.success(response, "Team member updated successfully");

        } catch (AppException e) {
            log.error("[TeamMemberUseCase] Error updating team member: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamMemberUseCase] Unexpected error updating team member: {}", e.getMessage(), e);
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
            log.error("[TeamMemberUseCase] Error fetching team member: {}", e.getMessage(), e);
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
            log.error("[TeamMemberUseCase] Error fetching team members: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch team members");
        }
    }

    @Transactional
    public GeneralResponse<?> removeTeamMember(Long teamId, Long id, Long tenantId) {
        try {
            teamMemberService.removeTeamMember(teamId, id, tenantId);

            log.info("[TeamMemberUseCase] Team member removed successfully: {}", id);
            return responseUtils.status("Team member removed successfully");

        } catch (AppException e) {
            log.error("[TeamMemberUseCase] Error removing team member: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamMemberUseCase] Unexpected error removing team member: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public GeneralResponse<?> removeAndReassignTeamMember(Long teamId, Long id,
            ReassignInactiveMemberRecordsRequest request, Long tenantId) {
        try {
            TeamMemberEntity member = teamMemberService.getTeamMemberById(id, tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
            if (!teamId.equals(member.getTeamId())) {
                throw new AppException(ErrorMessage.TEAM_MEMBER_DOES_NOT_BELONG_TO_TEAM);
            }

            teamMemberService.getActiveEligibleMember(teamId, request.getTargetUserId(), tenantId);

            int reassignedLeads = 0;
            for (var lead : leadService.getLeadsAssignedTo(member.getUserId(), tenantId,
                    PageRequest.builder().page(1).size(1000).build()).getFirst()) {
                if (lead.getLeadStatus() != LeadStatus.CONVERTED && lead.getLeadStatus() != LeadStatus.DISQUALIFIED) {
                    leadService.assignLead(lead.getId(), request.getTargetUserId(), request.getTargetUserId(), tenantId);
                    reassignedLeads++;
                }
            }

            int reassignedOpportunities = 0;
            for (var opportunity : opportunityService.getOpportunitiesAssignedTo(member.getUserId(), tenantId,
                    PageRequest.builder().page(1).size(1000).build()).getFirst()) {
                if (!opportunity.isClosed()) {
                    opportunityService.assignOpportunity(opportunity.getId(), request.getTargetUserId(),
                            request.getTargetUserId(), tenantId);
                    reassignedOpportunities++;
                }
            }

            int reassignedActivities = 0;
            for (var activity : activityService.getActivitiesByAssignee(member.getUserId(), tenantId,
                    PageRequest.builder().page(1).size(1000).build()).getFirst()) {
                if (!ActivityStatus.COMPLETED.equals(activity.getStatus())
                        && !ActivityStatus.CANCELLED.equals(activity.getStatus())) {
                    activity.setAssignedTo(request.getTargetUserId());
                    activityService.updateActivity(activity.getId(), activity, request.getTargetUserId(), tenantId);
                    reassignedActivities++;
                }
            }

            teamMemberService.removeTeamMember(teamId, id, tenantId);

            MemberReassignmentResponse response = MemberReassignmentResponse.builder()
                    .sourceUserId(member.getUserId())
                    .targetUserId(request.getTargetUserId())
                    .reassignedLeads(reassignedLeads)
                    .reassignedOpportunities(reassignedOpportunities)
                    .reassignedActivities(reassignedActivities)
                    .build();
            return responseUtils.success(response, "Team member removed and records reassigned successfully");
        } catch (AppException e) {
            log.error("[TeamMemberUseCase] Error removing and reassigning team member: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TeamMemberUseCase] Unexpected error removing and reassigning team member: {}", e.getMessage(), e);
            throw e;
        }
    }
}
