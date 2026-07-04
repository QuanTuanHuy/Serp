/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.mapper;

import serp.project.tms_billing_service.domain.ChargeableWeightConfig;
import serp.project.tms_billing_service.domain.DeliveryServiceConfig;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.dto.response.admin.ChargeableWeightConfigAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.DeliveryServiceConfigAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.SurchargeRuleAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;

public final class AdminPricingMapper {
    private AdminPricingMapper() {
    }

    public static TariffAdminResponse toTariffResponse(Tariff tariff) {
        return TariffAdminResponse.builder()
                .id(tariff.getId())
                .serviceCode(tariff.getServiceCode())
                .routeTypeCode(tariff.getRouteTypeCode())
                .baseWeight(tariff.getBaseWeight())
                .basePrice(tariff.getBasePrice())
                .stepWeight(tariff.getStepWeight())
                .stepPrice(tariff.getStepPrice())
                .effectiveDate(tariff.getEffectiveDate())
                .expirationDate(tariff.getExpirationDate())
                .build();
    }

    public static SurchargeRuleAdminResponse toSurchargeResponse(SurchargeRule rule) {
        return SurchargeRuleAdminResponse.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .name(rule.getName())
                .calculationType(rule.getCalculationType())
                .ratePercent(rule.getRatePercent())
                .fixedAmount(rule.getFixedAmount())
                .minAmount(rule.getMinAmount())
                .baseWeight(rule.getBaseWeight())
                .basePrice(rule.getBasePrice())
                .stepWeight(rule.getStepWeight())
                .stepPrice(rule.getStepPrice())
                .effectiveDate(rule.getEffectiveDate())
                .expirationDate(rule.getExpirationDate())
                .build();
    }

    public static ChargeableWeightConfigAdminResponse toChargeableWeightConfigResponse(
            ChargeableWeightConfig config
    ) {
        return ChargeableWeightConfigAdminResponse.builder()
                .id(config.getId())
                .serviceCode(config.getServiceCode())
                .minDimensionCm(config.getMinDimensionCm())
                .smallBulkyThresholdCm(config.getSmallBulkyThresholdCm())
                .baseWeightGram(config.getBaseWeightGram())
                .stepWeightGram(config.getStepWeightGram())
                .maxWeightGram(config.getMaxWeightGram())
                .volumetricDivisor(config.getVolumetricDivisor())
                .build();
    }

    public static DeliveryServiceConfigAdminResponse toDeliveryServiceConfigResponse(
            DeliveryServiceConfig config
    ) {
        return DeliveryServiceConfigAdminResponse.builder()
                .id(config.getId())
                .serviceCode(config.getServiceCode())
                .name(config.getName())
                .description(config.getDescription())
                .active(config.getActive())
                .sortOrder(config.getSortOrder())
                .build();
    }
}
