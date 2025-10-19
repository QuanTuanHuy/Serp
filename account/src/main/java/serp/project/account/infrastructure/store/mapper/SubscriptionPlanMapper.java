/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.CreateSubscriptionPlanRequest;
import serp.project.account.core.domain.dto.request.UpdateSubscriptionPlanRequest;
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

    public SubscriptionPlanEntity buildNewPlan(CreateSubscriptionPlanRequest request, Long createdBy) {
        return SubscriptionPlanEntity.builder()
                .planName(request.getPlanName())
                .planCode(request.getPlanCode())
                .description(request.getDescription())
                .monthlyPrice(request.getMonthlyPrice())
                .yearlyPrice(request.getYearlyPrice())
                .maxUsers(request.getMaxUsers())
                .trialDays(request.getTrialDays() != null ? request.getTrialDays() : 0)
                .isActive(true)
                .isCustom(request.getIsCustom() != null ? request.getIsCustom() : false)
                .organizationId(request.getOrganizationId())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 999)
                .createdBy(createdBy)
                .createdAt(Instant.now().toEpochMilli())
                .build();
    }

    public SubscriptionPlanEntity buildUpdatedPlan(SubscriptionPlanEntity existingPlan,
            UpdateSubscriptionPlanRequest request, Long updatedBy) {
        if (existingPlan == null || request == null) {
            return existingPlan;
        }

        if (request.getPlanName() != null) {
            existingPlan.setPlanName(request.getPlanName());
        }
        if (request.getDescription() != null) {
            existingPlan.setDescription(request.getDescription());
        }
        if (request.getMonthlyPrice() != null) {
            existingPlan.setMonthlyPrice(request.getMonthlyPrice());
        }
        if (request.getYearlyPrice() != null) {
            existingPlan.setYearlyPrice(request.getYearlyPrice());
        }
        if (request.getMaxUsers() != null) {
            existingPlan.setMaxUsers(request.getMaxUsers());
        }
        if (request.getTrialDays() != null) {
            existingPlan.setTrialDays(request.getTrialDays());
        }
        if (request.getIsActive() != null) {
            existingPlan.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            existingPlan.setDisplayOrder(request.getDisplayOrder());
        }

        existingPlan.setUpdatedBy(updatedBy);
        existingPlan.setUpdatedAt(Instant.now().toEpochMilli());

        return existingPlan;
    }
}
