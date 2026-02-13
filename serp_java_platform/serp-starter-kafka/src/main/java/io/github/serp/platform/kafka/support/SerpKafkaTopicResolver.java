/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.support;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.util.Assert;

import java.util.Map;

import io.github.serp.platform.kafka.properties.SerpKafkaProperties;

public class SerpKafkaTopicResolver {
    private final SerpKafkaProperties properties;

    public SerpKafkaTopicResolver(SerpKafkaProperties properties) {
        this.properties = properties;
    }

    public String resolveDeadLetterTopic(String originalTopic) {
        Assert.hasText(originalTopic, "Original topic is required");
        Map<String, String> topicMappings = properties.getConsumer().getDlt().getTopicMappings();
        if (topicMappings != null) {
            String mappedTopic = topicMappings.get(originalTopic);
            if (mappedTopic != null && !mappedTopic.isBlank()) {
                return mappedTopic.trim();
            }
        }

        String suffix = properties.getConsumer().getDlt().getSuffix();
        if (suffix == null || suffix.isBlank()) {
            suffix = ".dlt";
        }
        return originalTopic + suffix.trim();
    }

    public TopicPartition resolveDeadLetterTopicPartition(ConsumerRecord<?, ?> originalRecord) {
        Assert.notNull(originalRecord, "Original consumer record is required");
        String deadLetterTopic = resolveDeadLetterTopic(originalRecord.topic());

        if (!properties.getConsumer().getDlt().isSamePartition()) {
            return new TopicPartition(deadLetterTopic, -1);
        }

        return new TopicPartition(deadLetterTopic, originalRecord.partition());
    }
}
