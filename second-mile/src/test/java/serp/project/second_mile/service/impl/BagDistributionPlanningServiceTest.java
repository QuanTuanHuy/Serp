/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.dto.request.AutoPlanBagDistributionRequest;
import serp.project.second_mile.dto.response.BagDistributionPlanItemResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.BagDistributionManifestBagRepository;
import serp.project.second_mile.repository.BagDistributionManifestRepository;
import serp.project.second_mile.repository.BagRepository;
import serp.project.second_mile.repository.HandoverManifestRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BagDistributionPlanningServiceTest {
    private static final Long TENANT_ID = 1L;
    private static final Long ORIGIN_HUB_ID = 10L;
    private static final Long DESTINATION_HUB_ID = 11L;
    private static final Long BAG_ID = 50L;
    private static final Long OTHER_BAG_ID = 51L;
    private static final LocalDateTime DEPARTURE_AT = LocalDateTime.of(2026, 6, 6, 9, 0);
    private static final LocalDateTime ARRIVAL_AT = LocalDateTime.of(2026, 6, 6, 12, 0);

    @Mock
    private BagRepository bagRepository;

    @Mock
    private BagDistributionManifestRepository manifestRepository;

    @Mock
    private BagDistributionManifestBagRepository manifestBagRepository;

    @Mock
    private HandoverManifestRepository handoverManifestRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private HubStaffAssignmentRepository hubStaffAssignmentRepository;

    @Mock
    private SecondMileAccessUtils secondMileAccessUtils;

    @InjectMocks
    private BagDistributionPlanningService service;

    @Test
    void planReturnsNoRouteHintForSealedDestinationGroupWithoutRoute() {
        Bag sealedBag = Bag.builder()
                .id(BAG_ID)
                .bagCode("BAG-001")
                .originHubId(ORIGIN_HUB_ID)
                .destinationType(BagDestinationType.HUB)
                .destinationHubId(DESTINATION_HUB_ID)
                .currentWeight(10.0)
                .currentVolume(0.5)
                .currentOrders(1)
                .status(BagStatus.SEALED)
                .sealedAt(DEPARTURE_AT.minusHours(30))
                .tenantId(TENANT_ID)
                .build();

        when(bagRepository.findByTenantIdAndOriginHubIdAndStatusIn(
                TENANT_ID,
                ORIGIN_HUB_ID,
                List.of(BagStatus.SEALED, BagStatus.ARRIVED)
        ))
                .thenReturn(List.of(sealedBag));
        when(manifestBagRepository.findActiveBagIds(
                eq(TENANT_ID),
                eq(List.of(BAG_ID)),
                anyCollection()
        )).thenReturn(List.of());
        when(routeRepository.findByTenantIdAndStatusAndOriginTypeAndOriginHubIdAndDestinationTypeAndDestinationHubId(
                TENANT_ID,
                RouteStatus.ACTIVE,
                RouteEndpointType.HUB,
                ORIGIN_HUB_ID,
                RouteDestinationType.HUB,
                DESTINATION_HUB_ID
        )).thenReturn(List.of());

        List<BagDistributionPlanItemResponse> items = service.plan(TENANT_ID, new AutoPlanBagDistributionRequest(
                ORIGIN_HUB_ID,
                BagDestinationType.HUB,
                DESTINATION_HUB_ID,
                null,
                DEPARTURE_AT,
                ARRIVAL_AT,
                24,
                List.of(BAG_ID),
                false,
                null
        ));

        assertEquals(1, items.size());
        assertTrue(items.get(0).hints().contains("NO_ROUTE"));
        assertTrue(service.hasBlockingHint(items.get(0).hints()));
    }

    @Test
    void planIncludesArrivedBagsAtCurrentHub() {
        Bag arrivedBag = Bag.builder()
                .id(BAG_ID)
                .bagCode("BAG-ARRIVED-001")
                .originHubId(ORIGIN_HUB_ID)
                .destinationType(BagDestinationType.HUB)
                .destinationHubId(DESTINATION_HUB_ID)
                .currentWeight(10.0)
                .currentVolume(0.5)
                .currentOrders(1)
                .status(BagStatus.ARRIVED)
                .sealedAt(DEPARTURE_AT.minusHours(30))
                .tenantId(TENANT_ID)
                .build();

        when(bagRepository.findByTenantIdAndOriginHubIdAndStatusIn(
                TENANT_ID,
                ORIGIN_HUB_ID,
                List.of(BagStatus.SEALED, BagStatus.ARRIVED)
        ))
                .thenReturn(List.of(arrivedBag));
        when(manifestBagRepository.findActiveBagIds(
                eq(TENANT_ID),
                eq(List.of(BAG_ID)),
                anyCollection()
        )).thenReturn(List.of());
        when(routeRepository.findByTenantIdAndStatusAndOriginTypeAndOriginHubIdAndDestinationTypeAndDestinationHubId(
                TENANT_ID,
                RouteStatus.ACTIVE,
                RouteEndpointType.HUB,
                ORIGIN_HUB_ID,
                RouteDestinationType.HUB,
                DESTINATION_HUB_ID
        )).thenReturn(List.of());

        List<BagDistributionPlanItemResponse> items = service.plan(TENANT_ID, new AutoPlanBagDistributionRequest(
                ORIGIN_HUB_ID,
                BagDestinationType.HUB,
                DESTINATION_HUB_ID,
                null,
                DEPARTURE_AT,
                ARRIVAL_AT,
                24,
                List.of(BAG_ID),
                false,
                null
        ));

        assertEquals(1, items.size());
        assertTrue(items.get(0).bagIds().contains(BAG_ID));
    }

    @Test
    void planOnlyUsesRequestedBagIdsWhenProvided() {
        Bag selectedBag = Bag.builder()
                .id(BAG_ID)
                .bagCode("BAG-001")
                .originHubId(ORIGIN_HUB_ID)
                .destinationType(BagDestinationType.HUB)
                .destinationHubId(DESTINATION_HUB_ID)
                .currentWeight(10.0)
                .currentVolume(0.5)
                .currentOrders(1)
                .status(BagStatus.SEALED)
                .sealedAt(DEPARTURE_AT.minusHours(30))
                .tenantId(TENANT_ID)
                .build();
        Bag unselectedBag = Bag.builder()
                .id(OTHER_BAG_ID)
                .bagCode("BAG-002")
                .originHubId(ORIGIN_HUB_ID)
                .destinationType(BagDestinationType.HUB)
                .destinationHubId(DESTINATION_HUB_ID)
                .currentWeight(12.0)
                .currentVolume(0.6)
                .currentOrders(2)
                .status(BagStatus.SEALED)
                .sealedAt(DEPARTURE_AT.minusHours(29))
                .tenantId(TENANT_ID)
                .build();

        when(bagRepository.findByTenantIdAndOriginHubIdAndStatusIn(
                TENANT_ID,
                ORIGIN_HUB_ID,
                List.of(BagStatus.SEALED, BagStatus.ARRIVED)
        ))
                .thenReturn(List.of(selectedBag, unselectedBag));
        when(manifestBagRepository.findActiveBagIds(
                eq(TENANT_ID),
                eq(List.of(BAG_ID, OTHER_BAG_ID)),
                anyCollection()
        )).thenReturn(List.of());
        when(routeRepository.findByTenantIdAndStatusAndOriginTypeAndOriginHubIdAndDestinationTypeAndDestinationHubId(
                TENANT_ID,
                RouteStatus.ACTIVE,
                RouteEndpointType.HUB,
                ORIGIN_HUB_ID,
                RouteDestinationType.HUB,
                DESTINATION_HUB_ID
        )).thenReturn(List.of());

        List<BagDistributionPlanItemResponse> items = service.plan(TENANT_ID, new AutoPlanBagDistributionRequest(
                ORIGIN_HUB_ID,
                BagDestinationType.HUB,
                DESTINATION_HUB_ID,
                null,
                DEPARTURE_AT,
                ARRIVAL_AT,
                24,
                List.of(BAG_ID),
                false,
                null
        ));

        assertEquals(1, items.size());
        assertEquals(List.of(BAG_ID), items.get(0).bagIds());
        assertEquals(List.of("BAG-001"), items.get(0).bagCodes());
    }
}
