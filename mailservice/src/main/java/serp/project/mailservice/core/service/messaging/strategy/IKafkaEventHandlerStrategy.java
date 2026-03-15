/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.core.service.messaging.strategy;

import com.fasterxml.jackson.databind.JsonNode;

import serp.project.mailservice.core.domain.dto.message.KafkaEventContext;

public interface IKafkaEventHandlerStrategy {
    String getEventType();

    void handle(JsonNode message, KafkaEventContext context);
}
