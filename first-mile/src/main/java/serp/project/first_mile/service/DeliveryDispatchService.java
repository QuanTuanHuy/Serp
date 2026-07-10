/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.request.AutoAssignDeliveryPlanRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryFailureRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryPaymentRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryRequest;
import serp.project.first_mile.dto.request.ManualAssignDeliveryOrdersRequest;
import serp.project.first_mile.dto.request.ReturnToSenderRequest;
import serp.project.first_mile.dto.request.ScanOutDeliveryOrderRequest;
import serp.project.first_mile.dto.response.DeliveryAssignmentResponse;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentConfirmResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentInitResponse;
import serp.project.first_mile.dto.response.DeliveryScanOutResponse;
import serp.project.first_mile.dto.response.PickupOptimizationResponse;
import serp.project.first_mile.enums.PickupShift;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface DeliveryDispatchService {
    PickupOptimizationResponse optimizeDeliveryPlan(AutoAssignDeliveryPlanRequest request);

    DeliveryAssignmentResponse autoAssignDeliveryPlan(AutoAssignDeliveryPlanRequest request);

    DeliveryAssignmentResponse manualAssignDeliveryOrders(ManualAssignDeliveryOrdersRequest request);

    DeliveryAssignmentResponse getDeliveryTrips(Long postOfficeId, PickupShift shift, LocalDate tripDate);

    DeliveryScanOutResponse scanOutDeliveryOrder(Long tripId, ScanOutDeliveryOrderRequest request);

    DeliveryPaymentInitResponse initiateTripDeliveryPayment(Long tripId, String orderCode, Long tenantId);

    DeliveryPaymentConfirmResponse confirmTripDeliveryPayment(
            Long tripId, String orderCode, ConfirmDeliveryPaymentRequest request, Long tenantId);

    DeliveryAssignmentResponse confirmTripDelivered(
            Long tripId, String orderCode, ConfirmDeliveryRequest request, MultipartFile photo, Long tenantId);

    DeliveryAssignmentResponse confirmTripDeliveryFailed(
            Long tripId, String orderCode, ConfirmDeliveryFailureRequest request, Long tenantId);

    DeliveryAssignmentResponse returnTripOrderToSender(
            Long tripId, String orderCode, ReturnToSenderRequest request, Long tenantId);

    DeliveryManifestResponse completeDeliveryTrip(Long tripId, Long tenantId);
}
