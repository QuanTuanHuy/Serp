/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.support;

import org.junit.jupiter.api.Test;
import serp.project.tms_billing_service.domain.ChargeableWeightConfig;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.repository.ChargeableWeightConfigRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChargeableWeightServiceTest {
    private final ChargeableWeightConfigRepository chargeableWeightConfigRepository =
            mock(ChargeableWeightConfigRepository.class);
    private final ChargeableWeightService chargeableWeightService =
            new ChargeableWeightService(chargeableWeightConfigRepository);

    @Test
    void shouldUseActualWeightWhenSmallBulkyPackage() {
        givenDefaultConfig();

        long result = chargeableWeightService.calculate(DeliveryService.TIEU_CHUAN, 1200L, 20, 20, 20);

        assertEquals(1200L, result);
    }

    @Test
    void shouldUseVolumetricWeightAndRoundBy500gStep() {
        givenDefaultConfig();

        long result = chargeableWeightService.calculate(DeliveryService.TIEU_CHUAN, 2100L, 40, 30, 40);

        assertEquals(10000L, result);
    }

    @Test
    void shouldThrowExceptionWhenWeightExceedsLimit() {
        givenDefaultConfig();

        assertThrows(
                AppException.class,
                () -> chargeableWeightService.calculate(DeliveryService.TIEU_CHUAN, 15000L, 40, 40, 40)
        );
    }

    @Test
    void shouldThrowExceptionWhenServiceConfigMissing() {
        when(chargeableWeightConfigRepository.findByServiceCode(DeliveryService.TIEU_CHUAN))
                .thenReturn(Optional.empty());

        assertThrows(
                AppException.class,
                () -> chargeableWeightService.calculate(DeliveryService.TIEU_CHUAN, 1200L, 20, 20, 20)
        );
    }

    private void givenDefaultConfig() {
        ChargeableWeightConfig config = ChargeableWeightConfig.builder()
                .serviceCode(DeliveryService.TIEU_CHUAN)
                .minDimensionCm(10L)
                .smallBulkyThresholdCm(100L)
                .baseWeightGram(2_000L)
                .stepWeightGram(500L)
                .maxWeightGram(15_000L)
                .volumetricDivisor(5000d)
                .build();
        when(chargeableWeightConfigRepository.findByServiceCode(DeliveryService.TIEU_CHUAN))
                .thenReturn(Optional.of(config));
    }
}
