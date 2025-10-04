/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateTeamRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamRequest;
import serp.project.crm.core.domain.dto.response.TeamMemberResponse;
import serp.project.crm.core.domain.dto.response.TeamResponse;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.domain.entity.TeamMemberEntity;

import java.util.Collections;
import java.util.List;

@Component
public class TeamDtoMapper {

    private final TeamMemberDtoMapper teamMemberDtoMapper;

    public TeamDtoMapper(TeamMemberDtoMapper teamMemberDtoMapper) {
        this.teamMemberDtoMapper = teamMemberDtoMapper;
    }

    public TeamEntity toEntity(CreateTeamRequest request) {
        if (request == null) {
            return null;
        }

        return TeamEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .leaderId(request.getLeaderId())
                .notes(request.getNotes())
                .build();
    }

    public TeamEntity toEntity(UpdateTeamRequest request) {
        if (request == null) {
            return null;
        }

        return TeamEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .leaderId(request.getLeaderId())
                .notes(request.getNotes())
                .build();
    }

    public TeamResponse toResponse(TeamEntity entity) {
        if (entity == null) {
            return null;
        }

        List<TeamMemberResponse> memberResponses = Collections.emptyList();
        if (entity.getMembers() != null) {
            memberResponses = entity.getMembers().stream()
                    .map(teamMemberDtoMapper::toResponse)
                    .toList();
        }

        return TeamResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .leaderId(entity.getLeaderId())
                .notes(entity.getNotes())
                .members(memberResponses)
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
