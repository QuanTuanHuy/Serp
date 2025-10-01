/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.kernel.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // Reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry up to 3 times
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Ensure ordering
        
        // // Performance settings
        // configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Compress messages
        // configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Batch size in bytes
        // configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait 10ms to batch messages
        // configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer
        
        // // Timeout settings
        // configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        // configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 2 minutes
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
