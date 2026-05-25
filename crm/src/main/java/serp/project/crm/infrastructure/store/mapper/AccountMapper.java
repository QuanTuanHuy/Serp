/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.AccountTier;
import serp.project.crm.core.domain.enums.AccountType;
import serp.project.crm.core.domain.enums.PreferredTimeSlot;
import serp.project.crm.infrastructure.store.model.AccountModel;

import java.time.DayOfWeek;

@Component
@RequiredArgsConstructor
public class AccountMapper extends BaseMapper {

    public AccountEntity toEntity(AccountModel model) {
        if (model == null) {
            return null;
        }

        return AccountEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .phone(model.getPhone())
                .email(model.getEmail())
                .website(model.getWebsite())
                .industry(model.getIndustry())
                .companySize(model.getCompanySize())
                .parentAccountId(model.getParentAccountId())
                .taxId(model.getTaxId())
                .creditLimit(model.getCreditLimit())
                .totalOpportunities(model.getTotalOpportunities())
                .wonOpportunities(model.getWonOpportunities())
                .totalRevenue(model.getTotalRevenue())
                .activeStatus(stringToEnum(model.getActiveStatus(), ActiveStatus.class))
                .accountType(stringToEnum(model.getAccountType(), AccountType.class))
                .tier(stringToEnum(model.getTier(), AccountTier.class))
                .preferredTimeSlots(parseJsonToEnumList(model.getPreferredTimeSlots(), PreferredTimeSlot.class))
                .preferredDays(parseJsonToEnumList(model.getPreferredDays(), DayOfWeek.class))
                .language(model.getLanguage())
                .timezone(model.getTimezone())
                .notes(model.getNotes())
                .address(buildAddress(
                        model.getAddressStreet(),
                        model.getAddressCity(),
                        model.getAddressState(),
                        model.getAddressZipCode(),
                        model.getAddressCountry()))
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public AccountModel toModel(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        return AccountModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .industry(entity.getIndustry())
                .companySize(entity.getCompanySize())
                .parentAccountId(entity.getParentAccountId())
                .taxId(entity.getTaxId())
                .creditLimit(entity.getCreditLimit())
                .totalOpportunities(entity.getTotalOpportunities())
                .wonOpportunities(entity.getWonOpportunities())
                .totalRevenue(entity.getTotalRevenue())
                .activeStatus(enumToString(entity.getActiveStatus()))
                .accountType(enumToString(entity.getAccountType()))
                .tier(enumToString(entity.getTier()))
                .preferredTimeSlots(serializeEnumListToJson(entity.getPreferredTimeSlots()))
                .preferredDays(serializeEnumListToJson(entity.getPreferredDays()))
                .language(entity.getLanguage())
                .timezone(entity.getTimezone())
                .notes(entity.getNotes())
                .addressStreet(entity.getAddress() != null ? entity.getAddress().getStreet() : null)
                .addressCity(entity.getAddress() != null ? entity.getAddress().getCity() : null)
                .addressState(entity.getAddress() != null ? entity.getAddress().getState() : null)
                .addressZipCode(entity.getAddress() != null ? entity.getAddress().getZipCode() : null)
                .addressCountry(entity.getAddress() != null ? entity.getAddress().getCountry() : null)
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<AccountEntity> toEntityList(List<AccountModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
