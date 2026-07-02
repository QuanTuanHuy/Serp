/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kafka.TmsOrderStatusTransitionEventPublisher;
import serp.project.first_mile.service.TmsOrderTransitionPublisherService;

@Service
@RequiredArgsConstructor
public class TmsOrderTransitionPublisherServiceImpl implements TmsOrderTransitionPublisherService {

    private final TmsOrderStatusTransitionEventPublisher eventPublisher;

    @Override
    public void publish(TmsOrderStatusTransitionRequest request, Long tenantId) {
        if (request == null
                || tenantId == null
                || request.getIdempotencyKey() == null
                || request.getIdempotencyKey().isBlank()
                || request.getSource() == null
                || request.getSource().isBlank()
                || request.getItems() == null
                || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        request.setIdempotencyKey(request.getIdempotencyKey().trim());
        request.setSource(request.getSource().trim());
        eventPublisher.publish(request, tenantId);
    }
}
