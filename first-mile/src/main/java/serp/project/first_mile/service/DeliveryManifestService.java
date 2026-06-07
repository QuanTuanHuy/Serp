/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.ConfirmDeliveryFailureRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryPaymentRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryRequest;
import serp.project.first_mile.dto.request.CreateDeliveryManifestRequest;
import serp.project.first_mile.dto.request.ReturnToSenderRequest;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentConfirmResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentInitResponse;
import serp.project.first_mile.enums.DeliveryManifestStatus;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryManifestService {

    DeliveryManifestResponse createManifest(CreateDeliveryManifestRequest request, Long tenantId);

    DeliveryManifestResponse getManifest(Long manifestId, Long tenantId);

    List<DeliveryManifestResponse> getManifests(
            String postOfficeCode, DeliveryManifestStatus status, LocalDate date, Long tenantId);

    DeliveryManifestResponse confirmDelivered(
            Long manifestId, String orderCode, ConfirmDeliveryRequest request, MultipartFile photo, Long tenantId);

    DeliveryPaymentInitResponse initiateDeliveryPayment(Long manifestId, String orderCode, Long tenantId);

    DeliveryPaymentConfirmResponse confirmDeliveryPayment(
            Long manifestId, String orderCode, ConfirmDeliveryPaymentRequest request, Long tenantId);

    DeliveryManifestResponse confirmFailed(
            Long manifestId, String orderCode, ConfirmDeliveryFailureRequest request, Long tenantId);

    DeliveryManifestResponse returnToSender(
            Long manifestId, String orderCode, ReturnToSenderRequest request, Long tenantId);

    DeliveryManifestResponse startDelivery(Long manifestId, Long tenantId);
}
