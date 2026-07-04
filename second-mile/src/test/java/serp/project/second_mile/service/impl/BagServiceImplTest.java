/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.second_mile.caller.TmsOrderClient;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.dto.request.AutoBaggingPlanRequest;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.response.AutoBaggingPlanResponse;
import serp.project.second_mile.dto.response.BagCapacitySettingsResponse;
import serp.project.second_mile.dto.response.BagResponse;
import serp.project.second_mile.dto.response.BagSuggestionResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.BagOrderRepository;
import serp.project.second_mile.repository.BagRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.service.BagCapacitySettingsService;
import serp.project.second_mile.service.TmsOrderTransitionPublisherService;
import serp.project.second_mile.service.validator.BagValidator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BagServiceImplTest {
    private static final Long TENANT_ID = 1L;
    private static final Long HUB_ID = 10L;
    private static final Long DESTINATION_HUB_ID = 11L;
    private static final String ORIGIN_POST_OFFICE_CODE = "PO-ORIGIN";
    private static final String DESTINATION_POST_OFFICE_CODE = "PO-DEST";

    @Mock
    private BagRepository bagRepository;

    @Mock
    private BagOrderRepository bagOrderRepository;

    @Mock
    private HubRepository hubRepository;

    @Mock
    private HubPostOfficeMappingRepository hubPostOfficeMappingRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private HubStaffAssignmentRepository hubStaffAssignmentRepository;

    @Mock
    private SecondMileAccessUtils secondMileAccessUtils;

    @Mock
    private TmsOrderClient tmsOrderClient;

    @Mock
    private TmsOrderTransitionPublisherService tmsOrderTransitionPublisherService;

    @Mock
    private BagCapacitySettingsService bagCapacitySettingsService;

    private BagServiceImpl service;

    @BeforeEach
    void setUp() {
        BagValidator bagValidator = new BagValidator(
                hubRepository,
                hubPostOfficeMappingRepository,
                routeRepository,
                vehicleRepository,
                hubStaffAssignmentRepository,
                secondMileAccessUtils
        );
        service = new BagServiceImpl(
                bagRepository,
                bagOrderRepository,
                hubRepository,
                hubPostOfficeMappingRepository,
                routeRepository,
                vehicleRepository,
                secondMileAccessUtils,
                tmsOrderClient,
                tmsOrderTransitionPublisherService,
                bagCapacitySettingsService,
                bagValidator
        );
    }

    @Test
    void createBagRejectsHubDestinationMatchingOriginHub() {
        CreateBagRequest request = createHubBagRequest(HUB_ID);

        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(hubRepository.findById(HUB_ID)).thenReturn(Optional.of(hub()));

        AppException exception = assertThrows(AppException.class, () -> service.createBag(request));

        assertEquals(ErrorCode.BAG_DESTINATION_INVALID, exception.getErrorCode());
        verify(bagRepository, never()).save(any(Bag.class));
    }

    @Test
    void createBagLeavesRouteAndVehicleForDistributionFlow() {
        CreateBagRequest request = createHubBagRequest(DESTINATION_HUB_ID);

        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(hubRepository.findById(HUB_ID)).thenReturn(Optional.of(hub(HUB_ID)));
        when(hubRepository.findById(DESTINATION_HUB_ID)).thenReturn(Optional.of(hub(DESTINATION_HUB_ID)));
        when(bagCapacitySettingsService.getSettingsForTenant(TENANT_ID))
                .thenReturn(new BagCapacitySettingsResponse(1L, 50.0, 0.5, 30));
        when(bagOrderRepository.findByBag_IdAndTenantId(100L, TENANT_ID)).thenReturn(List.of());
        when(bagRepository.save(any(Bag.class))).thenAnswer(invocation -> {
            Bag bag = invocation.getArgument(0);
            bag.setId(100L);
            return bag;
        });

        BagResponse response = service.createBag(request);

        assertNull(response.routeId());
        assertNull(response.vehicleId());
        verify(routeRepository, never()).findById(any());
        verify(vehicleRepository, never()).findById(any());
    }

    @Test
    void suggestBagsUsesPostOfficeTargetForSameHubDestination() {
        TmsOrderOperationView order = TmsOrderOperationView.builder()
                .id(1L)
                .orderCode("ORD-001")
                .status(OrderStatus.INBOUND_AT_ORIGIN_HUB)
                .originPostOfficeCode(ORIGIN_POST_OFFICE_CODE)
                .destinationPostOfficeCode(DESTINATION_POST_OFFICE_CODE)
                .totalWeight(1.0)
                .totalVolume(0.01)
                .tenantId(TENANT_ID)
                .build();
        Bag candidate = Bag.builder()
                .id(100L)
                .bagCode("BAG-001")
                .originHubId(HUB_ID)
                .destinationType(BagDestinationType.POST_OFFICE)
                .destinationPostOfficeCode(DESTINATION_POST_OFFICE_CODE)
                .maxWeight(50.0)
                .maxVolume(0.5)
                .maxOrders(30)
                .currentWeight(0.0)
                .currentVolume(0.0)
                .currentOrders(0)
                .status(BagStatus.CREATED)
                .tenantId(TENANT_ID)
                .build();

        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(tmsOrderClient.lookupByCodes(List.of("ORD-001"))).thenReturn(List.of(order));
        when(bagOrderRepository.existsByTmsOrderIdAndTenantId(1L, TENANT_ID)).thenReturn(false);
        when(hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(TENANT_ID, DESTINATION_POST_OFFICE_CODE))
                .thenReturn(Optional.of(mapping()));
        when(bagCapacitySettingsService.getSettingsForTenant(TENANT_ID))
                .thenReturn(new BagCapacitySettingsResponse(1L, 50.0, 0.5, 30));
        when(bagRepository.findByTenantIdAndOriginHubIdAndDestinationTypeAndDestinationPostOfficeCodeIgnoreCaseAndStatus(
                TENANT_ID,
                HUB_ID,
                BagDestinationType.POST_OFFICE,
                DESTINATION_POST_OFFICE_CODE,
                BagStatus.CREATED
        )).thenReturn(List.of(candidate));

        List<BagSuggestionResponse> suggestions = service.suggestBags("ORD-001", HUB_ID);

        assertEquals(1, suggestions.size());
        assertEquals(100L, suggestions.get(0).bagId());
        verify(bagRepository, never()).findByTenantIdAndOriginHubIdAndDestinationTypeAndDestinationHubIdAndStatus(
                TENANT_ID,
                HUB_ID,
                BagDestinationType.HUB,
                HUB_ID,
                BagStatus.CREATED
        );
    }

    @Test
    void autoPlanBagsTreatsOrderWeightAsGramsAndBagCapacityAsKilograms() {
        AutoBaggingPlanRequest request = new AutoBaggingPlanRequest(
                HUB_ID,
                BagDestinationType.POST_OFFICE,
                null,
                DESTINATION_POST_OFFICE_CODE,
                List.of("ORD-001", "ORD-002"),
                false
        );
        TmsOrderOperationView firstOrder = order(1L, "ORD-001", 25_000.0, 0.1);
        TmsOrderOperationView secondOrder = order(2L, "ORD-002", 25_000.0, 0.1);

        stubAutoPlanDependencies(firstOrder, secondOrder);

        AutoBaggingPlanResponse response = service.autoPlanBags(request);

        assertEquals(1, response.bagCount());
        assertEquals(2, response.items().getFirst().orderCodes().size());
        assertEquals(50.0, response.items().getFirst().totalWeight(), 0.001);
        assertEquals(0.2, response.items().getFirst().totalVolume(), 0.001);
    }

    @Test
    void autoPlanBagsSplitsOrdersWhenConvertedWeightExceedsBagCapacity() {
        AutoBaggingPlanRequest request = new AutoBaggingPlanRequest(
                HUB_ID,
                BagDestinationType.POST_OFFICE,
                null,
                DESTINATION_POST_OFFICE_CODE,
                List.of("ORD-001", "ORD-002"),
                false
        );
        TmsOrderOperationView firstOrder = order(1L, "ORD-001", 30_000.0, 0.1);
        TmsOrderOperationView secondOrder = order(2L, "ORD-002", 25_000.0, 0.1);

        stubAutoPlanDependencies(firstOrder, secondOrder);

        AutoBaggingPlanResponse response = service.autoPlanBags(request);

        assertEquals(2, response.bagCount());
        assertEquals(30.0, response.items().get(0).totalWeight(), 0.001);
        assertEquals(25.0, response.items().get(1).totalWeight(), 0.001);
    }

    private Hub hub() {
        return hub(HUB_ID);
    }

    private Hub hub(Long id) {
        return Hub.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .build();
    }

    private CreateBagRequest createHubBagRequest(Long destinationHubId) {
        CreateBagRequest request = new CreateBagRequest();
        request.setBagCode("BAG-001");
        request.setOriginHubId(HUB_ID);
        request.setDestinationType(BagDestinationType.HUB);
        request.setDestinationHubId(destinationHubId);
        return request;
    }

    private HubPostOfficeMapping mapping() {
        return HubPostOfficeMapping.builder()
                .hub(hub())
                .tenantId(TENANT_ID)
                .build();
    }

    private TmsOrderOperationView order(Long id, String orderCode, Double totalWeightGram, Double totalVolume) {
        return TmsOrderOperationView.builder()
                .id(id)
                .orderCode(orderCode)
                .status(OrderStatus.INBOUND_AT_ORIGIN_HUB)
                .originPostOfficeCode(ORIGIN_POST_OFFICE_CODE)
                .destinationPostOfficeCode(DESTINATION_POST_OFFICE_CODE)
                .totalWeight(totalWeightGram)
                .totalVolume(totalVolume)
                .tenantId(TENANT_ID)
                .build();
    }

    private void stubAutoPlanDependencies(TmsOrderOperationView firstOrder, TmsOrderOperationView secondOrder) {
        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(hubRepository.findById(HUB_ID)).thenReturn(Optional.of(hub()));
        when(hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(TENANT_ID, DESTINATION_POST_OFFICE_CODE))
                .thenReturn(Optional.of(mapping()));
        when(hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(TENANT_ID, ORIGIN_POST_OFFICE_CODE))
                .thenReturn(Optional.of(mapping()));
        when(tmsOrderClient.lookupByCodes(List.of(firstOrder.getOrderCode(), secondOrder.getOrderCode())))
                .thenReturn(List.of(firstOrder, secondOrder));
        when(bagCapacitySettingsService.getSettingsForTenant(TENANT_ID))
                .thenReturn(new BagCapacitySettingsResponse(1L, 50.0, 0.5, 30));
    }
}
