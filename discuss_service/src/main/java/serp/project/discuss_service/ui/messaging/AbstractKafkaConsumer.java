/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka consumer template method
 */

package serp.project.discuss_service.ui.messaging;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;
import serp.project.discuss_service.kernel.utils.JsonUtils;
import serp.project.discuss_service.ui.messaging.handler.IEventHandler;

@Slf4j
public abstract class AbstractKafkaConsumer<T, E extends Enum<E>> {

    protected final JsonUtils jsonUtils;

    protected AbstractKafkaConsumer(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

    protected final void processRecord(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment,
            Map<E, ? extends IEventHandler<T, E>> handlers,
            String handlerName) {
        if (record == null) {
            acknowledge(acknowledgment);
            return;
        }

        String payload = record.value();
        try {
            T event = parsePayload(payload);
            if (event == null) {
                log.warn("Received {} event with invalid payload. topic={}, partition={}, offset={}",
                        handlerName, record.topic(), record.partition(), record.offset());
                return;
            }

            E eventType = resolveEventType(event);
            if (eventType == null) {
                log.warn("Received {} event without resolvable type. topic={}, partition={}, offset={}",
                        handlerName, record.topic(), record.partition(), record.offset());
                return;
            }

            if (handlers == null) {
                log.warn("No handlers configured for {} events", handlerName);
                return;
            }

            IEventHandler<T, E> handler = handlers.get(eventType);
            if (handler == null) {
                log.debug("Ignored {} event type: {}", handlerName, eventType);
                return;
            }

            handler.handle(event);
        } catch (Exception e) {
            log.error("Failed to process {} event: {}", handlerName, payload, e);
        } finally {
            acknowledge(acknowledgment);
        }
    }

    protected abstract T parsePayload(String payload);

    protected abstract E resolveEventType(T event);

    protected void acknowledge(Acknowledgment acknowledgment) {
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }
}
