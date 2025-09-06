/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.CreateClientRoleDto;
import serp.project.account.core.domain.dto.request.CreateRealmRoleDto;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.infrastructure.store.model.RoleModel;

@Component
public class RoleMapper extends BaseMapper {

    public RoleEntity toEntity(RoleModel model) {
        if (model == null) {
            return null;
        }

        return RoleEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public RoleModel toModel(RoleEntity entity) {
        if (entity == null) {
            return null;
        }

        return RoleModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<RoleEntity> toEntityList(List<RoleModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<RoleModel> toModelList(List<RoleEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public RoleEntity createRoleMapper(CreateRoleDto request) {
        return RoleEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }


    public CreateRoleDto toCreateRoleDto(CreateRealmRoleDto realmRoleDto) {
        if (realmRoleDto == null) {
            return null;
        }
        return CreateRoleDto.builder()
                .name(realmRoleDto.getName())
                .description(realmRoleDto.getDescription())
                .build();
    }

    public CreateRoleDto toCreateRoleDto(CreateClientRoleDto clientRoleDto) {
        if (clientRoleDto == null) {
            return null;
        }
        return CreateRoleDto.builder()
                .name(clientRoleDto.getName())
                .description(clientRoleDto.getDescription())
                .build();
    }
}
