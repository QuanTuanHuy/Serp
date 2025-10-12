/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.CreatePermissionDto;
import serp.project.account.core.domain.entity.PermissionEntity;
import serp.project.account.core.domain.enums.PermissionAction;
import serp.project.account.infrastructure.store.model.PermissionModel;
import serp.project.account.kernel.utils.ConvertUtils;

@Component
@RequiredArgsConstructor
public class PermissionMapper extends BaseMapper {

    private final ConvertUtils convertUtils;

    public PermissionEntity toEntity(PermissionModel model) {
        if (model == null) {
            return null;
        }

        return PermissionEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .resource(model.getResource())
                .action(convertUtils.convertStringToEnum(model.getAction(), PermissionAction.class))
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public PermissionModel toModel(PermissionEntity entity) {
        if (entity == null) {
            return null;
        }

        return PermissionModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .resource(entity.getResource())
                .action(convertUtils.convertEnumToString(entity.getAction()))
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<PermissionEntity> toEntityList(List<PermissionModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<PermissionModel> toModelList(List<PermissionEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public PermissionEntity createPermissionMapper(CreatePermissionDto request) {
        return PermissionEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .resource(request.getResource())
                .action(convertUtils.convertStringToEnum(request.getAction(), PermissionAction.class))
                .build();
    }
}
