/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka consumer template method
 */

package serp.project.discuss_service.ui.messaging;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import serp.project.discuss_service.ui.messaging.handler.IEventHandler;

@Slf4j
public abstract class AbstractKafkaConsumer<T, E extends Enum<E>> {

    protected final void processRecord(
            ConsumerRecord<String, String> record,
            Map<E, ? extends IEventHandler<T, E>> handlers,
            String handlerName) {
        if (record == null) {
            return;
        }

        T event = parsePayload(record);
        if (event == null) {
            throw new IllegalArgumentException(String.format(
                    "Received %s event with invalid payload. topic=%s, partition=%d, offset=%d",
                    handlerName, record.topic(), record.partition(), record.offset()));
        }

        E eventType = resolveEventType(event);
        if (eventType == null) {
            throw new IllegalArgumentException(String.format(
                    "Received %s event without resolvable type. topic=%s, partition=%d, offset=%d",
                    handlerName, record.topic(), record.partition(), record.offset()));
        }

        if (handlers == null) {
            throw new IllegalStateException("No handlers configured for " + handlerName + " events");
        }

        IEventHandler<T, E> handler = handlers.get(eventType);
        if (handler == null) {
            log.debug("Ignored {} event type: {}", handlerName, eventType);
            return;
        }

        handler.handle(event);
    }

    protected abstract T parsePayload(ConsumerRecord<String, String> record);

    protected abstract E resolveEventType(T event);
}
