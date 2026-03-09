/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.messaging.strategy;

import com.fasterxml.jackson.databind.JsonNode;

import serp.project.pmcore.core.domain.dto.message.KafkaEventContext;

public interface IKafkaEventHandlerStrategy {
    String getEventType();

    void handle(JsonNode message, KafkaEventContext context);
}
