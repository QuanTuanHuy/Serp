/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.OrderFilterRequest;
import serp.project.second_mile.dto.response.OrderResponse;

public interface OrderManagementService {
    PageResponse<OrderResponse> getOrders(int page, int size, OrderFilterRequest filterRequest);

    OrderResponse getOrderById(Long id);
}
