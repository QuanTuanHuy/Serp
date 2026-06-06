/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.support;

import org.springframework.stereotype.Component;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;

@Component
public class ChargeableWeightService {
    private static final long MIN_DIMENSION_CM = 10L;
    private static final long SMALL_BULKY_THRESHOLD_CM = 100L;
    private static final long BASE_WEIGHT_GRAM = 2_000L;
    private static final long STEP_WEIGHT_GRAM = 500L;
    private static final long MAX_WEIGHT_GRAM = 15_000L;
    private static final double VOLUMETRIC_DIVISOR = 5000d;

    public long calculate(long actualWeightGram, int lengthCm, int widthCm, int heightCm) {
        long normalizedLength = normalizeDimension(lengthCm);
        long normalizedWidth = normalizeDimension(widthCm);
        long normalizedHeight = normalizeDimension(heightCm);

        long chargeableWeight = actualWeightGram;
        if (normalizedLength + normalizedWidth + normalizedHeight >= SMALL_BULKY_THRESHOLD_CM) {
            long volumetricWeightGram = Math.round(
                    ((normalizedLength * normalizedWidth * normalizedHeight) / VOLUMETRIC_DIVISOR) * 1000
            );
            chargeableWeight = Math.max(actualWeightGram, volumetricWeightGram);
        }

        if (chargeableWeight >= MAX_WEIGHT_GRAM) {
            throw new AppException(
                    ErrorCode.WEIGHT_LIMIT_EXCEEDED,
                    String.format("Hỏa tốc chỉ hỗ trợ kiện hàng dưới 15000 gram, nhận vào %d gram.", chargeableWeight)
            );
        }

        return roundWeight(chargeableWeight);
    }

    private long normalizeDimension(long value) {
        return Math.max(value, MIN_DIMENSION_CM);
    }

    private long roundWeight(long weightGram) {
        if (weightGram <= BASE_WEIGHT_GRAM) {
            return weightGram;
        }

        long extra = weightGram - BASE_WEIGHT_GRAM;
        long roundedExtraSteps = (long) Math.ceil((double) extra / STEP_WEIGHT_GRAM);
        return BASE_WEIGHT_GRAM + (roundedExtraSteps * STEP_WEIGHT_GRAM);
    }
}
