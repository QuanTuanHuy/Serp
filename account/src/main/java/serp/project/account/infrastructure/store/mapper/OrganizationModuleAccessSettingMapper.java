/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.OrganizationModuleAccessSettingEntity;
import serp.project.account.infrastructure.store.model.OrganizationModuleAccessSettingModel;

@Component
public class OrganizationModuleAccessSettingMapper extends BaseMapper {

    public OrganizationModuleAccessSettingEntity toEntity(OrganizationModuleAccessSettingModel model) {
        if (model == null) {
            return null;
        }
        return OrganizationModuleAccessSettingEntity.builder()
                .id(model.getId())
                .organizationId(model.getOrganizationId())
                .moduleId(model.getModuleId())
                .autoGrantToNewUsers(model.getAutoGrantToNewUsers())
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public OrganizationModuleAccessSettingModel toModel(OrganizationModuleAccessSettingEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrganizationModuleAccessSettingModel.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .moduleId(entity.getModuleId())
                .autoGrantToNewUsers(Boolean.TRUE.equals(entity.getAutoGrantToNewUsers()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<OrganizationModuleAccessSettingEntity> toEntityList(
            List<OrganizationModuleAccessSettingModel> models) {
        if (models == null) {
            return List.of();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
