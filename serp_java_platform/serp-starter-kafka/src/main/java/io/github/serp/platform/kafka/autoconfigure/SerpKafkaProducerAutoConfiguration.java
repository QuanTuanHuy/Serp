/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.kafka.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import io.github.serp.platform.kafka.properties.SerpKafkaProperties;
import io.github.serp.platform.kafka.publisher.DefaultSerpKafkaPublisher;
import io.github.serp.platform.kafka.publisher.SerpKafkaPublisher;

@AutoConfiguration
@AutoConfigureAfter(KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(SerpKafkaProperties.class)
@ConditionalOnProperty(prefix = "serp.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SerpKafkaProducerAutoConfiguration {

    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean
    public SerpKafkaPublisher serpKafkaPublisher(
            KafkaTemplate<?, ?> kafkaTemplate,
            SerpKafkaProperties properties) {
        @SuppressWarnings("unchecked")
        KafkaTemplate<Object, Object> castedKafkaTemplate = (KafkaTemplate<Object, Object>) kafkaTemplate;
        return new DefaultSerpKafkaPublisher(castedKafkaTemplate, properties);
    }
}


