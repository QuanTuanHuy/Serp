/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.UserInvitationEntity;
import serp.project.account.infrastructure.store.model.UserInvitationModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserInvitationMapper extends BaseMapper {

    public UserInvitationEntity toEntity(UserInvitationModel model) {
        if (model == null) return null;
        return UserInvitationEntity.builder()
                .id(model.getId())
                .organizationId(model.getOrganizationId())
                .email(model.getEmail())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .userType(model.getUserType())
                .roleIds(model.getRoleIds() != null ? Arrays.asList(model.getRoleIds()) : List.of())
                .departmentId(model.getDepartmentId())
                .moduleIds(model.getModuleIds() != null ? Arrays.asList(model.getModuleIds()) : List.of())
                .message(model.getMessage())
                .token(model.getToken())
                .status(model.getStatus())
                .invitedBy(model.getInvitedBy())
                .invitedAt(localDateTimeToLong(model.getInvitedAt()))
                .expiresAt(localDateTimeToLong(model.getExpiresAt()))
                .acceptedAt(localDateTimeToLong(model.getAcceptedAt()))
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public UserInvitationModel toModel(UserInvitationEntity entity) {
        if (entity == null) return null;
        return UserInvitationModel.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .userType(entity.getUserType())
                .roleIds(entity.getRoleIds() != null ? entity.getRoleIds().toArray(new Long[0]) : new Long[0])
                .departmentId(entity.getDepartmentId())
                .moduleIds(entity.getModuleIds() != null ? entity.getModuleIds().toArray(new Long[0]) : new Long[0])
                .message(entity.getMessage())
                .token(entity.getToken())
                .status(entity.getStatus())
                .invitedBy(entity.getInvitedBy())
                .invitedAt(longToLocalDateTime(entity.getInvitedAt()))
                .expiresAt(longToLocalDateTime(entity.getExpiresAt()))
                .acceptedAt(longToLocalDateTime(entity.getAcceptedAt()))
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<UserInvitationEntity> toEntityList(List<UserInvitationModel> models) {
        if (models == null) return List.of();
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
