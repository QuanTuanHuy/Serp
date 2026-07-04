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
import serp.project.tms_billing_service.domain.DeliveryServiceConfig;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.ProductCategory;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.repository.DeliveryServiceConfigRepository;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguredDeliveryPricingCalculatorTest {
    private static final String TIEU_CHUAN = "TIEU_CHUAN";
    private static final String HOA_TOC = "HOA_TOC";

    @Mock
    private RouteClassificationService routeClassificationService;
    @Mock
    private ChargeableWeightService chargeableWeightService;
    @Mock
    private PricingRuleService pricingRuleService;
    @Mock
    private DeliveryServiceConfigRepository deliveryServiceConfigRepository;

    @InjectMocks
    private ConfiguredDeliveryPricingCalculator pricingCalculator;

    @Test
    void shouldCalculateTieredBasePriceByWeightAndRoute() {
        CalculateShippingFeeRequest request = new CalculateShippingFeeRequest();
        request.setServiceCode(TIEU_CHUAN);
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

        givenActiveService(TIEU_CHUAN);
        when(routeClassificationService.classify("010001", "010002")).thenReturn(RouteType.NOI_TINH_LIEN_CUM);
        when(routeClassificationService.isRemoteArea("010002")).thenReturn(false);
        when(chargeableWeightService.calculate(TIEU_CHUAN, 750L, 20, 20, 20)).thenReturn(750L);
        when(pricingRuleService.getTariff(TIEU_CHUAN, RouteType.NOI_TINH_LIEN_CUM)).thenReturn(tariff);

        CalculateShippingFeeResponse result = pricingCalculator.calculate(request);

        assertEquals(TIEU_CHUAN, result.getServiceCode());
        assertEquals(33_000L, result.getBaseFee());
        assertEquals(0L, result.getSurchargeFee());
        assertEquals(33_000L, result.getTotalFee());
    }

    @Test
    void shouldUseRequestServiceCodeWhenLoadingPricingConfig() {
        CalculateShippingFeeRequest request = new CalculateShippingFeeRequest();
        request.setServiceCode(HOA_TOC);
        request.setSenderWardCode("010001");
        request.setReceiverWardCode("010002");
        request.setActualWeightGram(1_200L);
        request.setLengthCm(20);
        request.setWidthCm(20);
        request.setHeightCm(20);

        Tariff expressTariff = Tariff.builder()
                .baseWeight(1000d)
                .basePrice(45_000d)
                .stepWeight(500d)
                .stepPrice(8_000d)
                .build();

        givenActiveService(HOA_TOC);
        when(routeClassificationService.classify("010001", "010002")).thenReturn(RouteType.NOI_TINH_LIEN_CUM);
        when(routeClassificationService.isRemoteArea("010002")).thenReturn(false);
        when(chargeableWeightService.calculate(HOA_TOC, 1_200L, 20, 20, 20)).thenReturn(1_200L);
        when(pricingRuleService.getTariff(HOA_TOC, RouteType.NOI_TINH_LIEN_CUM)).thenReturn(expressTariff);

        CalculateShippingFeeResponse result = pricingCalculator.calculate(request);

        assertEquals(HOA_TOC, result.getServiceCode());
        assertEquals(53_000L, result.getBaseFee());
        assertEquals(53_000L, result.getTotalFee());
    }

    @Test
    void shouldUseSpecialInterRegionTariffAndConfiguredRemoteFeeType() {
        CalculateShippingFeeRequest request = new CalculateShippingFeeRequest();
        request.setServiceCode(TIEU_CHUAN);
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
                .calculationType(CalculationType.FIXED_PER_KG)
                .fixedAmount(2_000d)
                .build();

        givenActiveService(TIEU_CHUAN);
        when(routeClassificationService.classify("010001", "790002")).thenReturn(RouteType.LIEN_MIEN_DAC_BIET);
        when(routeClassificationService.isRemoteArea("790002")).thenReturn(true);
        when(chargeableWeightService.calculate(TIEU_CHUAN, 3_100L, 30, 30, 30)).thenReturn(3_100L);
        when(pricingRuleService.getTariff(TIEU_CHUAN, RouteType.LIEN_MIEN_DAC_BIET)).thenReturn(tariff);
        when(pricingRuleService.getRequiredSurchargeRule(SurchargeRuleEnum.VUNG_XA)).thenReturn(remoteRule);

        CalculateShippingFeeResponse result = pricingCalculator.calculate(request);

        assertEquals(62_000L, result.getBaseFee());
        assertEquals(8_000L, result.getSurchargeFee());
        assertEquals(70_000L, result.getTotalFee());
        verify(pricingRuleService).getTariff(TIEU_CHUAN, RouteType.LIEN_MIEN_DAC_BIET);
    }

    @Test
    void shouldCalculateProductCategorySurchargeByConfiguredType() {
        CalculateShippingFeeRequest request = new CalculateShippingFeeRequest();
        request.setServiceCode(TIEU_CHUAN);
        request.setSenderWardCode("010001");
        request.setReceiverWardCode("010002");
        request.setActualWeightGram(2_000L);
        request.setLengthCm(20);
        request.setWidthCm(20);
        request.setHeightCm(20);
        request.setProductCategory(ProductCategory.FRAGILE);

        Tariff tariff = Tariff.builder()
                .baseWeight(3_000d)
                .basePrice(50_000d)
                .stepWeight(500d)
                .stepPrice(5_000d)
                .build();
        SurchargeRule fragileRule = SurchargeRule.builder()
                .code(SurchargeRuleEnum.DE_VO)
                .name("Fragile goods surcharge")
                .calculationType(CalculationType.PERCENTAGE)
                .ratePercent(10d)
                .minAmount(8_000d)
                .build();

        givenActiveService(TIEU_CHUAN);
        when(routeClassificationService.classify("010001", "010002")).thenReturn(RouteType.NOI_TINH_LIEN_CUM);
        when(routeClassificationService.isRemoteArea("010002")).thenReturn(false);
        when(chargeableWeightService.calculate(TIEU_CHUAN, 2_000L, 20, 20, 20)).thenReturn(2_000L);
        when(pricingRuleService.getTariff(TIEU_CHUAN, RouteType.NOI_TINH_LIEN_CUM)).thenReturn(tariff);
        when(pricingRuleService.getRequiredSurchargeRule(SurchargeRuleEnum.DE_VO)).thenReturn(fragileRule);

        CalculateShippingFeeResponse result = pricingCalculator.calculate(request);

        assertEquals(50_000L, result.getBaseFee());
        assertEquals(8_000L, result.getSurchargeFee());
        assertEquals(58_000L, result.getTotalFee());
    }

    @Test
    void shouldCalculateImportantDocumentSurchargePerShipment() {
        CalculateShippingFeeRequest request = new CalculateShippingFeeRequest();
        request.setServiceCode(TIEU_CHUAN);
        request.setSenderWardCode("010001");
        request.setReceiverWardCode("010002");
        request.setActualWeightGram(1_000L);
        request.setLengthCm(20);
        request.setWidthCm(20);
        request.setHeightCm(20);
        request.setProductCategory(ProductCategory.IMPORTANT_DOCUMENT);

        Tariff tariff = Tariff.builder()
                .baseWeight(3_000d)
                .basePrice(30_000d)
                .stepWeight(500d)
                .stepPrice(5_000d)
                .build();
        SurchargeRule documentRule = SurchargeRule.builder()
                .code(SurchargeRuleEnum.CHUNG_TU_QUAN_TRONG)
                .name("Important document surcharge")
                .calculationType(CalculationType.FIXED_PER_ORDER)
                .fixedAmount(5_000d)
                .build();

        givenActiveService(TIEU_CHUAN);
        when(routeClassificationService.classify("010001", "010002")).thenReturn(RouteType.NOI_TINH_LIEN_CUM);
        when(routeClassificationService.isRemoteArea("010002")).thenReturn(false);
        when(chargeableWeightService.calculate(TIEU_CHUAN, 1_000L, 20, 20, 20)).thenReturn(1_000L);
        when(pricingRuleService.getTariff(TIEU_CHUAN, RouteType.NOI_TINH_LIEN_CUM)).thenReturn(tariff);
        when(pricingRuleService.getRequiredSurchargeRule(SurchargeRuleEnum.CHUNG_TU_QUAN_TRONG))
                .thenReturn(documentRule);

        CalculateShippingFeeResponse result = pricingCalculator.calculate(request);

        assertEquals(30_000L, result.getBaseFee());
        assertEquals(5_000L, result.getSurchargeFee());
        assertEquals(35_000L, result.getTotalFee());
    }

    private void givenActiveService(String serviceCode) {
        DeliveryServiceConfig config = DeliveryServiceConfig.builder()
                .serviceCode(serviceCode)
                .name(serviceCode)
                .active(true)
                .sortOrder(10)
                .build();
        when(deliveryServiceConfigRepository.findByServiceCode(serviceCode)).thenReturn(Optional.of(config));
    }
}
