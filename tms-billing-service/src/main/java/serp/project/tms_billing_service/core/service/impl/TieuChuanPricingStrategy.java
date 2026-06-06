/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.impl;

import org.springframework.stereotype.Component;
import serp.project.tms_billing_service.core.service.IDeliveryPricingStrategy;
import serp.project.tms_billing_service.core.service.support.ChargeableWeightService;
import serp.project.tms_billing_service.core.service.support.PricingRuleService;
import serp.project.tms_billing_service.core.service.support.RouteClassificationService;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.domain.VasRule;
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.dto.response.FeeLineItemResponse;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.enums.VasRuleCode;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Component
public class TieuChuanPricingStrategy implements IDeliveryPricingStrategy {
    private static final long HIGH_VALUE_THRESHOLD = 3_000_000L;

    private final RouteClassificationService routeClassificationService;
    private final ChargeableWeightService chargeableWeightService;
    private final PricingRuleService pricingRuleService;

    public TieuChuanPricingStrategy(
            RouteClassificationService routeClassificationService,
            ChargeableWeightService chargeableWeightService,
            PricingRuleService pricingRuleService
    ) {
        this.routeClassificationService = routeClassificationService;
        this.chargeableWeightService = chargeableWeightService;
        this.pricingRuleService = pricingRuleService;
    }

    @Override
    public DeliveryService getSupportedService() {
        return DeliveryService.TIEU_CHUAN;
    }

    @Override
    public CalculateShippingFeeResponse calculate(CalculateShippingFeeRequest request) {
        RouteType routeType = routeClassificationService.classify(
                request.getSenderWardCode(),
                request.getReceiverWardCode()
        );
        long chargeableWeight = chargeableWeightService.calculate(
                request.getActualWeightGram(),
                request.getLengthCm(),
                request.getWidthCm(),
                request.getHeightCm()
        );

        List<FeeLineItemResponse> feeItems = new ArrayList<>();
        long baseFee = calculateBaseFreight(routeType, chargeableWeight, feeItems);
        long surchargeFee = calculateSurcharges(request, chargeableWeight, feeItems);
        long vasFee = calculateVasFees(request, feeItems);
        long totalFee = baseFee + surchargeFee + vasFee;

        return CalculateShippingFeeResponse.builder()
                .serviceCode(DeliveryService.TIEU_CHUAN)
                .routeType(routeType)
                .chargeableWeightGram(chargeableWeight)
                .baseFee(baseFee)
                .surchargeFee(surchargeFee)
                .vasFee(vasFee)
                .totalFee(totalFee)
                .feeItems(feeItems)
                .build();
    }

    private long calculateBaseFreight(RouteType routeType, long chargeableWeight, List<FeeLineItemResponse> feeItems) {
        RouteType normalizedRouteType = normalizeRouteType(routeType);
        Tariff tariff = pricingRuleService.getTariff(DeliveryService.TIEU_CHUAN, normalizedRouteType);

        long baseWeight = requiredLong(tariff.getBaseWeight(), "tariff.baseWeight");
        long basePrice = requiredLong(tariff.getBasePrice(), "tariff.basePrice");
        long stepWeight = requiredLong(tariff.getStepWeight(), "tariff.stepWeight");
        long stepPrice = requiredLong(tariff.getStepPrice(), "tariff.stepPrice");

        long amount = basePrice;
        if (chargeableWeight > baseWeight) {
            long extraWeight = chargeableWeight - baseWeight;
            long extraSteps = (long) Math.ceil((double) extraWeight / stepWeight);
            amount += extraSteps * stepPrice;
        }

        feeItems.add(FeeLineItemResponse.builder()
                .code("BASE_FREIGHT")
                .name("Cước chính tiêu chuẩn")
                .category("BASE")
                .amount(amount)
                .build());
        return amount;
    }

    private long calculateSurcharges(
            CalculateShippingFeeRequest request,
            long chargeableWeight,
            List<FeeLineItemResponse> feeItems
    ) {
        if (!routeClassificationService.isRemoteArea(request.getReceiverWardCode())) {
            return 0L;
        }

        long remoteAreaFee = calculateRemoteAreaFee(chargeableWeight);
        feeItems.add(FeeLineItemResponse.builder()
                .code(SurchargeRuleEnum.VUNG_XA.name())
                .name("Phụ phí vùng sâu vùng xa")
                .category("SURCHARGE")
                .amount(remoteAreaFee)
                .build());
        return remoteAreaFee;
    }

    private long calculateVasFees(CalculateShippingFeeRequest request, List<FeeLineItemResponse> feeItems) {
        long totalVas = 0L;

        if (request.getCodAmount() != null && request.getCodAmount() > 0) {
            feeItems.add(FeeLineItemResponse.builder()
                    .code(VasRuleCode.COD.name())
                    .name("Phí COD (miễn phí)")
                    .category("VAS")
                    .amount(0L)
                    .build());
        }

        long declaredValue = request.getDeclaredValue() == null ? 0L : request.getDeclaredValue();
        if (declaredValue > HIGH_VALUE_THRESHOLD) {
            long insuranceFee = calculateInsuranceFee(declaredValue);
            totalVas += insuranceFee;
            VasRule insuranceRule = pricingRuleService.getRequiredVasRule(VasRuleCode.BAO_HIEM);
            feeItems.add(FeeLineItemResponse.builder()
                    .code(insuranceRule.getCode().name())
                    .name(insuranceRule.getName())
                    .category("VAS")
                    .amount(insuranceFee)
                    .build());
        }

        return totalVas;
    }

    private long calculateRemoteAreaFee(long chargeableWeight) {
        SurchargeRule configuredRule = pricingRuleService.getRequiredSurchargeRule(SurchargeRuleEnum.VUNG_XA);
        return calculateStepWeightSurcharge(chargeableWeight, configuredRule);
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
        long extraSteps = (long) Math.ceil((double) extraWeight / stepWeight);
        return basePrice + (extraSteps * stepPrice);
    }

    private long calculateInsuranceFee(long declaredValue) {
        VasRule insuranceRule = pricingRuleService.getRequiredVasRule(VasRuleCode.BAO_HIEM);
        double ratePercent = requiredDouble(insuranceRule.getRatePercent(), "vas.ratePercent");
        long minAmount = requiredLong(insuranceRule.getMinAmount(), "vas.minAmount");

        long calculatedAmount = Math.round(declaredValue * (ratePercent / 100d));
        return Math.max(calculatedAmount, minAmount);
    }

    private RouteType normalizeRouteType(RouteType routeType) {
        if (routeType == RouteType.LIEN_MIEN_DAC_BIET) {
            return RouteType.LIEN_MIEN;
        }
        return routeType;
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

    private double requiredDouble(Double value, String fieldName) {
        if (value == null) {
            throw new AppException(
                    ErrorCode.BILLING_RULE_NOT_FOUND,
                    "Thiếu cấu hình trường giá: " + fieldName
            );
        }
        return value;
    }
}
