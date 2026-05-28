package serp.project.tms_billing_service.core.service.support;

import org.junit.jupiter.api.Test;
import serp.project.tms_billing_service.exception.AppException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChargeableWeightServiceTest {
    private final ChargeableWeightService chargeableWeightService = new ChargeableWeightService();

    @Test
    void shouldUseActualWeightWhenSmallBulkyPackage() {
        long result = chargeableWeightService.calculate(1200L, 20, 20, 20);

        assertEquals(1200L, result);
    }

    @Test
    void shouldUseVolumetricWeightAndRoundBy500gStep() {
        long result = chargeableWeightService.calculate(2100L, 40, 30, 40);

        assertEquals(10000L, result);
    }

    @Test
    void shouldThrowExceptionWhenWeightExceedsLimit() {
        assertThrows(AppException.class, () -> chargeableWeightService.calculate(15000L, 40, 40, 40));
    }
}
