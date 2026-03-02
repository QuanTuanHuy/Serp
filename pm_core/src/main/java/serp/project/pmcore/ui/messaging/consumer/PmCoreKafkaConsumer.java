/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import serp.project.pmcore.core.service.IConsumerInboxService;
import serp.project.pmcore.core.service.messaging.strategy.KafkaEventHandlerStrategyRegistry;
import serp.project.pmcore.kernel.utils.JsonUtils;

@Component
public class PmCoreKafkaConsumer extends AbstractKafkaConsumerTemplate {

    @Value("${spring.kafka.consumer.group-id:pmcore-group}")
    private String consumerGroupId;

    public PmCoreKafkaConsumer(
            JsonUtils jsonUtils,
            IConsumerInboxService consumerInboxService,
            KafkaEventHandlerStrategyRegistry strategyRegistry) {
        super(jsonUtils, consumerInboxService, strategyRegistry);
    }

    @KafkaListener(
            id = "pmCoreKafkaInboundConsumer",
            topics = "#{@kafkaConsumerProperties.resolvedTopics}",
            containerFactory = "pmCoreKafkaListenerContainerFactory",
            autoStartup = "${app.kafka.consumer.enabled:false}")
    @Transactional(rollbackFor = Exception.class)
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        processRecord(record, acknowledgment, consumerGroupId);
    }
}
