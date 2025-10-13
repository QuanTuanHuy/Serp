/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.infrastructure.store.model.OrganizationSubscriptionModel;

import java.util.List;

@Component
public class OrganizationSubscriptionMapper extends BaseMapper {

    public OrganizationSubscriptionEntity toEntity(OrganizationSubscriptionModel model) {
        if (model == null) {
            return null;
        }

        return OrganizationSubscriptionEntity.builder()
                .id(model.getId())
                .organizationId(model.getOrganizationId())
                .subscriptionPlanId(model.getSubscriptionPlanId())
                .status(model.getStatus())
                .billingCycle(model.getBillingCycle())
                .startDate(model.getStartDate())
                .endDate(model.getEndDate())
                .trialEndsAt(model.getTrialEndsAt())
                .isAutoRenew(model.getIsAutoRenew())
                .totalAmount(model.getTotalAmount())
                .notes(model.getNotes())
                .activatedBy(model.getActivatedBy())
                .activatedAt(model.getActivatedAt())
                .cancelledBy(model.getCancelledBy())
                .cancelledAt(model.getCancelledAt())
                .cancellationReason(model.getCancellationReason())
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public OrganizationSubscriptionModel toModel(OrganizationSubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrganizationSubscriptionModel.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .subscriptionPlanId(entity.getSubscriptionPlanId())
                .status(entity.getStatus())
                .billingCycle(entity.getBillingCycle())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .trialEndsAt(entity.getTrialEndsAt())
                .isAutoRenew(entity.getIsAutoRenew())
                .totalAmount(entity.getTotalAmount())
                .notes(entity.getNotes())
                .activatedBy(entity.getActivatedBy())
                .activatedAt(entity.getActivatedAt())
                .cancelledBy(entity.getCancelledBy())
                .cancelledAt(entity.getCancelledAt())
                .cancellationReason(entity.getCancellationReason())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<OrganizationSubscriptionEntity> toEntityList(List<OrganizationSubscriptionModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<OrganizationSubscriptionModel> toModelList(List<OrganizationSubscriptionEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toModel).toList();
    }
}
