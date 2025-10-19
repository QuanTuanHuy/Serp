/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.SubscriptionPlanFeatureEntity;
import serp.project.account.infrastructure.store.model.SubscriptionPlanFeatureModel;

@Component
public class SubscriptionPlanFeatureMapper extends BaseMapper {

    public SubscriptionPlanFeatureEntity toEntity(SubscriptionPlanFeatureModel model) {
        if (model == null) {
            return null;
        }

        return SubscriptionPlanFeatureEntity.builder()
                .id(model.getId())
                .subscriptionPlanId(model.getSubscriptionPlanId())
                .featureCode(model.getFeatureCode())
                .featureValue(model.getFeatureValue())
                .isEnabled(model.getIsEnabled())
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public SubscriptionPlanFeatureModel toModel(SubscriptionPlanFeatureEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionPlanFeatureModel.builder()
                .id(entity.getId())
                .subscriptionPlanId(entity.getSubscriptionPlanId())
                .featureCode(entity.getFeatureCode())
                .featureValue(entity.getFeatureValue())
                .isEnabled(entity.getIsEnabled())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<SubscriptionPlanFeatureEntity> toEntityList(List<SubscriptionPlanFeatureModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<SubscriptionPlanFeatureModel> toModelList(List<SubscriptionPlanFeatureEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toModel).toList();
    }
}
