/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.tms_billing_service.core.service.support.ChargeableWeightService;
import serp.project.tms_billing_service.core.service.support.PricingRuleService;
import serp.project.tms_billing_service.core.service.support.RouteClassificationService;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TieuChuanPricingStrategyTest {
    @Mock
    private RouteClassificationService routeClassificationService;
    @Mock
    private ChargeableWeightService chargeableWeightService;
    @Mock
    private PricingRuleService pricingRuleService;

    @InjectMocks
    private TieuChuanPricingStrategy tieuChuanPricingStrategy;

    @Test
    void shouldCalculateTieredBasePriceByWeightAndRoute() {
        CalculateShippingFeeRequest request = new CalculateShippingFeeRequest();
        request.setServiceCode(DeliveryService.TIEU_CHUAN);
        request.setSenderWardCode("010001");
        request.setReceiverWardCode("010002");
        request.setActualWeightGram(750L);
        request.setLengthCm(20);
        request.setWidthCm(20);
        request.setHeightCm(20);

        Tariff tariff = Tariff.builder()
                .baseWeight(500d)
                .basePrice(30_000d)
                .stepWeight(500d)
                .stepPrice(3_000d)
                .build();

        when(routeClassificationService.classify("010001", "010002")).thenReturn(RouteType.NOI_TINH_LIEN_CUM);
        when(routeClassificationService.isRemoteArea("010002")).thenReturn(false);
        when(chargeableWeightService.calculate(750L, 20, 20, 20)).thenReturn(750L);
        when(pricingRuleService.getTariff(DeliveryService.TIEU_CHUAN, RouteType.NOI_TINH_LIEN_CUM)).thenReturn(tariff);

        CalculateShippingFeeResponse result = tieuChuanPricingStrategy.calculate(request);

        assertEquals(33_000L, result.getBaseFee());
        assertEquals(0L, result.getSurchargeFee());
        assertEquals(0L, result.getVasFee());
        assertEquals(33_000L, result.getTotalFee());
    }

    @Test
    void shouldCalculateRemoteFeeWithoutSpecialGoodsFee() {
        CalculateShippingFeeRequest request = new CalculateShippingFeeRequest();
        request.setServiceCode(DeliveryService.TIEU_CHUAN);
        request.setSenderWardCode("010001");
        request.setReceiverWardCode("790002");
        request.setActualWeightGram(3_100L);
        request.setLengthCm(30);
        request.setWidthCm(30);
        request.setHeightCm(30);

        Tariff tariff = Tariff.builder()
                .baseWeight(3_000d)
                .basePrice(57_000d)
                .stepWeight(500d)
                .stepPrice(5_000d)
                .build();
        SurchargeRule remoteRule = SurchargeRule.builder()
                .code(SurchargeRuleEnum.VUNG_XA)
                .name("Remote area surcharge")
                .calculationType(CalculationType.STEP_WEIGHT)
                .baseWeight(5_000d)
                .basePrice(7_000d)
                .stepWeight(500d)
                .stepPrice(500d)
                .build();

        when(routeClassificationService.classify("010001", "790002")).thenReturn(RouteType.LIEN_MIEN_DAC_BIET);
        when(routeClassificationService.isRemoteArea("790002")).thenReturn(true);
        when(chargeableWeightService.calculate(3_100L, 30, 30, 30)).thenReturn(3_100L);
        when(pricingRuleService.getTariff(DeliveryService.TIEU_CHUAN, RouteType.LIEN_MIEN)).thenReturn(tariff);
        when(pricingRuleService.getRequiredSurchargeRule(SurchargeRuleEnum.VUNG_XA)).thenReturn(remoteRule);

        CalculateShippingFeeResponse result = tieuChuanPricingStrategy.calculate(request);

        assertEquals(62_000L, result.getBaseFee());
        assertEquals(7_000L, result.getSurchargeFee());
        assertEquals(0L, result.getVasFee());
        assertEquals(69_000L, result.getTotalFee());
    }
}
