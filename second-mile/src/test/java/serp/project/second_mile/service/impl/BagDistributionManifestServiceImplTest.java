/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import serp.project.second_mile.caller.TmsOrderClient;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.BagDistributionManifest;
import serp.project.second_mile.domain.BagDistributionManifestBag;
import serp.project.second_mile.domain.BagOrder;
import serp.project.second_mile.domain.Checkin;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubStaffAssignment;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.request.AutoPlanBagDistributionRequest;
import serp.project.second_mile.dto.request.CreateBagDistributionManifestRequest;
import serp.project.second_mile.dto.request.DriverHandoverCheckinRequest;
import serp.project.second_mile.dto.response.BagDistributionPlanItemResponse;
import serp.project.second_mile.dto.response.BagDistributionPlanResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagDistributionManifestStatus;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.CheckinType;
import serp.project.second_mile.enums.HubStatus;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.enums.VehicleType;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.BagDistributionManifestBagRepository;
import serp.project.second_mile.repository.BagDistributionManifestRepository;
import serp.project.second_mile.repository.BagOrderRepository;
import serp.project.second_mile.repository.BagRepository;
import serp.project.second_mile.repository.CheckinRepository;
import serp.project.second_mile.repository.HandoverManifestRepository;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.service.FileStorageService;
import serp.project.second_mile.service.TmsOrderTransitionPublisherService;
import serp.project.second_mile.service.dto.BagDestinationTarget;
import serp.project.second_mile.service.dto.response.FileUploadResponse;
import serp.project.second_mile.service.validator.BagValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BagDistributionManifestServiceImplTest {
    private static final Long TENANT_ID = 1L;
    private static final Long ORIGIN_HUB_ID = 10L;
    private static final Long DESTINATION_HUB_ID = 11L;
    private static final Long NEXT_HUB_ID = 12L;
    private static final Long VEHICLE_ID = 20L;
    private static final Long DRIVER_ID = 30L;
    private static final Long ROUTE_ID = 40L;
    private static final Long BAG_ID = 50L;
    private static final Long ORDER_ID = 60L;
    private static final String DESTINATION_POST_OFFICE_CODE = "PO-DST";
    private static final LocalDateTime DEPARTURE_AT = LocalDateTime.of(2026, 6, 6, 9, 0);
    private static final LocalDateTime ARRIVAL_AT = LocalDateTime.of(2026, 6, 6, 12, 0);

    @Mock
    private BagDistributionManifestRepository manifestRepository;

    @Mock
    private BagDistributionManifestBagRepository manifestBagRepository;

    @Mock
    private BagRepository bagRepository;

    @Mock
    private BagOrderRepository bagOrderRepository;

    @Mock
    private CheckinRepository checkinRepository;

    @Mock
    private HandoverManifestRepository handoverManifestRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private HubRepository hubRepository;

    @Mock
    private HubPostOfficeMappingRepository hubPostOfficeMappingRepository;

    @Mock
    private HubStaffAssignmentRepository hubStaffAssignmentRepository;

    @Mock
    private SecondMileAccessUtils secondMileAccessUtils;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private TmsOrderTransitionPublisherService tmsOrderTransitionPublisherService;

    @Mock
    private BagDistributionPlanningService planningService;

    @Mock
    private TmsOrderClient tmsOrderClient;

    @Mock
    private BagValidator bagValidator;

    @InjectMocks
    private BagDistributionManifestServiceImpl service;

    @Test
    void createRejectsNonSealedBag() {
        stubCreateDependencies();
        when(manifestBagRepository.findActiveBagIds(eq(TENANT_ID), eq(List.of(BAG_ID)), anyCollection()))
                .thenReturn(List.of());
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID)))
                .thenReturn(List.of(bag(BagStatus.CREATED)));

        AppException exception = assertThrows(AppException.class, () -> service.createManifest(createRequest()));

        assertEquals(ErrorCode.BAG_STATUS_INVALID, exception.getErrorCode());
        verify(manifestRepository, never()).save(any(BagDistributionManifest.class));
    }

    @Test
    void createRejectsBagAlreadyInActiveDistributionManifest() {
        stubCreateDependencies();
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID)))
                .thenReturn(List.of(bag(BagStatus.SEALED)));
        when(manifestBagRepository.findActiveBagIds(eq(TENANT_ID), eq(List.of(BAG_ID)), anyCollection()))
                .thenReturn(List.of(BAG_ID));

        AppException exception = assertThrows(AppException.class, () -> service.createManifest(createRequest()));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(manifestRepository, never()).save(any(BagDistributionManifest.class));
    }

    @Test
    void createRejectsVehicleOrDriverScheduleConflictAcrossHandoverManifests() {
        stubCreateDependencies();
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID)))
                .thenReturn(List.of(bag(BagStatus.SEALED)));
        when(manifestBagRepository.findActiveBagIds(eq(TENANT_ID), eq(List.of(BAG_ID)), anyCollection()))
                .thenReturn(List.of());
        when(handoverManifestRepository.existsOverlappingActiveAssignment(
                eq(TENANT_ID),
                eq(VEHICLE_ID),
                eq(DRIVER_ID),
                eq(DEPARTURE_AT),
                eq(ARRIVAL_AT),
                anyCollection(),
                eq(null)
        )).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> service.createManifest(createRequest()));

        assertEquals(ErrorCode.ROUTE_VEHICLE_INVALID, exception.getErrorCode());
        verify(manifestRepository, never()).save(any(BagDistributionManifest.class));
    }

    @Test
    void createPersistsManifestAndBagSnapshots() {
        stubCreateDependencies();
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID)))
                .thenReturn(List.of(bag(BagStatus.SEALED)));
        when(manifestBagRepository.findActiveBagIds(eq(TENANT_ID), eq(List.of(BAG_ID)), anyCollection()))
                .thenReturn(List.of());
        when(handoverManifestRepository.existsOverlappingActiveAssignment(
                eq(TENANT_ID),
                eq(VEHICLE_ID),
                eq(DRIVER_ID),
                eq(DEPARTURE_AT),
                eq(ARRIVAL_AT),
                anyCollection(),
                eq(null)
        )).thenReturn(false);
        when(manifestRepository.existsOverlappingActiveAssignment(
                eq(TENANT_ID),
                eq(VEHICLE_ID),
                eq(DRIVER_ID),
                eq(DEPARTURE_AT),
                eq(ARRIVAL_AT),
                anyCollection(),
                eq(null)
        )).thenReturn(false);
        when(manifestRepository.existsByTenantIdAndManifestCodeIgnoreCase(eq(TENANT_ID), any()))
                .thenReturn(false);
        when(manifestRepository.save(any(BagDistributionManifest.class)))
                .thenAnswer(invocation -> {
                    BagDistributionManifest manifest = invocation.getArgument(0);
                    manifest.setId(100L);
                    return manifest;
                });
        when(manifestBagRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.createManifest(createRequest());

        ArgumentCaptor<BagDistributionManifest> manifestCaptor = ArgumentCaptor.forClass(BagDistributionManifest.class);
        verify(manifestRepository).save(manifestCaptor.capture());
        assertEquals(BagDistributionManifestStatus.CREATED, manifestCaptor.getValue().getStatus());
        verify(manifestBagRepository).saveAll(any());
    }

    @Test
    void confirmOutboundMovesBagsAndOrdersInTransit() {
        BagDistributionManifest manifest = manifest(BagDistributionManifestStatus.CREATED);
        BagDistributionManifestBag manifestBag = manifestBag(manifest);
        Bag bag = bag(BagStatus.SEALED);
        BagOrder bagOrder = bagOrder(OrderStatus.BAG_SEALED);
        stubManifestLookup(manifest);
        stubLifecycleVehicle();
        when(manifestBagRepository.findByManifest_IdAndTenantId(100L, TENANT_ID)).thenReturn(List.of(manifestBag));
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID))).thenReturn(List.of(bag));
        when(bagOrderRepository.findByBag_IdAndTenantId(BAG_ID, TENANT_ID)).thenReturn(List.of(bagOrder));
        when(manifestRepository.save(any(BagDistributionManifest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmOutbound(100L);

        assertEquals(BagDistributionManifestStatus.OUTBOUND_CONFIRMED, manifest.getStatus());
        assertEquals(BagStatus.IN_TRANSIT, bag.getStatus());
        assertNotNull(manifestBag.getScanOutTime());
        assertEquals(OrderStatus.BAG_IN_TRANSIT.name(), bagOrder.getLastKnownStatus());
        verify(tmsOrderTransitionPublisherService).publish(any(), eq(TENANT_ID));
    }

    @Test
    void confirmOutboundAllowsOrdersAlreadyInboundAtDestinationHub() {
        BagDistributionManifest manifest = manifest(BagDistributionManifestStatus.CREATED);
        BagDistributionManifestBag manifestBag = manifestBag(manifest);
        Bag bag = bag(BagStatus.ARRIVED);
        BagOrder bagOrder = bagOrder(OrderStatus.INBOUND_AT_DESTINATION_HUB);
        stubManifestLookup(manifest);
        stubLifecycleVehicle();
        when(manifestBagRepository.findByManifest_IdAndTenantId(100L, TENANT_ID)).thenReturn(List.of(manifestBag));
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID))).thenReturn(List.of(bag));
        when(bagOrderRepository.findByBag_IdAndTenantId(BAG_ID, TENANT_ID)).thenReturn(List.of(bagOrder));
        when(manifestRepository.save(any(BagDistributionManifest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmOutbound(100L);

        ArgumentCaptor<TmsOrderStatusTransitionRequest> requestCaptor =
                ArgumentCaptor.forClass(TmsOrderStatusTransitionRequest.class);
        verify(tmsOrderTransitionPublisherService).publish(requestCaptor.capture(), eq(TENANT_ID));
        TmsOrderStatusTransitionRequest.Item item = requestCaptor.getValue().getItems().getFirst();
        assertEquals(OrderStatus.BAG_IN_TRANSIT, item.getTargetStatus());
        assertTrue(item.getExpectedStatuses().contains(OrderStatus.INBOUND_AT_DESTINATION_HUB));
        assertEquals(OrderStatus.BAG_IN_TRANSIT.name(), bagOrder.getLastKnownStatus());
    }

    @Test
    void driverCheckinStartStoresCentralCheckinAndKeepsManifestCheckinColumnsEmpty() {
        BagDistributionManifest manifest = manifest(BagDistributionManifestStatus.CREATED);
        BagDistributionManifestBag manifestBag = manifestBag(manifest);
        Bag bag = bag(BagStatus.SEALED);
        BagOrder bagOrder = bagOrder(OrderStatus.BAG_SEALED);
        stubManifestLookup(manifest);
        stubLifecycleVehicle();
        when(manifestBagRepository.findByManifest_IdAndTenantId(100L, TENANT_ID)).thenReturn(List.of(manifestBag));
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID))).thenReturn(List.of(bag));
        when(bagOrderRepository.findByBag_IdAndTenantId(BAG_ID, TENANT_ID)).thenReturn(List.of(bagOrder));
        when(fileStorageService.upload(any())).thenReturn(FileUploadResponse.builder()
                .url("https://files.local/bag-distribution-start.jpg")
                .build());
        when(checkinRepository.save(any(Checkin.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(manifestRepository.save(any(BagDistributionManifest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        DriverHandoverCheckinRequest request = new DriverHandoverCheckinRequest(
                10.0,
                106.0,
                "Cổng hub xuất phát"
        );
        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "bag-distribution-start.jpg",
                "image/jpeg",
                new byte[]{1}
        );

        service.driverCheckinStart(100L, request, photo);

        ArgumentCaptor<Checkin> checkinCaptor = ArgumentCaptor.forClass(Checkin.class);
        verify(checkinRepository).save(checkinCaptor.capture());
        Checkin savedCheckin = checkinCaptor.getValue();
        assertEquals(CheckinType.BAG_DISTRIBUTION_START, savedCheckin.getCheckinType());
        assertEquals(100L, savedCheckin.getBagDistributionManifestId());
        assertEquals(DRIVER_ID, savedCheckin.getDriverStaffId());
        assertEquals(TENANT_ID, savedCheckin.getTenantId());
        assertEquals("Cổng hub xuất phát", savedCheckin.getLocationLabel());
        assertEquals("https://files.local/bag-distribution-start.jpg", savedCheckin.getPhotoUrl());
        assertEquals(10.0, savedCheckin.getCheckinLocation().getY(), 0.001);
        assertEquals(106.0, savedCheckin.getCheckinLocation().getX(), 0.001);
        assertEquals(0.0, savedCheckin.getDistanceM(), 0.001);
        assertEquals(100.0, savedCheckin.getAllowedRadiusM(), 0.001);
        assertNotNull(manifest.getActualDepartureAt());
        assertEquals(BagDistributionManifestStatus.OUTBOUND_CONFIRMED, manifest.getStatus());
    }

    @Test
    void confirmInboundMovesBagsAndOrdersToDestinationHubInbound() {
        BagDistributionManifest manifest = manifest(BagDistributionManifestStatus.OUTBOUND_CONFIRMED);
        manifest.setActualDepartureAt(DEPARTURE_AT);
        BagDistributionManifestBag manifestBag = manifestBag(manifest);
        manifestBag.setScanOutTime(DEPARTURE_AT);
        Bag bag = bag(BagStatus.IN_TRANSIT);
        BagOrder bagOrder = bagOrder(OrderStatus.BAG_IN_TRANSIT);
        Hub destinationHub = hub(DESTINATION_HUB_ID);
        destinationHub.setDailyCapacity(100);
        destinationHub.setCurrentLoad(0);
        stubManifestLookup(manifest);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle()));
        when(manifestBagRepository.findByManifest_IdAndTenantId(100L, TENANT_ID))
                .thenReturn(List.of(manifestBag), List.of(manifestBag));
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID))).thenReturn(List.of(bag));
        when(bagOrderRepository.findByBag_IdAndTenantId(BAG_ID, TENANT_ID)).thenReturn(List.of(bagOrder));
        TmsOrderOperationView order = TmsOrderOperationView.builder()
                .id(ORDER_ID)
                .orderCode("ORD-001")
                .tenantId(TENANT_ID)
                .build();
        when(tmsOrderClient.lookupByIds(TENANT_ID, List.of(ORDER_ID))).thenReturn(List.of(order));
        when(bagValidator.resolveDestinationTargetForOrder(TENANT_ID, order, DESTINATION_HUB_ID))
                .thenReturn(new BagDestinationTarget(
                        BagDestinationType.POST_OFFICE,
                        null,
                        DESTINATION_POST_OFFICE_CODE
                ));
        when(hubRepository.findByIdAndTenantIdForUpdate(DESTINATION_HUB_ID, TENANT_ID))
                .thenReturn(Optional.of(destinationHub));
        when(manifestRepository.save(any(BagDistributionManifest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmInbound(100L, null);

        assertEquals(BagDistributionManifestStatus.INBOUND_CONFIRMED, manifest.getStatus());
        assertEquals(BagStatus.ARRIVED, bag.getStatus());
        assertEquals(DESTINATION_HUB_ID, bag.getOriginHubId());
        assertEquals(BagDestinationType.POST_OFFICE, bag.getDestinationType());
        assertNull(bag.getDestinationHubId());
        assertEquals(DESTINATION_POST_OFFICE_CODE, bag.getDestinationPostOfficeCode());
        assertNotNull(manifestBag.getScanInTime());
        assertEquals(OrderStatus.INBOUND_AT_DESTINATION_HUB.name(), bagOrder.getLastKnownStatus());
        verify(tmsOrderTransitionPublisherService).publish(any(), eq(TENANT_ID));
    }

    @Test
    void confirmInboundPreparesArrivedBagForNextPlannedHub() {
        BagDistributionManifest manifest = manifest(BagDistributionManifestStatus.OUTBOUND_CONFIRMED);
        manifest.setActualDepartureAt(DEPARTURE_AT);
        BagDistributionManifestBag manifestBag = manifestBag(manifest);
        manifestBag.setScanOutTime(DEPARTURE_AT);
        Bag bag = bag(BagStatus.IN_TRANSIT);
        BagOrder bagOrder = bagOrder(OrderStatus.BAG_IN_TRANSIT);
        Hub destinationHub = hub(DESTINATION_HUB_ID);
        TmsOrderOperationView order = TmsOrderOperationView.builder()
                .id(ORDER_ID)
                .orderCode("ORD-001")
                .tenantId(TENANT_ID)
                .build();
        stubManifestLookup(manifest);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle()));
        when(manifestBagRepository.findByManifest_IdAndTenantId(100L, TENANT_ID))
                .thenReturn(List.of(manifestBag), List.of(manifestBag));
        when(bagRepository.findByIdInAndTenantIdForUpdate(TENANT_ID, List.of(BAG_ID))).thenReturn(List.of(bag));
        when(bagOrderRepository.findByBag_IdAndTenantId(BAG_ID, TENANT_ID)).thenReturn(List.of(bagOrder));
        when(tmsOrderClient.lookupByIds(TENANT_ID, List.of(ORDER_ID))).thenReturn(List.of(order));
        when(bagValidator.resolveDestinationTargetForOrder(TENANT_ID, order, DESTINATION_HUB_ID))
                .thenReturn(new BagDestinationTarget(BagDestinationType.HUB, NEXT_HUB_ID, DESTINATION_POST_OFFICE_CODE));
        when(hubRepository.findByIdAndTenantIdForUpdate(DESTINATION_HUB_ID, TENANT_ID))
                .thenReturn(Optional.of(destinationHub));
        when(manifestRepository.save(any(BagDistributionManifest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmInbound(100L, null);

        assertEquals(DESTINATION_HUB_ID, bag.getOriginHubId());
        assertEquals(BagDestinationType.HUB, bag.getDestinationType());
        assertEquals(NEXT_HUB_ID, bag.getDestinationHubId());
        assertNull(bag.getDestinationPostOfficeCode());
    }

    @Test
    void cancelOnlyMarksCreatedManifestCancelledAndLeavesBagsUntouched() {
        BagDistributionManifest manifest = manifest(BagDistributionManifestStatus.CREATED);
        stubManifestLookup(manifest);
        when(manifestRepository.save(any(BagDistributionManifest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(manifestBagRepository.findByManifest_IdAndTenantId(100L, TENANT_ID)).thenReturn(List.of());

        service.cancel(100L);

        assertEquals(BagDistributionManifestStatus.CANCELLED, manifest.getStatus());
        verify(bagRepository, never()).saveAll(any());
    }

    @Test
    void autoPlanDelegatesToPlannerAndReturnsBlockingHints() {
        AutoPlanBagDistributionRequest request = new AutoPlanBagDistributionRequest(
                ORIGIN_HUB_ID,
                BagDestinationType.HUB,
                DESTINATION_HUB_ID,
                null,
                DEPARTURE_AT,
                ARRIVAL_AT,
                24,
                false,
                null
        );
        BagDistributionPlanItemResponse noRouteItem = new BagDistributionPlanItemResponse(
                ORIGIN_HUB_ID,
                BagDestinationType.HUB,
                DESTINATION_HUB_ID,
                null,
                null,
                null,
                null,
                null,
                null,
                DEPARTURE_AT,
                ARRIVAL_AT,
                List.of(BAG_ID),
                List.of("BAG-001"),
                10.0,
                0.5,
                1,
                0.0,
                List.of("NO_ROUTE"),
                null,
                null
        );
        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(hubRepository.findById(ORIGIN_HUB_ID)).thenReturn(Optional.of(hub(ORIGIN_HUB_ID)));
        when(planningService.plan(TENANT_ID, request)).thenReturn(List.of(noRouteItem));
        when(planningService.hasBlockingHint(List.of("NO_ROUTE"))).thenReturn(true);

        BagDistributionPlanResponse response = service.autoPlan(request);

        assertEquals(1, response.items().size());
        assertTrue(response.items().get(0).hints().contains("NO_ROUTE"));
    }

    private void stubCreateDependencies() {
        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(hubRepository.findById(ORIGIN_HUB_ID)).thenReturn(Optional.of(hub(ORIGIN_HUB_ID)));
        when(hubRepository.findById(DESTINATION_HUB_ID)).thenReturn(Optional.of(hub(DESTINATION_HUB_ID)));
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle()));
        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(route()));
        when(hubStaffAssignmentRepository.findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
                eq(DRIVER_ID), eq(ORIGIN_HUB_ID), eq(TENANT_ID), any(LocalDate.class)
        )).thenReturn(Optional.of(HubStaffAssignment.builder().build()));
    }

    private void stubManifestLookup(BagDistributionManifest manifest) {
        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(manifestRepository.findByIdAndTenantId(100L, TENANT_ID)).thenReturn(Optional.of(manifest));
        when(routeRepository.findById(ROUTE_ID)).thenReturn(Optional.of(route()));
        when(hubRepository.findById(ORIGIN_HUB_ID)).thenReturn(Optional.of(hub(ORIGIN_HUB_ID)));
        when(hubRepository.findById(DESTINATION_HUB_ID)).thenReturn(Optional.of(hub(DESTINATION_HUB_ID)));
    }

    private void stubLifecycleVehicle() {
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(Optional.of(vehicle()));
        when(hubStaffAssignmentRepository.findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
                eq(DRIVER_ID), eq(ORIGIN_HUB_ID), eq(TENANT_ID), any(LocalDate.class)
        )).thenReturn(Optional.of(HubStaffAssignment.builder().build()));
    }

    private CreateBagDistributionManifestRequest createRequest() {
        return new CreateBagDistributionManifestRequest(
                ORIGIN_HUB_ID,
                BagDestinationType.HUB,
                DESTINATION_HUB_ID,
                null,
                ROUTE_ID,
                VEHICLE_ID,
                DEPARTURE_AT,
                ARRIVAL_AT,
                List.of(BAG_ID),
                "Morning run"
        );
    }

    private BagDistributionManifest manifest(BagDistributionManifestStatus status) {
        return BagDistributionManifest.builder()
                .id(100L)
                .manifestCode("BDM-001")
                .originHubId(ORIGIN_HUB_ID)
                .destinationType(BagDestinationType.HUB)
                .destinationHubId(DESTINATION_HUB_ID)
                .routeId(ROUTE_ID)
                .vehicleId(VEHICLE_ID)
                .assignedDriverId(DRIVER_ID)
                .plannedDepartureAt(DEPARTURE_AT)
                .plannedArrivalAt(ARRIVAL_AT)
                .status(status)
                .tenantId(TENANT_ID)
                .build();
    }

    private BagDistributionManifestBag manifestBag(BagDistributionManifest manifest) {
        return BagDistributionManifestBag.builder()
                .id(200L)
                .manifest(manifest)
                .bagId(BAG_ID)
                .bagCode("BAG-001")
                .originHubId(ORIGIN_HUB_ID)
                .destinationType(BagDestinationType.HUB)
                .destinationHubId(DESTINATION_HUB_ID)
                .destinationPostOfficeCode(DESTINATION_POST_OFFICE_CODE)
                .totalWeightSnapshot(10.0)
                .totalVolumeSnapshot(0.5)
                .totalOrdersSnapshot(1)
                .tenantId(TENANT_ID)
                .build();
    }

    private Bag bag(BagStatus status) {
        return Bag.builder()
                .id(BAG_ID)
                .bagCode("BAG-001")
                .originHubId(ORIGIN_HUB_ID)
                .destinationType(BagDestinationType.HUB)
                .destinationHubId(DESTINATION_HUB_ID)
                .destinationPostOfficeCode(DESTINATION_POST_OFFICE_CODE)
                .currentWeight(10.0)
                .currentVolume(0.5)
                .currentOrders(1)
                .status(status)
                .sealedAt(DEPARTURE_AT.minusHours(2))
                .tenantId(TENANT_ID)
                .build();
    }

    private BagOrder bagOrder(OrderStatus status) {
        return BagOrder.builder()
                .id(300L)
                .bag(bag(BagStatus.SEALED))
                .tmsOrderId(ORDER_ID)
                .orderCode("ORD-001")
                .lastKnownStatus(status.name())
                .tenantId(TENANT_ID)
                .build();
    }

    private Hub hub(Long hubId) {
        return Hub.builder()
                .id(hubId)
                .code("HUB-" + hubId)
                .location(new GeometryFactory().createPoint(new Coordinate(106.0, 10.0)))
                .dailyCapacity(100)
                .currentLoad(0)
                .status(HubStatus.ACTIVE)
                .tenantId(TENANT_ID)
                .build();
    }

    private Vehicle vehicle() {
        return Vehicle.builder()
                .id(VEHICLE_ID)
                .licensePlate("51A-12345")
                .vehicleType(VehicleType.TRUCK)
                .maxWeight(100.0)
                .maxVolume(10.0)
                .maxBags(10)
                .hubId(ORIGIN_HUB_ID)
                .assignedStaffId(DRIVER_ID)
                .status(VehicleStatus.ACTIVE)
                .tenantId(TENANT_ID)
                .build();
    }

    private Route route() {
        return Route.builder()
                .id(ROUTE_ID)
                .routeCode("R-001")
                .originType(RouteEndpointType.HUB)
                .originHubId(ORIGIN_HUB_ID)
                .destinationType(RouteDestinationType.HUB)
                .destinationHubId(DESTINATION_HUB_ID)
                .vehicleId(VEHICLE_ID)
                .status(RouteStatus.ACTIVE)
                .tenantId(TENANT_ID)
                .build();
    }
}
