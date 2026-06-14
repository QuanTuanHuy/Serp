package serp.project.tms_payment_service.gateway;

import serp.project.tms_payment_service.dto.payment.PaymentBankListResponse;
import serp.project.tms_payment_service.dto.payment.PaymentCallbackRequest;
import serp.project.tms_payment_service.dto.payment.PaymentCreateOrderRequest;
import serp.project.tms_payment_service.dto.payment.PaymentCreateOrderResponse;
import serp.project.tms_payment_service.dto.payment.PaymentQueryOrderRequest;
import serp.project.tms_payment_service.dto.payment.PaymentQueryOrderResponse;
import serp.project.tms_payment_service.dto.payment.PaymentQueryRefundRequest;
import serp.project.tms_payment_service.dto.payment.PaymentQueryRefundResponse;
import serp.project.tms_payment_service.dto.payment.PaymentRefundRequest;
import serp.project.tms_payment_service.dto.payment.PaymentRefundResponse;

/**
 * Abstraction layer for payment gateway integrations.
 * New gateways should implement this interface and register as Spring beans.
 */
public interface PaymentGateway {

    String gatewayCode();

    String displayName();

    PaymentCreateOrderResponse createOrder(PaymentCreateOrderRequest request);

    String handleCallback(PaymentCallbackRequest callbackRequest);

    PaymentQueryOrderResponse queryOrderStatus(PaymentQueryOrderRequest request);

    PaymentBankListResponse getBankList();

    PaymentRefundResponse refundOrder(PaymentRefundRequest request);

    PaymentQueryRefundResponse queryRefundStatus(PaymentQueryRefundRequest request);
}
