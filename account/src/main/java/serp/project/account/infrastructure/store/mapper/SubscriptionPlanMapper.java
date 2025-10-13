/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.infrastructure.store.model.SubscriptionPlanModel;

@Component
public class SubscriptionPlanMapper extends BaseMapper {

    public SubscriptionPlanEntity toEntity(SubscriptionPlanModel model) {
        if (model == null) {
            return null;
        }

        return SubscriptionPlanEntity.builder()
                .id(model.getId())
                .planName(model.getPlanName())
                .planCode(model.getPlanCode())
                .description(model.getDescription())
                .monthlyPrice(model.getMonthlyPrice())
                .yearlyPrice(model.getYearlyPrice())
                .maxUsers(model.getMaxUsers())
                .trialDays(model.getTrialDays())
                .isActive(model.getIsActive())
                .isCustom(model.getIsCustom())
                .organizationId(model.getOrganizationId())
                .displayOrder(model.getDisplayOrder())
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public SubscriptionPlanModel toModel(SubscriptionPlanEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionPlanModel.builder()
                .id(entity.getId())
                .planName(entity.getPlanName())
                .planCode(entity.getPlanCode())
                .description(entity.getDescription())
                .monthlyPrice(entity.getMonthlyPrice())
                .yearlyPrice(entity.getYearlyPrice())
                .maxUsers(entity.getMaxUsers())
                .trialDays(entity.getTrialDays())
                .isActive(entity.getIsActive())
                .isCustom(entity.getIsCustom())
                .organizationId(entity.getOrganizationId())
                .displayOrder(entity.getDisplayOrder())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<SubscriptionPlanEntity> toEntityList(List<SubscriptionPlanModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
