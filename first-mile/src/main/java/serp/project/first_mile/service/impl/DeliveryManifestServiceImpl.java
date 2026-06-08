/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.caller.PaymentServiceCaller;
import serp.project.first_mile.caller.TmsOrderClient;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderRequest;
import serp.project.first_mile.caller.dto.payment.PaymentCreateOrderResponse;
import serp.project.first_mile.caller.dto.payment.PaymentQueryOrderResponse;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.domain.DeliveryManifest;
import serp.project.first_mile.domain.DeliveryManifestOrder;
import serp.project.first_mile.dto.request.ConfirmDeliveryFailureRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryPaymentRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryRequest;
import serp.project.first_mile.dto.request.CreateDeliveryManifestRequest;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.request.ReturnToSenderRequest;
import serp.project.first_mile.dto.response.DeliveryManifestOrderResponse;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentConfirmResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentInitResponse;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.enums.DeliveryManifestStatus;
import serp.project.first_mile.enums.DeliveryOrderStatus;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PaymentStatus;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.kernel.utils.ImageContentTypeUtils;
import serp.project.first_mile.repository.DeliveryManifestOrderRepository;
import serp.project.first_mile.repository.DeliveryManifestRepository;
import serp.project.first_mile.service.DeliveryManifestService;
import serp.project.first_mile.service.DeliveryRouteOptimizationService;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.TmsOrderTransitionOutboxService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeliveryManifestServiceImpl implements DeliveryManifestService {

    private static final String TRANSITION_SOURCE = "LAST_MILE_DELIVERY";
    private static final String RECEIVER = "RECEIVER";
    private static final String STORAGE_SERVICE_NAME = "first-mile";
    private static final String PAYMENT_SOURCE_SERVICE = "first-mile";
    private static final String PAYMENT_SOURCE = "last-mile-delivery";
    private static final long MIN_PAYMENT_SERVICE_AMOUNT = 1_000L;
    private static final String DELIVERY_CHECKIN_IMAGE_FOLDER = "orders/delivery-checkin";
    private static final double EARTH_RADIUS_METERS = 6_371_000D;

    private final DeliveryManifestRepository manifestRepository;
    private final DeliveryManifestOrderRepository manifestOrderRepository;
    private final DeliveryRouteOptimizationService routeOptimizationService;
    private final PaymentServiceCaller paymentServiceCaller;
    private final TmsOrderClient tmsOrderClient;
    private final TmsOrderTransitionOutboxService tmsOrderTransitionOutboxService;
    private final FirstMileAccessUtils firstMileAccessUtils;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Value("${app.delivery.max-attempts:3}")
    private int maxDeliveryAttempts;

    @Value("${payment.service.redirect-url:http://localhost:3000/payment/result}")
    private String paymentRedirectUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryManifestResponse createManifest(CreateDeliveryManifestRequest request, Long tenantId) {
        validateCreateManifestRequest(request);
        String postOfficeCode = request.getPostOfficeCode().trim();
        List<String> orderCodes = request.getOrderCodes().stream()
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (orderCodes.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "orderCodes must not be empty.");
        }

        // Lookup orders from tms-order
        List<TmsOrderOperationView> tmsOrders = tmsOrderClient.lookupByCodes(tenantId, orderCodes);

        // Validate all orders are READY_FOR_DELIVERY
        for (TmsOrderOperationView order : tmsOrders) {
            if (order.getStatus() != OrderStatus.READY_FOR_DELIVERY) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE,
                        "Order " + order.getOrderCode() + " is not READY_FOR_DELIVERY.");
            }
            if (!postOfficeCode.equalsIgnoreCase(order.getDestinationPostOfficeCode())) {
                throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE,
                        "Order " + order.getOrderCode() + " does not belong to post office " + postOfficeCode);
            }
            ensureOrderNotInActiveDeliveryManifest(tenantId, order.getOrderCode());
        }

        Map<String, TmsOrderOperationView> orderMap = tmsOrders.stream()
                .collect(Collectors.toMap(o -> o.getOrderCode().toUpperCase(), Function.identity()));

        // Build manifest
        DeliveryManifest manifest = DeliveryManifest.builder()
                .tenantId(tenantId)
                .manifestCode(generateManifestCode(tenantId))
                .postOfficeCode(postOfficeCode)
                .courierId(request.getCourierId())
                .vehicleId(request.getVehicleId())
                .status(DeliveryManifestStatus.CREATED)
                .plannedDate(request.getPlannedDate() != null ? request.getPlannedDate() : LocalDate.now())
                .plannedDepartureAt(request.getPlannedDepartureAt())
                .totalOrders(tmsOrders.size())
                .note(request.getNote())
                .build();

        // Build manifest orders from tms-order data
        List<DeliveryManifestOrder> manifestOrders = tmsOrders.stream()
                .map(o -> buildManifestOrder(o, manifest, tenantId))
                .collect(Collectors.toList());

        // Optimize route
        List<DeliveryManifestOrder> optimized = routeOptimizationService.optimizeRoute(
                postOfficeCode, manifestOrders);

        // Calculate totals
        long totalCod = 0L;
        long totalFee = 0L;
        for (DeliveryManifestOrder order : optimized) {
            totalCod += safeAmount(order.getCodAmount());
            if (RECEIVER.equalsIgnoreCase(order.getFeePayer())) {
                totalFee += safeAmount(order.getShippingFee());
            }
        }
        manifest.setTotalCodAmount(totalCod);
        manifest.setTotalShippingFee(totalFee);

        // Save
        for (DeliveryManifestOrder order : optimized) {
            manifest.addOrder(order);
        }
        DeliveryManifest saved = manifestRepository.save(manifest);

        return toResponse(saved);
    }

    @Override
    public DeliveryManifestResponse getManifest(Long manifestId, Long tenantId) {
        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureCanViewManifest(manifest, tenantId);
        return toResponse(manifest);
    }

    @Override
    public List<DeliveryManifestResponse> getManifests(
            String postOfficeCode, DeliveryManifestStatus status, LocalDate date, Long tenantId) {
        List<DeliveryManifest> manifests;

        if (isCourierScopedAccess()) {
            Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                    tenantId,
                    PostOfficeStaffRole.COURIER
            );
            if (status != null) {
                manifests = manifestRepository.findByTenantIdAndCourierIdAndStatus(
                        tenantId, currentCourierStaffId, status);
            } else if (date != null) {
                manifests = manifestRepository.findByTenantIdAndCourierIdAndPlannedDate(
                        tenantId, currentCourierStaffId, date);
            } else {
                manifests = manifestRepository.findByTenantIdAndCourierId(tenantId, currentCourierStaffId);
            }
            if (hasText(postOfficeCode)) {
                String normalizedPostOfficeCode = postOfficeCode.trim();
                manifests = manifests.stream()
                        .filter(manifest -> normalizedPostOfficeCode.equalsIgnoreCase(manifest.getPostOfficeCode()))
                        .toList();
            }
            return manifests.stream().map(this::toResponse).toList();
        }

        if (!hasText(postOfficeCode)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "postOfficeCode is required.");
        }

        String normalizedPostOfficeCode = postOfficeCode.trim();
        if (status != null) {
            manifests = manifestRepository.findByTenantIdAndPostOfficeCodeIgnoreCaseAndStatus(
                    tenantId, normalizedPostOfficeCode, status);
        } else if (date != null) {
            manifests = manifestRepository.findByTenantIdAndPostOfficeCodeIgnoreCaseAndPlannedDate(
                    tenantId, normalizedPostOfficeCode, date);
        } else {
            manifests = manifestRepository.findByTenantIdAndPostOfficeCodeIgnoreCase(tenantId, normalizedPostOfficeCode);
        }
        return manifests.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryManifestResponse startDelivery(Long manifestId, Long tenantId) {
        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureCurrentCourierOwnsManifest(manifest, tenantId);

        if (manifest.getStatus() != DeliveryManifestStatus.CREATED) {
            throw new AppException(ErrorCode.DELIVERY_MANIFEST_INVALID_STATUS,
                    "Manifest must be in CREATED status to start delivery.");
        }

        manifest.setStatus(DeliveryManifestStatus.IN_PROGRESS);
        manifest.setActualDepartureAt(LocalDateTime.now());

        // Mark all orders as OUT_FOR_DELIVERY
        for (DeliveryManifestOrder order : manifest.getOrders()) {
            if (order.getStatus() == DeliveryOrderStatus.PENDING) {
                order.setStatus(DeliveryOrderStatus.OUT_FOR_DELIVERY);
            }
        }

        DeliveryManifest saved = manifestRepository.save(manifest);
        enqueueTransitions(
                manifest.getOrders().stream().map(DeliveryManifestOrder::getOrderCode).toList(),
                OrderStatus.OUT_FOR_DELIVERY,
                "Courier departed with delivery manifest " + saved.getManifestCode(),
                tenantId
        );
        return toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryManifestResponse confirmDelivered(
            Long manifestId, String orderCode, ConfirmDeliveryRequest request, MultipartFile photo, Long tenantId) {

        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureManifestInProgress(manifest);
        ensureCurrentCourierOwnsManifest(manifest, tenantId);

        DeliveryManifestOrder manifestOrder = manifestOrderRepository
                .findByManifestIdAndOrderCode(manifestId, orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (manifestOrder.getStatus() != DeliveryOrderStatus.OUT_FOR_DELIVERY) {
            throw new AppException(ErrorCode.DELIVERY_ORDER_INVALID_STATUS,
                    "Order must be OUT_FOR_DELIVERY to confirm delivery.");
        }

        validateDeliveryCheckinRequest(request, photo);
        validateDeliveryPayment(manifestOrder);
        long codCollected = requiredCodAmount(manifestOrder);
        long shippingFeeCollected = requiredReceiverShippingFee(manifestOrder);

        double distanceMeters = calculateDeliveryDistanceMeters(
                request.getLatitude(),
                request.getLongitude(),
                manifestOrder.getReceiverLat(),
                manifestOrder.getReceiverLng()
        );
        String contentType = ImageContentTypeUtils.normalizeImageContentType(photo.getContentType());
        FileUploadResponse uploadResponse = uploadDeliveryCheckinPhoto(photo, contentType, tenantId);

        // Update order
        manifestOrder.setStatus(DeliveryOrderStatus.DELIVERED);
        manifestOrder.setProofPhotoUrl(uploadResponse.getUrl());
        manifestOrder.setCodCollected(codCollected);
        manifestOrder.setShippingFeeCollected(shippingFeeCollected);
        manifestOrder.setDeliveredAt(request.getDeliveredAt() != null ? request.getDeliveredAt() : LocalDateTime.now());
        manifestOrder.setDeliveryCheckinLat(request.getLatitude());
        manifestOrder.setDeliveryCheckinLng(request.getLongitude());
        manifestOrder.setDeliveryCheckinDistanceM(round3(distanceMeters));
        manifestOrder.setNote(request.getNote());

        // Update manifest totals
        manifest.setDeliveredCount(manifest.getDeliveredCount() + 1);
        manifest.setCollectedCodAmount(manifest.getCollectedCodAmount() + codCollected);
        manifest.setCollectedShippingFee(manifest.getCollectedShippingFee() + shippingFeeCollected);

        // Transition order to DELIVERED via tms-order
        enqueueTransition(manifestOrder.getOrderCode(), OrderStatus.DELIVERED,
                "Delivered successfully", tenantId);

        markReceiverShippingFeePaidIfNeeded(manifestOrder, tenantId);

        // Check if all orders processed → complete manifest
        if (manifest.isAllProcessed()) {
            manifest.setStatus(DeliveryManifestStatus.COMPLETED);
            manifest.setActualReturnAt(LocalDateTime.now());
        }

        return toResponse(manifestRepository.save(manifest));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryPaymentInitResponse initiateDeliveryPayment(Long manifestId, String orderCode, Long tenantId) {
        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureManifestInProgress(manifest);
        ensureCurrentCourierOwnsManifest(manifest, tenantId);

        DeliveryManifestOrder manifestOrder = manifestOrderRepository
                .findByManifestIdAndOrderCodeForUpdate(manifestId, orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        ensureOrderOutForDelivery(manifestOrder);

        long requiredAmount = requiredDeliveryPaymentAmount(manifestOrder);
        if (requiredAmount <= 0L) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "This delivery does not require customer payment.");
        }
        if (requiredAmount < MIN_PAYMENT_SERVICE_AMOUNT) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Customer payment amount must be at least 1,000 VND for payment service."
            );
        }

        if (PaymentStatus.PAID.equals(manifestOrder.getDeliveryPaymentStatus())) {
            return new DeliveryPaymentInitResponse(
                    manifestId,
                    manifestOrder.getOrderCode(),
                    requiredAmount,
                    manifestOrder.getDeliveryPaymentStatus(),
                    manifestOrder.getDeliveryPaymentAppTransId(),
                    null,
                    "SUCCESS",
                    "Customer payment is already confirmed."
            );
        }

        PaymentCreateOrderRequest paymentRequest = buildDeliveryPaymentRequest(manifestId, manifestOrder, requiredAmount, tenantId);
        PaymentCreateOrderResponse paymentResponse = paymentServiceCaller.createOrder(paymentRequest);
        if (paymentResponse.getStatus() == null || !"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    paymentResponse.getMessage() == null
                            ? "Cannot create payment order for delivery customer payment."
                            : paymentResponse.getMessage()
            );
        }

        manifestOrder.setDeliveryPaymentStatus(PaymentStatus.UNPAID);
        manifestOrder.setDeliveryPaymentAmount(requiredAmount);
        manifestOrder.setDeliveryPaymentAppTransId(paymentResponse.getAppTransId());
        manifestOrder.setDeliveryPaymentConfirmedAt(null);
        manifestOrderRepository.save(manifestOrder);

        return new DeliveryPaymentInitResponse(
                manifestId,
                manifestOrder.getOrderCode(),
                requiredAmount,
                manifestOrder.getDeliveryPaymentStatus(),
                paymentResponse.getAppTransId(),
                paymentResponse.getOrderUrl(),
                paymentResponse.getStatus(),
                paymentResponse.getMessage()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryPaymentConfirmResponse confirmDeliveryPayment(
            Long manifestId, String orderCode, ConfirmDeliveryPaymentRequest request, Long tenantId) {
        if (request == null || !hasText(request.getAppTransId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "appTransId is required.");
        }

        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureManifestInProgress(manifest);
        ensureCurrentCourierOwnsManifest(manifest, tenantId);

        DeliveryManifestOrder manifestOrder = manifestOrderRepository
                .findByManifestIdAndOrderCodeForUpdate(manifestId, orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        ensureOrderOutForDelivery(manifestOrder);

        long requiredAmount = requiredDeliveryPaymentAmount(manifestOrder);
        if (requiredAmount <= 0L) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "This delivery does not require customer payment.");
        }

        String expectedAppTransId = normalizeText(manifestOrder.getDeliveryPaymentAppTransId());
        String requestedAppTransId = request.getAppTransId().trim();
        if (expectedAppTransId == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Customer payment must be initiated before confirmation.");
        }
        if (!expectedAppTransId.equals(requestedAppTransId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Payment transaction does not match this delivery order.");
        }

        if (PaymentStatus.PAID.equals(manifestOrder.getDeliveryPaymentStatus())) {
            ensureConfirmedPaymentAmountCoversRequiredAmount(manifestOrder, requiredAmount);
            return new DeliveryPaymentConfirmResponse(
                    manifestId,
                    manifestOrder.getOrderCode(),
                    manifestOrder.getDeliveryPaymentAmount(),
                    manifestOrder.getDeliveryPaymentStatus(),
                    expectedAppTransId,
                    "SUCCESS",
                    "Customer payment is already confirmed."
            );
        }

        PaymentQueryOrderResponse queryResponse = paymentServiceCaller.queryOrderStatus(expectedAppTransId);
        String gatewayStatus = queryResponse.getStatus() == null ? "UNKNOWN" : queryResponse.getStatus();
        if (!"SUCCESS".equalsIgnoreCase(gatewayStatus)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Payment status is not successful yet. Current status: " + gatewayStatus
            );
        }
        if (queryResponse.getAmount() != null && queryResponse.getAmount() < requiredAmount) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Paid amount is lower than required customer payment."
            );
        }

        manifestOrder.setDeliveryPaymentStatus(PaymentStatus.PAID);
        manifestOrder.setDeliveryPaymentAmount(requiredAmount);
        manifestOrder.setDeliveryPaymentConfirmedAt(LocalDateTime.now());
        DeliveryManifestOrder savedOrder = manifestOrderRepository.save(manifestOrder);
        markReceiverShippingFeePaidIfNeeded(savedOrder, tenantId);

        return new DeliveryPaymentConfirmResponse(
                manifestId,
                savedOrder.getOrderCode(),
                savedOrder.getDeliveryPaymentAmount(),
                savedOrder.getDeliveryPaymentStatus(),
                expectedAppTransId,
                gatewayStatus,
                queryResponse.getMessage()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryManifestResponse confirmFailed(
            Long manifestId, String orderCode, ConfirmDeliveryFailureRequest request, Long tenantId) {

        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureManifestInProgress(manifest);
        ensureCurrentCourierOwnsManifest(manifest, tenantId);

        DeliveryManifestOrder manifestOrder = manifestOrderRepository
                .findByManifestIdAndOrderCode(manifestId, orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (manifestOrder.getStatus() != DeliveryOrderStatus.OUT_FOR_DELIVERY) {
            throw new AppException(ErrorCode.DELIVERY_ORDER_INVALID_STATUS,
                    "Order must be OUT_FOR_DELIVERY to mark as failed.");
        }

        manifestOrder.setStatus(DeliveryOrderStatus.FAILED);
        manifestOrder.setFailureReason(request.getFailureReason());
        manifestOrder.setNote(request.getNote());
        manifestOrder.setDeliveryAttemptCount(manifestOrder.getDeliveryAttemptCount() + 1);

        manifest.setFailedCount(manifest.getFailedCount() + 1);

        // Transition to DELIVERY_FAILED
        enqueueTransition(manifestOrder.getOrderCode(), OrderStatus.DELIVERY_FAILED,
                "Delivery failed: " + request.getFailureReason(), tenantId);

        // Auto return if max attempts exceeded
        if (manifestOrder.getDeliveryAttemptCount() >= maxDeliveryAttempts) {
            manifestOrder.setStatus(DeliveryOrderStatus.RETURNED);
            enqueueTransition(manifestOrder.getOrderCode(), OrderStatus.RETURNED_TO_SENDER,
                    "Max delivery attempts (" + maxDeliveryAttempts + ") exceeded", tenantId);
        }

        // Check if all orders processed → complete manifest
        if (manifest.isAllProcessed()) {
            manifest.setStatus(DeliveryManifestStatus.COMPLETED);
            manifest.setActualReturnAt(LocalDateTime.now());
        }

        return toResponse(manifestRepository.save(manifest));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryManifestResponse returnToSender(
            Long manifestId, String orderCode, ReturnToSenderRequest request, Long tenantId) {

        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureCurrentCourierOwnsManifest(manifest, tenantId);

        DeliveryManifestOrder manifestOrder = manifestOrderRepository
                .findByManifestIdAndOrderCode(manifestId, orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (manifestOrder.getStatus() != DeliveryOrderStatus.FAILED) {
            throw new AppException(ErrorCode.DELIVERY_ORDER_INVALID_STATUS,
                    "Order must be FAILED to return to sender.");
        }

        manifestOrder.setStatus(DeliveryOrderStatus.RETURNED);
        manifestOrder.setNote(request.getNote());

        enqueueTransition(manifestOrder.getOrderCode(), OrderStatus.RETURNED_TO_SENDER,
                "Returned to sender: " + (request.getNote() != null ? request.getNote() : ""), tenantId);

        if (manifest.isAllProcessed()) {
            manifest.setStatus(DeliveryManifestStatus.COMPLETED);
            manifest.setActualReturnAt(LocalDateTime.now());
        }

        return toResponse(manifestRepository.save(manifest));
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private void validateCreateManifestRequest(CreateDeliveryManifestRequest request) {
        if (request == null || !hasText(request.getPostOfficeCode())
                || request.getOrderCodes() == null || request.getOrderCodes().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "postOfficeCode and orderCodes are required.");
        }
    }

    private void ensureOrderNotInActiveDeliveryManifest(Long tenantId, String orderCode) {
        List<DeliveryManifestOrder> activeOrders = manifestOrderRepository
                .findByTenantIdAndOrderCodeAndStatusIn(
                        tenantId,
                        orderCode,
                        List.of(DeliveryOrderStatus.PENDING, DeliveryOrderStatus.OUT_FOR_DELIVERY)
                );
        if (!activeOrders.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_NOT_ASSIGNABLE,
                    "Order " + orderCode + " is already in an active delivery manifest.");
        }
    }

    private void ensureManifestInProgress(DeliveryManifest manifest) {
        if (manifest.getStatus() != DeliveryManifestStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.DELIVERY_MANIFEST_INVALID_STATUS,
                    "Manifest must be IN_PROGRESS to update delivery results.");
        }
    }

    private void ensureCanViewManifest(DeliveryManifest manifest, Long tenantId) {
        if (!isCourierScopedAccess()) {
            return;
        }
        ensureCurrentCourierOwnsManifest(manifest, tenantId);
    }

    private boolean isCourierScopedAccess() {
        return firstMileAccessUtils.isCourier()
                && !firstMileAccessUtils.isAdmin()
                && !firstMileAccessUtils.isPostOfficerManager();
    }

    private void ensureCurrentCourierOwnsManifest(DeliveryManifest manifest, Long tenantId) {
        if (manifest == null || manifest.getCourierId() == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Delivery manifest must be assigned to a courier before driver actions."
            );
        }

        Long currentCourierStaffId = firstMileAccessUtils.resolveCurrentStaffIdByRoleOrThrow(
                tenantId,
                PostOfficeStaffRole.COURIER
        );
        if (!Objects.equals(currentCourierStaffId, manifest.getCourierId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateDeliveryCheckinRequest(ConfirmDeliveryRequest request, MultipartFile photo) {
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Delivery check-in request is required.");
        }
        if (request.getLatitude() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "latitude is required.");
        }
        if (request.getLongitude() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "longitude is required.");
        }
        if (!isValidCoordinate(request.getLatitude(), request.getLongitude())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    String.format(
                            Locale.ROOT,
                            "Invalid check-in coordinates (latitude=%s, longitude=%s). latitude must be between -90 and 90 and longitude between -180 and 180.",
                            request.getLatitude(),
                            request.getLongitude()
                    )
            );
        }
        if (photo == null || photo.isEmpty()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }
    }

    private void validateDeliveryPayment(
            DeliveryManifestOrder manifestOrder
    ) {
        long requiredPayment = requiredDeliveryPaymentAmount(manifestOrder);
        if (requiredPayment > 0 && !PaymentStatus.PAID.equals(manifestOrder.getDeliveryPaymentStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Customer payment must be confirmed through payment service before delivery check-in."
            );
        }
        ensureConfirmedPaymentAmountCoversRequiredAmount(manifestOrder, requiredPayment);
    }

    private DeliveryManifestOrder buildManifestOrder(
            TmsOrderOperationView tmsOrder, DeliveryManifest manifest, Long tenantId) {
        long shippingFee = RECEIVER.equalsIgnoreCase(tmsOrder.getFeePayer())
                ? safeAmount(tmsOrder.getTotalShippingFee())
                : 0L;

        return DeliveryManifestOrder.builder()
                .tenantId(tenantId)
                .manifest(manifest)
                .orderId(tmsOrder.getId())
                .orderCode(tmsOrder.getOrderCode())
                .status(DeliveryOrderStatus.PENDING)
                .receiverName(tmsOrder.getReceiverName())
                .receiverPhone(tmsOrder.getReceiverPhone())
                .receiverAddressDetail(tmsOrder.getReceiverAddressDetail())
                .receiverWardCode(tmsOrder.getReceiverWardCode())
                .receiverProvinceCode(tmsOrder.getReceiverProvinceCode())
                .receiverLat(tmsOrder.getReceiverLatitude())
                .receiverLng(tmsOrder.getReceiverLongitude())
                .codAmount(safeAmount(tmsOrder.getCodAmount()))
                .shippingFee(shippingFee)
                .feePayer(tmsOrder.getFeePayer())
                .deliveryPaymentStatus(PaymentStatus.UNPAID)
                .deliveryPaymentAmount(safeAmount(tmsOrder.getCodAmount()) + shippingFee)
                .deliveryAttemptCount(0)
                .build();
    }

    private String generateManifestCode(Long tenantId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "DM-" + dateStr + "-" + tenantId + "-";
        return manifestRepository.findTopByTenantIdAndManifestCodeStartingWithOrderByManifestCodeDesc(tenantId, prefix)
                .map(m -> {
                    String code = m.getManifestCode();
                    String seqStr = code.substring(prefix.length());
                    int seq = Integer.parseInt(seqStr) + 1;
                    return String.format("%s%05d", prefix, seq);
                })
                .orElse(prefix + "00001");
    }

    private void enqueueTransition(String orderCode, OrderStatus targetStatus, String description, Long tenantId) {
        tmsOrderTransitionOutboxService.enqueue(TmsOrderStatusTransitionRequest.builder()
                .source(TRANSITION_SOURCE)
                .idempotencyKey(UUID.randomUUID().toString())
                .items(List.of(TmsOrderStatusTransitionRequest.Item.builder()
                        .orderCode(orderCode)
                        .targetStatus(targetStatus)
                        .description(description)
                        .build()))
                .build(), tenantId);
    }

    private void enqueueTransitions(List<String> orderCodes, OrderStatus targetStatus,
                                    String description, Long tenantId) {
        List<TmsOrderStatusTransitionRequest.Item> items = orderCodes.stream()
                .filter(this::hasText)
                .map(orderCode -> TmsOrderStatusTransitionRequest.Item.builder()
                        .orderCode(orderCode)
                        .targetStatus(targetStatus)
                        .description(description)
                        .build())
                .toList();

        tmsOrderTransitionOutboxService.enqueue(TmsOrderStatusTransitionRequest.builder()
                .source(TRANSITION_SOURCE)
                .idempotencyKey(UUID.randomUUID().toString())
                .items(items)
                .build(), tenantId);
    }

    private DeliveryManifestResponse toResponse(DeliveryManifest manifest) {
        List<DeliveryManifestOrderResponse> orderResponses = manifest.getOrders() != null
                ? manifest.getOrders().stream().map(this::toOrderResponse).toList()
                : List.of();

        return DeliveryManifestResponse.builder()
                .id(manifest.getId())
                .manifestCode(manifest.getManifestCode())
                .postOfficeCode(manifest.getPostOfficeCode())
                .courierId(manifest.getCourierId())
                .courierName(manifest.getCourierName())
                .vehicleId(manifest.getVehicleId())
                .status(manifest.getStatus())
                .plannedDate(manifest.getPlannedDate())
                .plannedDepartureAt(manifest.getPlannedDepartureAt())
                .actualDepartureAt(manifest.getActualDepartureAt())
                .actualReturnAt(manifest.getActualReturnAt())
                .totalOrders(manifest.getTotalOrders())
                .deliveredCount(manifest.getDeliveredCount())
                .failedCount(manifest.getFailedCount())
                .totalCodAmount(manifest.getTotalCodAmount())
                .collectedCodAmount(manifest.getCollectedCodAmount())
                .totalShippingFee(manifest.getTotalShippingFee())
                .collectedShippingFee(manifest.getCollectedShippingFee())
                .note(manifest.getNote())
                .createdAt(manifest.getCreatedAt())
                .orders(orderResponses)
                .build();
    }

    private DeliveryManifestOrderResponse toOrderResponse(DeliveryManifestOrder order) {
        return DeliveryManifestOrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .orderCode(order.getOrderCode())
                .sequence(order.getSequence())
                .deliveryAttemptCount(order.getDeliveryAttemptCount())
                .status(order.getStatus())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddressDetail(order.getReceiverAddressDetail())
                .receiverWardCode(order.getReceiverWardCode())
                .receiverProvinceCode(order.getReceiverProvinceCode())
                .receiverLat(order.getReceiverLat())
                .receiverLng(order.getReceiverLng())
                .codAmount(order.getCodAmount())
                .codCollected(order.getCodCollected())
                .shippingFee(order.getShippingFee())
                .shippingFeeCollected(order.getShippingFeeCollected())
                .feePayer(order.getFeePayer())
                .deliveryPaymentStatus(order.getDeliveryPaymentStatus())
                .deliveryPaymentAmount(order.getDeliveryPaymentAmount())
                .deliveryPaymentAppTransId(order.getDeliveryPaymentAppTransId())
                .deliveryPaymentConfirmedAt(order.getDeliveryPaymentConfirmedAt())
                .proofPhotoUrl(order.getProofPhotoUrl())
                .failureReason(order.getFailureReason())
                .deliveredAt(order.getDeliveredAt())
                .deliveryCheckinLat(order.getDeliveryCheckinLat())
                .deliveryCheckinLng(order.getDeliveryCheckinLng())
                .deliveryCheckinDistanceM(order.getDeliveryCheckinDistanceM())
                .note(order.getNote())
                .build();
    }

    private FileUploadResponse uploadDeliveryCheckinPhoto(MultipartFile photo, String contentType, Long tenantId) {
        try {
            return fileStorageService.upload(FileUploadRequest.builder()
                    .content(photo.getBytes())
                    .originalFileName(photo.getOriginalFilename())
                    .contentType(contentType)
                    .serviceName(STORAGE_SERVICE_NAME)
                    .folder(DELIVERY_CHECKIN_IMAGE_FOLDER)
                    .tenantId(tenantId)
                    .uploaderId(firstMileAccessUtils.getCurrentUserIdOrNull())
                    .publicFile(true)
                    .build());
        } catch (IOException exception) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private long safeAmount(Long amount) {
        return amount != null ? amount : 0L;
    }

    private long requiredDeliveryPaymentAmount(DeliveryManifestOrder manifestOrder) {
        return requiredCodAmount(manifestOrder) + requiredReceiverShippingFee(manifestOrder);
    }

    private long requiredCodAmount(DeliveryManifestOrder manifestOrder) {
        return safeAmount(manifestOrder.getCodAmount());
    }

    private long requiredReceiverShippingFee(DeliveryManifestOrder manifestOrder) {
        return RECEIVER.equalsIgnoreCase(manifestOrder.getFeePayer())
                ? safeAmount(manifestOrder.getShippingFee())
                : 0L;
    }

    private void ensureOrderOutForDelivery(DeliveryManifestOrder manifestOrder) {
        if (manifestOrder.getStatus() != DeliveryOrderStatus.OUT_FOR_DELIVERY) {
            throw new AppException(
                    ErrorCode.DELIVERY_ORDER_INVALID_STATUS,
                    "Order must be OUT_FOR_DELIVERY to process delivery payment."
            );
        }
    }

    private PaymentCreateOrderRequest buildDeliveryPaymentRequest(
            Long manifestId,
            DeliveryManifestOrder manifestOrder,
            long requiredAmount,
            Long tenantId
    ) {
        Long actorId = firstMileAccessUtils.getCurrentUserIdOrNull();
        String orderCode = manifestOrder.getOrderCode();
        return PaymentCreateOrderRequest.builder()
                .appUser(orderCode)
                .amount(requiredAmount)
                .description("Customer payment for delivery order " + orderCode)
                .embedData(PaymentCreateOrderRequest.EmbedData.builder()
                        .redirectUrl(paymentRedirectUrl
                                + "?source=first-mile&manifestId=" + manifestId
                                + "&orderCode=" + orderCode)
                        .merchantInfo(buildDeliveryPaymentMerchantInfo(manifestId, manifestOrder, requiredAmount, tenantId, actorId))
                        .build())
                .title("Delivery payment - " + orderCode)
                .tenantId(tenantId)
                .actorId(actorId)
                .userId(actorId)
                .items(List.of(PaymentCreateOrderRequest.Item.builder()
                        .itemId("delivery-payment-" + orderCode)
                        .itemName("Customer payment for delivery order " + orderCode)
                        .itemPrice(requiredAmount)
                        .itemQuantity(1)
                        .build()))
                .build();
    }

    private String buildDeliveryPaymentMerchantInfo(
            Long manifestId,
            DeliveryManifestOrder manifestOrder,
            long requiredAmount,
            Long tenantId,
            Long actorId
    ) {
        try {
            Map<String, Object> merchantInfo = new java.util.LinkedHashMap<>();
            merchantInfo.put("sourceService", PAYMENT_SOURCE_SERVICE);
            merchantInfo.put("source", PAYMENT_SOURCE);
            merchantInfo.put("tenantId", tenantId);
            merchantInfo.put("actorId", actorId);
            merchantInfo.put("userId", actorId);
            merchantInfo.put("manifestId", manifestId);
            merchantInfo.put("orderCode", manifestOrder.getOrderCode());
            merchantInfo.put("amount", requiredAmount);
            merchantInfo.put("codAmount", requiredCodAmount(manifestOrder));
            merchantInfo.put("shippingFee", requiredReceiverShippingFee(manifestOrder));
            return objectMapper.writeValueAsString(merchantInfo);
        } catch (JsonProcessingException exception) {
            throw new AppException(
                    ErrorCode.UNCATEGORIZED_EXCEPTION,
                    "Cannot serialize delivery payment metadata."
            );
        }
    }

    private void markReceiverShippingFeePaidIfNeeded(DeliveryManifestOrder manifestOrder, Long tenantId) {
        if (requiredReceiverShippingFee(manifestOrder) <= 0L) {
            return;
        }
        try {
            tmsOrderClient.updatePaymentStatus(manifestOrder.getOrderCode(), tenantId, "PAID");
        } catch (Exception e) {
            log.warn("Failed to update payment status for order {}: {}", manifestOrder.getOrderCode(), e.getMessage());
        }
    }

    private void ensureConfirmedPaymentAmountCoversRequiredAmount(
            DeliveryManifestOrder manifestOrder,
            long requiredPayment
    ) {
        if (requiredPayment > 0 && safeAmount(manifestOrder.getDeliveryPaymentAmount()) < requiredPayment) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Confirmed customer payment amount is insufficient.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private double calculateDeliveryDistanceMeters(
            double checkinLatitude,
            double checkinLongitude,
            Double receiverLatitude,
            Double receiverLongitude
    ) {
        if (receiverLatitude == null
                || receiverLongitude == null
                || !isValidCoordinate(receiverLatitude, receiverLongitude)) {
            return 0D;
        }

        double latitudeDeltaRadians = Math.toRadians(receiverLatitude - checkinLatitude);
        double longitudeDeltaRadians = Math.toRadians(receiverLongitude - checkinLongitude);

        double checkinLatitudeRadians = Math.toRadians(checkinLatitude);
        double receiverLatitudeRadians = Math.toRadians(receiverLatitude);

        double haversineComponent = Math.sin(latitudeDeltaRadians / 2) * Math.sin(latitudeDeltaRadians / 2)
                + Math.cos(checkinLatitudeRadians) * Math.cos(receiverLatitudeRadians)
                * Math.sin(longitudeDeltaRadians / 2) * Math.sin(longitudeDeltaRadians / 2);

        double normalizedHaversineComponent = Math.max(0D, Math.min(1D, haversineComponent));
        double centralAngle = 2 * Math.atan2(
                Math.sqrt(normalizedHaversineComponent),
                Math.sqrt(1D - normalizedHaversineComponent)
        );

        return EARTH_RADIUS_METERS * centralAngle;
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return !Double.isNaN(latitude)
                && !Double.isNaN(longitude)
                && !Double.isInfinite(latitude)
                && !Double.isInfinite(longitude)
                && latitude >= -90.0
                && latitude <= 90.0
                && longitude >= -180.0
                && longitude <= 180.0;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
