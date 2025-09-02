package serp.project.loggingtracker.ui.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import serp.project.loggingtracker.core.constants.TopicConstants;
import serp.project.loggingtracker.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogHandler {
    private final JsonUtils jsonUtils;

    @KafkaListener(topics = TopicConstants.TOPIC_LOGGING)
    public void handleMessage(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset,
            Acknowledgment acknowledgment) {
        log.info("Received message from topic: {}, partition: {}, offset: {}, key: {}, value: {}",
                topic, partition, offset, record.key(), record.value());

        try {

        } catch (Exception e) {
            log.error("Error processing create schedule task message: {}", e.getMessage());
        } finally {
            acknowledgment.acknowledge();
        }
    }

}