/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.dto.request.admin.UpsertSurchargeRuleRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.response.admin.SurchargeRuleAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;
import serp.project.tms_billing_service.enums.CalculationType;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.repository.SurchargeRuleRepository;
import serp.project.tms_billing_service.repository.TariffRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPricingServiceImplTest {
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private SurchargeRuleRepository surchargeRuleRepository;

    @InjectMocks
    private AdminPricingServiceImpl adminPricingService;

    @Test
    void shouldUpsertTariffByServiceRouteAndEffectiveDate() {
        UpsertTariffRequest request = new UpsertTariffRequest();
        request.setServiceCode(DeliveryService.TIEU_CHUAN);
        request.setRouteTypeCode(RouteType.NOI_MIEN);
        request.setBaseWeight(2000d);
        request.setBasePrice(20000d);
        request.setStepWeight(500d);
        request.setStepPrice(4000d);
        request.setEffectiveDate(LocalDate.of(2026, 5, 20));
        request.setExpirationDate(null);

        when(tariffRepository.findByServiceCodeAndRouteTypeCodeAndEffectiveDate(
                DeliveryService.TIEU_CHUAN,
                RouteType.NOI_MIEN,
                LocalDate.of(2026, 5, 20)
        )).thenReturn(Optional.empty());

        when(tariffRepository.save(org.mockito.ArgumentMatchers.any(Tariff.class)))
                .thenAnswer(invocation -> {
                    Tariff tariff = invocation.getArgument(0);
                    tariff.setId(1L);
                    return tariff;
                });

        TariffAdminResponse response = adminPricingService.upsertTariff(request);

        ArgumentCaptor<Tariff> captor = ArgumentCaptor.forClass(Tariff.class);
        verify(tariffRepository).save(captor.capture());
        Tariff savedTariff = captor.getValue();

        assertEquals(20000d, savedTariff.getBasePrice());
        assertEquals(4000d, savedTariff.getStepPrice());
        assertEquals(1L, response.getId());
        assertEquals(RouteType.NOI_MIEN, response.getRouteTypeCode());
    }

    @Test
    void shouldListOnlyActiveSurchargeRules() {
        SurchargeRule remoteRule = SurchargeRule.builder()
                .id(1L)
                .code(SurchargeRuleEnum.VUNG_XA)
                .name("Remote area surcharge")
                .calculationType(CalculationType.STEP_WEIGHT)
                .build();
        SurchargeRule legacySpecialGoodsRule = SurchargeRule.builder()
                .id(2L)
                .code(SurchargeRuleEnum.QUA_KHO)
                .name("Oversized goods surcharge")
                .calculationType(CalculationType.FIXED_PER_KG)
                .build();

        when(surchargeRuleRepository.findAll()).thenReturn(List.of(remoteRule, legacySpecialGoodsRule));

        List<SurchargeRuleAdminResponse> response = adminPricingService.listSurchargeRules();

        assertEquals(1, response.size());
        assertEquals(SurchargeRuleEnum.VUNG_XA, response.getFirst().getCode());
    }

    @Test
    void shouldRejectUnsupportedSpecialGoodsSurchargeRule() {
        UpsertSurchargeRuleRequest request = new UpsertSurchargeRuleRequest();
        request.setCode(SurchargeRuleEnum.QUA_KHO);
        request.setName("Oversized goods surcharge");
        request.setCalculationType(CalculationType.FIXED_PER_KG);

        assertThrows(AppException.class, () -> adminPricingService.upsertSurchargeRule(request));
    }
}
