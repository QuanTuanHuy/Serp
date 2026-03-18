/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.message;

public record KafkaEventContext(
        String consumerGroupId,
        String topic,
        Integer partition,
        Long offset,
        String key,
        String eventId,
        String eventType,
        Long tenantId) {
}
