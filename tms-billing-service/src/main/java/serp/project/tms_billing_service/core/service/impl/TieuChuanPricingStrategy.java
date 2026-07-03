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
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.dto.response.FeeLineItemResponse;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Component
public class TieuChuanPricingStrategy implements IDeliveryPricingStrategy {
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

    /**
     * Trả về dịch vụ tiêu chuẩn để ShippingFeeService có thể định tuyến đúng strategy.
     */
    @Override
    public DeliveryService getSupportedService() {
        return DeliveryService.TIEU_CHUAN;
    }

    /**
     * Tính phí tiêu chuẩn theo tuyến, trọng lượng quy đổi, biểu phí và phụ phí vùng xa.
     *
     * @param request thông tin tuyến gửi/nhận và thông số kiện hàng
     * @return tổng phí và các dòng phí cấu thành
     */
    @Override
    public CalculateShippingFeeResponse calculate(CalculateShippingFeeRequest request) {
        // Loại tuyến quyết định bảng tariff, còn khối lượng tính cước quyết định bậc giá trong tariff.
        RouteType routeType = routeClassificationService.classify(
                request.getSenderWardCode(),
                request.getReceiverWardCode()
        );
        long chargeableWeight = chargeableWeightService.calculate(
                DeliveryService.TIEU_CHUAN,
                request.getActualWeightGram(),
                request.getLengthCm(),
                request.getWidthCm(),
                request.getHeightCm()
        );

        List<FeeLineItemResponse> feeItems = new ArrayList<>();
        // Tổng phí được tách thành cước chính và phụ phí để UI/API có thể giải thích từng dòng phí.
        long baseFee = calculateBaseFreight(routeType, chargeableWeight, feeItems);
        long surchargeFee = calculateSurcharges(request, chargeableWeight, feeItems);
        long totalFee = baseFee + surchargeFee;

        return CalculateShippingFeeResponse.builder()
                .serviceCode(DeliveryService.TIEU_CHUAN)
                .routeType(routeType)
                .chargeableWeightGram(chargeableWeight)
                .baseFee(baseFee)
                .surchargeFee(surchargeFee)
                .totalFee(totalFee)
                .feeItems(feeItems)
                .build();
    }

    /**
     * Tính cước chính theo biểu phí hiệu lực của loại tuyến đã chuẩn hóa.
     *
     * @param routeType loại tuyến được phân loại từ địa chỉ gửi/nhận
     * @param chargeableWeight trọng lượng tính phí sau khi quy đổi
     * @param feeItems danh sách dòng phí để bổ sung dòng cước chính
     * @return số tiền cước chính
     */
    private long calculateBaseFreight(RouteType routeType, long chargeableWeight, List<FeeLineItemResponse> feeItems) {
        RouteType normalizedRouteType = normalizeRouteType(routeType);
        Tariff tariff = pricingRuleService.getTariff(DeliveryService.TIEU_CHUAN, normalizedRouteType);

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
                .name("Cước chính tiêu chuẩn")
                .category("BASE")
                .amount(amount)
                .build());
        return amount;
    }

    /**
     * Tính các phụ phí còn được hỗ trợ cho đơn tiêu chuẩn.
     *
     * @param request thông tin tuyến nhận để kiểm tra vùng xa
     * @param chargeableWeight trọng lượng tính phí
     * @param feeItems danh sách dòng phí để bổ sung phụ phí nếu có
     * @return tổng phụ phí
     */
    private long calculateSurcharges(
            CalculateShippingFeeRequest request,
            long chargeableWeight,
            List<FeeLineItemResponse> feeItems
    ) {
        // Hiện chỉ phụ thu vùng xa, nên đơn không thuộc vùng xa sẽ không phát sinh surcharge.
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

    /**
     * Tính phụ phí vùng xa theo cấu hình STEP_WEIGHT hiện hành.
     */
    private long calculateRemoteAreaFee(long chargeableWeight) {
        SurchargeRule configuredRule = pricingRuleService.getRequiredSurchargeRule(SurchargeRuleEnum.VUNG_XA);
        return calculateStepWeightSurcharge(chargeableWeight, configuredRule);
    }

    /**
     * Áp dụng công thức phụ phí theo ngưỡng khối lượng và bước tăng.
     */
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

    /**
     * Quy đổi tuyến đặc biệt về tuyến liên miền vì bảng giá hiện chỉ cấu hình mức liên miền.
     */
    private RouteType normalizeRouteType(RouteType routeType) {
        if (routeType == RouteType.LIEN_MIEN_DAC_BIET) {
            return RouteType.LIEN_MIEN;
        }
        return routeType;
    }

    /**
     * Chuyển cấu hình số thực trong database sang số tiền/trọng lượng nguyên và báo lỗi nếu thiếu cấu hình.
     */
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
