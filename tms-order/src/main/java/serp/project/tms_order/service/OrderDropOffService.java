/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.request.ConfirmDropOffOrderRequest;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.dto.response.OrderDropOffPostOfficeSuggestionResponse;

import java.util.List;

public interface OrderDropOffService {

    OrderConfirmationResponse confirmDropOffOrderAtPostOffice(
            Long orderId,
            Long tenantId,
            ConfirmDropOffOrderRequest request
    );

    List<OrderDropOffPostOfficeSuggestionResponse> getDropOffPostOfficeSuggestions(
            Long orderId,
            Integer limit,
            Long tenantId
    );
}
