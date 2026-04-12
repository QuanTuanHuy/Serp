/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.mailservice.ui.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import serp.project.mailservice.core.service.IConsumerInboxService;
import serp.project.mailservice.core.service.messaging.strategy.KafkaEventHandlerStrategyRegistry;
import serp.project.mailservice.kernel.utils.JsonUtils;

@Component
public class MailServiceKafkaConsumer extends AbstractKafkaConsumerTemplate {

    @Value("${spring.kafka.consumer.group-id:mail-service-group}")
    private String consumerGroupId;

    public MailServiceKafkaConsumer(
            JsonUtils jsonUtils,
            IConsumerInboxService consumerInboxService,
            KafkaEventHandlerStrategyRegistry strategyRegistry) {
        super(jsonUtils, consumerInboxService, strategyRegistry);
    }

    @KafkaListener(
            id = "mailServiceKafkaInboundConsumer",
            topics = "#{@kafkaConsumerProperties.resolvedTopics}",
            containerFactory = "mailServiceKafkaListenerContainerFactory",
            autoStartup = "${app.kafka.consumer.enabled:false}")
    @Transactional(rollbackFor = Exception.class)
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        processRecord(record, acknowledgment, consumerGroupId);
    }
}
