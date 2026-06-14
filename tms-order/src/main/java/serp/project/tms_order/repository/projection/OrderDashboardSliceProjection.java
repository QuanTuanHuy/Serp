/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.repository.projection;

import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.PaymentStatus;

import java.time.LocalDateTime;

public interface OrderDashboardSliceProjection {
    Long getId();

    String getOrderCode();

    String getCustomerOrderCode();

    OrderStatus getStatus();

    String getOriginPostOfficeCode();

    String getDestinationPostOfficeCode();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    LocalDateTime getPickupTimeEnd();

    Long getTotalShippingFee();

    Long getBaseShippingFee();

    Long getCodFee();

    Long getExtraFee();

    Long getCodAmount();

    PaymentStatus getPaymentStatus();
}
