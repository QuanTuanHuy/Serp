/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import serp.project.pmcore.core.domain.enums.ConsumerInboxAcquireResult;

public interface IConsumerInboxService {
    ConsumerInboxAcquireResult acquireForProcessing(
            String consumerGroup,
            String eventId,
            String eventType,
            String topic,
            Integer partitionNo,
            Long offsetNo,
            Long tenantId,
            String payloadHash,
            String rawPayload);

    void markProcessed(String consumerGroup, String eventId, Long offsetNo);

    void markFailed(String consumerGroup, String eventId, String errorMessage);

    void markDead(String consumerGroup, String eventId, String errorMessage);

    int deleteTerminalEventsBefore(long timestamp);
}
