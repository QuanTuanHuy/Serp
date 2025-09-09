/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.CreateGroupDto;
import serp.project.account.core.domain.dto.request.CreateKeycloakGroupDto;
import serp.project.account.core.domain.entity.GroupEntity;
import serp.project.account.infrastructure.store.model.GroupModel;

@Component
public class GroupMapper extends BaseMapper {

    public GroupEntity toEntity(GroupModel model) {
        if (model == null) {
            return null;
        }

        return GroupEntity.builder()
                .id(model.getId())
                .groupName(model.getGroupName())
                .description(model.getDescription())
                .keycloakGroupId(model.getKeycloakGroupId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public GroupModel toModel(GroupEntity entity) {
        if (entity == null) {
            return null;
        }

        return GroupModel.builder()
                .id(entity.getId())
                .groupName(entity.getGroupName())
                .description(entity.getDescription())
                .keycloakGroupId(entity.getKeycloakGroupId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<GroupEntity> toEntityList(List<GroupModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<GroupModel> toModelList(List<GroupEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public GroupEntity createGroupMapper(CreateGroupDto dto, String keycloakGroupId) {
        if (dto == null) {
            return null;
        }

        return GroupEntity.builder()
                .groupName(dto.getGroupName())
                .description(dto.getDescription())
                .keycloakGroupId(keycloakGroupId)
                .build();
    }

    public CreateKeycloakGroupDto toCreateKeycloakGroupDto(CreateGroupDto dto) {
        if (dto == null) {
            return null;
        }

        return CreateKeycloakGroupDto.builder()
                .name(dto.getGroupName())
                .description(dto.getDescription())
                .parentGroupId(null)
                .build();
    }
}
