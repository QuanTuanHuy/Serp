/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import serp.project.second_mile.caller.TmsOrderClient;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.domain.HandoverManifest;
import serp.project.second_mile.domain.HandoverManifestOrder;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubPostOfficeMapping;
import serp.project.second_mile.domain.HubStaffAssignment;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.request.DriverHandoverCheckinRequest;
import serp.project.second_mile.enums.HandoverManifestStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.enums.VehicleType;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.kafka.HandoverManifestSyncEventPublisher;
import serp.project.second_mile.kafka.event.HandoverManifestSyncEvent;
import serp.project.second_mile.kafka.event.HandoverManifestSyncEventType;
import serp.project.second_mile.kafka.event.HandoverManifestSyncOrigin;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.HandoverManifestOrderRepository;
import serp.project.second_mile.repository.HandoverManifestRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.service.FileStorageService;
import serp.project.second_mile.service.TmsOrderTransitionOutboxService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandoverManifestServiceImplTest {
    private static final Long TENANT_ID = 1L;
    private static final Long HUB_ID = 10L;
    private static final Long VEHICLE_ID = 20L;
    private static final Long DRIVER_ID = 30L;
    private static final Long ROUTE_ID = 40L;
    private static final String POST_OFFICE_CODE = "PO-HCM-01";
    private static final LocalDateTime DEPARTURE_AT = LocalDateTime.of(2026, 6, 2, 8, 0);
    private static final LocalDateTime ARRIVAL_AT = LocalDateTime.of(2026, 6, 2, 9, 0);

    @Mock
    private HandoverManifestRepository handoverManifestRepository;

    @Mock
    private HandoverManifestOrderRepository handoverManifestOrderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private HubPostOfficeMappingRepository hubPostOfficeMappingRepository;

    @Mock
    private HubRepository hubRepository;

    @Mock
    private HubStaffAssignmentRepository hubStaffAssignmentRepository;

    @Mock
    private SecondMileAccessUtils secondMileAccessUtils;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private TmsOrderClient tmsOrderClient;

    @Mock
    private TmsOrderTransitionOutboxService tmsOrderTransitionOutboxService;

    @Mock
    private HandoverManifestSyncEventPublisher handoverManifestSyncEventPublisher;

    @InjectMocks
    private HandoverManifestServiceImpl service;

    @Test
    void applyOutboundSyncRejectsMissingAssignmentFields() {
        HandoverManifestSyncEvent event = HandoverManifestSyncEvent.builder()
                .eventType(HandoverManifestSyncEventType.OUTBOUND_CONFIRMED)
                .origin(HandoverManifestSyncOrigin.FIRST_MILE)
                .tenantId(TENANT_ID)
                .manifestCode("HM-001")
                .originPostOfficeCode(POST_OFFICE_CODE)
                .targetHubId(HUB_ID)
                .build();

        assertThrows(AppException.class, () -> service.applyOutboundSync(event));
    }

    @Test
    void applyOutboundSyncPersistsValidatedRouteVehicleAndWindow() {
        HandoverManifestSyncEvent event = baseOutboundEvent();
        Hub hub = hub();
        Vehicle vehicle = vehicle();
        Route route = route();
        TmsOrderOperationView order = order("ORD-001", 10.0, 0.2);

        when(handoverManifestRepository.findByTenantIdAndManifestCodeIgnoreCase(TENANT_ID, "HM-001"))
                .thenReturn(Optional.empty());
        when(hubPostOfficeMappingRepository.findByTenantIdAndPostOfficeCode(TENANT_ID, POST_OFFICE_CODE))
                .thenReturn(Optional.of(HubPostOfficeMapping.builder()
                        .hub(hub)
                        .postOfficeCode(POST_OFFICE_CODE)
                        .tenantId(TENANT_ID)
                        .build()));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle));
        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(route));
        when(hubStaffAssignmentRepository.findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
                eq(DRIVER_ID), eq(HUB_ID), eq(TENANT_ID), any(LocalDate.class)
        )).thenReturn(Optional.of(HubStaffAssignment.builder().build()));
        when(tmsOrderClient.lookupByCodes(TENANT_ID, List.of("ORD-001")))
                .thenReturn(List.of(order));
        when(handoverManifestRepository.existsOverlappingActiveAssignment(
                eq(TENANT_ID),
                eq(VEHICLE_ID),
                eq(DRIVER_ID),
                eq(DEPARTURE_AT),
                eq(ARRIVAL_AT),
                anyCollection(),
                eq(null)
        )).thenReturn(false);
        when(handoverManifestOrderRepository.findByManifest_IdAndTmsOrderIdAndTenantId(
                any(),
                eq(1L),
                eq(TENANT_ID)
        )).thenReturn(Optional.empty());
        when(handoverManifestRepository.save(any(HandoverManifest.class)))
                .thenAnswer(invocation -> {
                    HandoverManifest manifest = invocation.getArgument(0);
                    if (manifest.getId() == null) {
                        manifest.setId(200L);
                    }
                    return manifest;
                });

        service.applyOutboundSync(event);

        ArgumentCaptor<HandoverManifest> manifestCaptor = ArgumentCaptor.forClass(HandoverManifest.class);
        verify(handoverManifestRepository).save(manifestCaptor.capture());
        HandoverManifest savedManifest = manifestCaptor.getValue();
        assertEquals(VEHICLE_ID, savedManifest.getVehicleId());
        assertEquals(DRIVER_ID, savedManifest.getAssignedDriverId());
        assertEquals(ROUTE_ID, savedManifest.getRouteId());
        assertEquals(DEPARTURE_AT, savedManifest.getPlannedDepartureAt());
        assertEquals(ARRIVAL_AT, savedManifest.getPlannedArrivalAt());
        assertEquals(HandoverManifestStatus.OUTBOUND_CONFIRMED, savedManifest.getStatus());
        verify(tmsOrderTransitionOutboxService).enqueue(any(), eq(TENANT_ID));
    }

    @Test
    void driverCheckinStartRejectsLocationOutsidePostOfficeRadius() {
        HandoverManifest manifest = HandoverManifest.builder()
                .id(100L)
                .manifestCode("HM-001")
                .originPostOfficeCode(POST_OFFICE_CODE)
                .targetHubId(HUB_ID)
                .vehicleId(VEHICLE_ID)
                .assignedDriverId(DRIVER_ID)
                .routeId(ROUTE_ID)
                .plannedDepartureAt(DEPARTURE_AT)
                .plannedArrivalAt(ARRIVAL_AT)
                .originPostOfficeLatitude(10.0)
                .originPostOfficeLongitude(106.0)
                .status(HandoverManifestStatus.OUTBOUND_CONFIRMED)
                .tenantId(TENANT_ID)
                .build();

        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(handoverManifestRepository.findByIdAndTenantId(100L, TENANT_ID)).thenReturn(Optional.of(manifest));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle()));
        when(hubStaffAssignmentRepository.findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
                eq(DRIVER_ID), eq(HUB_ID), eq(TENANT_ID), any(LocalDate.class)
        )).thenReturn(Optional.of(HubStaffAssignment.builder().build()));

        DriverHandoverCheckinRequest request = new DriverHandoverCheckinRequest(11.0, 107.0, null);
        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "handover.jpg",
                "image/jpeg",
                new byte[]{1}
        );

        assertThrows(AppException.class, () -> service.driverCheckinStart(100L, request, photo));
        assertNull(manifest.getDriverStartCheckinAt());
    }

    private HandoverManifestSyncEvent baseOutboundEvent() {
        return HandoverManifestSyncEvent.builder()
                .eventType(HandoverManifestSyncEventType.OUTBOUND_CONFIRMED)
                .origin(HandoverManifestSyncOrigin.FIRST_MILE)
                .tenantId(TENANT_ID)
                .manifestCode("HM-001")
                .originPostOfficeCode(POST_OFFICE_CODE)
                .targetHubId(HUB_ID)
                .vehicleId(VEHICLE_ID)
                .routeId(ROUTE_ID)
                .plannedDepartureAt(DEPARTURE_AT)
                .plannedArrivalAt(ARRIVAL_AT)
                .originPostOfficeLatitude(10.0)
                .originPostOfficeLongitude(106.0)
                .orderCodes(List.of("ORD-001"))
                .build();
    }

    private Hub hub() {
        return Hub.builder()
                .id(HUB_ID)
                .tenantId(TENANT_ID)
                .build();
    }

    private Vehicle vehicle() {
        return Vehicle.builder()
                .id(VEHICLE_ID)
                .vehicleType(VehicleType.TRUCK)
                .maxWeight(100.0)
                .maxVolume(2.0)
                .hubId(HUB_ID)
                .assignedStaffId(DRIVER_ID)
                .status(VehicleStatus.ACTIVE)
                .tenantId(TENANT_ID)
                .build();
    }

    private Route route() {
        return Route.builder()
                .id(ROUTE_ID)
                .originType(RouteEndpointType.POST_OFFICE)
                .originPostOfficeCode(POST_OFFICE_CODE)
                .destinationType(RouteDestinationType.HUB)
                .destinationHubId(HUB_ID)
                .vehicleId(VEHICLE_ID)
                .status(RouteStatus.ACTIVE)
                .tenantId(TENANT_ID)
                .build();
    }

    private TmsOrderOperationView order(String orderCode, Double weight, Double volume) {
        return TmsOrderOperationView.builder()
                .id(1L)
                .orderCode(orderCode)
                .originPostOfficeCode(POST_OFFICE_CODE)
                .status(OrderStatus.AT_ORIGIN_POST_OFFICE)
                .totalWeight(weight)
                .totalVolume(volume)
                .tenantId(TENANT_ID)
                .build();
    }
}
