/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.caller.TmsOrderClient;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.domain.DeliveryManifest;
import serp.project.first_mile.domain.DeliveryManifestOrder;
import serp.project.first_mile.dto.request.ConfirmDeliveryFailureRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryRequest;
import serp.project.first_mile.dto.request.CreateDeliveryManifestRequest;
import serp.project.first_mile.dto.request.ReturnToSenderRequest;
import serp.project.first_mile.dto.response.DeliveryManifestOrderResponse;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
import serp.project.first_mile.enums.DeliveryManifestStatus;
import serp.project.first_mile.enums.DeliveryOrderStatus;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.repository.DeliveryManifestOrderRepository;
import serp.project.first_mile.repository.DeliveryManifestRepository;
import serp.project.first_mile.service.DeliveryManifestService;
import serp.project.first_mile.service.DeliveryRouteOptimizationService;
import serp.project.first_mile.service.TmsOrderTransitionOutboxService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

    private final DeliveryManifestRepository manifestRepository;
    private final DeliveryManifestOrderRepository manifestOrderRepository;
    private final DeliveryRouteOptimizationService routeOptimizationService;
    private final TmsOrderClient tmsOrderClient;
    private final TmsOrderTransitionOutboxService tmsOrderTransitionOutboxService;

    @Value("${app.delivery.max-attempts:3}")
    private int maxDeliveryAttempts;

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
        return toResponse(manifest);
    }

    @Override
    public List<DeliveryManifestResponse> getManifests(
            String postOfficeCode, DeliveryManifestStatus status, LocalDate date, Long tenantId) {
        List<DeliveryManifest> manifests;
        if (status != null) {
            manifests = manifestRepository.findByTenantIdAndPostOfficeCodeIgnoreCaseAndStatus(
                    tenantId, postOfficeCode, status);
        } else if (date != null) {
            manifests = manifestRepository.findByTenantIdAndPostOfficeCodeIgnoreCaseAndPlannedDate(
                    tenantId, postOfficeCode, date);
        } else {
            manifests = manifestRepository.findByTenantIdAndPostOfficeCodeIgnoreCase(tenantId, postOfficeCode);
        }
        return manifests.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryManifestResponse startDelivery(Long manifestId, Long tenantId) {
        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));

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
            Long manifestId, String orderCode, ConfirmDeliveryRequest request, Long tenantId) {

        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureManifestInProgress(manifest);

        DeliveryManifestOrder manifestOrder = manifestOrderRepository
                .findByManifestIdAndOrderCode(manifestId, orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (manifestOrder.getStatus() != DeliveryOrderStatus.OUT_FOR_DELIVERY) {
            throw new AppException(ErrorCode.DELIVERY_ORDER_INVALID_STATUS,
                    "Order must be OUT_FOR_DELIVERY to confirm delivery.");
        }

        // Update order
        manifestOrder.setStatus(DeliveryOrderStatus.DELIVERED);
        manifestOrder.setProofPhotoUrl(request.getProofPhotoUrl());
        manifestOrder.setCodCollected(safeAmount(request.getCodCollected()));
        manifestOrder.setShippingFeeCollected(safeAmount(request.getShippingFeeCollected()));
        manifestOrder.setDeliveredAt(request.getDeliveredAt() != null ? request.getDeliveredAt() : LocalDateTime.now());
        manifestOrder.setNote(request.getNote());

        // Update manifest totals
        manifest.setDeliveredCount(manifest.getDeliveredCount() + 1);
        manifest.setCollectedCodAmount(manifest.getCollectedCodAmount() + safeAmount(request.getCodCollected()));
        manifest.setCollectedShippingFee(manifest.getCollectedShippingFee() + safeAmount(request.getShippingFeeCollected()));

        // Transition order to DELIVERED via tms-order
        enqueueTransition(manifestOrder.getOrderCode(), OrderStatus.DELIVERED,
                "Delivered successfully", tenantId);

        // Update payment status if feePayer = RECEIVER and fee is fully collected
        if (RECEIVER.equalsIgnoreCase(manifestOrder.getFeePayer())
                && manifestOrder.getShippingFeeCollected() >= manifestOrder.getShippingFee()) {
            try {
                tmsOrderClient.updatePaymentStatus(orderCode, tenantId, "PAID");
            } catch (Exception e) {
                log.warn("Failed to update payment status for order {}: {}", orderCode, e.getMessage());
            }
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
    public DeliveryManifestResponse confirmFailed(
            Long manifestId, String orderCode, ConfirmDeliveryFailureRequest request, Long tenantId) {

        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));
        ensureManifestInProgress(manifest);

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
                .proofPhotoUrl(order.getProofPhotoUrl())
                .failureReason(order.getFailureReason())
                .deliveredAt(order.getDeliveredAt())
                .note(order.getNote())
                .build();
    }

    private long safeAmount(Long amount) {
        return amount != null ? amount : 0L;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
