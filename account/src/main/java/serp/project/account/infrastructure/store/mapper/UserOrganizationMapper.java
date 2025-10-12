/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.UserOrganizationEntity;
import serp.project.account.infrastructure.store.model.UserOrganizationModel;

@Component
public class UserOrganizationMapper extends BaseMapper {

    public UserOrganizationEntity toEntity(UserOrganizationModel model) {
        if (model == null) {
            return null;
        }

        return UserOrganizationEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .organizationId(model.getOrganizationId())
                .roleId(model.getRoleId())
                .description(model.getDescription())
                .isDefault(model.getIsDefault())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public UserOrganizationModel toModel(UserOrganizationEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserOrganizationModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .organizationId(entity.getOrganizationId())
                .roleId(entity.getRoleId())
                .description(entity.getDescription())
                .isDefault(entity.getIsDefault())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<UserOrganizationEntity> toEntityList(List<UserOrganizationModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<UserOrganizationModel> toModelList(List<UserOrganizationEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public UserOrganizationEntity assignUserOrganizationMapper(Long userId, Long organizationId, Long roleId,
            Boolean isDefault) {
        if (userId == null || organizationId == null || roleId == null) {
            return null;
        }

        return UserOrganizationEntity.builder()
                .userId(userId)
                .organizationId(organizationId)
                .roleId(roleId)
                .isDefault(isDefault)
                .build();
    }
}
