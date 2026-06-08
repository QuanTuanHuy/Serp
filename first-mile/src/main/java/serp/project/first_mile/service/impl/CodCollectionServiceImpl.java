/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.DeliveryManifest;
import serp.project.first_mile.dto.response.DeliveryManifestOrderResponse;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.repository.DeliveryManifestRepository;
import serp.project.first_mile.service.CodCollectionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CodCollectionServiceImpl implements CodCollectionService {

    private final DeliveryManifestRepository manifestRepository;

    @Override
    public DeliveryManifestResponse getFinancialSummary(Long manifestId, Long tenantId) {
        DeliveryManifest manifest = manifestRepository.findByIdAndTenantId(manifestId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_MANIFEST_NOT_FOUND));

        List<DeliveryManifestOrderResponse> orderResponses = manifest.getOrders().stream()
                .map(order -> DeliveryManifestOrderResponse.builder()
                        .id(order.getId())
                        .orderId(order.getOrderId())
                        .orderCode(order.getOrderCode())
                        .sequence(order.getSequence())
                        .status(order.getStatus())
                        .codAmount(order.getCodAmount())
                        .codCollected(order.getCodCollected())
                        .shippingFee(order.getShippingFee())
                        .shippingFeeCollected(order.getShippingFeeCollected())
                        .feePayer(order.getFeePayer())
                        .receiverName(order.getReceiverName())
                        .receiverPhone(order.getReceiverPhone())
                        .build())
                .toList();

        return DeliveryManifestResponse.builder()
                .id(manifest.getId())
                .manifestCode(manifest.getManifestCode())
                .courierId(manifest.getCourierId())
                .courierName(manifest.getCourierName())
                .postOfficeCode(manifest.getPostOfficeCode())
                .status(manifest.getStatus())
                .totalCodAmount(manifest.getTotalCodAmount())
                .collectedCodAmount(manifest.getCollectedCodAmount())
                .totalShippingFee(manifest.getTotalShippingFee())
                .collectedShippingFee(manifest.getCollectedShippingFee())
                .totalOrders(manifest.getTotalOrders())
                .deliveredCount(manifest.getDeliveredCount())
                .failedCount(manifest.getFailedCount())
                .orders(orderResponses)
                .build();
    }
}
