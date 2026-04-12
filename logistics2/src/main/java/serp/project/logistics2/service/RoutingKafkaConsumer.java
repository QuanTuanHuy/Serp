package serp.project.logistics2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.dto.message.RoutingResponse;
import serp.project.logistics2.orchestrator.RoutingOrchestrator;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingKafkaConsumer {

    private final RoutingOrchestrator routingOrchestrator;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic.routing-response}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenRoutingResponse(String message) {
        log.info("[RoutingKafkaConsumer] Nhận được message từ Kafka (Raw String): {}", message);

        try {
            // 1. Parse JSON String thành Object
            RoutingResponse response = objectMapper.readValue(message, RoutingResponse.class);

            // 2. Xử lý logic hệ thống với đối tượng đã parse
            processResponse(response);

        } catch (Exception e) {
            log.error("[RoutingKafkaConsumer] Lỗi khi xử lý message từ Python trả về. Nội dung: {}", message, e);
        }
    }

    private void processResponse(RoutingResponse response) {
        log.info("=== KẾT QUẢ TỐI ƯU ===");
        log.info("Plan ID: {}", response.getPlanId());
        log.info("Status: {}", response.getStatus());

        if ("COMPLETED".equals(response.getStatus())) {
            log.info("Tổng quãng đường: {} km", response.getTotalPlanDistance());
            log.info("Số xe được sử dụng: {}", response.getRoutes().size());
            log.info("Số đơn rớt: {}", response.getDroppedSlipIds().size());

            routingOrchestrator.createRouteForDeliveryPlan(response);
        } else {
            log.warn("[RoutingKafkaConsumer] Kế hoạch thất bại hoặc không có nghiệm!");
        }
    }
}
