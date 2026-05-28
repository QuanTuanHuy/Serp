/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.domain.VasRule;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.enums.VasRuleCode;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;
import serp.project.tms_billing_service.repository.SurchargeRuleRepository;
import serp.project.tms_billing_service.repository.TariffRepository;
import serp.project.tms_billing_service.repository.VasRuleRepository;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PricingRuleService {
    private final TariffRepository tariffRepository;
    private final SurchargeRuleRepository surchargeRuleRepository;
    private final VasRuleRepository vasRuleRepository;

    public Tariff getTariff(DeliveryService serviceCode, RouteType routeType) {
        LocalDate today = LocalDate.now();
        return tariffRepository
                .findFirstByServiceCodeAndRouteTypeCodeAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqualOrderByEffectiveDateDesc(
                        serviceCode,
                        routeType,
                        today,
                        today
                )
                .or(() -> tariffRepository
                        .findFirstByServiceCodeAndRouteTypeCodeAndEffectiveDateLessThanEqualAndExpirationDateIsNullOrderByEffectiveDateDesc(
                                serviceCode,
                                routeType,
                                today
                        ))
                .orElseThrow(() -> new AppException(
                        ErrorCode.BILLING_RULE_NOT_FOUND,
                        String.format("Không có bảng giá cho service=%s, routeType=%s", serviceCode, routeType)
                ));
    }

    public SurchargeRule getRequiredSurchargeRule(SurchargeRuleEnum code) {
        LocalDate today = LocalDate.now();
        return surchargeRuleRepository
                .findFirstByCodeAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqualOrderByEffectiveDateDesc(
                        code,
                        today,
                        today
                )
                .or(() -> surchargeRuleRepository
                        .findFirstByCodeAndEffectiveDateLessThanEqualAndExpirationDateIsNullOrderByEffectiveDateDesc(
                                code,
                                today
                        ))
                .or(() -> surchargeRuleRepository.findByCode(code))
                .orElseThrow(() -> new AppException(
                        ErrorCode.BILLING_RULE_NOT_FOUND,
                        "Không có cấu hình phụ phí cho mã: " + code
                ));
    }

    public VasRule getRequiredVasRule(VasRuleCode code) {
        return vasRuleRepository.findByCode(code)
                .orElseThrow(() -> new AppException(
                        ErrorCode.BILLING_RULE_NOT_FOUND,
                        "Không có cấu hình VAS cho mã: " + code
                ));
    }

    public Optional<SurchargeRule> findSurchargeRule(SurchargeRuleEnum code) {
        LocalDate today = LocalDate.now();
        return surchargeRuleRepository
                .findFirstByCodeAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqualOrderByEffectiveDateDesc(
                        code,
                        today,
                        today
                )
                .or(() -> surchargeRuleRepository
                        .findFirstByCodeAndEffectiveDateLessThanEqualAndExpirationDateIsNullOrderByEffectiveDateDesc(
                                code,
                                today
                        ))
                .or(() -> surchargeRuleRepository.findByCode(code));
    }
}
