/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.caller.TmsOrderClient;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.domain.OrderTransitionOutbox;
import serp.project.first_mile.enums.OrderTransitionOutboxStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.TransactionAfterCommit;
import serp.project.first_mile.repository.OrderTransitionOutboxRepository;
import serp.project.first_mile.service.TmsOrderTransitionOutboxService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TmsOrderTransitionOutboxServiceImpl implements TmsOrderTransitionOutboxService {

    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int MAX_ERROR_LENGTH = 2000;

    private final ObjectMapper objectMapper;
    private final TmsOrderClient tmsOrderClient;
    private final OrderTransitionOutboxRepository outboxRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enqueue(TmsOrderStatusTransitionRequest request, Long tenantId) {
        if (request == null
                || request.getIdempotencyKey() == null
                || request.getIdempotencyKey().isBlank()
                || request.getSource() == null
                || request.getSource().isBlank()
                || request.getItems() == null
                || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        try {
            outboxRepository.save(OrderTransitionOutbox.builder()
                    .tenantId(tenantId)
                    .idempotencyKey(request.getIdempotencyKey())
                    .source(request.getSource())
                    .requestPayload(objectMapper.writeValueAsString(request))
                    .status(OrderTransitionOutboxStatus.PENDING)
                    .retryCount(0)
                    .nextRetryAt(LocalDateTime.now())
                    .build());
            TransactionAfterCommit.run(this::processDueTransitionsSafely);
        } catch (JsonProcessingException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${tms-order.transition-outbox.retry-interval-ms:30000}")
    @Transactional(rollbackFor = Exception.class)
    public void processDueTransitions() {
        List<OrderTransitionOutbox> outboxRows = outboxRepository
                .findByStatusInAndNextRetryAtLessThanEqualOrderByIdAsc(
                        List.of(OrderTransitionOutboxStatus.PENDING, OrderTransitionOutboxStatus.FAILED),
                        LocalDateTime.now(),
                        PageRequest.of(0, DEFAULT_BATCH_SIZE)
                );

        for (OrderTransitionOutbox outbox : outboxRows) {
            processOne(outbox);
        }
    }

    private void processDueTransitionsSafely() {
        try {
            processDueTransitions();
        } catch (Exception exception) {
            log.warn("Immediate TMS order transition outbox processing failed: {}", exception.getMessage());
        }
    }

    private void processOne(OrderTransitionOutbox outbox) {
        if (outbox == null || outbox.getId() == null) {
            return;
        }

        try {
            outbox.setStatus(OrderTransitionOutboxStatus.PROCESSING);
            outboxRepository.save(outbox);

            TmsOrderStatusTransitionRequest request = objectMapper.readValue(
                    outbox.getRequestPayload(),
                    TmsOrderStatusTransitionRequest.class
            );
            tmsOrderClient.applyTransitions(request, outbox.getTenantId());

            outbox.setStatus(OrderTransitionOutboxStatus.SUCCEEDED);
            outbox.setLastError(null);
            outbox.setProcessedAt(LocalDateTime.now());
            outboxRepository.save(outbox);
        } catch (Exception exception) {
            int retryCount = outbox.getRetryCount() == null ? 0 : outbox.getRetryCount();
            retryCount++;
            outbox.setRetryCount(retryCount);
            outbox.setStatus(OrderTransitionOutboxStatus.FAILED);
            outbox.setLastError(truncate(exception.getMessage()));
            outbox.setNextRetryAt(LocalDateTime.now().plusSeconds(resolveBackoffSeconds(retryCount)));
            outboxRepository.save(outbox);
        }
    }

    private long resolveBackoffSeconds(int retryCount) {
        return Math.min(300L, (long) Math.pow(2, Math.min(retryCount, 8)));
    }

    private String truncate(String message) {
        if (message == null || message.length() <= MAX_ERROR_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_LENGTH);
    }
}
