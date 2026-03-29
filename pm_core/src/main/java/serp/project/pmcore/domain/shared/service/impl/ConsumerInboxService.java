/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import serp.project.pmcore.domain.shared.service.IConsumerInboxService;
import serp.project.pmcore.domain.shared.enums.ConsumerInboxAcquireResult;
import serp.project.pmcore.domain.shared.port.store.IConsumerInboxPort;

@Service
@RequiredArgsConstructor
public class ConsumerInboxService implements IConsumerInboxService {

    private final IConsumerInboxPort consumerInboxPort;

    @Override
    @Transactional
    public ConsumerInboxAcquireResult acquireForProcessing(
            String consumerGroup,
            String eventId,
            String eventType,
            String topic,
            Integer partitionNo,
            Long offsetNo,
            Long tenantId,
            String payloadHash,
            String rawPayload) {
        return consumerInboxPort.acquireForProcessing(
                consumerGroup,
                eventId,
                eventType,
                topic,
                partitionNo,
                offsetNo,
                tenantId,
                payloadHash,
                rawPayload);
    }

    @Override
    @Transactional
    public void markProcessed(String consumerGroup, String eventId, Long offsetNo) {
        consumerInboxPort.markProcessed(consumerGroup, eventId, offsetNo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String consumerGroup, String eventId, String errorMessage) {
        consumerInboxPort.markFailed(consumerGroup, eventId, errorMessage);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDead(String consumerGroup, String eventId, String errorMessage) {
        consumerInboxPort.markDead(consumerGroup, eventId, errorMessage);
    }

    @Override
    @Transactional
    public int deleteTerminalEventsBefore(long timestamp) {
        return consumerInboxPort.deleteTerminalEventsBefore(timestamp);
    }
}
