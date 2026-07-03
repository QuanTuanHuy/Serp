/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.tms_billing_service.domain.ChargeableWeightConfig;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;
import serp.project.tms_billing_service.repository.ChargeableWeightConfigRepository;

@Component
@RequiredArgsConstructor
public class ChargeableWeightService {
    private final ChargeableWeightConfigRepository chargeableWeightConfigRepository;

    public long calculate(
            DeliveryService serviceCode,
            long actualWeightGram,
            int lengthCm,
            int widthCm,
            int heightCm
    ) {
        ChargeableWeightConfig config = chargeableWeightConfigRepository.findByServiceCode(serviceCode)
                .orElseThrow(() -> new AppException(
                        ErrorCode.BILLING_RULE_NOT_FOUND,
                        "Không có cấu hình khối lượng tính cước cho service=" + serviceCode
                ));

        // Mỗi chiều được nâng lên ngưỡng tối thiểu để tránh kiện hàng quá mỏng làm giảm khối lượng thể tích.
        long normalizedLength = normalizeDimension(lengthCm, config);
        long normalizedWidth = normalizeDimension(widthCm, config);
        long normalizedHeight = normalizeDimension(heightCm, config);

        long chargeableWeight = actualWeightGram;
        // Chỉ áp dụng khối lượng thể tích khi tổng ba chiều đạt ngưỡng hàng cồng kềnh của dịch vụ.
        if (normalizedLength + normalizedWidth + normalizedHeight >= config.getSmallBulkyThresholdCm()) {
            long volumetricWeightGram = Math.round(
                    ((normalizedLength * normalizedWidth * normalizedHeight) / config.getVolumetricDivisor()) * 1000
            );
            chargeableWeight = Math.max(actualWeightGram, volumetricWeightGram);
        }

        // Giới hạn được kiểm tra trên khối lượng tính cước trước khi làm tròn theo nấc.
        if (chargeableWeight >= config.getMaxWeightGram()) {
            throw new AppException(
                    ErrorCode.WEIGHT_LIMIT_EXCEEDED,
                    String.format(
                            "Dịch vụ vận chuyển %s chỉ hỗ trợ kiện hàng dưới %d gram, nhận vào %d gram.",
                            serviceCode,
                            config.getMaxWeightGram(),
                            chargeableWeight
                    )
            );
        }

        return roundWeight(chargeableWeight, config);
    }

    private long normalizeDimension(long value, ChargeableWeightConfig config) {
        return Math.max(value, config.getMinDimensionCm());
    }

    private long roundWeight(long weightGram, ChargeableWeightConfig config) {
        if (weightGram <= config.getBaseWeightGram()) {
            return weightGram;
        }

        // Phần vượt khối lượng gốc luôn làm tròn lên để không bỏ sót nấc tính phí lẻ.
        long extra = weightGram - config.getBaseWeightGram();
        long roundedExtraSteps = (long) Math.ceil((double) extra / config.getStepWeightGram());
        return config.getBaseWeightGram() + (roundedExtraSteps * config.getStepWeightGram());
    }
}
