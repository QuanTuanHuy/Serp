/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.request.CancelOrderRequest;
import serp.project.tms_order.dto.request.CreateOrderRequest;
import serp.project.tms_order.dto.request.OrderImportDTO;
import serp.project.tms_order.dto.request.OrderFilterRequest;
import serp.project.tms_order.dto.request.UpdateOrderRequest;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.dto.response.OrderDetailResponse;
import serp.project.tms_order.dto.response.ValidateImportFileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface OrderService {
    byte[] exportTemplate(Long tenantId);

    ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId);

    ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId);

    PageResponse<OrderDetailResponse> getOrders(int page, int size, OrderFilterRequest filterRequest, Long tenantId);

    OrderDetailResponse createOrder(CreateOrderRequest request, Long tenantId);

    OrderDetailResponse getOrderById(Long orderId, Long tenantId);

    OrderDetailResponse updateOrder(Long orderId, UpdateOrderRequest request, Long tenantId);

    OrderDetailResponse cancelOrder(Long orderId, Long tenantId, CancelOrderRequest request);

    void deleteOrder(Long orderId, Long tenantId);

    OrderConfirmationResponse confirmOrder(Long orderId, Long tenantId);
}
