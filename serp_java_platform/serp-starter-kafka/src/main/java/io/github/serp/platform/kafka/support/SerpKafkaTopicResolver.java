/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.support;

import org.springframework.util.Assert;

import io.github.serp.platform.kafka.properties.SerpKafkaProperties;

public class SerpKafkaTopicResolver {
    private final SerpKafkaProperties properties;

    public SerpKafkaTopicResolver(SerpKafkaProperties properties) {
        this.properties = properties;
    }

    public String resolveDeadLetterTopic(String originalTopic) {
        Assert.hasText(originalTopic, "Original topic is required");
        String suffix = properties.getConsumer().getDltSuffix();
        if (suffix == null || suffix.isBlank()) {
            suffix = ".dlt";
        }
        return originalTopic + suffix.trim();
    }
}
