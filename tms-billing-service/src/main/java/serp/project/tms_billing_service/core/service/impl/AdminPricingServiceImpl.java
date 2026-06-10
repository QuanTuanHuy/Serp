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
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.enums.VasRuleCode;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;
import serp.project.tms_billing_service.repository.SurchargeRuleRepository;
import serp.project.tms_billing_service.repository.TariffRepository;
import serp.project.tms_billing_service.repository.VasRuleRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminPricingServiceImpl implements IAdminPricingService {
    private static final Set<SurchargeRuleEnum> ACTIVE_SURCHARGE_CODES = Set.of(SurchargeRuleEnum.VUNG_XA);
    private static final Set<VasRuleCode> ACTIVE_VAS_CODES = Set.of(VasRuleCode.COD);

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
        return toTariffResponse(saved);
    }

    @Override
    @Transactional
    public SurchargeRuleAdminResponse upsertSurchargeRule(UpsertSurchargeRuleRequest request) {
        validateActiveSurchargeCode(request.getCode());
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
        return toSurchargeResponse(saved);
    }

    @Override
    @Transactional
    public VasRuleAdminResponse upsertVasRule(UpsertVasRuleRequest request) {
        validateActiveVasCode(request.getCode());

        VasRule rule = vasRuleRepository.findByCode(request.getCode()).orElseGet(VasRule::new);
        rule.setCode(request.getCode());
        rule.setName(request.getName());
        rule.setCalculationType(request.getCalculationType());
        rule.setRatePercent(request.getRatePercent());
        rule.setFixedAmount(request.getFixedAmount());
        rule.setMinAmount(request.getMinAmount());

        VasRule saved = vasRuleRepository.save(rule);
        return toVasResponse(saved);
    }

    @Override
    public List<TariffAdminResponse> listTariffs(DeliveryService serviceCode) {
        List<Tariff> tariffs = serviceCode == null
                ? tariffRepository.findAllByOrderByServiceCodeAscRouteTypeCodeAscEffectiveDateDesc()
                : tariffRepository.findAllByServiceCodeOrderByRouteTypeCodeAscEffectiveDateDesc(serviceCode);
        return tariffs.stream()
                .map(this::toTariffResponse)
                .toList();
    }

    @Override
    public List<SurchargeRuleAdminResponse> listSurchargeRules() {
        return surchargeRuleRepository.findAll().stream()
                .filter(rule -> rule.getCode() != null && ACTIVE_SURCHARGE_CODES.contains(rule.getCode()))
                .map(this::toSurchargeResponse)
                .toList();
    }

    @Override
    public List<VasRuleAdminResponse> listVasRules() {
        return vasRuleRepository.findAll().stream()
                .filter(rule -> rule.getCode() != null && ACTIVE_VAS_CODES.contains(rule.getCode()))
                .map(this::toVasResponse)
                .toList();
    }

    private void validateActiveSurchargeCode(SurchargeRuleEnum code) {
        if (code == null || !ACTIVE_SURCHARGE_CODES.contains(code)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Surcharge rule is no longer supported for shipping fee calculation"
            );
        }
    }

    private void validateActiveVasCode(VasRuleCode code) {
        if (code == null || !ACTIVE_VAS_CODES.contains(code)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "VAS rule is no longer supported for shipping fee calculation"
            );
        }
    }

    private void validateDateRange(java.time.LocalDate effectiveDate, java.time.LocalDate expirationDate) {
        if (Objects.nonNull(effectiveDate) && Objects.nonNull(expirationDate) && expirationDate.isBefore(effectiveDate)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "expirationDate phải lớn hơn hoặc bằng effectiveDate"
            );
        }
    }

    private TariffAdminResponse toTariffResponse(Tariff tariff) {
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

    private SurchargeRuleAdminResponse toSurchargeResponse(SurchargeRule rule) {
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

    private VasRuleAdminResponse toVasResponse(VasRule rule) {
        return VasRuleAdminResponse.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .name(rule.getName())
                .calculationType(rule.getCalculationType())
                .ratePercent(rule.getRatePercent())
                .fixedAmount(rule.getFixedAmount())
                .minAmount(rule.getMinAmount())
                .build();
    }
}
