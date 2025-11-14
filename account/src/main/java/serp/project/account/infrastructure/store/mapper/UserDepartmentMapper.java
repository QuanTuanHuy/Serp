/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.AssignUserToDepartmentRequest;
import serp.project.account.core.domain.entity.UserDepartmentEntity;
import serp.project.account.infrastructure.store.model.UserDepartmentModel;

import java.util.List;

@Component
public class UserDepartmentMapper extends BaseMapper {

    public UserDepartmentEntity toEntity(UserDepartmentModel model) {
        if (model == null) {
            return null;
        }

        return UserDepartmentEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .departmentId(model.getDepartmentId())
                .isPrimary(model.getIsPrimary())
                .jobTitle(model.getJobTitle())
                .description(model.getDescription())
                .isActive(model.getIsActive())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public UserDepartmentModel toModel(UserDepartmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserDepartmentModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .departmentId(entity.getDepartmentId())
                .isPrimary(entity.getIsPrimary())
                .jobTitle(entity.getJobTitle())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<UserDepartmentEntity> toEntityList(List<UserDepartmentModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .toList();
    }

    public List<UserDepartmentModel> toModelList(List<UserDepartmentEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .toList();
    }

    public UserDepartmentEntity createMapper(AssignUserToDepartmentRequest request) {
        if (request.getUserId() == null || request.getDepartmentId() == null) {
            return null;
        }

        return UserDepartmentEntity.builder()
                .userId(request.getUserId())
                .departmentId(request.getDepartmentId())
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .jobTitle(request.getJobTitle())
                .isActive(true)
                .build();
    }
}
