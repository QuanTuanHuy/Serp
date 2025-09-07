/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.GroupRoleEntity;
import serp.project.account.infrastructure.store.model.GroupRoleModel;

@Component
public class GroupRoleMapper extends BaseMapper {

    public GroupRoleEntity toEntity(GroupRoleModel model) {
        if (model == null) {
            return null;
        }

        return GroupRoleEntity.builder()
                .id(model.getId())
                .groupId(model.getGroupId())
                .roleId(model.getRoleId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public GroupRoleModel toModel(GroupRoleEntity entity) {
        if (entity == null) {
            return null;
        }

        return GroupRoleModel.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .roleId(entity.getRoleId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<GroupRoleEntity> toEntityList(List<GroupRoleModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<GroupRoleModel> toModelList(List<GroupRoleEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
