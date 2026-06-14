/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.request.CancelOrderRequest;
import serp.project.tms_order.dto.request.ConfirmDropOffOrderRequest;
import serp.project.tms_order.dto.request.ConfirmOrderPaymentRequest;
import serp.project.tms_order.dto.request.CreateOrderRequest;
import serp.project.tms_order.dto.request.InitiateOrderPaymentRequest;
import serp.project.tms_order.dto.request.OrderImportDTO;
import serp.project.tms_order.dto.request.OrderFilterRequest;
import serp.project.tms_order.dto.request.PaymentOrderConfirmedWebhookRequest;
import serp.project.tms_order.dto.request.UpdateOrderRequest;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.dto.response.OrderDetailResponse;
import serp.project.tms_order.dto.response.OrderPaymentConfirmResponse;
import serp.project.tms_order.dto.response.OrderPaymentInitResponse;
import serp.project.tms_order.dto.response.PaymentWebhookProcessResponse;
import serp.project.tms_order.dto.response.ValidateImportFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    OrderPaymentInitResponse initiateOrderPayment(
            Long orderId,
            Long tenantId,
            InitiateOrderPaymentRequest request
    );

    OrderPaymentConfirmResponse confirmOrderPayment(
            Long orderId,
            Long tenantId,
            ConfirmOrderPaymentRequest request
    );

    PaymentWebhookProcessResponse processPaymentOrderConfirmedWebhook(PaymentOrderConfirmedWebhookRequest request);

    void updatePaymentStatus(String orderCode, Long tenantId, String paymentStatus);
}
