package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.InitiateOrderPaymentRequest;
import serp.project.first_mile.dto.request.ConfirmOrderPaymentRequest;
import serp.project.first_mile.dto.request.PaymentOrderConfirmedWebhookRequest;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.OrderPaymentConfirmResponse;
import serp.project.first_mile.dto.response.OrderPaymentInitResponse;
import serp.project.first_mile.dto.response.PaymentWebhookProcessResponse;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.dto.response.OrderTimelineResponse;

import java.util.List;

public interface OrderService {
	OrderConfirmationResponse confirmOrder(Long orderId, Long tenantId);

	List<OrderDropOffPostOfficeSuggestionResponse> getDropOffPostOfficeSuggestions(Long orderId, Integer limit, Long tenantId);

	OrderConfirmationResponse confirmDropOffOrderAtPostOffice(Long orderId, Long postOfficeId, Long tenantId);

	List<OrderTimelineResponse> getOrderTimeline(Long orderId, Long tenantId);

	OrderPaymentInitResponse initiateOrderPayment(
			Long orderId,
			Long tenantId,
			InitiateOrderPaymentRequest request
	);

	OrderPaymentConfirmResponse confirmOrderPayment(Long orderId, Long tenantId, ConfirmOrderPaymentRequest request);

	PaymentWebhookProcessResponse processPaymentOrderConfirmedWebhook(PaymentOrderConfirmedWebhookRequest request);

	PickupCheckinResponse checkInPickupOrder(
			Long orderId,
			Double checkinLatitude,
			Double checkinLongitude,
			MultipartFile photo,
			Long tenantId
	);

	void publishOrderEvent(String orderCode);
}
