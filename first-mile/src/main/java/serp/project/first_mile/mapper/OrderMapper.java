package serp.project.first_mile.mapper;

import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;

public final class OrderMapper {
    private OrderMapper() {
    }

    public static OrderConfirmationResponse toOrderConfirmationResponse(
            Order order,
            PostOffice postOffice,
            boolean alreadyConfirmed
    ) {
        String postOfficeCode = postOffice == null
                ? order.getOriginPostOfficeCode()
                : postOffice.getCode();

        OrderConfirmationResponse.OriginPostOfficeInfo originInfo =
                new OrderConfirmationResponse.OriginPostOfficeInfo(
                        postOffice == null ? null : postOffice.getId(),
                        postOfficeCode,
                        postOffice == null ? null : postOffice.getName(),
                        postOffice == null ? null : postOffice.getCurrentLoad(),
                        postOffice == null ? null : postOffice.getDailyCapacity()
                );

        return new OrderConfirmationResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus(),
                alreadyConfirmed,
                originInfo
        );
    }
}
