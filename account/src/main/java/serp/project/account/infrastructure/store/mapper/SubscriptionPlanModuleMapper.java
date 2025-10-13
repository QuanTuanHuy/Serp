/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.infrastructure.store.model.SubscriptionPlanModuleModel;

@Component
public class SubscriptionPlanModuleMapper extends BaseMapper {

    public SubscriptionPlanModuleEntity toEntity(SubscriptionPlanModuleModel model) {
        if (model == null) {
            return null;
        }

        return SubscriptionPlanModuleEntity.builder()
                .id(model.getId())
                .subscriptionPlanId(model.getSubscriptionPlanId())
                .moduleId(model.getModuleId())
                .isIncluded(model.getIsIncluded())
                .licenseType(model.getLicenseType())
                .maxUsersPerModule(model.getMaxUsersPerModule())
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public SubscriptionPlanModuleModel toModel(SubscriptionPlanModuleEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionPlanModuleModel.builder()
                .id(entity.getId())
                .subscriptionPlanId(entity.getSubscriptionPlanId())
                .moduleId(entity.getModuleId())
                .isIncluded(entity.getIsIncluded())
                .licenseType(entity.getLicenseType())
                .maxUsersPerModule(entity.getMaxUsersPerModule())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<SubscriptionPlanModuleEntity> toEntityList(List<SubscriptionPlanModuleModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<SubscriptionPlanModuleModel> toModelList(List<SubscriptionPlanModuleEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toModel).toList();
    }
}
