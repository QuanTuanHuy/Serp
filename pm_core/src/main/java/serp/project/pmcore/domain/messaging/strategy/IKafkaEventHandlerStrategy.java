/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.messaging.strategy;

import com.fasterxml.jackson.databind.JsonNode;

import serp.project.pmcore.domain.shared.dto.message.KafkaEventContext;

public interface IKafkaEventHandlerStrategy {
    String getEventType();

    void handle(JsonNode message, KafkaEventContext context);
}
