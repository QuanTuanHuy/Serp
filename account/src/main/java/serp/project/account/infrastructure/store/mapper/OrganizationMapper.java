/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.CreateOrganizationDto;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.enums.OrganizationStatus;
import serp.project.account.core.domain.enums.OrganizationType;
import serp.project.account.infrastructure.store.model.OrganizationModel;
import serp.project.account.kernel.utils.ConvertUtils;

@Component
@RequiredArgsConstructor
public class OrganizationMapper extends BaseMapper {

    private final ConvertUtils convertUtils;

    public OrganizationEntity toEntity(OrganizationModel model) {
        if (model == null) {
            return null;
        }

        return OrganizationEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .code(model.getCode())
                .description(model.getDescription())
                .address(model.getAddress())
                .ownerId(model.getOwnerId())
                .organizationType(model.getOrganizationType())
                .industry(model.getIndustry())
                .employeeCount(model.getEmployeeCount())
                .subscriptionPlanId(model.getSubscriptionPlanId())
                .subscriptionExpiresAt(model.getSubscriptionExpiresAt())
                .currentBillingCycle(model.getCurrentBillingCycle())
                .nextBillingDate(model.getNextBillingDate())
                .status(model.getStatus())
                .timezone(model.getTimezone())
                .currency(model.getCurrency())
                .language(model.getLanguage())
                .logoUrl(model.getLogoUrl())
                .primaryColor(model.getPrimaryColor())
                .website(model.getWebsite())
                .phoneNumber(model.getPhoneNumber())
                .email(model.getEmail())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public OrganizationModel toModel(OrganizationEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrganizationModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .address(entity.getAddress())
                .ownerId(entity.getOwnerId())
                .organizationType(entity.getOrganizationType())
                .industry(entity.getIndustry())
                .employeeCount(entity.getEmployeeCount())
                .subscriptionPlanId(entity.getSubscriptionPlanId())
                .subscriptionExpiresAt(entity.getSubscriptionExpiresAt())
                .currentBillingCycle(entity.getCurrentBillingCycle())
                .nextBillingDate(entity.getNextBillingDate())
                .status(entity.getStatus())
                .timezone(entity.getTimezone())
                .currency(entity.getCurrency())
                .language(entity.getLanguage())
                .logoUrl(entity.getLogoUrl())
                .primaryColor(entity.getPrimaryColor())
                .website(entity.getWebsite())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<OrganizationEntity> toEntityList(List<OrganizationModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<OrganizationModel> toModelList(List<OrganizationEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public OrganizationEntity createOrganizationMapper(CreateOrganizationDto request) {
        if (request == null) {
            return null;
        }

        return OrganizationEntity.builder()
                .name(request.getName())
                .code(getOrganizationCode(request.getName()))
                .organizationType(
                        convertUtils.convertStringToEnum(request.getOrganizationType(), OrganizationType.class))
                .subscriptionPlanId(1L) // TODO: Get FREE plan ID from database
                .status(OrganizationStatus.TRIAL)
                .build();
    }

    private String getOrganizationCode(String name) {
        return name.trim().toUpperCase().replaceAll("\\s+", "_");
    }

    public OrganizationEntity updateOrganizationMapper(OrganizationEntity existing, OrganizationEntity update) {
        if (existing == null || update == null) {
            return existing;
        }

        if (update.getName() != null && !update.getName().trim().isEmpty()) {
            existing.setName(update.getName());
        }

        if (update.getCode() != null && !update.getCode().trim().isEmpty()) {
            existing.setCode(update.getCode());
        }

        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }

        if (update.getAddress() != null) {
            existing.setAddress(update.getAddress());
        }

        if (update.getOwnerId() != null) {
            existing.setOwnerId(update.getOwnerId());
        }

        if (update.getOrganizationType() != null) {
            existing.setOrganizationType(update.getOrganizationType());
        }

        if (update.getIndustry() != null && !update.getIndustry().trim().isEmpty()) {
            existing.setIndustry(update.getIndustry());
        }

        if (update.getEmployeeCount() != null) {
            existing.setEmployeeCount(update.getEmployeeCount());
        }

        if (update.getSubscriptionPlanId() != null) {
            existing.setSubscriptionPlanId(update.getSubscriptionPlanId());
        }

        if (update.getSubscriptionExpiresAt() != null) {
            existing.setSubscriptionExpiresAt(update.getSubscriptionExpiresAt());
        }

        if (update.getCurrentBillingCycle() != null) {
            existing.setCurrentBillingCycle(update.getCurrentBillingCycle());
        }

        if (update.getNextBillingDate() != null) {
            existing.setNextBillingDate(update.getNextBillingDate());
        }

        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }

        if (update.getTimezone() != null && !update.getTimezone().trim().isEmpty()) {
            existing.setTimezone(update.getTimezone());
        }

        if (update.getCurrency() != null && !update.getCurrency().trim().isEmpty()) {
            existing.setCurrency(update.getCurrency());
        }

        if (update.getLanguage() != null && !update.getLanguage().trim().isEmpty()) {
            existing.setLanguage(update.getLanguage());
        }

        if (update.getLogoUrl() != null && !update.getLogoUrl().trim().isEmpty()) {
            existing.setLogoUrl(update.getLogoUrl());
        }

        if (update.getPrimaryColor() != null && !update.getPrimaryColor().trim().isEmpty()) {
            existing.setPrimaryColor(update.getPrimaryColor());
        }

        if (update.getWebsite() != null && !update.getWebsite().trim().isEmpty()) {
            existing.setWebsite(update.getWebsite());
        }

        if (update.getPhoneNumber() != null && !update.getPhoneNumber().trim().isEmpty()) {
            existing.setPhoneNumber(update.getPhoneNumber());
        }

        if (update.getEmail() != null && !update.getEmail().trim().isEmpty()) {
            existing.setEmail(update.getEmail());
        }

        if (update.getUpdatedAt() != null) {
            existing.setUpdatedAt(update.getUpdatedAt());
        }

        return existing;
    }
}
