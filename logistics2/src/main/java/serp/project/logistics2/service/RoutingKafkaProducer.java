package serp.project.logistics2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.dto.message.RoutingRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.routing-request}")
    private String requestTopic;

    public void sendRoutingRequest(RoutingRequest request) {
        try {
            // 1. Chuyển Object thành JSON String
            String jsonMessage = objectMapper.writeValueAsString(request);

            // 2. Đẩy vào Kafka
            kafkaTemplate.send(requestTopic, request.getPlanId(), jsonMessage);

            log.info("[RoutingKafkaProducer] Đã gửi request tối ưu cho Plan ID: {}", request.getPlanId());
        } catch (Exception e) {
            log.error("[RoutingKafkaProducer] Lỗi khi chuyển đổi đối tượng sang JSON: {}", e.getMessage(), e);
        }
    }
}
