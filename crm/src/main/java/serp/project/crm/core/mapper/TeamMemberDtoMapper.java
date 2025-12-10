/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateTeamMemberRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamMemberRequest;
import serp.project.crm.core.domain.dto.response.TeamMemberResponse;
import serp.project.crm.core.domain.dto.response.user.UserProfileResponse;
import serp.project.crm.core.domain.entity.TeamMemberEntity;

@Component
public class TeamMemberDtoMapper {

    public TeamMemberEntity toEntity(CreateTeamMemberRequest request, UserProfileResponse user) {
        if (request == null) {
            return null;
        }

        return TeamMemberEntity.builder()
                .name(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .teamId(request.getTeamId())
                .userId(request.getUserId())
                .role(user.getRolesInCrm())
                .build();
    }

    public TeamMemberEntity toEntity(UpdateTeamMemberRequest request) {
        if (request == null) {
            return null;
        }

        return TeamMemberEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(request.getStatus())
                .build();
    }

    public TeamMemberResponse toResponse(TeamMemberEntity entity) {
        if (entity == null) {
            return null;
        }

        return TeamMemberResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .teamId(entity.getTeamId())
                .userId(entity.getUserId())
                .role(entity.getRole())
                .status(entity.getStatus())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public TeamMemberEntity toEntity(UserProfileResponse userProfile, Long teamId) {
        return TeamMemberEntity.builder()
                .userId(userProfile.getId())
                .teamId(teamId)
                .name(userProfile.getFullName())
                .email(userProfile.getEmail())
                .phone(userProfile.getPhoneNumber())
                .role(userProfile.getRolesInCrm())
                .build();
    }
}
