/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.support;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import io.github.serp.platform.kafka.properties.SerpKafkaProperties;

import static org.assertj.core.api.Assertions.assertThat;

class SerpKafkaTopicResolverTest {

    @Test
    void shouldResolveDeadLetterTopicWithSuffix() {
        SerpKafkaProperties properties = new SerpKafkaProperties();
        properties.getConsumer().getDlt().setSuffix(".err");

        SerpKafkaTopicResolver resolver = new SerpKafkaTopicResolver(properties);

        assertThat(resolver.resolveDeadLetterTopic("topic.orders")).isEqualTo("topic.orders.err");
    }

    @Test
    void shouldResolveSamePartitionByDefault() {
        SerpKafkaProperties properties = new SerpKafkaProperties();
        SerpKafkaTopicResolver resolver = new SerpKafkaTopicResolver(properties);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic.events", 2, 14L, "k", "v");
        TopicPartition destination = resolver.resolveDeadLetterTopicPartition(record);

        assertThat(destination.topic()).isEqualTo("topic.events.dlt");
        assertThat(destination.partition()).isEqualTo(2);
    }

    @Test
    void shouldResolveAnyPartitionWhenSamePartitionDisabled() {
        SerpKafkaProperties properties = new SerpKafkaProperties();
        properties.getConsumer().getDlt().setSamePartition(false);
        SerpKafkaTopicResolver resolver = new SerpKafkaTopicResolver(properties);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic.events", 2, 14L, "k", "v");
        TopicPartition destination = resolver.resolveDeadLetterTopicPartition(record);

        assertThat(destination.topic()).isEqualTo("topic.events.dlt");
        assertThat(destination.partition()).isEqualTo(-1);
    }

    @Test
    void shouldResolveDeadLetterTopicFromMappings() {
        SerpKafkaProperties properties = new SerpKafkaProperties();
        properties.getConsumer().getDlt().getTopicMappings().put("topic.events", "topic.events.fail");

        SerpKafkaTopicResolver resolver = new SerpKafkaTopicResolver(properties);

        assertThat(resolver.resolveDeadLetterTopic("topic.events")).isEqualTo("topic.events.fail");
    }
}
