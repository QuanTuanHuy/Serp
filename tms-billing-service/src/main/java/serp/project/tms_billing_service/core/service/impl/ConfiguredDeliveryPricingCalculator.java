/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.tms_billing_service.core.service.support.ChargeableWeightService;
import serp.project.tms_billing_service.core.service.support.PricingRuleService;
import serp.project.tms_billing_service.core.service.support.RouteClassificationService;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.dto.response.FeeLineItemResponse;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.ProductCategory;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;
import serp.project.tms_billing_service.repository.DeliveryServiceConfigRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ConfiguredDeliveryPricingCalculator {
    private final RouteClassificationService routeClassificationService;
    private final ChargeableWeightService chargeableWeightService;
    private final PricingRuleService pricingRuleService;
    private final DeliveryServiceConfigRepository deliveryServiceConfigRepository;

    /**
     * Tính phí bằng công thức chung, còn dữ liệu tariff và cấu hình cân nặng được chọn theo serviceCode.
     */
    public CalculateShippingFeeResponse calculate(CalculateShippingFeeRequest request) {
        String serviceCode = request.getServiceCode().trim().toUpperCase();
        deliveryServiceConfigRepository.findByServiceCode(serviceCode)
                .filter(config -> Boolean.TRUE.equals(config.getActive()))
                .orElseThrow(() -> new AppException(
                        ErrorCode.BILLING_RULE_NOT_FOUND,
                        "Dịch vụ vận chuyển chưa được kích hoạt: " + serviceCode
                ));

        // Loại tuyến quyết định bảng tariff, còn khối lượng tính cước quyết định bậc giá trong tariff.
        RouteType routeType = routeClassificationService.classify(
                request.getSenderWardCode(),
                request.getReceiverWardCode()
        );
        long chargeableWeight = chargeableWeightService.calculate(
                serviceCode,
                request.getActualWeightGram(),
                request.getLengthCm(),
                request.getWidthCm(),
                request.getHeightCm()
        );

        List<FeeLineItemResponse> feeItems = new ArrayList<>();
        // Tổng phí được tách thành cước chính và phụ phí để UI/API có thể giải thích từng dòng phí.
        long baseFee = calculateBaseFreight(serviceCode, routeType, chargeableWeight, feeItems);
        long surchargeFee = calculateSurcharges(request, chargeableWeight, baseFee, feeItems);
        long totalFee = baseFee + surchargeFee;

        return CalculateShippingFeeResponse.builder()
                .serviceCode(serviceCode)
                .routeType(routeType)
                .chargeableWeightGram(chargeableWeight)
                .baseFee(baseFee)
                .surchargeFee(surchargeFee)
                .totalFee(totalFee)
                .feeItems(feeItems)
                .build();
    }

    private long calculateBaseFreight(
            String serviceCode,
            RouteType routeType,
            long chargeableWeight,
            List<FeeLineItemResponse> feeItems
    ) {
        Tariff tariff = pricingRuleService.getTariff(serviceCode, routeType);

        long baseWeight = requiredLong(tariff.getBaseWeight(), "tariff.baseWeight");
        long basePrice = requiredLong(tariff.getBasePrice(), "tariff.basePrice");
        long stepWeight = requiredLong(tariff.getStepWeight(), "tariff.stepWeight");
        long stepPrice = requiredLong(tariff.getStepPrice(), "tariff.stepPrice");

        // Tariff luôn có giá cơ bản cho khối lượng đầu tiên; phần vượt mới tính thêm theo nấc.
        long amount = basePrice;
        if (chargeableWeight > baseWeight) {
            long extraWeight = chargeableWeight - baseWeight;
            // Làm tròn lên số nấc để mọi phần lẻ vượt ngưỡng đều được tính một nấc đầy đủ.
            long extraSteps = (long) Math.ceil((double) extraWeight / stepWeight);
            amount += extraSteps * stepPrice;
        }

        feeItems.add(FeeLineItemResponse.builder()
                .code("BASE_FREIGHT")
                .name("Cước chính " + serviceCode)
                .category("BASE")
                .amount(amount)
                .build());
        return amount;
    }

    private long calculateSurcharges(
            CalculateShippingFeeRequest request,
            long chargeableWeight,
            long baseFee,
            List<FeeLineItemResponse> feeItems
    ) {
        Set<SurchargeRuleEnum> surchargeCodes = resolveSurchargeCodes(request);
        long totalSurchargeFee = 0L;

        for (SurchargeRuleEnum code : surchargeCodes) {
            SurchargeRule rule = pricingRuleService.getRequiredSurchargeRule(code);
            long amount = calculateSurchargeAmount(chargeableWeight, baseFee, rule);
            feeItems.add(FeeLineItemResponse.builder()
                    .code(code.name())
                    .name(rule.getName())
                    .category("SURCHARGE")
                    .amount(amount)
                    .build());
            totalSurchargeFee += amount;
        }

        return totalSurchargeFee;
    }

    private Set<SurchargeRuleEnum> resolveSurchargeCodes(CalculateShippingFeeRequest request) {
        Set<SurchargeRuleEnum> surchargeCodes = new LinkedHashSet<>();
        if (routeClassificationService.isRemoteArea(request.getReceiverWardCode())) {
            surchargeCodes.add(SurchargeRuleEnum.VUNG_XA);
        }
        SurchargeRuleEnum categorySurchargeCode = resolveProductCategorySurchargeCode(request.getProductCategory());
        if (categorySurchargeCode != null) {
            surchargeCodes.add(categorySurchargeCode);
        }
        if (request.getSurchargeRuleCodes() != null) {
            surchargeCodes.addAll(request.getSurchargeRuleCodes());
        }
        return surchargeCodes;
    }

    private SurchargeRuleEnum resolveProductCategorySurchargeCode(ProductCategory productCategory) {
        if (productCategory == null) {
            return null;
        }
        return switch (productCategory) {
            case HIGH_VALUE -> SurchargeRuleEnum.HANG_GIA_TRI_CAO;
            case FRAGILE -> SurchargeRuleEnum.DE_VO;
            case IMPORTANT_DOCUMENT -> SurchargeRuleEnum.CHUNG_TU_QUAN_TRONG;
            case OVERSIZED -> SurchargeRuleEnum.QUA_KHO;
            case LIQUID -> SurchargeRuleEnum.CHAT_LONG;
            case SOLID, MAGNETIC_BATTERY -> null;
        };
    }

    private long calculateSurchargeAmount(long chargeableWeight, long baseFee, SurchargeRule rule) {
        CalculationType calculationType = rule.getCalculationType();
        if (calculationType == null) {
            throw new AppException(
                    ErrorCode.BILLING_RULE_NOT_FOUND,
                    "Thiếu cấu hình cách tính phụ phí cho mã: " + rule.getCode()
            );
        }

        long amount = switch (calculationType) {
            case FIXED_PER_ORDER -> requiredLong(rule.getFixedAmount(), "surcharge.fixedAmount");
            case FIXED_PER_KG -> calculateFixedPerKgSurcharge(chargeableWeight, rule);
            case PERCENTAGE -> calculatePercentageSurcharge(baseFee, rule);
            case STEP_WEIGHT -> calculateStepWeightSurcharge(chargeableWeight, rule);
        };

        if (rule.getMinAmount() == null) {
            return amount;
        }
        return Math.max(amount, requiredLong(rule.getMinAmount(), "surcharge.minAmount"));
    }

    private long calculateFixedPerKgSurcharge(long chargeableWeight, SurchargeRule rule) {
        long fixedAmount = requiredLong(rule.getFixedAmount(), "surcharge.fixedAmount");
        long chargedKg = (long) Math.ceil((double) chargeableWeight / 1000);
        return chargedKg * fixedAmount;
    }

    private long calculatePercentageSurcharge(long baseFee, SurchargeRule rule) {
        Double ratePercent = rule.getRatePercent();
        if (ratePercent == null) {
            throw new AppException(
                    ErrorCode.BILLING_RULE_NOT_FOUND,
                    "Thiếu cấu hình trường giá: surcharge.ratePercent"
            );
        }
        return Math.round(baseFee * ratePercent / 100);
    }

    private long calculateStepWeightSurcharge(long chargeableWeight, SurchargeRule rule) {
        long baseWeight = requiredLong(rule.getBaseWeight(), "surcharge.baseWeight");
        long basePrice = requiredLong(rule.getBasePrice(), "surcharge.basePrice");
        long stepWeight = requiredLong(rule.getStepWeight(), "surcharge.stepWeight");
        long stepPrice = requiredLong(rule.getStepPrice(), "surcharge.stepPrice");

        if (chargeableWeight <= baseWeight) {
            return basePrice;
        }

        long extraWeight = chargeableWeight - baseWeight;
        // Quy tắc STEP_WEIGHT của phụ phí dùng cùng nguyên tắc làm tròn lên như cước chính.
        long extraSteps = (long) Math.ceil((double) extraWeight / stepWeight);
        return basePrice + (extraSteps * stepPrice);
    }

    private long requiredLong(Double value, String fieldName) {
        if (value == null) {
            throw new AppException(
                    ErrorCode.BILLING_RULE_NOT_FOUND,
                    "Thiếu cấu hình trường giá: " + fieldName
            );
        }
        return Math.round(value);
    }
}
