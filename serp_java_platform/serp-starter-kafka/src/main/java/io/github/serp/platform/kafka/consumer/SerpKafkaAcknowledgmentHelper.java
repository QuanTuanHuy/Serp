/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.consumer;

import org.springframework.kafka.support.Acknowledgment;

import java.time.Duration;

public class SerpKafkaAcknowledgmentHelper {
    public void acknowledge(Acknowledgment acknowledgment) {
        if (acknowledgment == null) {
            return;
        }
        acknowledgment.acknowledge();
    }

    public void nack(Acknowledgment acknowledgment, Duration sleepDuration) {
        if (acknowledgment == null) {
            return;
        }

        Duration effectiveDuration = sleepDuration;
        if (effectiveDuration == null || effectiveDuration.isZero() || effectiveDuration.isNegative()) {
            effectiveDuration = Duration.ofSeconds(1);
        }

        acknowledgment.nack(effectiveDuration);
    }
}
