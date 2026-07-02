/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

public interface OutboxEventService {
    void enqueue(
            String aggregateType,
            String aggregateId,
            String eventType,
            String topic,
            String messageKey,
            String payload,
            Long tenantId
    );
}
