/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.publisher;

import java.util.Map;

public interface SerpKafkaPublisher {
    void publish(String topic, String key, Object payload);

    void publish(String topic, String key, Object payload, Map<String, String> headers);
}
