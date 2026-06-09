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
import serp.project.tms_billing_service.domain.VasRule;
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.enums.VasRuleCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoaTocPricingStrategyTest {
    @Mock
    private RouteClassificationService routeClassificationService;
    @Mock
    private ChargeableWeightService chargeableWeightService;
    @Mock
    private PricingRuleService pricingRuleService;

    @InjectMocks
    private HoaTocPricingStrategy hoaTocPricingStrategy;

    @Test
    void shouldCalculateHoaTocShippingFeeWithBreakdown() {
        CalculateShippingFeeRequest request = new CalculateShippingFeeRequest();
        request.setServiceCode(DeliveryService.HOA_TOC);
        request.setSenderWardCode("010001");
        request.setReceiverWardCode("790002");
        request.setActualWeightGram(2200L);
        request.setLengthCm(130);
        request.setWidthCm(20);
        request.setHeightCm(20);
        request.setCodAmount(100_000L);
        request.setDeclaredValue(4_000_000L);

        Tariff tariff = Tariff.builder()
                .baseWeight(2000d)
                .basePrice(20000d)
                .stepWeight(500d)
                .stepPrice(4000d)
                .build();
        SurchargeRule remoteRule = SurchargeRule.builder()
                .code(SurchargeRuleEnum.VUNG_XA)
                .name("Phụ phí vùng sâu vùng xa")
                .calculationType(CalculationType.STEP_WEIGHT)
                .baseWeight(5000d)
                .basePrice(7000d)
                .stepWeight(500d)
                .stepPrice(500d)
                .build();
        SurchargeRule oversizeRule = SurchargeRule.builder()
                .code(SurchargeRuleEnum.QUA_KHO)
                .name("Phụ phí hàng quá khổ")
                .calculationType(CalculationType.FIXED_PER_KG)
                .fixedAmount(2000d)
                .build();
        VasRule insuranceRule = VasRule.builder()
                .code(VasRuleCode.BAO_HIEM)
                .name("Phí bảo hiểm hàng giá trị cao")
                .calculationType(CalculationType.PERCENTAGE)
                .ratePercent(0.5d)
                .minAmount(5000d)
                .build();

        when(routeClassificationService.classify("010001", "790002")).thenReturn(RouteType.NOI_MIEN);
        when(routeClassificationService.isRemoteArea("790002")).thenReturn(true);
        when(chargeableWeightService.calculate(2200L, 130, 20, 20)).thenReturn(2500L);
        when(pricingRuleService.getTariff(DeliveryService.HOA_TOC, RouteType.NOI_MIEN)).thenReturn(tariff);
        when(pricingRuleService.getRequiredSurchargeRule(SurchargeRuleEnum.VUNG_XA)).thenReturn(remoteRule);
        when(pricingRuleService.getRequiredSurchargeRule(SurchargeRuleEnum.QUA_KHO)).thenReturn(oversizeRule);
        when(pricingRuleService.getRequiredVasRule(VasRuleCode.BAO_HIEM)).thenReturn(insuranceRule);

        CalculateShippingFeeResponse result = hoaTocPricingStrategy.calculate(request);

        assertEquals(24_000L, result.getBaseFee());
        assertEquals(12_000L, result.getSurchargeFee());
        assertEquals(20_000L, result.getVasFee());
        assertEquals(56_000L, result.getTotalFee());
        assertEquals(5, result.getFeeItems().size());
    }
}
