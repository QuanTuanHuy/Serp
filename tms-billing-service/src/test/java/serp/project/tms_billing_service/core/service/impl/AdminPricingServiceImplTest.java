package serp.project.tms_billing_service.core.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.repository.SurchargeRuleRepository;
import serp.project.tms_billing_service.repository.TariffRepository;
import serp.project.tms_billing_service.repository.VasRuleRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPricingServiceImplTest {
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private SurchargeRuleRepository surchargeRuleRepository;
    @Mock
    private VasRuleRepository vasRuleRepository;

    @InjectMocks
    private AdminPricingServiceImpl adminPricingService;

    @Test
    void shouldUpsertTariffByServiceRouteAndEffectiveDate() {
        UpsertTariffRequest request = new UpsertTariffRequest();
        request.setServiceCode(DeliveryService.HOA_TOC);
        request.setRouteTypeCode(RouteType.NOI_MIEN);
        request.setBaseWeight(2000d);
        request.setBasePrice(20000d);
        request.setStepWeight(500d);
        request.setStepPrice(4000d);
        request.setEffectiveDate(LocalDate.of(2026, 5, 20));
        request.setExpirationDate(null);

        when(tariffRepository.findByServiceCodeAndRouteTypeCodeAndEffectiveDate(
                DeliveryService.HOA_TOC,
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
}
