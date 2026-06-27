/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.mapper;

import serp.project.tms_order.caller.dto.firstmile.DestinationPostOfficeReservationResponse;
import serp.project.tms_order.caller.dto.firstmile.OriginPostOfficeReservationResponse;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.service.order.OrderTextUtils;

public final class OrderConfirmationMapper {

    private OrderConfirmationMapper() {
    }

    public static OrderConfirmationResponse toResponse(
            Order order,
            OriginPostOfficeReservationResponse postOffice,
            DestinationPostOfficeReservationResponse destinationPostOffice,
            boolean alreadyConfirmed
    ) {
        String postOfficeCode = postOffice == null
                ? order.getOriginPostOfficeCode()
                : postOffice.getCode();

        OrderConfirmationResponse.OriginPostOfficeInfo originInfo = OrderTextUtils.hasText(postOfficeCode)
                ? new OrderConfirmationResponse.OriginPostOfficeInfo(
                        postOffice == null ? null : postOffice.getId(),
                        postOfficeCode,
                        postOffice == null ? null : postOffice.getName(),
                        postOffice == null ? null : postOffice.getCurrentLoad(),
                        postOffice == null ? null : postOffice.getDailyCapacity()
                )
                : null;

        String destinationPostOfficeCode = destinationPostOffice == null
                ? order.getDestinationPostOfficeCode()
                : destinationPostOffice.getCode();

        OrderConfirmationResponse.DestinationPostOfficeInfo destinationInfo =
                OrderTextUtils.hasText(destinationPostOfficeCode)
                        ? new OrderConfirmationResponse.DestinationPostOfficeInfo(
                                destinationPostOffice == null ? null : destinationPostOffice.getId(),
                                destinationPostOfficeCode,
                                destinationPostOffice == null ? null : destinationPostOffice.getName(),
                                destinationPostOffice == null ? null : destinationPostOffice.getCurrentDeliveryLoad(),
                                destinationPostOffice == null ? null : destinationPostOffice.getDeliveryCapacity()
                        )
                        : null;

        return new OrderConfirmationResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus(),
                alreadyConfirmed,
                originInfo,
                destinationInfo
        );
    }
}
