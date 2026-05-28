/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.tms_billing_service.core.service.IAdminPricingService;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.domain.VasRule;
import serp.project.tms_billing_service.dto.request.admin.UpsertSurchargeRuleRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertVasRuleRequest;
import serp.project.tms_billing_service.dto.response.admin.SurchargeRuleAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.VasRuleAdminResponse;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;
import serp.project.tms_billing_service.repository.SurchargeRuleRepository;
import serp.project.tms_billing_service.repository.TariffRepository;
import serp.project.tms_billing_service.repository.VasRuleRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminPricingServiceImpl implements IAdminPricingService {
    private final TariffRepository tariffRepository;
    private final SurchargeRuleRepository surchargeRuleRepository;
    private final VasRuleRepository vasRuleRepository;

    @Override
    @Transactional
    public TariffAdminResponse upsertTariff(UpsertTariffRequest request) {
        validateDateRange(request.getEffectiveDate(), request.getExpirationDate());

        Tariff tariff = tariffRepository
                .findByServiceCodeAndRouteTypeCodeAndEffectiveDate(
                        request.getServiceCode(),
                        request.getRouteTypeCode(),
                        request.getEffectiveDate()
                )
                .orElseGet(Tariff::new);

        tariff.setServiceCode(request.getServiceCode());
        tariff.setRouteTypeCode(request.getRouteTypeCode());
        tariff.setBaseWeight(request.getBaseWeight());
        tariff.setBasePrice(request.getBasePrice());
        tariff.setStepWeight(request.getStepWeight());
        tariff.setStepPrice(request.getStepPrice());
        tariff.setEffectiveDate(request.getEffectiveDate());
        tariff.setExpirationDate(request.getExpirationDate());

        Tariff saved = tariffRepository.save(tariff);
        return TariffAdminResponse.builder()
                .id(saved.getId())
                .serviceCode(saved.getServiceCode())
                .routeTypeCode(saved.getRouteTypeCode())
                .baseWeight(saved.getBaseWeight())
                .basePrice(saved.getBasePrice())
                .stepWeight(saved.getStepWeight())
                .stepPrice(saved.getStepPrice())
                .effectiveDate(saved.getEffectiveDate())
                .expirationDate(saved.getExpirationDate())
                .build();
    }

    @Override
    @Transactional
    public SurchargeRuleAdminResponse upsertSurchargeRule(UpsertSurchargeRuleRequest request) {
        validateDateRange(request.getEffectiveDate(), request.getExpirationDate());

        SurchargeRule rule = surchargeRuleRepository.findByCode(request.getCode()).orElseGet(SurchargeRule::new);
        rule.setCode(request.getCode());
        rule.setName(request.getName());
        rule.setCalculationType(request.getCalculationType());
        rule.setRatePercent(request.getRatePercent());
        rule.setFixedAmount(request.getFixedAmount());
        rule.setMinAmount(request.getMinAmount());
        rule.setBaseWeight(request.getBaseWeight());
        rule.setBasePrice(request.getBasePrice());
        rule.setStepWeight(request.getStepWeight());
        rule.setStepPrice(request.getStepPrice());
        rule.setEffectiveDate(request.getEffectiveDate());
        rule.setExpirationDate(request.getExpirationDate());

        SurchargeRule saved = surchargeRuleRepository.save(rule);
        return SurchargeRuleAdminResponse.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .name(saved.getName())
                .calculationType(saved.getCalculationType())
                .ratePercent(saved.getRatePercent())
                .fixedAmount(saved.getFixedAmount())
                .minAmount(saved.getMinAmount())
                .baseWeight(saved.getBaseWeight())
                .basePrice(saved.getBasePrice())
                .stepWeight(saved.getStepWeight())
                .stepPrice(saved.getStepPrice())
                .effectiveDate(saved.getEffectiveDate())
                .expirationDate(saved.getExpirationDate())
                .build();
    }

    @Override
    @Transactional
    public VasRuleAdminResponse upsertVasRule(UpsertVasRuleRequest request) {
        VasRule rule = vasRuleRepository.findByCode(request.getCode()).orElseGet(VasRule::new);
        rule.setCode(request.getCode());
        rule.setName(request.getName());
        rule.setCalculationType(request.getCalculationType());
        rule.setRatePercent(request.getRatePercent());
        rule.setFixedAmount(request.getFixedAmount());
        rule.setMinAmount(request.getMinAmount());

        VasRule saved = vasRuleRepository.save(rule);
        return VasRuleAdminResponse.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .name(saved.getName())
                .calculationType(saved.getCalculationType())
                .ratePercent(saved.getRatePercent())
                .fixedAmount(saved.getFixedAmount())
                .minAmount(saved.getMinAmount())
                .build();
    }

    private void validateDateRange(java.time.LocalDate effectiveDate, java.time.LocalDate expirationDate) {
        if (Objects.nonNull(effectiveDate) && Objects.nonNull(expirationDate) && expirationDate.isBefore(effectiveDate)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "expirationDate phải lớn hơn hoặc bằng effectiveDate"
            );
        }
    }
}
