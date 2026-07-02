/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.mapper;

import org.locationtech.jts.geom.Point;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.domain.Checkin;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.Trip;
import serp.project.first_mile.domain.TripOrder;
import serp.project.first_mile.dto.response.PickupCheckinDetailResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.dto.response.PickupTrackingOverviewResponse;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.kernel.utils.GeoPointUtils;

import java.time.LocalDate;
import java.util.List;

public final class PickupTrackingMapper {

    private PickupTrackingMapper() {
    }

    public static PickupTrackingOverviewResponse emptyOverview(
            LocalDate tripDate,
            String actorScope,
            Long selectedPostOfficeId,
            Long selectedCourierStaffId
    ) {
        return new PickupTrackingOverviewResponse(
                tripDate,
                actorScope,
                selectedPostOfficeId,
                selectedCourierStaffId,
                0,
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

    public static PickupTrackingOverviewResponse.PickupTrackingOrderResponse toTrackingOrderResponse(
            TripOrder tripOrder,
            Trip trip,
            TmsOrderOperationView order,
            Checkin pickupCheckin,
            PostOffice postOffice,
            PostOfficeStaff courier
    ) {
        Point senderLocation = order.getSenderLocation();
        Point checkinLocation = pickupCheckin == null ? null : pickupCheckin.getCheckinLocation();
        boolean checkedIn = pickupCheckin != null;

        return new PickupTrackingOverviewResponse.PickupTrackingOrderResponse(
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
                GeoPointUtils.latitude(senderLocation),
                GeoPointUtils.longitude(senderLocation),
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
                GeoPointUtils.latitude(checkinLocation),
                GeoPointUtils.longitude(checkinLocation),
                pickupCheckin == null ? null : pickupCheckin.getPhotoUrl(),
                pickupCheckin == null ? null : pickupCheckin.getDistanceM(),
                pickupCheckin == null ? null : pickupCheckin.getAllowedRadiusM()
        );
    }

    public static PickupTrackingOverviewResponse.PickupTrackingTripResponse toTrackingTripResponse(
            Trip trip,
            PostOffice postOffice,
            PostOfficeStaff courier,
            TripSummary summary
    ) {
        return new PickupTrackingOverviewResponse.PickupTrackingTripResponse(
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
                summary.totalOrders(),
                summary.checkedInOrders(),
                summary.pendingCheckinOrders(),
                summary.returnableToPostOfficeOrders(),
                summary.pendingPostOfficeInboundOrders()
        );
    }

    public static PickupCheckinResponse toPickupCheckinResponse(
            TmsOrderOperationView order,
            Trip trip,
            Checkin pickupCheckin,
            Point pickupLocation
    ) {
        Point checkinLocation = pickupCheckin == null ? null : pickupCheckin.getCheckinLocation();

        return new PickupCheckinResponse(
                pickupCheckin == null ? null : pickupCheckin.getId(),
                order == null ? null : order.getId(),
                order == null ? null : order.getOrderCode(),
                OrderStatus.PICKING_UP,
                trip == null ? null : trip.getId(),
                trip == null ? null : trip.getTripCode(),
                pickupCheckin == null ? null : pickupCheckin.getCourierStaffId(),
                pickupCheckin == null ? null : pickupCheckin.getCheckinTime(),
                pickupCheckin == null ? null : pickupCheckin.getPhotoUrl(),
                checkinLocation == null ? null : GeoPointUtils.round3(checkinLocation.getY()),
                checkinLocation == null ? null : GeoPointUtils.round3(checkinLocation.getX()),
                pickupLocation == null ? null : GeoPointUtils.round3(pickupLocation.getY()),
                pickupLocation == null ? null : GeoPointUtils.round3(pickupLocation.getX()),
                pickupCheckin == null ? null : pickupCheckin.getDistanceM(),
                pickupCheckin == null ? null : pickupCheckin.getAllowedRadiusM()
        );
    }

    public static PickupCheckinDetailResponse toPickupCheckinDetailResponse(
            Checkin pickupCheckin,
            TmsOrderOperationView order,
            Trip trip,
            PostOffice postOffice,
            PostOfficeStaff courier
    ) {
        Point senderLocation = order.getSenderLocation();
        Point checkinLocation = pickupCheckin.getCheckinLocation();

        return new PickupCheckinDetailResponse(
                pickupCheckin.getId(),
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus(),
                trip.getId(),
                trip.getTripCode(),
                trip.getStatus(),
                pickupCheckin.getCourierStaffId(),
                courier == null ? null : courier.getCode(),
                courier == null ? null : courier.getFullName(),
                trip.getPostOfficeId(),
                postOffice == null ? null : postOffice.getCode(),
                postOffice == null ? null : postOffice.getName(),
                order.getSenderName(),
                order.getSenderPhone(),
                order.getSenderAddressDetail(),
                GeoPointUtils.latitude(senderLocation),
                GeoPointUtils.longitude(senderLocation),
                pickupCheckin.getCheckinTime(),
                GeoPointUtils.latitude(checkinLocation),
                GeoPointUtils.longitude(checkinLocation),
                pickupCheckin.getPhotoUrl(),
                pickupCheckin.getDistanceM(),
                pickupCheckin.getAllowedRadiusM()
        );
    }

    public record TripSummary(
            int totalOrders,
            int checkedInOrders,
            int pendingCheckinOrders,
            int returnableToPostOfficeOrders,
            int pendingPostOfficeInboundOrders
    ) {
    }
}
