/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.ParameterizedTypeReference;

public interface SerpKafkaRecordParser {
    <T> T parse(ConsumerRecord<String, String> record, Class<T> type);

    <T> T parse(ConsumerRecord<String, String> record, ParameterizedTypeReference<T> typeReference);
}
