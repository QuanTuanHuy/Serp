/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PickupCheckin;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.dto.response.PickupTrackingOverviewResponse;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.TripStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PickupCheckinRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.repository.TripRepository;
import serp.project.first_mile.service.PickupTrackingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickupTrackingServiceImpl implements PickupTrackingService {

    private static final String ACTOR_SCOPE_ADMIN_ALL = "ADMIN_ALL";
    private static final String ACTOR_SCOPE_MANAGER_SCOPED = "MANAGER_SCOPED";
    private static final String ACTOR_SCOPE_COURIER_SELF = "COURIER_SELF";

    private static final List<TripStatus> TRACKABLE_TRIP_STATUSES = List.of(
            TripStatus.PLANNED,
            TripStatus.IN_PROGRESS,
            TripStatus.COMPLETED
    );

    private final FirstMileAccessUtils firstMileAccessUtils;
    private final TripRepository tripRepository;
    private final TripOrderRepository tripOrderRepository;
    private final OrderRepository orderRepository;
    private final PickupCheckinRepository pickupCheckinRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final PostOfficeStaffRepository postOfficeStaffRepository;

    @Override
    public PickupTrackingOverviewResponse getPickupTrackingOverview(
            LocalDate tripDate,
            Long postOfficeId,
            Long courierStaffId
    ) {
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        LocalDate effectiveTripDate = tripDate == null ? LocalDate.now() : tripDate;

        ScopeContext scopeContext = resolveScopeContext(
                tenantId,
                normalizePositiveId(postOfficeId),
                normalizePositiveId(courierStaffId)
        );

        if (scopeContext.visiblePostOfficeIds() != null
                && scopeContext.visiblePostOfficeIds().isEmpty()) {
            return buildEmptyOverview(effectiveTripDate, scopeContext);
        }

        List<Trip> allTrips = tripRepository
                .findByTenantIdAndTripDateAndStatusInOrderByPlannedStartTimeAscIdAsc(
                        tenantId,
                        effectiveTripDate,
                        TRACKABLE_TRIP_STATUSES
                );

        List<Trip> scopedTrips = allTrips.stream()
                .filter(trip -> isTripInScope(trip, scopeContext))
                .toList();

        if (scopedTrips.isEmpty()) {
            return buildEmptyOverview(effectiveTripDate, scopeContext);
        }

        Map<Long, Trip> tripById = scopedTrips.stream()
                .collect(Collectors.toMap(Trip::getId, trip -> trip));

        List<Long> tripIds = scopedTrips.stream()
                .map(Trip::getId)
                .toList();

        List<TripOrder> tripOrders = tripOrderRepository
                .findByTenantIdAndTripIdInOrderByTripIdAscSequenceNoAsc(tenantId, tripIds);

        List<Long> orderIds = tripOrders.stream()
                .map(TripOrder::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Order> orderById = orderIds.isEmpty()
                ? Map.of()
                : orderRepository.findByIdInAndTenantId(orderIds, tenantId).stream()
                        .collect(Collectors.toMap(Order::getId, order -> order));

        List<Long> tripOrderIds = tripOrders.stream()
                .map(TripOrder::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, PickupCheckin> pickupCheckinByTripOrderId = tripOrderIds.isEmpty()
                ? Map.of()
                : pickupCheckinRepository.findByTenantIdAndTripOrderIdIn(tenantId, tripOrderIds).stream()
                        .collect(Collectors.toMap(PickupCheckin::getTripOrderId, pickupCheckin -> pickupCheckin));

        Map<Long, PostOffice> postOfficeById = loadPostOfficeById(tenantId, scopedTrips);
        Map<Long, PostOfficeStaff> courierById = loadCourierById(tenantId, scopedTrips);

        Map<Long, TripSummaryAccumulator> tripSummaryByTripId = new HashMap<>();
        for (Trip trip : scopedTrips) {
            tripSummaryByTripId.put(trip.getId(), new TripSummaryAccumulator());
        }

        int totalOrders = 0;
        int checkedInOrders = 0;
        int pendingCheckinOrders = 0;
        int pickingUpOrders = 0;
        int pickedUpOrders = 0;
        int pickupFailedOrders = 0;

        List<PickupTrackingOverviewResponse.PickupTrackingOrderResponse> orderResponses = new ArrayList<>();

        for (TripOrder tripOrder : tripOrders) {
            if (tripOrder == null || tripOrder.getTrip() == null || tripOrder.getTrip().getId() == null) {
                continue;
            }

            Trip trip = tripById.get(tripOrder.getTrip().getId());
            if (trip == null) {
                continue;
            }

            Order order = orderById.get(tripOrder.getOrderId());
            if (order == null || order.getId() == null) {
                continue;
            }

            PickupCheckin pickupCheckin = pickupCheckinByTripOrderId.get(tripOrder.getId());
            boolean checkedIn = pickupCheckin != null;

            totalOrders += 1;
            if (checkedIn) {
                checkedInOrders += 1;
            } else {
                pendingCheckinOrders += 1;
            }

            if (OrderStatus.PICKING_UP.equals(order.getStatus())) {
                pickingUpOrders += 1;
            }
            if (OrderStatus.PICKED_UP.equals(order.getStatus())) {
                pickedUpOrders += 1;
            }
            if (OrderStatus.PICKUP_FAILED.equals(order.getStatus())) {
                pickupFailedOrders += 1;
            }

            TripSummaryAccumulator tripSummary = tripSummaryByTripId.computeIfAbsent(
                    trip.getId(),
                    ignored -> new TripSummaryAccumulator()
            );
            tripSummary.totalOrders += 1;
            if (checkedIn) {
                tripSummary.checkedInOrders += 1;
            } else {
                tripSummary.pendingCheckinOrders += 1;
            }

            PostOffice postOffice = postOfficeById.get(trip.getPostOfficeId());
            PostOfficeStaff courier = courierById.get(trip.getCourierStaffId());

            Point senderLocation = order.getSenderLocation();
            Point checkinLocation = pickupCheckin == null ? null : pickupCheckin.getCheckinLocation();

            orderResponses.add(new PickupTrackingOverviewResponse.PickupTrackingOrderResponse(
                    tripOrder.getId(),
                    trip.getId(),
                    trip.getTripCode(),
                    trip.getStatus(),
                    tripOrder.getSequenceNo(),
                    order.getId(),
                    order.getOrderCode(),
                    order.getCustomerOrderCode(),
                    order.getStatus(),
                    order.getSenderName(),
                    order.getSenderPhone(),
                    order.getSenderAddressDetail(),
                    getLatitude(senderLocation),
                    getLongitude(senderLocation),
                    order.getPickupTimeStart(),
                    order.getPickupTimeEnd(),
                    tripOrder.getPlannedArrivalTime(),
                    tripOrder.getPlannedStartServiceTime(),
                    tripOrder.getPlannedDepartureTime(),
                    trip.getCourierStaffId(),
                    courier == null ? null : courier.getCode(),
                    courier == null ? null : courier.getFullName(),
                    trip.getPostOfficeId(),
                    postOffice == null ? null : postOffice.getCode(),
                    postOffice == null ? null : postOffice.getName(),
                    checkedIn,
                    pickupCheckin == null ? null : pickupCheckin.getId(),
                    pickupCheckin == null ? null : pickupCheckin.getCheckinTime(),
                    getLatitude(checkinLocation),
                    getLongitude(checkinLocation),
                    pickupCheckin == null ? null : pickupCheckin.getPhotoUrl(),
                    pickupCheckin == null ? null : pickupCheckin.getDistanceM(),
                    pickupCheckin == null ? null : pickupCheckin.getAllowedRadiusM()
            ));
        }

        List<PickupTrackingOverviewResponse.PickupTrackingTripResponse> tripResponses = new ArrayList<>();
        for (Trip trip : scopedTrips) {
            TripSummaryAccumulator tripSummary = tripSummaryByTripId.getOrDefault(
                    trip.getId(),
                    new TripSummaryAccumulator()
            );

            PostOffice postOffice = postOfficeById.get(trip.getPostOfficeId());
            PostOfficeStaff courier = courierById.get(trip.getCourierStaffId());

            tripResponses.add(new PickupTrackingOverviewResponse.PickupTrackingTripResponse(
                    trip.getId(),
                    trip.getTripCode(),
                    trip.getStatus(),
                    trip.getShift(),
                    trip.getPostOfficeId(),
                    postOffice == null ? null : postOffice.getCode(),
                    postOffice == null ? null : postOffice.getName(),
                    trip.getCourierStaffId(),
                    courier == null ? null : courier.getCode(),
                    courier == null ? null : courier.getFullName(),
                    trip.getPlannedStartTime(),
                    trip.getPlannedEndTime(),
                    tripSummary.totalOrders,
                    tripSummary.checkedInOrders,
                    tripSummary.pendingCheckinOrders
            ));
        }

        return new PickupTrackingOverviewResponse(
                effectiveTripDate,
                scopeContext.actorScope(),
                scopeContext.selectedPostOfficeId(),
                scopeContext.selectedCourierStaffId(),
                scopedTrips.size(),
                totalOrders,
                checkedInOrders,
                pendingCheckinOrders,
                pickingUpOrders,
                pickedUpOrders,
                pickupFailedOrders,
                tripResponses,
                orderResponses
        );
    }

    private Map<Long, PostOffice> loadPostOfficeById(Long tenantId, List<Trip> trips) {
        List<Long> postOfficeIds = trips.stream()
                .map(Trip::getPostOfficeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (postOfficeIds.isEmpty()) {
            return Map.of();
        }

        return postOfficeRepository.findAllByTenantIdAndIdIn(tenantId, postOfficeIds).stream()
                .collect(Collectors.toMap(PostOffice::getId, postOffice -> postOffice));
    }

    private Map<Long, PostOfficeStaff> loadCourierById(Long tenantId, List<Trip> trips) {
        List<Long> courierIds = trips.stream()
                .map(Trip::getCourierStaffId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (courierIds.isEmpty()) {
            return Map.of();
        }

        return postOfficeStaffRepository.findByTenantIdAndIdIn(tenantId, courierIds).stream()
                .collect(Collectors.toMap(PostOfficeStaff::getId, courier -> courier));
    }

    private ScopeContext resolveScopeContext(Long tenantId, Long postOfficeId, Long courierStaffId) {
        if (firstMileAccessUtils.isAdmin()) {
            return new ScopeContext(
                    ACTOR_SCOPE_ADMIN_ALL,
                    postOfficeId == null ? null : Set.of(postOfficeId),
                    postOfficeId,
                    courierStaffId,
                    courierStaffId
            );
        }

        if (firstMileAccessUtils.isPostOfficerManager()) {
            Set<Long> managedPostOfficeIds = firstMileAccessUtils.getManagedPostOfficeIdsOrThrow(tenantId);

            if (postOfficeId != null && !managedPostOfficeIds.contains(postOfficeId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            Set<Long> visiblePostOfficeIds = postOfficeId == null
                    ? managedPostOfficeIds
                    : Set.of(postOfficeId);

            return new ScopeContext(
                    ACTOR_SCOPE_MANAGER_SCOPED,
                    visiblePostOfficeIds,
                    postOfficeId,
                    courierStaffId,
                    courierStaffId
            );
        }

        if (firstMileAccessUtils.isCourier()) {
            Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );

            if (courierStaffId != null && !courierStaffId.equals(currentCourierStaffId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            return new ScopeContext(
                    ACTOR_SCOPE_COURIER_SELF,
                    postOfficeId == null ? null : Set.of(postOfficeId),
                    postOfficeId,
                    currentCourierStaffId,
                    currentCourierStaffId
            );
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private boolean isTripInScope(Trip trip, ScopeContext scopeContext) {
        if (trip == null || trip.getId() == null) {
            return false;
        }

        if (scopeContext.visiblePostOfficeIds() != null
                && !scopeContext.visiblePostOfficeIds().contains(trip.getPostOfficeId())) {
            return false;
        }

        return scopeContext.effectiveCourierStaffId() == null
                || Objects.equals(scopeContext.effectiveCourierStaffId(), trip.getCourierStaffId());
    }

    private PickupTrackingOverviewResponse buildEmptyOverview(LocalDate tripDate, ScopeContext scopeContext) {
        return new PickupTrackingOverviewResponse(
                tripDate,
                scopeContext.actorScope(),
                scopeContext.selectedPostOfficeId(),
                scopeContext.selectedCourierStaffId(),
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                List.of(),
                List.of()
        );
    }

    private Long normalizePositiveId(Long value) {
        if (value == null) {
            return null;
        }

        if (value <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return value;
    }

    private Double getLatitude(Point point) {
        if (point == null) {
            return null;
        }
        return point.getY();
    }

    private Double getLongitude(Point point) {
        if (point == null) {
            return null;
        }
        return point.getX();
    }

    private record ScopeContext(
            String actorScope,
            Set<Long> visiblePostOfficeIds,
            Long selectedPostOfficeId,
            Long effectiveCourierStaffId,
            Long selectedCourierStaffId
    ) {
    }

    private static class TripSummaryAccumulator {
        private int totalOrders;
        private int checkedInOrders;
        private int pendingCheckinOrders;
    }
}
