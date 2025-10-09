
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.ui.kafkahandler;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.ptm_optimization.core.domain.constant.TopicConstants;
import serp.project.ptm_optimization.core.domain.dto.message.KafkaBaseDto;
import serp.project.ptm_optimization.kernel.utils.JsonUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskMessageHandler {
    private final JsonUtils jsonUtils;

    @KafkaListener(topics = TopicConstants.PTMTask.TOPIC)
    public void handleMessage(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset,
            Acknowledgment acknowledgement) {
        log.info("Received message: topic={}, partition={}, offset={}, key={}, value={}",
                topic, partition, offset, record.key(), record.value());

        try {
            var kafkaBaseDto = jsonUtils.fromJson(record.value(), KafkaBaseDto.class);
            switch (kafkaBaseDto.getCmd()) {
                case TopicConstants.PTMTask.CREATE_TASK -> {
                    log.info("Handling create task: {}", kafkaBaseDto);
                }
                case TopicConstants.PTMTask.UPDATE_TASK -> {
                    log.info("Handling update task: {}", kafkaBaseDto);
                }
                case TopicConstants.PTMTask.DELETE_TASK -> {
                    log.info("Handling delete task: {}", kafkaBaseDto);
                }
                default -> log.warn("Unknown command: {}", kafkaBaseDto.getCmd());
            }
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }
}
